DESCRIPTION = "A C++ server and Python client for exposing an instrumentation control plane across a network."
HOMEPAGE = "https://github.com/gsmecher/tuberd"
RECIPE_MAINTAINER = "Graeme Smecher <gsmecher@t0.technology>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=92ff510efeaad7ba38823f5dc8fda92d"

SRC_URI[sha256sum] = "ed6a9e6d2858f7b79ed679cfb87d743d3e97044c050e6b140b6d65bdf8130222"
SRC_URI:append = " \
    file://0001-Workaround-for-pre-PEP-621-setuptools.patch \
    file://FindLibHttpServer.cmake \
"

inherit cmake pypi python_setuptools_build_meta

DEPENDS += " \
    python3-pybind11-native \
    libhttpserver \
    python3-setuptools-scm-native \
"

# Ensure we can find FindLibHttpServer.cmake
export CMAKE_ARGS += "${OECMAKE_ARGS} -DCMAKE_MODULE_PATH=${WORKDIR}"

RDEPENDS:${PN} += " \
    python3-pybind11 \
    python3-orjson \
    python3-cbor2 \
    python3-dbus \
    python3-requests \
    python3-requests-futures \
    libmicrohttpd \
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
