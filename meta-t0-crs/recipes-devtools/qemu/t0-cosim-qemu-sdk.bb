SUMMARY = "AMD's QEMU, with the snapshot patches, as an installable SDK"
DESCRIPTION = "nativesdk-qemu-xilinx (qemu-system-aarch64, \
qemu-system-microblazeel, qemu-img), with meta-t0-crs's snapshot patches, \
for system-level cosimulation (c/x86_64/cosim in crs-mkids), which runs the \
image's QEMU from it: a self-relocating installer, beside the image."
LICENSE = "MIT"

# (as buildtools-tarball: host tools only, for no target)
TOOLCHAIN_TARGET_TASK = ""
# (the system emulators are packaged separately from nativesdk-qemu-xilinx,
# which carries only qemu-img and the other tools)
TOOLCHAIN_HOST_TASK = "nativesdk-sdk-provides-dummy nativesdk-qemu-xilinx \
    nativesdk-qemu-xilinx-system-aarch64 nativesdk-qemu-xilinx-system-microblazeel"

MULTIMACH_TARGET_SYS = "${SDK_ARCH}-nativesdk${SDK_VENDOR}-${SDK_OS}"
PACKAGE_ARCH = "${SDK_ARCH}_${SDK_OS}"
PACKAGE_ARCHS = ""
TARGET_ARCH = "none"
TARGET_OS = "none"

TOOLCHAIN_OUTPUTNAME = "t0-cosim-qemu-${SDK_ARCH}"
SDK_TITLE = "AMD QEMU for CRS system-level cosimulation"

RDEPENDS = "${TOOLCHAIN_HOST_TASK}"

EXCLUDE_FROM_WORLD = "1"

inherit populate_sdk
inherit toolchain-scripts-base
inherit nopackages

deltask install
deltask populate_sysroot

# (as buildtools-tarball, which does all of these too: for no target, there's
# no target compiler or libc to depend on, configure cache to make, or SDK to
# test)
INHIBIT_DEFAULT_DEPS = "1"
TOOLCHAIN_NEED_CONFIGSITE_CACHE = ""
SDK_CLASSES:remove = "testsdk"
do_populate_sdk[stamp-extra-info] = "${PACKAGE_ARCH}"
REAL_MULTIMACH_TARGET_SYS = "none"

# The default environment script sets up a cross toolchain for the "none"
# target; replace it with one that only puts the SDK's programs on PATH. It
# must still name OECORE_NATIVE_SYSROOT: the installer reads the sysroot to
# relocate from it, and without it leaves every program's ELF interpreter
# pointing into the build machine's SDK path.
create_sdk_files:append () {
	rm -f ${SDK_OUTPUT}/${SDKPATH}/site-config-*
	rm -f ${SDK_OUTPUT}/${SDKPATH}/environment-setup-*
	rm -f ${SDK_OUTPUT}/${SDKPATH}/version-*

	script=${SDK_OUTPUT}/${SDKPATH}/environment-setup-${SDK_SYS}
	echo 'export PATH="${SDKPATHNATIVE}${bindir_nativesdk}:$PATH"' > $script
	echo 'export OECORE_NATIVE_SYSROOT="${SDKPATHNATIVE}"' >> $script
	toolchain_create_sdk_version ${SDK_OUTPUT}/${SDKPATH}/version-${SDK_SYS}
}
