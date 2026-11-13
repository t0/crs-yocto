# Snapshots of the system-level cosimulation (c/x86_64/cosim in crs-mkids):
# a ZynqMP guest saved with migrate and restored with -incoming must come
# back with its state. 0001 is AMD's own fix from their master (9a86eab1),
# which the I/O module's state in 0004 relies on; 0002-0006 are ours, for
# AMD's fork (0003, the MicroBlaze one, is an upstream bug too). 0005 is for
# this release only: on AMD's master, hwdtb creates that memory, and the fix
# is there. Drop each once the fork has it.
#
# 0007 and 0008 are not about snapshots: they close a race in booting through
# the FSBL, which loads the PMU's firmware while the PMU's ROM may still be
# handling the IPI that announces it (with the hardware device tree's
# posted-updates = <0> on the PMU's interrupt line: qemu-board.dtso).
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " \
    file://0001-hw-intc-xilinx_iomod_intc.c-fix-memory-corruption.patch \
    file://0002-cpu-target-migrate-the-CPU-reset-and-halt-pins.patch \
    file://0003-target-microblaze-migrate-the-CPU-s-registers.patch \
    file://0004-hw-migrate-the-I-O-module-s-and-the-generic-timer-s-.patch \
    file://0005-hw-microblaze-microblaze_generic_fdt-migrate-the-mai.patch \
    file://0006-hw-misc-xlnx-zynqmp-boot-migrate-the-boot-sequence-s.patch \
    file://0007-target-microblaze-lower-mb_sleep-when-an-interrupt-w.patch \
    file://0008-hw-core-remote-port-process-what-precedes-a-response.patch \
"
