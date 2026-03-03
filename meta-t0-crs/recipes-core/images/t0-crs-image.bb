DESCRIPTION = "Standard image definition for t0.technology CRS board"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

IMAGE_FEATURES += " \
        ssh-server-openssh \
        read-only-rootfs \
        allow-root-login \
        allow-empty-password \
        empty-root-password \
"

IMAGE_INSTALL += " \
        coreutils \
        packagegroup-xilinx-jupyter \
        devmem2 \
        nvme-cli \
        pciutils pciutils-ids \
        python3-rfmux \
        python3-tuberd \
        python3-numpy \
        python3-matplotlib \
        python3-aiohttp \
        python3-scipy \
        python3-pyyaml \
        python3-simplejson \
        python3-sqlalchemy \
        python3-psutil \
        python3-requests-futures \
        python3-awaitless \
        valgrind \
        crs-rfmux \
        crs-base-config \
        mtd-utils \
        "

IMAGE_CLASSES += "extrausers"
EXTRA_USERS_PARAMS = "useradd -m -d /home/jupyter -s /bin/sh jupyter;"

IMAGE_NAME_SUFFIX ?= ""
IMAGE_FSTYPES += "wic"
