#include "IrrigationManager.h"
#include "MachineState.h"

IrrigationManager::IrrigationManager(MachineState* mstate, int pin){
  this->mstate = mstate;
  this->pin = pin;
  this->pos = MOTOR_POS_MIN;
  this->delta = 20;
  this->position = CLOSED;
  this->speedlevel = MEDIUMS;
  this->servo = new ServoTimer2();
  this->servo->attach(pin);
  this->servo->write(this->pos);
}

void IrrigationManager::init(int period){
  Task::init(period);
}

void IrrigationManager::startIrrigation(){
  this->position = OPEN;
  
}
void IrrigationManager::stopIrrigation(){
  this->position = CLOSED;
}

void IrrigationManager::setIrrigationLevel(speed speedlevel){
  this->speedlevel = speedlevel;
}

IrrState IrrigationManager::getStatus(){
  return this->position;
}

int IrrigationManager::getIrrigationSpeed(){
  if(this->speedlevel == LOWS){
    return 1;
  } else if (this->speedlevel == MEDIUMS){
    return 2;
  } else if (this->speedlevel == HIGHS){
    return 3;
  }
  return 0;
}

void IrrigationManager::tick(){
  noInterrupts();
  volatile MachineMode currentGardenState = mstate->getStatus();
  volatile IrrState tempstatus = this->position;
  interrupts();
  
  if (currentGardenState != ALARM){
    switch(tempstatus){
      case(OPEN):
        this->servo->write(pos);
        int tempdelta = this->delta * (this->speedlevel / 10);
        this->pos = this->pos + tempdelta;
        if(pos >= MOTOR_POS_MAX - abs(tempdelta) && this->delta > 0){
          this->delta = - this->delta;
        }
        if(pos <= MOTOR_POS_MIN + abs(tempdelta) && this->delta < 0){
          this->delta = -delta;
        }
        break;
      case(CLOSED):
        break;
    }
  }
  
}
