default: rootfs

.PHONY: default clean rootfs

rootfs:
	git submodule init
	git submodule update
	bitbake petalinux-image-minimal

clean:
	rm -rf build
