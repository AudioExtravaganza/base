import argparse
import threading
import asyncio
import logging
from datetime import datetime
from serial import Serial

from pythonosc import osc_message_builder
from pythonosc import udp_client
from pythonosc.osc_server import AsyncIOOSCUDPServer
from pythonosc.dispatcher import Dispatcher

logging.basicConfig(
    level=logging.DEBUG,
    format='%(levelname)s - %(message)s'
)

SERIAL_STATE = 0

class Knob:
    def __init__(self, control_id, serial_a, serial_b, osc_client):
        self._client = osc_client
        self.control_id = control_id
        self.serial_a = serial_a
        self.serial_b = serial_b

        self.value = 0

        logging.debug(f"Created Knob {control_id}; {1 << control_id:016b}")

    def handle_state(self, state):
        cw = int(1 << self.serial_b & state > 0)
        ccw = int(1 << self.serial_a & state > 0)

        # Pulse has not occured
        if not (cw or ccw): return
        
        # Update state
        self.set_state(1 if cw else -1)

    def set_state(self, val):
        self.value += val
        self._client.send_message('/knob', [self.control_id, self.value])
        logging.debug(f"MSG: ['/knob', [{self.control_id}, {self.value}]")

class MenuKnob(Knob): 
    def __init__(self, serial_a, serial_b, osc_client):
        Knob.__init__(self, 0, serial_a, serial_b, osc_client)

    def set_state(self, val):
        direction = 'left' if val == -1 else 'right'
        self._client.send_message('/menu', [direction])
        logging.debug(f"MSG: ['/menu', [{direction}]")

class Footswitch:
    def __init__(self, control_id, serial_address, osc_client):
        self._client = osc_client
        self._press_start = 0
        self._num_pressed = 0

        self.pedal_state = 0

        self.control_state = 0
        self.control_id = control_id
        self.signal_address = serial_address

        logging.debug(f"Created Pedal {control_id}; {1 << control_id:016b}")

    def handle_state(self, state):
        # Get the new control state
        new_state = int(1 << self.control_id & state > 0)

        if (self.pedal_state != 2):
            # Button has been released
            if (self.control_state == 1 and new_state == 0):
                self.set_state(0 if self.pedal_state > 0 else 1)
                self._press_start = None                          # Stop timer
                self.control_state = 0

            # Button Pressed
            elif(self.control_state == 0 and new_state == 1):
                self.control_state = 1              # Change control state
                self._press_start = datetime.now()  # Start timer

            # Button Held
            if (self._press_start and (datetime.now() - self._press_start).seconds > 1):
                self._press_start = None
                self.set_state(2)         # Send state update
                self.control_state == 0
                self._num_pressed = 0

        else:

            if self.control_state == 1 and new_state == 0:
                self.control_state = 0
                self._press_start = None
                if self._num_pressed > 0:
                    self.set_state(1)

            elif self.control_state == 0 and new_state == 1:
                self.control_state = 1
                self._num_pressed += 1

    def set_state(self, val):
        self.pedal_state = val
        logging.debug(
            f"MSG: [ '/pedal', {self.control_id}, {self.pedal_state}]")
        self._client.send_message(
            '/pedal', [self.control_id, self.pedal_state])


def echo_test(address, *args):
    print(f"{address}: {args}")


dispatcher = Dispatcher()
dispatcher.map("/*", echo_test)

ip = "127.0.0.1"
port = 1337


async def loop(controls, serial):
    global SERIAL_STATE
    while True:
        if(serial.inWaiting() > 0):
            cc = serial.readline()
            SERIAL_STATE = cc[0] << 8 | cc[1]
            # logging.debug(f"{STATE:016b}")

        for k, c in controls.items():
            c.handle_state(SERIAL_STATE)

        await asyncio.sleep(0)


async def init_main():
    global SERIAL_STATE
    server = AsyncIOOSCUDPServer(
        (ip, port), dispatcher, asyncio.get_event_loop())
    client = udp_client.SimpleUDPClient("127.0.0.1", 57120)
    # Create datagram endpoint and start serving
    transport, protocol = await server.create_serve_endpoint()

    controls = {
        "Pedal1": Footswitch(0, 0, client),
        "Pedal2": Footswitch(1, 1, client),
        "Pedal3": Footswitch(2, 2, client),
        "Knob1": Knob(0, 3, 4, client),
        "Knob2": Knob(1, 5, 6, client),
        "Knob3": Knob(2, 7, 8, client),
        "Knob4": Knob(3, 9, 10, client),
        "Menu": MenuKnob(11, 12, client),
    }

    SERIAL_STATE = 0
    # Enter main loop of program
    await loop(controls,  serial=Serial('/dev/tty.usbmodem14201'))

    transport.close()  # Clean up serve endpoint


asyncio.run(init_main())
