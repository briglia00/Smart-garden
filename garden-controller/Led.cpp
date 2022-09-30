#include "Led.h"
#include "Arduino.h"

Led::Led(int pin){
  this->pin = pin;
} 

bool Led::isOn(){
  return digitalRead(this->pin);  
}

void Led::setOn(){
  digitalWrite(this->pin, 255);
}

void Led::setOff(){
  digitalWrite(this->pin, LOW);
}