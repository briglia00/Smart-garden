#include "MachineManager.h"

String gardenMessages[] = {"STRIRR", "STPIRR", "LVLIRR", "LM12ON", "LM12OF", "LM34LV", "SETALM"};

MachineManager::MachineManager(MachineState* mstate, Led* lamp1, Led* lamp2, VariableLed* lamp3, VariableLed* lamp4, 
IrrigationManager* irrmanager, MsgServiceClass* msgservice, MsgServiceBT* btmsg){
  this->mstate = mstate;
  this->lamp1 = lamp1; this->lamp2 = lamp2; this->lamp3 = lamp3; this->lamp4 = lamp4;
  this->irrmanager = irrmanager;
  this->msgservice = msgservice;
  this->btmsg = btmsg;
  this->btmsg->init();  
}

void MachineManager::init(int period){
  Task::init(period);
}

void MachineManager::tick(){  
  noInterrupts();
  this->machineState = this->mstate->getStatus();
  interrupts();

  switch(this->machineState){
    case(MANUAL):
      {
        if(this->btmsg->isMsgAvailable() && this->btmsg->receiveMsg()->getContent() == "AUTOREQ") {
          //invia messaggio di transizione a AUTO MODE
          noInterrupts();
          this->mstate->setStatus(AUTO);
          interrupts();
          break;
        }
        if(!this->btmsg->isMsgAvailable()){
          break;
        }
        Msg* msg2 = this->btmsg->receiveMsg();
        /*if (msg2 == NULL){
          break;
        }*/
        Serial.println(msg2->getContent());
        String s2 = msg2->getContent(); //.substring(0,6);
        Serial.println(s2);
        if (s2 == "LM12ON"){
          //set lamp 1-2 on
          this->lamp1->setOn();
          this->lamp2->setOn();
        } else if (s2 == "LM12OF"){
          //set lamp 1-2 off
          this->lamp1->setOff();
          this->lamp2->setOff();
        }
        delete msg2;
      }
      break;
    case(AUTO):
      {
        if(this->btmsg->isMsgAvailable() && this->btmsg->receiveMsg()->getContent() == "MANUALREQ") {
          //invia messaggio di transizione a MANUAL MODE
          noInterrupts();
          this->mstate->setStatus(MANUAL);
          interrupts();
          break;
        }
        if(!this->msgservice->isMsgAvailable()){
          break;
        }
        Msg* msg = this->msgservice->receiveMsg();
        if (msg == NULL){
          break;
        }
        String s = msg->getContent().substring(0,6);
        if (s == "STRIRR"){
          this->irrmanager->startIrrigation();
        } else if (s == "STPIRR"){
          this->irrmanager->stopIrrigation();
        } else if (msg->getContent() == "LVLIRRL"){
          this->irrmanager->setIrrigationLevel(LOWS);
        } else if (msg->getContent() == "LVLIRRM"){
          this->irrmanager->setIrrigationLevel(MEDIUMS);
        } else if (msg->getContent() == "LVLIRRH"){
          this->irrmanager->setIrrigationLevel(HIGHS);
        } else if (s == "LM12ON"){
          //set lamp 1-2 on
          this->lamp1->setOn();
          this->lamp2->setOn();
        } else if (s == "LM12OF"){
          //set lamp 1-2 off
          this->lamp1->setOff();
          this->lamp2->setOff();
        } else if (s == "LM11ON"){
          this->lamp1->setOn();
        } else if (s == "LM22ON"){
          this->lamp2->setOn();
        } else if (s == "LM34LV"){
          //set lamp 3-4 level
          String temp = msg->getContent().substring(6);
          Serial.println(temp);
          if (temp == 0 || temp == "0"){
            this->lamp3->setOff();
            this->lamp4->setOff();
          } else if (temp == 1 || temp == "1"){
            this->lamp3->setBrightness(level4);
            this->lamp4->setBrightness(level4);
          } else if (temp == 2 || temp == "2"){
            this->lamp3->setBrightness(level3);
            this->lamp4->setBrightness(level3);
          } else if (temp == 3 || temp == "3"){
            this->lamp3->setBrightness(level2);
            this->lamp4->setBrightness(level2);
          } else if (temp == 4 || temp == "4"){
            this->lamp3->setBrightness(level1);
            this->lamp4->setBrightness(level1);
          }
        } else if (s == "SETALM"){
          this->irrmanager->stopIrrigation();
          this->mstate->setStatus(ALARM);
        }
        delete msg;
      }
      break;
  }
}
