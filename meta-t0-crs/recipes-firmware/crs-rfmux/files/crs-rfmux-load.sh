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

# eth1 (gem0) is created by the overlay, so it's invisible to U-Boot's
# env->DT MAC fixup and probes with a random address.  Assign it eth0's
# address (0c:0e:c1:e0:ss:s0, derived from the FRU serial in U-Boot)
# with the final nibble set to 1.  This must happen before mod_rfmux
# brings the interface up.
for _ in $(seq 50); do
	[ -e /sys/class/net/eth1 ] && break
	sleep 0.1
done
eth0_mac="$(cat /sys/class/net/eth0/address)"
ip link set dev eth1 address "${eth0_mac%?}1"

modprobe mod_rfmux
