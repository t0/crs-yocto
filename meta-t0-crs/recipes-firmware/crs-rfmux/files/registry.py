import sys
sys.path.append('/usr/share/tuberd')

# Parse IPMI data and register ourselves via mDNS.
import dbus
import avahi
import fru  # this is a clunky little piece of Lenovo code
import socket
f = fru.FRU(open('/sys/bus/i2c/devices/2-0057/eeprom', 'rb').read())
serial = f.info['Board serial number']
revision = f.info['Board model']

print(f"Registering hostname rfmux{serial}.local")
try:
    bus = dbus.SystemBus()
    server = dbus.Interface(bus.get_object(avahi.DBUS_NAME, '/'), avahi.DBUS_INTERFACE_SERVER)
    server.SetHostName(f'rfmux{serial}')
except dbus.DBusException:
    # Usually an "org.freedesktop.Avahi.NoChangeError" for redundant calls
    pass

# Register service for generic discovery (not tied to hostname)
print(f"Registering service _crs-rfmux._tcp.local")
try:
    group = dbus.Interface(
        bus.get_object(avahi.DBUS_NAME, server.EntryGroupNew()),
        avahi.DBUS_INTERFACE_ENTRY_GROUP
    )

    group.AddService(
        avahi.IF_UNSPEC,
        avahi.PROTO_UNSPEC,
        dbus.UInt32(0),
        f"crs-rfmux-{serial}",
        "_crs-rfmux._tcp",
        "local",
        "",
        dbus.UInt16(80), # port
        avahi.string_array_to_txt_array([
            f"serial={serial}",
            f"revision={revision}"
        ])
    )

    group.Commit()
    print(f"Service registered: crs-rfmux-{serial}._crs-rfmux._tcp.local on port 80")

except dbus.DBusException as e:
    print(f"Failed to register service: {e}")

# Create registry with a Dfmux instance in it
import libmkids
d = libmkids.Dfmux(ipmi_serial=serial, ipmi_revision=revision)

registry = { "Dfmux": d }
