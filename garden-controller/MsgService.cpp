#include "MsgService.h"

String content;

//MsgServiceClass MsgService;

MsgServiceClass::MsgServiceClass(){
  Serial.begin(9600);
  content.reserve(256);
  content = "";
  this->currentMsg = NULL;
  this->msgAvailable = false;
}

bool MsgServiceClass::isMsgAvailable(){
  serialEvent();
  return msgAvailable;
}

Msg* MsgServiceClass::receiveMsg(){
  if (msgAvailable){
    Msg* msg = currentMsg;
    msgAvailable = false;
    currentMsg = NULL;
    content = "";
    return msg;  
  } else {
    return NULL; 
  }
}

void MsgServiceClass::sendMsg(const String& msg){
  Serial.println(msg);  
}

void MsgServiceClass::serialEvent() {
  while (Serial.available()) {
    char ch = (char) Serial.read();
    if (ch == '\n'){
      currentMsg = new Msg(content);
      msgAvailable = true;      
    } else {
      content += ch;      
    }
  }
}

bool MsgServiceClass::isMsgAvailable(Pattern& pattern){
  serialEvent();
  return (msgAvailable && pattern.match(*currentMsg));
}

Msg* MsgServiceClass::receiveMsg(Pattern& pattern){
  if (msgAvailable && pattern.match(*currentMsg)){
    Msg* msg = currentMsg;
    msgAvailable = false;
    currentMsg = NULL;
    content = "";
    return msg;  
  } else {
    return NULL; 
  }
  
}
