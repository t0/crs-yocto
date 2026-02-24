SUMMARY = "Python API for t0.technology CRS boards"
HOMEPAGE = "https://github.com/t0/rfmux"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2491ac3ff16d34a839711d75658cded7"

SRC_URI[sha256sum] = "06d6325f3126b05487401f00af3dc031f29a3865feef5f46c51fe3a5bd663f0a"

inherit cmake pypi setuptools3-base python_pep517

# Tell scikit-build-core where to find cmake/ninja so it doesn't try to
# import the cmake Python module (which lacks the actual binary).
export CMAKE_EXECUTABLE = "${STAGING_BINDIR_NATIVE}/cmake"
export NINJA_EXECUTABLE = "${STAGING_BINDIR_NATIVE}/ninja"

do_configure() {
    python_pep517_do_configure
}
do_compile() {
    python_pep517_do_compile
}
do_install() {
    python_pep517_do_install
}

DEPENDS += " \
    cmake-native \
    ninja-native \
    python3-scikit-build-core-native \
    python3-pybind11-native \
    python3-pybind11 \
    python3-setuptools-scm-native \
    fmt \
"

RDEPENDS:${PN} += " \
    python3-aiohttp \
    python3-numpy \
    python3-pyyaml \
    python3-simplejson \
    python3-requests \
    python3-scipy \
"

INSANE_SKIP:${PN} += "already-stripped"
