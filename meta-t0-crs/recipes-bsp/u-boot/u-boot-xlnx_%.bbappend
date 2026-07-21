FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

UBOOTURI:t0-crs = "git://github.com/t0/u-boot.git;protocol=https"
UBRANCH:t0-crs = "t0-crs"
SRCREV:t0-crs = "42d6f3c29326b606a44ff2921f99765602b9e84c"
LIC_FILES_CHKSUM:t0-crs = "file://README;beginline=1;endline=4;md5=c5130931598a8ad21840e124ffe64ea0"

SRC_URI:append:t0-crs = " \
    file://t0-crs.cfg \
    file://t0-crs.env \
    file://0001-zynqmp-add-NVMe-to-boot-targets.patch \
    file://0002-zynqmp-derive-ethaddr-from-board-serial.patch \
    "

# Install the Yocto-managed board environment into the U-Boot source tree
do_configure:prepend:t0-crs() {
    cp ${WORKDIR}/t0-crs.env ${S}/board/xilinx/zynqmp/t0-crs.env
}
