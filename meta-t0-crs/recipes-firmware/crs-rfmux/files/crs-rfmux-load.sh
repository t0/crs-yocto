#!/bin/sh
# Load CRS rfmux FPGA bitstream and apply device tree overlay.
# The overlay's firmware-name property triggers the kernel to load the
# bitstream via the firmware loader automatically.  The HMC7044 clock
# tree is configured as part of overlay application (SPI driver probe).
#
# mod_rfmux is loaded *after* the overlay so the PL fabric is clocked
# before the driver touches AXI registers.

set -eu

mkdir -p /sys/kernel/config/device-tree/overlays/crs-rfmux
echo xilinx/crs-rfmux/crs-rfmux.dtbo > /sys/kernel/config/device-tree/overlays/crs-rfmux/path

modprobe mod_rfmux
