SUMMARY = "CRS firmware release notes"
DESCRIPTION = "Firmware release notes, served on-board via jupyterlab. \
These describe the assembled firmware image (i.e. this yocto tree), so they \
live and version here rather than in the rfmux client package, whose \
release cadence is independent of firmware releases."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI = "file://release-notes"

S = "${WORKDIR}"

do_install() {
    install -d "${D}${datadir}/crs-docs/Release Notes"
    cp -r --no-preserve=ownership ${WORKDIR}/release-notes/. \
        "${D}${datadir}/crs-docs/Release Notes/"
    chmod -R u=rwX,go=rX "${D}${datadir}/crs-docs"
}

FILES:${PN} = "${datadir}/crs-docs"

COMPATIBLE_MACHINE = "t0-crs"
