#include <Rotary.h>

#define NUM_KNOBS 5

uint16_t lst_buf;
uint16_t int_buf;


Rotary knobs[5] = {
  Rotary(2, 3),
  Rotary(4, 5),
  Rotary(6, 7),
  Rotary(8, 9),
  Rotary(10, 11)
};


// the setup function runs once when you press reset or power the board
void setup() {
  Serial.begin(9600);
  
  pinMode(A0, INPUT_PULLUP); // Button 1
  pinMode(A1, INPUT_PULLUP); // Button 2
  pinMode(A2, INPUT_PULLUP); // Button 3

}

// the loop function runs over and over again forever
void loop() {

  // Buttons
  bitWrite(int_buf,  0, digitalRead(A0) == LOW);
  bitWrite(int_buf,  1, digitalRead(A1) == LOW);
  bitWrite(int_buf,  2, digitalRead(A2) == LOW);

  // Rotary Encoders
  for( int i = 0; i < NUM_KNOBS; i++ ){
    int res = int(knobs[i].process() >> 4);
    if ( int(res) != 0 ){
      int_buf = (res << (3 + 2 * i)) | int_buf;
    }
  }


  if(lst_buf != int_buf){
    Serial.write((int_buf >> 8) & 0xFF);  // Send the upper byte
    Serial.write(int_buf & 0xFF);         // Send the lower byte
    Serial.write('\n');                   // Send finish read

    // Store previous buffer state
    lst_buf = int_buf;
  }
  // Reset current buffer
  int_buf = 0;
}
