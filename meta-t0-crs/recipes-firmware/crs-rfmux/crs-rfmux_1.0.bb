SUMMARY = "CRS rfmux application package"
DESCRIPTION = "Kernel module (mod_rfmux), shared library (libmkids), \
display binary, FPGA firmware, and tuberd service for the CRS rfmux instrument."
LICENSE = "CLOSED"

# Software sources from crs-mkids submodule
FILESEXTRAPATHS:prepend := "${TOPDIR}/../crs-mkids:"
SRC_URI = " \
    file://c/src \
    file://c/include \
    file://c/mak \
    file://c/dat \
    file://c/arm/mod_rfmux/Makefile \
    file://c/arm/libmkids \
"

# RFDC driver source from the shared Xilinx embeddedsw checkout (same version
# used by SDT/lopper to generate param-list, so the XRFdc_Config struct layout
# is guaranteed to match).
ESW_VER = "2025.2"
EMBEDDEDSW_SHARED = "${TMPDIR}/work-shared/embeddedsw-${ESW_VER}/git"
do_compile[depends] += "embeddedsw-source-${ESW_VER}:do_configure"

# Config files (live in meta-t0-crs, not crs-mkids)
SRC_URI += " \
    file://t0-crs-rfmux.dtsi \
    file://tuberd.service \
    file://crs-rfmux-load.service \
    file://crs-rfmux-load.sh \
    file://registry.py \
    file://fru.py \
    file://index.html \
    file://80-net-setup-link.link \
"

S = "${WORKDIR}"

inherit module python3targetconfig systemd

DEPENDS = " \
    coreutils-native \
    dtc-native \
    python3 \
    python3-pybind11 \
    python3-tuberd \
    fmt \
    libgpiod \
    avahi \
    libmetal \
    libiio \
    boost \
"

RDEPENDS:${PN} = " \
    crs-rfmux-bitstream \
    python3-tuberd \
    python3-numpy \
    avahi-daemon \
    avahi \
"

SYSTEMD_SERVICE:${PN} = "tuberd.service crs-rfmux-load.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

export RFDC_DRIVER_SRC = "${EMBEDDEDSW_SHARED}/XilinxProcessorIPLib/drivers/rfdc/src"

do_compile() {
    # Build the kernel module
    oe_runmake -C ${STAGING_KERNEL_BUILDDIR} \
        M=${S}/c/arm/mod_rfmux \
        EXTRA_CFLAGS="-I${S}/c/include ${DEBUG_PREFIX_MAP}" \
        BUILDROOT_CRS=/ \
        modules

    # Build libmkids.so
    oe_runmake -C ${S}/c/arm/libmkids \
        -f ../../mak/libmkids.mak \
        BUILDROOT_CRS=/ \
        BR_STAGING=${STAGING_DIR_TARGET} \
        BR_TARGET=${STAGING_DIR_TARGET} \
        RFDC_DRIVER_SRC=${RFDC_DRIVER_SRC} \
        CC="${CC}" CXX="${CXX}" LD="${LD}" \
        libmkids.so

    # Build the display binary
    mkdir -p ${S}/c/arm/display
    oe_runmake -C ${S}/c/arm/display \
        -f ../../mak/display.mak \
        CC="${CC}" CXX="${CXX}" LD="${LD}" \
        display

    # Compile the device tree overlay (preprocessor + dtc)
    ${BUILD_CPP} -nostdinc -undef -x assembler-with-cpp \
        -I${STAGING_KERNEL_DIR}/include \
        ${WORKDIR}/t0-crs-rfmux.dtsi -o ${B}/crs-rfmux.cpp
    dtc -O dtb -o ${B}/crs-rfmux.dtbo -@ ${B}/crs-rfmux.cpp
}

do_install() {
    # Kernel module
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/c/arm/mod_rfmux/mod_rfmux.ko \
        ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/

    # libmkids.so (pybind11 Python extension, imported by registry.py)
    install -d ${D}${datadir}/tuberd
    install -m 0755 ${S}/c/arm/libmkids/libmkids.so ${D}${datadir}/tuberd/

    # Display binary
    install -d ${D}${bindir}
    install -m 0755 ${S}/c/arm/display/display ${D}${bindir}/

    # Device tree overlay (bitstream shipped by crs-rfmux-bitstream)
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}
    install -m 0644 ${B}/crs-rfmux.dtbo \
        ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.dtbo

    # Systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/tuberd.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/crs-rfmux-load.service ${D}${systemd_system_unitdir}/

    # FPGA load script
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/crs-rfmux-load.sh ${D}${sbindir}/crs-rfmux-load

    # Tuberd support files
    install -d ${D}${datadir}/tuberd
    install -m 0644 ${WORKDIR}/registry.py ${D}${datadir}/tuberd/
    install -m 0644 ${WORKDIR}/fru.py ${D}${datadir}/tuberd/

    # Web redirect
    install -d ${D}/var/www
    install -m 0644 ${WORKDIR}/index.html ${D}/var/www/

    # Force kernel network interface names (eth0 instead of end0)
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/80-net-setup-link.link ${D}${sysconfdir}/systemd/network/
}

# Module is loaded explicitly by crs-rfmux-load.sh after the FPGA bitstream
# and HMC7044 clock tree are configured.  Do NOT autoload — the PL fabric
# must be clocked before mod_rfmux touches AXI registers.
#KERNEL_MODULE_AUTOLOAD += "mod_rfmux"

FILES:${PN} = " \
    ${bindir}/display \
    ${sbindir}/crs-rfmux-load \
    ${nonarch_base_libdir}/firmware/xilinx/${PN} \
    ${systemd_system_unitdir}/tuberd.service \
    ${systemd_system_unitdir}/crs-rfmux-load.service \
    ${sysconfdir}/systemd/network \
    ${datadir}/tuberd \
    /var/www \
"

# libmkids.so is a pybind11 extension in /usr/share/tuberd, not a system library
INSANE_SKIP:${PN} += "libdir"
INSANE_SKIP:${PN}-dbg += "libdir"

COMPATIBLE_MACHINE = "t0-crs"
