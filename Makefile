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

# Vivado install path (bind-mounted into container)
# 2025.1+: /opt/xilinx/2025.1/Vivado
# older:   /opt/xilinx/Vivado/2023.2
XILINX_ROOT ?= $(realpath $(XILINX_VIVADO)/../..)

DOCKER_RUN = docker run --rm \
	-u $(shell id -u):$(shell id -g) \
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
		'cd $(PWD) && TEMPLATECONF="$(PWD)/meta-t0-crs/conf/templates/t0-crs" source poky/oe-init-build-env'
	touch $(INITIALIZED)

# Step 1: XSA -> sdt (requires Vivado)
sdt: $(SDTFILE)
$(SDTFILE): $(INITIALIZED) $(XSA) $(DTSI) bin/sdtgen
	$(DOCKER_RUN) bash -c \
		'cd $(PWD) && source $(XILINX_VIVADO)/settings64.sh && bin/sdtgen $(XSA) $(DTSI)'

# Step 2: sdt -> machine configuration
machine: $(MACHINEFILE)
$(MACHINEFILE): $(SDTFILE)
	$(DOCKER_RUN) bash -c \
		'cd $(PWD) && source poky/oe-init-build-env && \
		MACHINE=qemu-zynqmp \
		../meta-xilinx/meta-xilinx-core/gen-machine-conf/gen-machine-conf \
		parse-sdt --hw-description ../sdt --machine-name=t0-crs'

# Step 3: build image
rootfs: $(MACHINEFILE)
	$(DOCKER_RUN) bash -c \
		'cd $(PWD) && source poky/oe-init-build-env && bitbake t0-crs-image'

clean:
	rm -rf build output sdt
