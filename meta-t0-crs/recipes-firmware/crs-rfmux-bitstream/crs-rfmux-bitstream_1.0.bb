SUMMARY = "CRS rfmux FPGA bitstream (Vivado synthesis)"
DESCRIPTION = "Builds the rfmux FPGA bitstream from RTL sources using Vivado."
LICENSE = "CLOSED"

# Only the RTL/TCL/constraints affect the bitstream — software changes in c/
# do not invalidate this recipe's sstate hash.
FILESEXTRAPATHS:prepend := "${TOPDIR}/../crs-mkids:"
SRC_URI = " \
    file://rtl \
    file://tcl/hw.tcl \
    file://tcl/bd.tcl \
    file://tcl/bitstream.tcl \
    file://tcl/report.tcl \
    file://xdc \
    file://cores \
"

S = "${WORKDIR}"

inherit deploy

XILINX_VIVADO ?= ""

# Prepend Vivado to PATH at the bitbake level (like xsct-tc.bbclass does for XSCT).
# This avoids sourcing settings64.sh, which poisons LD_LIBRARY_PATH.
PATH =. "${XILINX_VIVADO}/bin:"

do_configure[noexec] = "1"

do_compile() {
    if [ -z "${XILINX_VIVADO}" ]; then
        bbfatal "XILINX_VIVADO is not set. Cannot build bitstream without Vivado."
    fi

    # Vivado's license manager crashes in Docker when its bundled libudev
    # conflicts with the container's version. Preload the system libudev.
    # See: https://github.com/systemd/systemd/issues/19733
    export LD_PRELOAD=/lib/x86_64-linux-gnu/libudev.so.1

    cd ${S}/tcl
    rm -rf hw
    # hw.tcl creates the project; bitstream.tcl runs synthesis/implementation
    # in the same session (it needs the project open).
    vivado -mode batch -source hw.tcl -source bitstream.tcl -notrace
}

# Vivado happily writes a bitstream for a design that fails timing, and
# wait_on_run doesn't propagate failed runs either, so check the outcome here.
# This is a separate task so that a failure doesn't throw away the (very
# expensive) do_compile stamp: the implementation results stay in the workdir
# for inspection, and re-running only repeats this check.
#
# timing.rpt is written by report.tcl as a TCL.POST hook after each
# implementation step, so the copy left behind reflects the final routed (and
# post-route phys_opt'd) design. It covers setup, hold, and pulse width.
do_check_timing() {
    impl=${S}/tcl/hw/hw.runs/impl_1
    rpt=$impl/timing.rpt

    if [ ! -f $impl/bd_wrapper.bin ]; then
        bbfatal "Implementation did not produce a bitstream; see $impl/runme.log"
    fi
    if [ ! -f $rpt ]; then
        bbfatal "No timing report at $rpt"
    fi

    if ! grep -q "All user specified timing constraints are met." $rpt; then
        bbfatal "FPGA timing constraints not met:
$(sed -n '/Design Timing Summary/,+9p' $rpt)
Full report: $rpt"
    fi
}
addtask check_timing after do_compile before do_install do_deploy

do_install() {
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/crs-rfmux
    install -m 0644 ${S}/tcl/hw/hw.runs/impl_1/bd_wrapper.bin \
        ${D}${nonarch_base_libdir}/firmware/xilinx/crs-rfmux/crs-rfmux.bin
}

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${S}/tcl/hw/hw.runs/impl_1/bd_wrapper.bin ${DEPLOYDIR}/bd_wrapper.bin
}
addtask deploy after do_compile

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/crs-rfmux"

# Vivado synthesis is very slow
do_compile[timeout] = "36000"

# Vivado's node-locked license checks the host MAC address, which requires
# visibility of the host's network interfaces. Bitbake normally isolates tasks
# in a new network namespace (CLONE_NEWNET); disable that for this task.
do_compile[network] = "1"
