#include "PhotoResistor.h"
#include "Arduino.h"

PhotoResistor::PhotoResistor(int pin){
  this->pin = pin;
} 
  
float PhotoResistor::LightDetected(){
  //TODO
  //restituisco la quantità di luce in scala da 1 a 8
  float temp = ((analogRead(pin) * 0.00488) - 0.5) / 0.01;
  return temp;
}
