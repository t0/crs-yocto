FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Auto-enable Jupyter on boot
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# Override config and add systemd drop-in to run as jupyter user
SRC_URI:append = " \
    file://jupyter_server_config.py \
    file://ipython_config.py \
    file://crs_preload.py \
    file://jupyter-user.conf \
"

do_install:append() {
    # Install systemd drop-in to run as jupyter user
    install -d ${D}${systemd_system_unitdir}/jupyter-setup.service.d
    install -m 0644 ${WORKDIR}/jupyter-user.conf \
        ${D}${systemd_system_unitdir}/jupyter-setup.service.d/jupyter-user.conf

    # System-wide IPython config (applies to Jupyter kernels and interactive sessions)
    install -d ${D}${sysconfdir}/ipython
    install -m 0644 ${WORKDIR}/ipython_config.py ${D}${sysconfdir}/ipython/
    install -m 0644 ${WORKDIR}/crs_preload.py ${D}${sysconfdir}/ipython/
}

FILES:${PN} += " \
    ${systemd_system_unitdir}/jupyter-setup.service.d \
    ${sysconfdir}/ipython \
"
