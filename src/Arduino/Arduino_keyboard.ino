//#include <Keyboard.h>

uint8_t buf[8] = {0}; //Keyboard Report Buffer

// the setup function runs once when you press reset or power the board
void setup() {
  Serial.begin(9600);
  // initialize digital pin LED_BUILTIN as an output.
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(2, INPUT_PULLUP);
  pinMode(3, INPUT_PULLUP);
  pinMode(4, INPUT_PULLUP);
  pinMode(5, INPUT_PULLUP);
  pinMode(6, INPUT_PULLUP);
  pinMode(7, INPUT_PULLUP);
  pinMode(8, INPUT_PULLUP);
  pinMode(9, INPUT_PULLUP);
  //Keyboard.begin();
}

// the loop function runs over and over again forever
void loop() {
  if (digitalRead(2) == LOW) { //Knob 1 CCW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 97;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(3) == LOW) { //Knob 1 CW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 115;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(4) == LOW) { //Knob 2 CCW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 100;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(5) == LOW) { //Knob 2 CW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 102;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(6) == LOW) { //Knob 3 CCW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 103;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(7) == LOW) { //Knob 3 CW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 104;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(8) == LOW) { //Knob 4 CCW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 106;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(9) == LOW) { //Knob 4 CW
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 107;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else {
    buf[0] = 0;
    Serial.write(buf, 8);
    digitalWrite(LED_BUILTIN, LOW);    // turn the LED off by making the voltage LOW
  }
}
