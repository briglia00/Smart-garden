#include "PhotoResistor.h"
#include "Arduino.h"

PhotoResistor::PhotoResistor(int pin){
  this->pin = pin;
  pinMode(this->pin, INPUT);
} 

int PhotoResistor::lightDetected(){
  int light = analogRead(this->pin);
  return map(light, 0, 4095, 1, 8);
}
