# The RFDC driver's source, which libmkids compiles in (for crs-rfmux, and for
# t0-cosim-sdk, which ships it to build libmkids with).
#
# The driver reads its configuration (XRFdc_Config) by copying the RFDC
# node's "param-list" over the struct, unchecked. sdtgen wrote that
# param-list (sdt/pl.dtsi) with the serializer in the Vivado install's
# data/system-device-tree-xlnx/rfdc, which pairs with the drivers in the
# same install's data/embeddedsw: so the driver comes from there too, from
# the same XILINX_VIVADO (the newest rfdc_v*, the one that release's RFDC IP
# uses), rather than from an embeddedsw checkout whose version would have to
# be kept in step by hand. (rfdc_param_list_check, below, checks that the
# sizes agree.)
def t0_rfdc_driver_src(d):
    import glob
    vivado = d.getVar('XILINX_VIVADO')
    if not vivado:
        return ""
    drivers = glob.glob(os.path.join(os.path.dirname(vivado), 'data/embeddedsw/XilinxProcessorIPLib/drivers/rfdc_v*'))
    if not drivers:
        return ""
    version = lambda p: [int(n) for n in os.path.basename(p)[len('rfdc_v'):].split('_')]
    return os.path.join(max(drivers, key=version), 'src')

XILINX_VIVADO ?= ""
RFDC_DRIVER_SRC = "${@t0_rfdc_driver_src(d)}"
# (local.conf keeps XILINX_VIVADO out of task signatures; the driver's path,
# which names the release and the driver's version, goes in)
RFDC_DRIVER_SRC[vardepvalue] = "${RFDC_DRIVER_SRC}"

# The SDT that sdtgen wrote, whose param-list the driver will read (as
# configured: gen-machine-conf's input)
RFDC_SDT_PL = "${TOPDIR}/../sdt/pl.dtsi"

# A shell function: fails unless RFDC_DRIVER_SRC is there, and its
# XRFdc_Config is the size of the SDT's param-list (compiled with $1, a C
# compiler for the target, and the rest of the arguments)
rfdc_param_list_check() {
	if [ ! -d "${RFDC_DRIVER_SRC}" ]; then
		bbfatal "No RFDC driver in XILINX_VIVADO's install (\"${XILINX_VIVADO}\"/../data/embeddedsw): set XILINX_VIVADO to the Vivado that made the SDT"
	fi
	bytes=$(sed -n '/param-list = \[/{s/.*param-list = \[\(.*\)\];.*/\1/p}' ${RFDC_SDT_PL} | wc -w)
	if [ "$bytes" -eq 0 ]; then
		bbfatal "No RFDC param-list in ${RFDC_SDT_PL}"
	fi
	echo "#include \"xrfdc.h\"
_Static_assert(sizeof(XRFdc_Config) == $bytes,
	\"XRFdc_Config (${RFDC_DRIVER_SRC}) isn't the size of the SDT's param-list ($bytes bytes): the driver doesn't match the Vivado that made the SDT\");" |
		"$@" -fsyntax-only -I${RFDC_DRIVER_SRC} -x c - ||
		bbfatal "The RFDC driver (${RFDC_DRIVER_SRC}) doesn't match the SDT's param-list (${RFDC_SDT_PL})"
}
