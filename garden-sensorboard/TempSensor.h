#ifndef __TEMPSENSOR__
#define __TEMPSENSOR__

#include <DHT.h>

#define DHTTYPE DHT11

class TempSensor{
 
public: 
  TempSensor(int pin);
  float TemperatureDetected();

private:
  int pin;
  DHT temp(int pin, const uint8_t type);
};

#endif