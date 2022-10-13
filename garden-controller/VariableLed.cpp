#include "VariableLed.h"
#include "Arduino.h"

VariableLed::VariableLed(int pin){
  this->pin = pin;
  this->bl = level0;
} 

bool VariableLed::isOn(){
  return analogRead(this->pin) != 0;  
}

void VariableLed::setOn(){
  this->bl = level4;
  analogWrite(this->pin, level4);
}

void VariableLed::setOff(){
  this->bl = level0;
  analogWrite(this->pin, 0);
}

void VariableLed::setBrightness(brightnesslevel blevel){
  this->bl = blevel;
  analogWrite(this->pin, blevel);
}

int VariableLed::getBrightnessLevel(){
  if(this->bl == level0){
    return 0;
  } else if(this->bl == level1){
    return 1;
  } else if(this->bl == level2){
    return 2;
  } else if(this->bl == level3){
    return 3;
  } else if(this->bl == level4){
    return 4;
  }
}
