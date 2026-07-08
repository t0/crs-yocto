#!/bin/sh
# Provision and mount /home partition.
#
# The WIC image ships with only boot + rootfs partitions. This script
# creates partition 3 (/home) at the end of the device if it doesn't
# exist, formats it if needed, and mounts it.
#
# After a --reflash-mmc cycle, u-boot preserves the /home partition
# table entry, so only the mount is needed on subsequent boots.

set -e

LABEL=home
MARKER=.provisioned
MOUNTPOINT=/home

# Determine root block device from kernel command line
for arg in $(cat /proc/cmdline); do
    case "$arg" in
        root=*)
            rootdev="${arg#root=}"
            ;;
    esac
done

case "$rootdev" in
    /dev/mmcblk*)
        # e.g. /dev/mmcblk0p2 -> /dev/mmcblk0, partition prefix is "p"
        DISK=$(echo "$rootdev" | sed 's/p[0-9]*$//')
        PARTPREFIX="${DISK}p"
        ;;
    /dev/nvme*)
        # e.g. /dev/nvme0n1p2 -> /dev/nvme0n1, partition prefix is "p"
        DISK=$(echo "$rootdev" | sed 's/p[0-9]*$//')
        PARTPREFIX="${DISK}p"
        ;;
    LABEL=root)
        # Resolve by label
        DISK=$(readlink -f /dev/disk/by-label/root | sed 's/p\?[0-9]*$//')
        if echo "$DISK" | grep -q 'mmcblk\|nvme'; then
            PARTPREFIX="${DISK}p"
        else
            PARTPREFIX="${DISK}"
        fi
        ;;
    *)
        echo "crs-home-reprovision: cannot determine disk from root=$rootdev" >&2
        exit 1
        ;;
esac

PART3="${PARTPREFIX}3"

do_mount() {
    mount -t ext4 -o defaults,noatime,sync "$PART3" "$MOUNTPOINT"
}

# If partition 3 device node exists, try to mount it
if [ -b "$PART3" ]; then
    if do_mount 2>/dev/null; then
        echo "crs-home-reprovision: $PART3 mounted"
        exit 0
    fi
    # Partition exists but can't mount — reformat it
    echo "crs-home-reprovision: $PART3 exists but unreadable, reformatting"
    mkfs.ext4 -F -L "$LABEL" "$PART3"
    do_mount
    echo "crs-home-reprovision: $PART3 reformatted and mounted"
    exit 0
fi

# Partition 3 doesn't exist — create it at the end of the device.
# This leaves room between rootfs and /home for rootfs to grow.
echo "crs-home-reprovision: creating $PART3 on $DISK"

HOME_SIZE_MB=256
HOME_SIZE_SECTORS=$((HOME_SIZE_MB * 2048))  # 512-byte sectors

# Get disk size in sectors
DISK_SECTORS=$(blockdev --getsz "$DISK")

# Leave the last 33 sectors alone (GPT backup, even though we use MBR —
# avoids issues if the disk was previously GPT-formatted)
DISK_END=$((DISK_SECTORS - 33))

# Place /home at the end, aligned to 1 MiB
START=$(( ((DISK_END - HOME_SIZE_SECTORS) / 2048) * 2048 ))

# Sanity check: make sure we don't overlap existing partitions
LAST_USED=$(sfdisk -l "$DISK" 2>/dev/null | awk '/^\/dev/ {end=$2+$4} END {print end+0}')
if [ "$START" -le "$LAST_USED" ]; then
    echo "crs-home-reprovision: not enough space for ${HOME_SIZE_MB}M /home partition" >&2
    exit 1
fi

echo "start=${START}, size=$((DISK_END - START)), type=83" | sfdisk --append --force "$DISK"

# Notify kernel of the new partition
partx --add --nr 3 "$DISK"

if [ "$(blkid -o value -s TYPE "$PART3")" != "ext4" ]; then
	mkfs.ext4 -F -L "$LABEL" "$PART3"
fi
do_mount

echo "crs-home-reprovision: $PART3 created and mounted"
