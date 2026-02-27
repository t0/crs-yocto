DESCRIPTION = "Teach async and interactive ipython sessions how to get along"
HOMEPAGE = "https://github.com/gsmecher/awaitless"
DEPENDS += "python3-setuptools-scm-native"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=62151a11abe620db80d63bb13b5e1ecc"

SRC_URI[sha256sum] = "0e33230b8c421c2e8c93fb9939995f46924fc3d62e17eab13dbfec824895a845"

inherit pypi python_setuptools_build_meta

RDEPENDS:${PN} += " \
    python3-aiohttp \
    python3-ipython \
"
