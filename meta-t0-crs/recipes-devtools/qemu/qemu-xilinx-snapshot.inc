# Snapshots of the system-level cosimulation (c/x86_64/cosim in crs-mkids):
# a ZynqMP guest saved with migrate and restored with -incoming must come
# back with its state. 0001 is AMD's own fix from their master (9a86eab1),
# which the I/O module's state in 0004 relies on; 0002-0005 are ours, for
# AMD's fork (0003, the MicroBlaze one, is an upstream bug too). Drop each
# once the fork has it.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " \
    file://0001-hw-intc-xilinx_iomod_intc.c-fix-memory-corruption.patch \
    file://0002-cpu-target-migrate-the-CPU-reset-and-halt-pins.patch \
    file://0003-target-microblaze-migrate-the-CPU-s-registers.patch \
    file://0004-hw-xilinx_iomod-migrate-the-I-O-module-s-state.patch \
    file://0005-hw-microblaze-microblaze_generic_fdt-migrate-the-mai.patch \
"
