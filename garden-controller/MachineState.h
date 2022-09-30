#ifndef __MACHINESTATE__
#define __MACHINESTATE__

enum status {
  AUTO, MANUAL, ALARM
};

class MachineState {
private:
  status machineState;

public:
  MachineState();
  void setStatus(status stat);
  status getStatus();
  
};

#endif