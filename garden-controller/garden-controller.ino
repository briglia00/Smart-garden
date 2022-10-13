#include "Scheduler.h"
#include "ServoTimer2.h"
#include "Task.h"
#include "MachineState.h"
#include "MachineManager.h"
#include "IrrigationManager.h"
#include "MsgService.h"
#include "Led.h"
#include "VariableLed.h"
#include "MsgServiceBT.h"

#define LAMP1_PIN 8
#define LAMP2_PIN 2
#define LAMP3_PIN 5
#define LAMP4_PIN 6
#define SERVO_PIN 3
#define RXPIN 11
#define TXPIN 10
#define BTSTATUS_PIN 13

Scheduler sched;

void setup() {
  sched.init(100);
  Led* lamp1 = new Led(LAMP1_PIN);
  Led* lamp2 = new Led(LAMP2_PIN);
  VariableLed* lamp3 = new VariableLed(LAMP3_PIN);
  VariableLed* lamp4 = new VariableLed(LAMP4_PIN);
  MachineState* mstate = new MachineState();
  MsgServiceBT* bt = new MsgServiceBT(RXPIN, TXPIN, BTSTATUS_PIN);
  IrrigationManager* irrmanager = new IrrigationManager(mstate, SERVO_PIN);
  MsgServiceClass* msgservice = new MsgServiceClass();
  Task* mmanager = new MachineManager(mstate, lamp1, lamp2, lamp3, lamp4, irrmanager, msgservice, bt);
  mmanager->init(80);
  irrmanager->init(70);
  sched.addTask(mmanager);
  sched.addTask(irrmanager);
  Serial.begin(9600);
  Serial.println("READY");
}

void loop() {
  sched.schedule();
}
