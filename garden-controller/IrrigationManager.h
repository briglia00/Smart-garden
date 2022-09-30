#ifndef __IRRIGATIONMANAGER__
#define __IRRIGATIONMANAGER__

#include "Task.h"
#include "MachineState.h"
#include "ServoTimer2.h"

enum speed {
  LOWS=10, MEDIUMS=25, HIGHS=40
};

enum state{
  OPEN, CLOSED
};

class IrrigationManager: public Task {
  MachineState* mstate;
  int pin;
  int pos;
  int delta;
  state position;
  speed speedlevel;
  ServoTimer2* servo;

public:
  IrrigationManager(MachineState* mstate, int pin);
  void init(int period);  
  void tick();
  void startIrrigation();
  void stopIrrigation();
  void setIrrigationLevel(speed speedlevel);
};

#endif