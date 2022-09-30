#ifndef __LED__
#define __LED__

class Led {
 
public: 
  Led(int pin);
  bool isOn();
  void setOn();
  void setOff();
  
private:
  int pin;
};

#endif
