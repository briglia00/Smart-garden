#ifndef __PHOTORESISTOR__
#define __PHOTORESISTOR__

#define SENSORPIN 5

class PhotoResistor{
 
public: 
  PhotoResistor(int pin);
  float LightDetected();

private:
  int pin;
};

#endif