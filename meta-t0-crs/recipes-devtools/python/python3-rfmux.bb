SUMMARY = "Python API for t0.technology CRS boards"
HOMEPAGE = "https://github.com/t0/rfmux"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2491ac3ff16d34a839711d75658cded7"

PV = "1.6.0"
# de-vendored tuber (tuber client now comes from python3-tuberd);
# revert branch to main once devendor_tuber merges
SRCREV = "56f97d6c53a1e1374bed2650ad9a61471f422e0e"
SRC_URI = "git://github.com/t0/rfmux.git;protocol=https;branch=devendor_tuber"
S = "${WORKDIR}/git"

export SETUPTOOLS_SCM_PRETEND_VERSION = "${PV}"

inherit cmake setuptools3-base python_pep517

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
    python3-scipy \
    python3-tuberd \
"

INSANE_SKIP:${PN} += "already-stripped"
