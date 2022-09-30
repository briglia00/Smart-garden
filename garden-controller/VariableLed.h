#ifndef __VARIABLELED__
#define __VARIABLELED__


enum brightnesslevel {
  level1=30, level2=108, level3=162, level4=254
};

class VariableLed {
 
public: 
  VariableLed(int pin);
  bool isOn();
  void setOn();
  void setOff();
  void setBrightness(brightnesslevel blevel);
  
private:
  int pin;
};

#endif
