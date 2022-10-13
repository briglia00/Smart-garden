#ifndef __MACHINEMANAGER__
#define __MACHINEMANAGER__

#include "Task.h"
#include "Arduino.h"
#include "MachineState.h"
#include "Led.h"
#include "VariableLed.h"
#include "IrrigationManager.h"
#include "MsgService.h"
#include "MsgServiceBT.h"

class MachineManager: public Task {
private:
  volatile MachineMode machineState;
  MachineState* mstate;
  Led* lamp1;
  Led* lamp2;
  VariableLed* lamp3;
  VariableLed* lamp4;
  IrrigationManager* irrmanager;
  MsgServiceClass* msgservice;
  MsgServiceBT* btmsg;
  void ManageMessages(Msg* msg);
  void onBtConnect();

public:
  MachineManager(MachineState* mstate, Led* lamp1, Led* lamp2, VariableLed* lamp3, VariableLed* lamp4, 
  IrrigationManager* irrmanager, MsgServiceClass* msgservice, MsgServiceBT* btmsg);
  void init(int period);
  void tick();
};

#endif