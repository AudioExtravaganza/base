//#include <Keyboard.h>

uint8_t lst[8] = {0};
uint8_t buf[8] = {0}; //Keyboard Report Buffer
uint16_t lst_buf;
uint16_t int_buf;

// the setup function runs once when you press reset or power the board
void setup() {
  Serial.begin(9600);
  // initialize digital pin LED_BUILTIN as an output.
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(2, INPUT_PULLUP); // Dial 1 Left
  pinMode(3, INPUT_PULLUP); // Dial 1 Right
  pinMode(4, INPUT_PULLUP); // Dial 2 Left
  pinMode(5, INPUT_PULLUP); // Dial 2 Right
  pinMode(6, INPUT_PULLUP); // Dial 3 Left
  pinMode(7, INPUT_PULLUP); // Dial 3 Right
  pinMode(8, INPUT_PULLUP); // Dial M Left
  pinMode(9, INPUT_PULLUP); // Dial M Right

  pinMode(A0, INPUT_PULLUP); // Button 1
  pinMode(A1, INPUT_PULLUP); // Button 2
  pinMode(A2, INPUT_PULLUP); // Button 3
  pinMode(A3, INPUT_PULLUP); // Dial 1 Button
  pinMode(A4, INPUT_PULLUP); // Dial 2 Button
  pinMode(A5, INPUT_PULLUP); // Dial 3 Button
  //Keyboard.begin();
}

// the loop function runs over and over again forever
void loop() {

  // Store the last state
  lst_buf = int_buf;

  // Assign the first group
  bitWrite(int_buf,  0, digitalRead(A0) == LOW);
  bitWrite(int_buf,  1, digitalRead(A1) == LOW);
  bitWrite(int_buf,  2, digitalRead(A2) == LOW);
  bitWrite(int_buf,  3, digitalRead(A3) == LOW);
  bitWrite(int_buf,  4, digitalRead(A4) == LOW);
  bitWrite(int_buf,  5, digitalRead(A5) == LOW);

  // Assign the second group
  bitWrite(int_buf,  6, digitalRead(2) == LOW);
  bitWrite(int_buf,  7, digitalRead(3) == LOW);
  bitWrite(int_buf,  8, digitalRead(4) == LOW);
  bitWrite(int_buf,  9, digitalRead(5) == LOW);
  bitWrite(int_buf, 10, digitalRead(6) == LOW);
  bitWrite(int_buf, 11, digitalRead(7) == LOW);
  bitWrite(int_buf, 12, digitalRead(8) == LOW);
  bitWrite(int_buf, 13, digitalRead(9) == LOW);

  if(lst_buf != int_buf){
    Serial.write((int_buf >> 8) & 0xFF);  // Send the upper byte
    Serial.write(int_buf & 0xFF);         // Send the lower byte
    Serial.write('\n');                   // Send finish read
  }

  delay(10);
}
