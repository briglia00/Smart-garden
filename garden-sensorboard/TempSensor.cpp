#include "TempSensor.h"
#include "Arduino.h"

TempSensor::TempSensor(int pin){
  this->pin = pin;
  //this->temp(pin, DHTTYPE);
} 
  
float TempSensor::TemperatureDetected(){
  //converto il segnale acquisito in un valore
  //espresso in gradi centigradi
  float temp = ((analogRead(pin) * 0.00488) - 0.5) / 0.01;
  //float t = this->temp.readTemperature();
  if (isnan(temp)) return 0;
  else return temp;
}
