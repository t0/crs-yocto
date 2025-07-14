default: rootfs

.PHONY: default clean rootfs

rootfs:
	git submodule init
	git submodule update
	source ./setenv.sh && bitbake t0-crs-image

clean:
	rm -rf build
