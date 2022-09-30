#include "IrrigationManager.h"
#include "MachineState.h"

#define MOTOR_POS_MIN 545
#define MOTOR_POS_MAX 2400


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

void IrrigationManager::tick(){
  /* Quando la temperatura/ luce scende sotto un certo valore(prendendolo da machinestate), attivo il sistema di irrigazione.
  in modalita manuale aspetto messaggi per metterlo in funzione */
  noInterrupts();
  volatile status currentGardenState = mstate->getStatus();
  volatile state tempstatus = this->position;
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
