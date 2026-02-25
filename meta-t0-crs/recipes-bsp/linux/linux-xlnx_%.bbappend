FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:t0-crs = " file://linux-t0-crs.cfg"

# The PCA9541 arbitration timeout patch from meta-system-controller is
# obsolete: upstream v2025.2 replaced the hardcoded timeouts with the
# I2C adapter's configurable timeout.
SRC_URI:remove:t0-crs = " file://0001-PCA9541-Increase-I2C-bus-arbitration-timeout.patch"

# Use the Analog Devices kernel fork, which includes the HMC7044 clock
# jitter attenuator driver and other ADI IIO/JESD204 infrastructure
# needed by crs-rfmux.
KERNELURI:t0-crs = "git://github.com/analogdevicesinc/linux.git;protocol=https;name=machine"
SRCREV:t0-crs = "8654f35e24e97c40401e5e67a6843e2c9ed6b9fd"
KBRANCH:t0-crs = "main"
LINUX_VERSION:t0-crs = "6.12.0"
LINUX_VERSION_EXTENSION:t0-crs = "-adi"
