#include "Arduino.h"
#include "MachineState.h"

MachineState::MachineState(){
  this->machineState = AUTO;
}

status MachineState::getStatus(){
  return this->machineState;
}

void MachineState::setStatus(status stat){
  this->machineState = stat;
}
