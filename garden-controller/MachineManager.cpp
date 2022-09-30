#include "MachineManager.h"

String gardenMessages[] = {"STRIRR", "STPIRR", "LVLIRR", "LM12ON", "LM12OF", "LM34LV", "SETALM"};

MachineManager::MachineManager(MachineState* mstate, Led* lamp1, Led* lamp2, VariableLed* lamp3, VariableLed* lamp4, 
IrrigationManager* irrmanager, MsgServiceClass* msgservice){
  this->mstate = mstate;
  this->lamp1 = lamp1; this->lamp2 = lamp2; this->lamp3 = lamp3; this->lamp4 = lamp4;
  this->irrmanager = irrmanager;
  this->msgservice = msgservice;
}

void MachineManager::init(int period){
  Task::init(period);
}

void MachineManager::tick(){  
  switch(this->mstate->getStatus()){
    case(AUTO): 
      if(!this->msgservice->isMsgAvailable()){
        break;
      }
      Msg* msg = this->msgservice->receiveMsg();
      if (msg == NULL){
        break;
      }
      String s = msg->getContent().substring(0,6);
      if(s == "STRIRR"){
        this->irrmanager->startIrrigation();
      } else if(s == "STPIRR"){
        this->irrmanager->stopIrrigation();
      } else if(s == "LVLIRR"){
        String temp = msg->getContent().substring(6);
        //set irrigation level
        if(temp == "L"){
          this->irrmanager->setIrrigationLevel(LOWS);
        } else if(temp == "M"){
          this->irrmanager->setIrrigationLevel(MEDIUMS);
        } else if(temp == "H"){
          this->irrmanager->setIrrigationLevel(HIGHS);
        }
      } else if(s == "LM12ON"){
        //set lamp 1-2 on
        this->lamp1->setOn();
        this->lamp2->setOn();
      } else if(s == "LM12OF"){
        //set lamp 1-2 off
        this->lamp1->setOff();
        this->lamp2->setOff();
      } else if(s == "LM34LV"){
        //set lamp 3-4 level
        String temp = msg->getContent().substring(6);
        Serial.println(temp);
        if (temp == 0 || temp == "0"){
          this->lamp3->setOff();
          this->lamp4->setOff();
        } else if (temp == 1 || temp == "1"){
          this->lamp3->setBrightness(level1);
          this->lamp4->setBrightness(level1);
        } else if (temp == 2 || temp == "2"){
          this->lamp3->setBrightness(level2);
          this->lamp4->setBrightness(level2);
        } else if (temp == 3 || temp == "3"){
          this->lamp3->setBrightness(level3);
          this->lamp4->setBrightness(level3);
        } else if (temp == 4 || temp == "4"){
          this->lamp3->setBrightness(level4);
          this->lamp4->setBrightness(level4);
        }
      } else if(s == "SETALM"){
        this->irrmanager->stopIrrigation();
        this->mstate->setStatus(ALARM);
      }
      delete msg;
      break;
    case(MANUAL):
      //bluetooth
      break;
    case(ALARM):
      break;
  }
}
