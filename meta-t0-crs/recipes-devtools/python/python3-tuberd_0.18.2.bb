DESCRIPTION = "A C++ server and Python client for exposing an instrumentation control plane across a network."
HOMEPAGE = "https://github.com/gsmecher/tuberd"
DEPENDS +="python3-setuptools-scm-native"
RECIPE_MAINTAINER = "Graeme Smecher <gsmecher@t0.technology>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=92ff510efeaad7ba38823f5dc8fda92d"

SRC_URI[sha256sum] = "b91fa9021fc1d718761b2e96b3427ac0c592f1796fc9dfd84a0902ac7a13667a"
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
