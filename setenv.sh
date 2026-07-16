export ROOT=$(readlink -f $(dirname "$BASH_SOURCE"))

export TEMPLATECONF=$ROOT/meta-t0-crs/conf/templates/t0-crs
export PATH=$PATH:$ROOT/meta-xilinx/meta-xilinx-core/gen-machine-conf

source $ROOT/poky/oe-init-build-env build

BUILD_SERVER=${BUILD_SERVER:-straylight}
BUILD_BASE=${BUILD_BASE:-autobuild}

build-rootfs() {
    # Tags ride along because GIT_DESCRIBE (git-describe.bbclass) derives the
    # firmware version from them; plain `git push` doesn't transfer tags, and
    # --follow-tags only handles annotated ones. -f keeps re-pointed tags
    # (e.g. a re-cut rc) in sync too.
    git push -f $BUILD_SERVER:$BUILD_BASE HEAD:rootfs 'refs/tags/*:refs/tags/*'
}

fetch-collateral() {
    # usage: fetch-collateral <collateral> <hash>
    #
    # When hash is not provided (which is probably the most ergonomically
    # plausible case), this fetches the most recent successfully built
    # collateral, so it will reach past failed builds. Because the filename has
    # the hash embedded in it, this situation should be fairly noticeable.
    local path hash
    path=$(ssh "$BUILD_SERVER" \
        "cd $BUILD_BASE/autobuild && ls -td ${2}*-rootfs/$1 2>/dev/null | head -n1") || return
    if [ -z "$path" ]; then
	    echo "fetch-wic: no collateral matching $1 for hash ${2:-(unset)} found" >&2
        return 1
    fi
    hash="${path%%-rootfs/*}"
    scp -p "$BUILD_SERVER:$BUILD_BASE/autobuild/$path" "${hash}-$(basename $1)"
}

fetch-dcp() {
    fetch-collateral 'build/tmp/work/cortexa72-cortexa53-poky-linux/crs-rfmux-bitstream/*/tcl/hw/hw.runs/impl_1/bd_wrapper_routed.dcp' $1
}

fetch-wic() {
    fetch-collateral 'build/tmp/deploy/images/t0-crs/t0-crs-image-t0-crs.wic' $1
}

fetch-uboot() {
    fetch-collateral 'build/tmp/deploy/images/t0-crs/boot.bin' $1
}

fetch-fsbl() {
    fetch-collateral 'build/tmp/deploy/images/t0-crs/fsbl-t0-crs.elf' $1
}

fetch-release() {
    fetch-wic
    fetch-uboot
    fetch-fsbl
}
