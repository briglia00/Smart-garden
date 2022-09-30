#ifndef __PHOTORESISTOR__
#define __PHOTORESISTOR__

class PhotoResistor{
 
public: 
  PhotoResistor(int pin);
  float LightDetected();

private:
  int pin;
};

#endif