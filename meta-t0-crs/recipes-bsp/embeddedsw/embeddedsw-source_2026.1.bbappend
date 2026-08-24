FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Whitelist the eFUSE device DNA registers (0xFFCC100C..0xFFCC1014, 96 bits)
# in the ZynqMP PMUFW MMIO access table so the APU/RPU can read them via the
# EEMI PM_MMIO_READ call (used by U-Boot's "zynqmp mmio_read").
SRC_URI += "file://0001-zynqmp_pmufw-allow-EFUSE-DNA-read-via-PM-MMIO.patch"
