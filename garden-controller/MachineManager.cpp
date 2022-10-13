#include "MachineManager.h"

String irrLevels[] = {"LOWS", "STPIRR", "LVLIRR", "LM12ON", "LM12OF", "LM34LV", "SETALM"};
brightnesslevel lightLevels[] = {level0, level1, level2, level3, level4};


MachineManager::MachineManager(MachineState* mstate, Led* lamp1, Led* lamp2, VariableLed* lamp3, VariableLed* lamp4, 
IrrigationManager* irrmanager, MsgServiceClass* msgservice, MsgServiceBT* btmsg){
  this->mstate = mstate;
  this->lamp1 = lamp1; this->lamp2 = lamp2; this->lamp3 = lamp3; this->lamp4 = lamp4;
  this->irrmanager = irrmanager;
  this->msgservice = msgservice;
  this->btmsg = btmsg;
  this->btmsg->init();
  this->machineState = this->mstate->getStatus();
}

void MachineManager::init(int period){
  Task::init(period);
}

void MachineManager::ManageMessages(Msg* msg){
  String s = msg->getContent();
  if (s.equals("STRIRR")){
    this->irrmanager->startIrrigation();
  } else if (s.equals("STPIRR")){
    this->irrmanager->stopIrrigation();
  } else if (s.equals("LVLIRRL")){
    this->irrmanager->setIrrigationLevel(LOWS);
  } else if (s.equals("LVLIRRM")){
    this->irrmanager->setIrrigationLevel(MEDIUMS);
  } else if (s.equals("LVLIRRH")){
    this->irrmanager->setIrrigationLevel(HIGHS);
  } else if (s.equals("LM12ON")){
    this->lamp1->setOn();
    this->lamp2->setOn();
  } else if (s.equals("LM12OF")){
    this->lamp1->setOff();
    this->lamp2->setOff();
  } else if (s.equals("LM1ON")){
    this->lamp1->setOn();
  } else if (s.equals("LM2ON")){
    this->lamp2->setOn();
  } else if (s.equals("LM1OF")){
    this->lamp1->setOff();
  } else if (s.equals("LM2OF")){
    this->lamp2->setOff();
  } else if (s.substring(0,5) == "LM3LV"){
    int templevel = s.substring(5).toInt();
    if (templevel >= 0 && templevel <= 4){
      this->lamp3->setBrightness(lightLevels[templevel]);
    }
  } else if (s.substring(0,5) == "LM4LV"){
    int templevel = s.substring(5).toInt();
    if (templevel >= 0 && templevel <= 4){
      this->lamp4->setBrightness(lightLevels[templevel]);
    }
  } else if (s == "GETSTT"){
    //this->msgservice->sendMsg(this->mstate->getStatus());
  } else if (s == "SETALM"){
    this->mstate->setStatus(ALARM);
  }

  if (this->machineState == MANUAL){
    this->msgservice->sendMsg(s);
  }
}

void MachineManager::onBtConnect(){
  String temps = "AUTO";
  if(this->machineState == MANUAL){
    temps = "MANUAL";
  } else if(this->machineState == ALARM){
    temps = "ALARM";
  }
  String x = "ALL;" + String(this->lamp1->isOn() ? "ON" : "OFF") + ";" +  
  String(this->lamp2->isOn() ? "ON" : "OFF") + ";" + 
  String(this->lamp3->getBrightnessLevel()) + ";" + 
  String(this->lamp4->getBrightnessLevel()) + ";" + 
  String(this->irrmanager->getStatus() == OPEN ? "CLOSE" : "OPEN") + ";" +
  String(this->irrmanager->getIrrigationSpeed()) + ";" + temps + ";";
  this->btmsg->sendMsg(x);
}


void MachineManager::tick(){  
  noInterrupts();
  this->machineState = this->mstate->getStatus();
  interrupts();

  switch(this->machineState){
    case(MANUAL):
      {
        if(!this->btmsg->isConnected()){
          this->msgservice->sendMsg("MODEAUTO");
          //onBtConnect();
          noInterrupts();
          this->mstate->setStatus(AUTO);
          interrupts();
          break;
        }
        if(!this->btmsg->isMsgAvailable()){
          break;
        }
        Msg* msgb = this->btmsg->receiveMsg();
        if (msgb == NULL){
          break;
        }
        if(msgb->getContent() == "AUTOREQ") {
          this->msgservice->sendMsg("MODEAUTO");
          this->irrmanager->stopIrrigation();
          noInterrupts();
          this->mstate->setStatus(AUTO);
          interrupts();
          break;
        }
        this->ManageMessages(msgb);
        delete msgb;
      }
      break;
    case(AUTO):
      {
        if(this->btmsg->isMsgAvailable() && this->btmsg->receiveMsg()->getContent().equals("MANUALREQ")) {
          this->msgservice->sendMsg("MODEMANUAL");
          noInterrupts();
          this->mstate->setStatus(MANUAL);
          this->machineState = MANUAL;
          interrupts();
          onBtConnect();
          break;
        }
        if(!this->msgservice->isMsgAvailable()){
          break;
        }
        Msg* msg = this->msgservice->receiveMsg();
        if (msg == NULL){
          break;
        }
        this->ManageMessages(msg);
        delete msg;
      }
      break;
    case(ALARM):
    {
      if(!this->btmsg->isMsgAvailable()){
        break;
      } 
      Msg* msg = this->btmsg->receiveMsg();
      if(msg->getContent().equals("MANUALREQ")) {
        onBtConnect();
      } else if(msg->getContent().equals("DISABLEALARM")) {
        this->msgservice->sendMsg("MODEMANUAL");
        noInterrupts();
        this->mstate->setStatus(MANUAL);
        this->machineState = MANUAL;
        interrupts();
        onBtConnect();
      }
      delete msg;
    }
    break;
  }
}
