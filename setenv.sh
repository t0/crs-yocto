export ROOT=$(readlink -f $(dirname "$BASH_SOURCE"))

export TEMPLATECONF=$ROOT/meta-t0-crs/conf/templates/t0-crs
export PATH=$PATH:$ROOT/meta-xilinx/meta-xilinx-core/gen-machine-conf

source $ROOT/poky/oe-init-build-env build

alias go_rootfs="git push -f straylight:autobuild HEAD:rootfs"
