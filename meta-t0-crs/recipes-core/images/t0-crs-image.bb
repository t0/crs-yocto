DESCRIPTION = "Standard image definition for t0.technology CRS board"
LICENSE = "MIT"

IMAGE_FEATURES += "ssh-server-openssh read-only-rootfs"

require recipes-core/images/core-image-minimal.bb

IMAGE_NAME_SUFFIX ?= ""
