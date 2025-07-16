default: rootfs

.PHONY: init default clean rootfs

XSA ?= t0-crs.xsa
MACHINEFILE := build/conf/machine/t0-crs.conf
SDTFILE := sdt/system-top.dts

# Step 1: XSA -> sdt
$(SDTFILE): $(XSA)
	bin/sdtgen $<

Step 2: sdt -> machine configuration
$(MACHINEFILE): init $(SDTFILE)
	# Generate the machine definition from the materials in 'sdt', which
	# themselves can be updated from a Vivado-generated .xsa file using
	# bin/sdtgen. There's an annoying chicken-and-egg: gen-machine-conf
	# only works if MACHINE can be found, but we're calling this script in
	# order to generate it. We use zcu208-zynqmp as a placeholder.
	source poky/oe-init-build-env && \
		MACHINE=zcu208-zynqmp \
		../meta-xilinx/meta-xilinx-core/gen-machine-conf/gen-machine-conf \
		parse-sdt --hw-description ../sdt --machine-name=t0-crs

init:
	git submodule update --init --recursive

rootfs: init $(MACHINEFILE)
	source poky/oe-init-build-env && bitbake t0-crs-image

clean:
	rm -rf build output
