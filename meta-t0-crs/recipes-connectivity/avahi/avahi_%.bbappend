EXTRA_OECONF:remove = "--disable-python"
EXTRA_OECONF:append = " --enable-python --disable-python-dbus"
DEPENDS:append = " python3-pygobject"

inherit python3-dir

# avahi's Makefile.am gates __init__.py installation on HAVE_PYTHON_DBUS,
# but the module itself is just constants — install it manually.
do_install:append() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/avahi
    install -m 0644 ${S}/avahi-python/avahi/__init__.py ${D}${PYTHON_SITEPACKAGES_DIR}/avahi/

    # Suppress mDNS advertisements on the data-plane interface.
    sed -i 's/^#allow-interfaces=eth0$/allow-interfaces=eth0/' ${D}${sysconfdir}/avahi/avahi-daemon.conf
    grep -qx 'allow-interfaces=eth0' ${D}${sysconfdir}/avahi/avahi-daemon.conf
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}/avahi"
