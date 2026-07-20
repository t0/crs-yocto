# Preload a CRS handle for on-board IPython kernels and consoles.
import socket
import rfmux

# SimpleContext must come from the tuber snapshot vendored into rfmux -
# it is the implementation the CRS class is built on. (The standalone
# "tuber" package from python3-tuberd is a separate copy; mixing the two
# breaks metadata parsing.)
from rfmux.tuber.client import SimpleContext

_serial = socket.gethostname().removeprefix("rfmux")
s = rfmux.load_session('!HardwareMap [ !CRS { serial: "%s" } ]' % _serial)
crs = s.query(rfmux.CRS).one()

# Resolve serially: no event loop is running yet during kernel init.
with SimpleContext(crs, convert_json=False, return_exceptions=False, timeout=10) as ctx:
    ctx._add_call(object=crs._tuber_objname, resolve=True)
    _meta = ctx()[0]

crs._resolve_meta(_meta)
