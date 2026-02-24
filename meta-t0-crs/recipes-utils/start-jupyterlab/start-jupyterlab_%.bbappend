FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Auto-enable Jupyter on boot
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# Override the config to disable token/password auth
SRC_URI:append = " file://jupyter_server_config.py"
