SUMMARY = "Fast, correct Python JSON library supporting dataclasses, datetimes, and numpy"
HOMEPAGE = "https://github.com/ijl/orjson"
LICENSE = "Apache-2.0 | MIT"
LIC_FILES_CHKSUM = "file://LICENSE-APACHE;md5=1836efb2eb779966696f473ee8540542"
RECIPE_MAINTAINER = "Tom Geelen <t.f.g.geelen@gmail.com>"

require ${BPN}-crates.inc

PYPI_PACKAGE = "orjson"

SRC_URI[sha256sum] = "06fb40f8e49088ecaa02f1162581d39e2cf3fd9dbbfe411eb2284147c99bad79"
SRC_URI:append = " \
    file://0001-Fix-compilation-error-for-orjson.patch \
"

inherit pypi python_maturin

PTEST_PYTEST_DIR = "test"

# orjson segfaults if these are not present
RDEPENDS:${PN} += " \
    python3-zoneinfo \
    python3-json \
"
