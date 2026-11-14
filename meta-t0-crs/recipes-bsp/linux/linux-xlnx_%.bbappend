FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:t0-crs = " file://linux-t0-crs.cfg"

# Use the Analog Devices kernel fork, which includes the HMC7044 clock
# jitter attenuator driver and other ADI IIO/JESD204 infrastructure
# needed by crs-rfmux.
KERNELURI:t0-crs = "git://github.com/analogdevicesinc/linux.git;protocol=https;name=machine"
SRCREV:t0-crs = "40201abd70d8f7848547a09fc7e366fba064483c"
KBRANCH:t0-crs = "2026_R1"
LINUX_VERSION:t0-crs = "6.12.0"
LINUX_VERSION_EXTENSION:t0-crs = "-adi"

# The kernel's System.map, for system-level cosimulation with snapshots
do_deploy:append:t0-crs() {
    install -m 0644 ${B}/System.map ${DEPLOYDIR}/System.map-${KERNEL_ARTIFACT_NAME}
    ln -sf System.map-${KERNEL_ARTIFACT_NAME} ${DEPLOYDIR}/System.map
}
