FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:t0-crs = " \
    file://boot.cmd.t0-crs \
    "

BOOTMODE = "t0-crs"
