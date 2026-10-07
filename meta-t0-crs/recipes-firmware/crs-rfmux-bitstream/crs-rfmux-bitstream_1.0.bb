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
#
# A timing failure is a loud warning, not a build failure: the bitstream and
# rootfs are still produced so they can be retrieved and inspected (or
# deliberately run on hardware). A missing bitstream is still fatal.
#
# The check reads the timing summary Vivado's impl run writes itself after
# the last implementation step (post-route phys_opt if enabled, otherwise
# route). It is a default (min_max) report_timing_summary, so it covers
# setup, hold and pulse width. (report.tcl's timing.rpt is setup-only.)
do_check_timing() {
    impl=${S}/tcl/hw/hw.runs/impl_1

    if [ ! -f $impl/bd_wrapper.bin ]; then
        bbfatal "Implementation did not produce a bitstream; see $impl/runme.log"
    fi

    rpt=$impl/bd_wrapper_timing_summary_postroute_physopted.rpt
    [ -f $rpt ] || rpt=$impl/bd_wrapper_timing_summary_routed.rpt
    if [ ! -f $rpt ]; then
        bbwarn "FPGA timing UNVERIFIED - no routed timing summary in $impl"
        return
    fi

    if ! grep -q "All user specified timing constraints are met." $rpt; then
        bbwarn "FPGA timing constraints NOT met - this bitstream may misbehave on hardware.
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
