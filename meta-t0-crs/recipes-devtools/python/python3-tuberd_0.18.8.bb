DESCRIPTION = "A C++ server and Python client for exposing an instrumentation control plane across a network."
HOMEPAGE = "https://github.com/gsmecher/tuberd"
RECIPE_MAINTAINER = "Graeme Smecher <gsmecher@t0.technology>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=92ff510efeaad7ba38823f5dc8fda92d"

SRC_URI[sha256sum] = "74cc6b165299a3372619b6a52af3afb99c2c2215fac91f50fd421f425a906e30"
SRC_URI:append = " \
    file://0001-Workaround-for-pre-PEP-621-setuptools.patch \
"

inherit cmake pypi python_setuptools_build_meta

DEPENDS += " \
    python3-pybind11-native \
    python3-setuptools-scm-native \
"

export CMAKE_ARGS += "${OECMAKE_ARGS}"

# pyproject.toml asks for pybind11 2.13 (for free-threaded Python); the tree's
# is 2.12 (meta-python-ai), which the source still supports
PEP517_BUILD_OPTS = "--skip-dependency-check"

RDEPENDS:${PN} += " \
    python3-pybind11 \
    python3-orjson \
    python3-cbor2 \
    python3-dbus \
    python3-requests \
    python3-requests-futures \
"

INSANE_SKIP:${PN}:append = "already-stripped"

# we needed the cmake class to set up the environment, but don't want to
# actually invoke it
do_configure() {
    python_pep517_do_configure
}
do_compile() {
    python_pep517_do_compile
}
do_install() {
    python_pep517_do_install
}
