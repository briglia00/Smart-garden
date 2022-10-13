#include "Led.h"
#include "Arduino.h"

Led::Led(int pin){
  this->pin = pin;
} 

bool Led::isOn(){
  return digitalRead(this->pin == HIGH);  
}

void Led::setOn(){
  digitalWrite(this->pin, HIGH);
}

void Led::setOff(){
  digitalWrite(this->pin, LOW);
}