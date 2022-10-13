#include "Arduino.h"
#include "MachineState.h"

MachineState::MachineState(){
  this->machineState = AUTO;
}

MachineMode MachineState::getStatus(){
  return this->machineState;
}

void MachineState::setStatus(MachineMode stat){
  this->machineState = stat;
}
