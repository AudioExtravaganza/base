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
  if (digitalRead(2) == LOW) { //Menu Knob Left (CCW)
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 44;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(3) == LOW) { //Menu Knob Right (CW)
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 46;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(4) == LOW) { //Menu Knob Button
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    //We need to figure out if we need a Menu Knob Button, or if just using the left/right will work.
    //buf[0] = ;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(5) == LOW) { //Button 1
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 49;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(6) == LOW) { //Button 2
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 50;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else if (digitalRead(7) == LOW) { //Button 3
    digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
    buf[0] = 51;
    Serial.write(buf, 8);
    delay(10);                       // wait for a second
  }
  else {
    buf[0] = 0;
    Serial.write(buf, 8);
    digitalWrite(LED_BUILTIN, LOW);    // turn the LED off by making the voltage LOW
  }
}
