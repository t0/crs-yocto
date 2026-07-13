#!/bin/sh
# Populate /home/jupyter on first boot.
# The /home partition starts as an empty ext4 filesystem; this script
# creates the jupyter user's home directory and provisions reference notebooks.

JUPYTER_HOME=/home/jupyter

if [ ! -d "$JUPYTER_HOME" ]; then
    mkdir -p "$JUPYTER_HOME"
    chown jupyter:jupyter "$JUPYTER_HOME"
fi

# Locate rfmux reference-notebooks inside the installed package.
# Use a glob rather than importing rfmux — the import is slow and can
# fail in the minimal early-boot systemd environment.
REF_SRC=$(echo /usr/lib/python3*/site-packages/rfmux/reference-notebooks)
# The glob is literal (no match) if rfmux isn't installed; the -d test catches that.

if [ -n "$REF_SRC" ] && [ -d "$REF_SRC" ]; then
    # Copy each subdirectory (Demos, Release Notes, etc.) if not already present
    for dir in "$REF_SRC"/*/; do
        name=$(basename "$dir")
        dest="$JUPYTER_HOME/$name"
		cp -a "$dir". "$dest"
		chmod -R a-w "$dest"
		chown -R jupyter:jupyter "$dest"
    done

    # Copy README.md if not already present
    if [ -f "$REF_SRC/README.md" ] && [ ! -e "$JUPYTER_HOME/README.md" ]; then
        cp "$REF_SRC/README.md" "$JUPYTER_HOME/README.md"
        chmod 444 "$JUPYTER_HOME/README.md"
        chown jupyter:jupyter "$JUPYTER_HOME/README.md"
    fi
fi
