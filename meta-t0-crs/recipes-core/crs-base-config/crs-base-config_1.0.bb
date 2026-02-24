SUMMARY = "CRS base system configuration"
DESCRIPTION = "System-level configuration for CRS boards: \
sysctl tuning, configfs mount, Ethernet ring buffers."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://99-crs-network.conf \
    file://configfs.mount \
    file://crs-ethtool-rings.service \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "configfs.mount crs-ethtool-rings.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "ethtool"

do_install() {
    # sysctl: 128 MB socket receive buffer for streaming
    install -d ${D}${sysconfdir}/sysctl.d
    install -m 0644 ${WORKDIR}/99-crs-network.conf ${D}${sysconfdir}/sysctl.d/

    # systemd units
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/configfs.mount ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/crs-ethtool-rings.service ${D}${systemd_system_unitdir}/

    # configfs mount point
    install -d ${D}/configfs
}

FILES:${PN} = " \
    ${sysconfdir}/sysctl.d \
    ${systemd_system_unitdir} \
    /configfs \
"

COMPATIBLE_MACHINE = "t0-crs"
