#ifndef __TEMPSENSOR__
#define __TEMPSENSOR__

class TempSensor{
 
public: 
  TempSensor(int pin);
  float TemperatureDetected();

private:
  int pin;
};

#endif