#ifndef __MACHINESTATE__
#define __MACHINESTATE__

enum MachineMode {
  AUTO, MANUAL, ALARM
};

class MachineState {
private:
  MachineMode machineState;

public:
  MachineState();
  void setStatus(MachineMode stat);
  MachineMode getStatus();
  
};

#endif