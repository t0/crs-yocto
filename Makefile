default: rootfs

.PHONY: default clean rootfs

rootfs:
	git submodule init
	git submodule update
	source ./setenv.sh && bitbake petalinux-image-minimal

clean:
	rm -rf build
