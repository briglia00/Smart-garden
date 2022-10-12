#ifndef __MSGSERVICEBT__
#define __MSGSERVICEBT__

#include "Arduino.h"
#include "SoftwareSerial.h"
#include "MsgService.h"
/*
class Msg {
  String content;

public:
  Msg(const String& content){
    this->content = content;
  }
  
  String getContent(){
    return content;
  }
};*/

class MsgServiceBT {
    
public: 
  MsgServiceBT(int rxPin, int txPin, int statuspin);  
  void init();  
  bool isMsgAvailable();
  Msg* receiveMsg();
  bool sendMsg(Msg msg);
  bool isConnected();

private:
  String content;
  int statuspin;
  Msg* availableMsg;
  SoftwareSerial* channel;
  
};

#endif
