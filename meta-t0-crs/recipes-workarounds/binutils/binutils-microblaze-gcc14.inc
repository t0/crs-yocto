# Workaround gcc-14 build errors in the elf64-microblaze and microblaze
# assembler/disassembler code. These patches are from
# github.com/gsmecher/meta-xilinx (branch binutils-patches-fix) and should
# be dropped once Xilinx incorporates them upstream.

FILESEXTRAPATHS:prepend := "${THISDIR}/binutils:"

SRC_URI += " \
    file://0057-elf64-microblaze-correct-type-signature-mismatches.patch \
    file://0058-microblaze64-use-elf_dyn_relocs-rather-than-elf64_mb.patch \
    file://0059-microblaze-Correct-missing-return-type-in-function-s.patch \
    file://0060-microblaze-correct-incompatible-type-cast-gcc-14.patch \
    file://0061-RFC-work-around-compiler-errors-that-certainly-deser.patch \
"
