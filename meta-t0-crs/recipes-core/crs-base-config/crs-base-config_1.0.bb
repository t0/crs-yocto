SUMMARY = "CRS base system configuration"
DESCRIPTION = "System-level configuration for CRS boards: \
sysctl tuning, configfs mount, Ethernet ring buffers."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://99-crs-network.conf \
    file://configfs.mount \
    file://home.mount \
    file://jupyter-home-init.service \
    file://jupyter-home-init.sh \
    file://crs-ethtool-rings.service \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "configfs.mount home.mount jupyter-home-init.service crs-ethtool-rings.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "ethtool python3-rfmux"

do_install() {
    # sysctl: 128 MB socket receive buffer for streaming
    install -d ${D}${sysconfdir}/sysctl.d
    install -m 0644 ${WORKDIR}/99-crs-network.conf ${D}${sysconfdir}/sysctl.d/

    # systemd units
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/configfs.mount ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/home.mount ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/jupyter-home-init.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/crs-ethtool-rings.service ${D}${systemd_system_unitdir}/

    # helper scripts
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/jupyter-home-init.sh ${D}${sbindir}/

    # system-wide environment variables (login shells via profile.d)
    install -d ${D}${sysconfdir}/profile.d
    echo 'export CRS_EMBEDDED=1' > ${D}${sysconfdir}/profile.d/crs.sh

    # configfs mount point
    install -d ${D}/configfs
}

FILES:${PN} = " \
    ${sysconfdir}/sysctl.d \
    ${sysconfdir}/profile.d/crs.sh \
    ${systemd_system_unitdir} \
    ${sbindir}/jupyter-home-init.sh \
    /configfs \
"

COMPATIBLE_MACHINE = "t0-crs"
