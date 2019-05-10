# Arduino Setup
## Arduino Revision 3 will be reading dials 1 - 4
- Mapping:
  - Pins 2, 4, 6, and 8 are Counter Clockwise Turns (Green and Orange Wires) for knobs 1, 2, 3, and 4 respectively.
  - Pins 3, 4, 5, and 7 are Clockwise Turns (Yellow and Red Wires) for knobs 1, 2, 3, and 4 respectively.
- Code exists in "Arduino_Keyboard.ino".

## Arduino Version 1 will be reading our push buttons and our main selection dial.
- Mapping:
  - Pins 2 and 3 are for the menu knob CCW and CW using the Orange and Green wires respectively.
  - Pin 4 is reserved for if we need to use the push button on the knob.
  - Pins 5, 6, and 7 are for the buttons 1 - 3 respectively. The wire color does not matter because it is just completing a circuit.
- Code exists in "Arduino_Keyboard_2.ino"
