#ifndef __VARIABLELED__
#define __VARIABLELED__


enum brightnesslevel {
  level0=0, level1=20, level2=60, level3=128, level4=254
};

class VariableLed {
 
public: 
  VariableLed(int pin);
  bool isOn();
  void setOn();
  void setOff();
  void setBrightness(brightnesslevel blevel);
  int getBrightnessLevel();
  
private:
  int pin;
  brightnesslevel bl;
};

#endif
