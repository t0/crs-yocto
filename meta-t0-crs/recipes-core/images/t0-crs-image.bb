DESCRIPTION = "Standard image definition for t0.technology CRS board"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

IMAGE_FEATURES += "ssh-server-openssh read-only-rootfs"
IMAGE_INSTALL += " \
        packagegroup-xilinx-jupyter \
        python3-tuberd \
        "

IMAGE_NAME_SUFFIX ?= ""
IMAGE_FSTYPES += "wic"
