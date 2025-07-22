SUMMARY = "A C++ library for building high performance RESTful web servers."
HOMEPAGE = "https://github.com/etr/libhttpserver"
LICENSE = "LGPL-2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fbc093901857fcd118f065f900982c24"
DEPENDS += "libmicrohttpd"

SRC_URI = "https://github.com/etr/${BPN}/archive/refs/tags/${PV}.tar.gz"
SRC_URI[sha256sum] = "b108769ed68d72c58961c517ab16c3a64e4efdc4c45687723bb45bb9e04c5193"

inherit autotools

PACKAGECONFIG_CONFARGS += "--disable-examples"
