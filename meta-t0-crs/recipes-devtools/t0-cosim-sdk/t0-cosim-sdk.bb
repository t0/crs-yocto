SUMMARY = "SDK for system-level cosimulation: AMD's QEMU and the CRS toolchain"
DESCRIPTION = "For system-level cosimulation (c/x86_64/cosim in crs-mkids), \
as a self-relocating installer beside the image: AMD's QEMU with \
meta-t0-crs's snapshot patches (qemu-system-aarch64, \
qemu-system-microblazeel, qemu-img), to run the image in; and a cross \
toolchain with what libmkids and mod_rfmux build against, to build the \
crs-mkids tree under test into it."
LICENSE = "MIT"

# An ordinary toolchain SDK (as meta-toolchain), and on its host side, QEMU:
# the system emulators are packaged separately from nativesdk-qemu-xilinx,
# which carries only qemu-img and the other tools.
TOOLCHAIN_HOST_TASK:append = " nativesdk-qemu-xilinx \
    nativesdk-qemu-xilinx-system-aarch64 nativesdk-qemu-xilinx-system-microblazeel"

# On its target side, what crs-rfmux (libmkids, mod_rfmux) builds against
TOOLCHAIN_TARGET_TASK:append = " \
    python3-dev \
    python3-pybind11-dev \
    python3-tuberd \
    fmt-dev \
    libgpiod-dev \
    avahi-dev \
    libmetal-xlnx-dev \
    libiio-dev \
    boost-dev \
    kernel-devsrc \
"

# ... and the RFDC driver's source, which libmkids compiles in (crs-rfmux's,
# from the Vivado install that made the SDT), at /usr/src/rfdc in the target
# sysroot (RFDC_DRIVER_SRC, for libmkids.mak)
require recipes-firmware/crs-rfmux/rfdc-driver.inc

t0_cosim_sdk_rfdc() {
	[ -d "${RFDC_DRIVER_SRC}" ] || bbfatal "No RFDC driver in XILINX_VIVADO's install (\"${XILINX_VIVADO}\")"
	install -d ${SDK_OUTPUT}${SDKTARGETSYSROOT}/usr/src/rfdc
	cp -r ${RFDC_DRIVER_SRC}/. ${SDK_OUTPUT}${SDKTARGETSYSROOT}/usr/src/rfdc/
}
POPULATE_SDK_POST_TARGET_COMMAND:append = " t0_cosim_sdk_rfdc"

TOOLCHAIN_OUTPUTNAME = "t0-cosim-sdk-${SDK_ARCH}"
SDK_TITLE = "CRS system-level cosimulation SDK"

inherit populate_sdk
