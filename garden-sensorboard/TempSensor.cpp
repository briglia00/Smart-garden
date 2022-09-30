#include "TempSensor.h"
#include "Arduino.h"

TempSensor::TempSensor(int pin){
  this->pin = pin;
} 
  
float TempSensor::TemperatureDetected(){
  //converto il segnale acquisito in un valore
  //espresso in gradi centigradi
  float temp = ((analogRead(pin) * 0.00488) - 0.5) / 0.01;
  return temp;
}
