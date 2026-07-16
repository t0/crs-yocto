# Expose the top-level repository's `git describe` as BUILD_ID in
# /etc/os-release.  This is the firmware release identifier:
# get_firmware_release() (crs-mkids, c/src/crs.cpp) reads it back at runtime,
# so cutting a release is just tagging the top-level repo and rebuilding.
#
# BUILD_ID is emitted by adding it to OS_RELEASE_FIELDS, which also puts it
# in do_compile[vardeps] (see os-release.bb).
inherit git-describe

OS_RELEASE_FIELDS:append = " BUILD_ID"
BUILD_ID = "${GIT_DESCRIBE}"
