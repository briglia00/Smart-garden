#include "VariableLed.h"
#include "Arduino.h"

VariableLed::VariableLed(int pin){
  this->pin = pin;
} 

bool VariableLed::isOn(){
  return analogRead(this->pin) != 0;  
}

void VariableLed::setOn(){
  analogWrite(this->pin, 1023);
}

void VariableLed::setOff(){
  analogWrite(this->pin, 0);
}

void VariableLed::setBrightness(brightnesslevel blevel){
  analogWrite(this->pin, blevel);
}
