/*
 * Programmers: Oti Oritsejafor and Jared Holzmeyer
 * Project: Process Management
 * Date: 3/12/19
 * Instructor: Dr. Hwang
*/

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>

using namespace std;

void initialize();
void printStatus();
void runCommand(string command);
void createProcess(int pid, int burst);
void burstDelete();

struct Process{
  int pid;
  int burst;
  int eventID;
};

Process init;
Process current;
int quantum;
vector<Process> rq;
vector<Process> wq;

int main(int argc, char* argv[]){
  if(argc != 3){
    cout << "USAGE: " << argv[0] << " <input_file>" << " <quantum_size>" << endl;
    exit(1); // maybe check if 0 or 1
  }

  quantum = atoi(argv[2]);
  string command;
  ifstream inputFile(argv[1]);
  if (inputFile.is_open()){
    initialize();
    printStatus(); 
    while(getline(inputFile, command)){
      cout << command << endl;
      
      runCommand(command);
      if (current.pid != 0){
	cout << "PID " << current.pid << " " << current.burst
	   << " running with " << quantum << " left" << endl;
      }
      else
	initialize();
      printStatus(); 
    }
  }
}

void runCommand(string command){
  string cmd = "  ";
  switch (command[0])
  {
  case 'C':
    if (command[5] != ' ') {
      cmd[0] = command[4];
      cmd[1] = command[5];
    } else {
      cmd = command[4];
    }
    createProcess(command[2]-'0', stoi(cmd));
    break;
  case 'D':
    runCommand("I");
    break;
  case 'I':
    if (current.burst > 0) {
      current.burst--;
      burstDelete();
    }
    else {
      current = (rq.size() != 0) ? rq[0] : init;
      rq.pop_back();
    }
    if (quantum == 1){
      quantum = 3;
      rq.push_back(current);
      cout << "PID " << current.pid << " " << current.burst
	   << " placed on Ready Queue" << endl;
      current = rq[0];
      rq.erase(rq.begin());
    }
    else
      quantum--;
    break;
  case 'W':
    current.burst--;
    burstDelete();
    quantum = 3;
    current.eventID = 4;//atoi(&command[2]);
    //cout << atoi(command[2] << endl;
    wq.push_back(current);
    cout << "PID " << current.pid << " " << current.burst
	 << " placed on Wait Queue" << endl;
    current = (rq.size() != 0) ? rq[0] : init;
    rq.erase(rq.begin());
    break;
  case 'E':
    runCommand("I");
    for(int i = 0; i < wq.size(); i++){
      if (wq.at(i).eventID == (command[2]-'0')) {
	rq.push_back(wq.at(i));
	cout << "PID " << wq.at(i).pid << " " << wq.at(i).burst
	     << " placed on Ready Queue" << endl;
	wq.erase(wq.begin()+i);
      }
    }
    break;
  case 'X':
    cout << "Current state of simulation:" << endl;
    if (current.pid != 0){
	cout << "PID " << current.pid << " " << current.burst
	   << " running with " << quantum << " left" << endl;
    }
    else
      initialize();
    printStatus();
    exit(0);
    break;
  }
}

void burstDelete(){
  if(current.burst == 0) {
    cout << "PID " << current.pid << " " <<
      current.burst << " terminated" << endl;
    for (int i = 0; i < rq.size(); i++){
      if(rq.at(i).pid > current.pid){
	cout << "PID " << rq.at(i).pid << " " <<
	  rq.at(i).burst << " terminated" << endl;
	//rq.erase(rq.begin()+i);
	/*
	  rq.size() is changing when the erase line is there so
	  it is not checking them all. We need to make it 
	  so it figures out which need to be deleted
	  first, then delete them after we know
	 * /
      }
    }
    for (int i = 0; i < wq.size(); i++){
      if(wq.at(i).pid > current.pid){
	wq.erase(wq.begin()+i);
	cout << "PID " << wq.at(i).pid << " " <<
	  wq.at(i).burst << " terminated" << endl;
      }
    }
    current = (rq.size() != 0) ? rq[0] : init;
    rq.pop_back();
  }
}
void createProcess(int pid, int burst){
  Process newProcess = {pid, burst, 0};
  rq.push_back(newProcess);
  cout << "PID " << pid << " " << burst << " placed on Ready Queue" << endl;
  if (current.pid == 0) {
      current = newProcess;
      rq.pop_back();
  }
  else 
    runCommand("I");
}

void initialize(){
  init = {0, 1, 0};
  cout << "PID 0 running" << endl;
}

void printStatus(){
  cout << "Ready Queue: ";
  if (rq.size() != 0){
    for(int i=0; i < rq.size(); i++){
      cout << "PID " << rq.at(i).pid << " " << rq.at(i).burst << " ";
    }
  }
  cout << endl;
  cout << "Wait Queue: ";
  if(wq.size() != 0){
    for(int i=0; i < wq.size(); i++){
      cout << "PID " << wq.at(i).pid << " " <<
	wq.at(i).burst << " " << wq.at(i).eventID;
    }
  }
  cout << endl;
}
