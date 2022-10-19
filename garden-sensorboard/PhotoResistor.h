#ifndef __PHOTORESISTOR__
#define __PHOTORESISTOR__

class PhotoResistor{
 
public: 
  PhotoResistor(int pin);
  int lightDetected();

private:
  int pin;
};

#endif