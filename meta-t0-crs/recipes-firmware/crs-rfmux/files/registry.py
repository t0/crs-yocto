import sys
sys.path.append('/usr/share/tuberd')

# Parse IPMI data and register ourselves via mDNS.
import dbus
import avahi
import fru  # this is a clunky little piece of Lenovo code
f = fru.FRU(open('/sys/bus/i2c/devices/2-0057/eeprom', 'rb').read())
serial = f.info['Board serial number']
revision = f.info['Board model']

bus = dbus.SystemBus()
server = dbus.Interface(bus.get_object(avahi.DBUS_NAME, '/'), avahi.DBUS_INTERFACE_SERVER)

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

backplane_serial = ''
backplane_manufacture_date = ''
backplane_manufacturer = ''
backplane_product = ''
backplane_part = ''
backplane_slot = -1
crate_type = ''
crate_serial = ''

try:
    with open('/sys/bus/i2c/devices/0-0051/eeprom', 'rb') as file:
        f = fru.FRU(file.read())
        backplane_serial = f.info['Board serial number']
        backplane_manufacture_date = f.info['Board manufacture date']
        backplane_manufacturer = f.info['Board manufacturer']
        backplane_product = f.info['Board product name']
        backplane_part = f.info['Board model']
        crate_type = f.info['Chassis type']
        crate_serial = f.info['Chassis serial number']
        custom_fields = f.info['chassis_extra'] # should be ["slot=3"]
        for field in custom_fields:
            tokens = field.split('=')
            if len(tokens) == 2 and tokens[0] == 'slot':
                try:
                    backplane_slot = int(tokens[1])
                except (ValueError, TypeError):
                    backplane_slot = -1
except FileNotFoundError:
    print("Not connected to a backplane")
except PermissionError:
    print("Permission denied")
except OSError as e:
    print(f"Failed to read backplane EEPROM: {e}")
except KeyError as e:
    print(f"Backplane FRU data is missing field {e}")
except (ValueError, IndexError) as e:
    print(f"Backplane FRU data is invalid: {e}")

# Create registry with a Dfmux instance in it
import libmkids
d = libmkids.Dfmux(ipmi_serial=serial, ipmi_revision=revision)
d._set_backplane_ipmi(backplane_manufacturer=backplane_manufacturer,
                     backplane_product=backplane_product,
                     backplane_serial=backplane_serial,
                     backplane_slot=backplane_slot,
                     crate_type=crate_type,
                     crate_serial=crate_serial)

registry = { "Dfmux": d }
