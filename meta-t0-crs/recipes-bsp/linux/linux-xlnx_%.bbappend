FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:t0-crs = " file://linux-t0-crs.cfg"

# The PCA9541 arbitration timeout patch from meta-system-controller is
# obsolete: upstream v2025.2 replaced the hardcoded timeouts with the
# I2C adapter's configurable timeout.
SRC_URI:remove:t0-crs = " file://0001-PCA9541-Increase-I2C-bus-arbitration-timeout.patch"
