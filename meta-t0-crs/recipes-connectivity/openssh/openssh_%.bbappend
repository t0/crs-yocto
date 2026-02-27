FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Install fixed host keys so boards keep the same fingerprint across reflashes.
SRC_URI:append = " \
    file://ssh_host_ecdsa_key \
    file://ssh_host_ecdsa_key.pub \
    file://ssh_host_ed25519_key \
    file://ssh_host_ed25519_key.pub \
    file://ssh_host_rsa_key \
    file://ssh_host_rsa_key.pub \
"

do_install:append() {
    install -d ${D}${sysconfdir}/ssh
    install -m 0600 ${WORKDIR}/ssh_host_ecdsa_key     ${D}${sysconfdir}/ssh/
    install -m 0644 ${WORKDIR}/ssh_host_ecdsa_key.pub  ${D}${sysconfdir}/ssh/
    install -m 0600 ${WORKDIR}/ssh_host_ed25519_key    ${D}${sysconfdir}/ssh/
    install -m 0644 ${WORKDIR}/ssh_host_ed25519_key.pub ${D}${sysconfdir}/ssh/
    install -m 0600 ${WORKDIR}/ssh_host_rsa_key        ${D}${sysconfdir}/ssh/
    install -m 0644 ${WORKDIR}/ssh_host_rsa_key.pub    ${D}${sysconfdir}/ssh/
}
