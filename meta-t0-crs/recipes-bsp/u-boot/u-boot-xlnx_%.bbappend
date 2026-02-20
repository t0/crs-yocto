FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:t0-crs = " \
    file://t0-crs.cfg \
    file://0001-zynqmp-add-NVMe-to-boot-targets.patch \
    "
