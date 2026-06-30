default: rootfs
.PHONY: default clean rootfs sdt machine dockerenv

XSA ?= t0-crs.xsa
DTSI ?= dts/t0-crs.dtsi

MACHINEFILE := build/conf/machine/t0-crs.conf
SDTFILE := sdt/system-top.dts
INITIALIZED = .initialized

# Docker boilerplate
DOCKERSTAMP := .dockerenv-$(shell hostname)
DOCKERNAME := crs-yocto-docker

# The LD_LIBRARY_PATH modifications done by Vivado's settings64.sh seem to poison
# Yocto. You should set XILINX_VIVADO but not alter LD_LIBRARY_PATH.
ifneq ($(shell which vivado 2>/dev/null),)
 $(error Vivado is in $$PATH — settings64.sh appears to have been sourced. \
 This will poison Yocto builds via LD_LIBRARY_PATH. \
 Please start a clean shell and set XILINX_VIVADO instead)
endif
XILINX_VIVADO ?= /opt/xilinx/2025.1/Vivado
export XILINX_VIVADO

# Vivado install path (bind-mounted into container)
# 2025.1+: /opt/xilinx/2025.1/Vivado
# older:   /opt/xilinx/Vivado/2023.2
XILINX_ROOT ?= $(realpath $(XILINX_VIVADO)/../..)

# Use a "yocto" group if it exists, or fall back on the user's group
GID := $(shell getent group yocto 2>/dev/null | cut -d: -f3 || id -g)

DOCKER_RUN = docker run --rm \
	--network host \
	--ipc=host \
	--pid=host \
	-u $(shell id -u):$(GID) \
	-v $(HOME):$(HOME) \
	-v /var/tmp:/var/tmp \
	-v $(XILINX_ROOT):$(XILINX_ROOT) \
	-e HOME \
	-e XILINX_VIVADO \
	$(DOCKERNAME)

$(DOCKERSTAMP) dockerenv: Dockerfile
	docker build -t $(DOCKERNAME) - < $<
	touch $(DOCKERSTAMP)

# Step 0: prepare the build filesystem (submodules run on host, oe-init in container)
$(INITIALIZED): $(DOCKERSTAMP)
	git submodule sync --recursive
	git submodule update --init --recursive
	$(DOCKER_RUN) bash -c \
		'cd $(CURDIR) && TEMPLATECONF="$(CURDIR)/meta-t0-crs/conf/templates/t0-crs" source poky/oe-init-build-env'
	touch $(INITIALIZED)

# Step 1: XSA from Vivado project (no synthesis — just block design metadata)
# The XSA only contains the block design; RTL/constraints don't affect it.
$(XSA): $(INITIALIZED) crs-mkids/tcl/hw.tcl crs-mkids/tcl/bd.tcl
	$(DOCKER_RUN) bash -c \
		'cd $(CURDIR)/crs-mkids/tcl && source $(XILINX_VIVADO)/settings64.sh && \
		vivado -mode batch -source xsa.tcl -tclargs -force $(CURDIR)/$(XSA)'

# Step 2: XSA -> sdt (requires Vivado)
sdt: $(SDTFILE)
$(SDTFILE): $(XSA) $(DTSI) bin/sdtgen
	$(DOCKER_RUN) bash -c \
		'cd $(CURDIR) && source $(XILINX_VIVADO)/settings64.sh && bin/sdtgen $(XSA) $(DTSI)'

# Step 3: sdt -> machine configuration
machine: $(MACHINEFILE)
$(MACHINEFILE): $(SDTFILE)
	$(DOCKER_RUN) bash -c \
		'cd $(CURDIR) && source poky/oe-init-build-env && \
		MACHINE=qemu-zynqmp \
		../meta-xilinx/meta-xilinx-core/gen-machine-conf/gen-machine-conf \
		parse-sdt --hw-description ../sdt --machine-name=t0-crs'

# Step 4: build image (bitstream is built here via crs-rfmux recipe)
rootfs: $(MACHINEFILE)
	$(DOCKER_RUN) bash -c \
		'cd $(CURDIR) && source poky/oe-init-build-env && bitbake t0-crs-image'

clean:
	rm -rf build output sdt
