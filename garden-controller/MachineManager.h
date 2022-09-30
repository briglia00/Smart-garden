#ifndef __MACHINEMANAGER__
#define __MACHINEMANAGER__

#include "Task.h"
#include "Arduino.h"
#include "MachineState.h"
#include "Led.h"
#include "VariableLed.h"
#include "IrrigationManager.h"
#include "MsgService.h"

class MachineManager: public Task {
private:
  volatile status machineState;
  MachineState* mstate;
  Led* lamp1;
  Led* lamp2;
  VariableLed* lamp3;
  VariableLed* lamp4;
  IrrigationManager* irrmanager;
  MsgServiceClass* msgservice;

public:
  MachineManager(MachineState* mstate, Led* lamp1, Led* lamp2, VariableLed* lamp3, VariableLed* lamp4, IrrigationManager* irrmanager, MsgServiceClass* msgservice);
  void init(int period);
  void tick();
  /*void switchOnBaseLamps();
  void switchOffBaseLamps();
  void setVariableLamps(brightnesslevel level);*/
};

#endif