#!/bin/sh
# Populate /home/jupyter on first boot.
# The /home partition starts as an empty ext4 filesystem; this script
# creates the jupyter user's home directory with correct ownership.

JUPYTER_HOME=/home/jupyter

if [ ! -d "$JUPYTER_HOME" ]; then
    mkdir -p "$JUPYTER_HOME"
    chown jupyter:jupyter "$JUPYTER_HOME"
fi
