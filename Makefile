default: rootfs
.PHONY: default clean rootfs

XSA ?= t0-crs.xsa
DTSI ?= dts/t0-crs.dtsi

MACHINEFILE := build/conf/machine/t0-crs.conf
SDTFILE := sdt/system-top.dts
INITIALIZED = .initialized

# Step 0: prepare the build filesystem
$(INITIALIZED):
	git submodule sync --recursive
	git submodule update --init --recursive
	TEMPLATECONF="$(PWD)/meta-t0-crs/conf/templates/t0-crs" source poky/oe-init-build-env
	touch $(INITIALIZED)

# Step 1: XSA -> sdt
$(SDTFILE): $(INITIALIZED) $(XSA) $(DTSI) bin/sdtgen
	source /opt/xilinx/2025.1/Vivado/settings64.sh && bin/sdtgen $(XSA) $(DTSI)

# Step 2: sdt -> machine configuration
$(MACHINEFILE): $(SDTFILE)
	# Generate the machine definition from the materials in 'sdt', which
	# themselves can be updated from a Vivado-generated .xsa file using
	# bin/sdtgen. There's an annoying chicken-and-egg: gen-machine-conf
	# only works if MACHINE can be found, but we're calling this script in
	# order to generate it. We use qemu-zynqmp as a placeholder.
	source poky/oe-init-build-env && \
		MACHINE=qemu-zynqmp \
		../meta-xilinx/meta-xilinx-core/gen-machine-conf/gen-machine-conf \
		parse-sdt --hw-description ../sdt --machine-name=t0-crs

# Step 3: build image
rootfs: $(MACHINEFILE)
	source poky/oe-init-build-env && bitbake t0-crs-image

clean:
	rm -rf build output sdt
