/*
 * Programmers: Oti Oritsejafor and Jared Holzmeyer
 * Project: Process Management
 * Date: 3/12/19
 * Instructor: Dr. Hwang
*/
/*

OTI:

The output needs to be to a log file with all three dat files as inputs
with each with quantum 1,5,10-------check if these work and we are good

Deleting children of children

Delete command

*/
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>

using namespace std;

struct Process{
  int pid;
  int burst;
  int eventID;
  vector<Process> children;
};

void initialize();
void printStatus();
void runCommand(string command);
void createProcess(int pid, int burst);
void burstDelete(Process parent);
vector<string> seperate(string str, char delim);
void checkDeleteQueue(vector<Process> & queue, int i);
void terminate(vector<Process> & queue, int pidToDelete);

Process init;
Process *current;
int quantumInit;
int quantum;
vector<Process> rq;
vector<Process> wq;

int main(int argc, char* argv[]){
  if(argc != 3){
    cout << "USAGE: " << argv[0] << " <input_file>" << " <quantum_size>" << endl;
    exit(1); // maybe check if 0 or 1
  }

  quantumInit = atoi(argv[2]);
  quantum = quantumInit;
  string command;
  ifstream inputFile(argv[1]);
  if (inputFile.is_open()){
    initialize();
    printStatus(); 
    while(getline(inputFile, command)){
      cout << command << endl;
      
      runCommand(command);
      if ((*current).pid != 0){
	cout << "PID " << current->pid << " " << current->burst
	   << " running with " << quantum << " left" << endl;
      }
      else
	initialize();
      printStatus(); 
    }
  }
}

string function(string command, int index){
  string num = " ";
  int j = 0;
  for(int i = index; i < command.size(); i++) {
    num[j] = command[i];
    j++;
    if (command[i] == ' ')
      break;
    else
      num = num + " ";
  }
  return num;
}

void runCommand(string command){
  string cmd = "  ";
  switch (command[0]) {
    case 'C':
      createProcess(stoi(function(command, 2)),
		    stoi(function(command, function(command, 2).size()+2)));
    break;
  case 'D':
    runCommand("I");
    break;
  case 'I':
    cout << current->pid << " " << current->burst << endl;
    if (current->pid != 0){
     
      if (current->burst > 0){
      	current->burst--;
      	if (current->burst == 0){
      	  burstDelete(current);
      	  current = &((rq.size() != 0) ? rq[0] : init);
      	  quantum = quantumInit;
      	  if (current->pid != 0)
      	    rq.erase(rq.begin());
      	}
      }
      quantum--;
      if (quantum == 0){
      	quantum = quantumInit;
      	rq.push_back(*current);
      	cout << "PID " << current->pid << " " << current->burst
       	     << " placed on Ready Queue" << endl;
       	current = &(rq[0]);
       	rq.erase(rq.begin());
      }
    }
    break;
  case 'W':
    if (current->pid != 0){
      current->burst--;
      if (current->burst == 0){
	burstDelete(*current);
      }
      quantum = quantumInit;
      current->eventID = stoi(function(command, 2));
      wq.push_back(*current);
      cout << "PID " << current->pid << " " << current->burst
	   << " placed on Wait Queue" << endl;
      current = &((rq.size() != 0) ? rq[0] : init);
      if (current->pid != 0)
	rq.erase(rq.begin());
    }
    break;
  case 'E':
    runCommand("I");
    for(int i = 0; i < wq.size(); i++){
      if (wq.at(i).eventID == stoi(function(command, 2))/*(command[2]-'0')*/) {
  	rq.push_back(wq.at(i));
  	cout << "PID " << wq.at(i).pid << " " << wq.at(i).burst
  	     << " placed on Ready Queue" << endl;
  	wq.erase(wq.begin()+i);
      }
    }
    break;
  case 'X':
    cout << "Current state of simulation:" << endl;
    if (current->pid != 0){
      cout << "PID " << current->pid << " " << current->burst
  	   << " running with " << quantum << " left" << endl;
    }
    else {
      initialize();
    }
    printStatus();
    exit(0);
    break;
  }
}

void terminate(vector<Process> & queue, int pidToDelete){
  for (int j = 0; j < queue.size(); j++){
    if(queue.at(j).pid == pidToDelete){
      cout << "PID " << queue.at(j).pid << " " <<
	queue.at(j).burst << " terminated" << endl;
      queue.erase(queue.begin() + j);
      break;
    }
  }
}

void burstDelete(Process *parent){
  cout << "PID " << current->pid << " " <<
    current->burst << " terminated" << endl;
  for (int i = 0; i < parent.children.size(); i++) {
    Process current_parent = parent.children.at(i);
    if (current_parent.children.size() > 0) {
      for(int i = 0; i < current_parent.children.size(); i++) {
	burstDelete(parent.children.at(i));
      }
      terminate(rq, current_parent.children.at(i).pid);
      terminate(wq, current_parent.children.at(i).pid);
    }
    terminate(rq, current_parent.pid);
    terminate(wq, current_parent.pid);
  }
  terminate(rq, parent.pid);
  terminate(wq, parent.pid);
} 

void createProcess(int pid, int burst){
  Process newProcess = {pid, burst, 0};
  cout << current->pid << "and " << current->burst << endl;
  current->children.push_back(newProcess);
  cout << "dfasdfasdfs" << endl;
  cout << "PID " << newProcess.pid << " " << newProcess.burst
       << " placed on Ready Queue" << endl;
  rq.push_back(newProcess);
  if (current->pid == 0) {
    current = &(newProcess);
      rq.pop_back();
      quantum = quantumInit;
  }
    else
    runCommand("I");
}

void initialize(){
  init = {0, 1, 0};
  cout << "PID 0 running" << endl;
  current = &init;
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
	wq.at(i).burst << " " << wq.at(i).eventID << " ";
    }
  }
  cout << endl;
}

void checkDeleteQueue(vector<Process> & queue, int i){
  vector<int> toBeDeleted;
  for (int j = 0; j < queue.size(); j++){
    if(queue.at(j).pid == current->children.at(i).pid){
      cout << "PID " << queue.at(j).pid << " " <<
	queue.at(j).burst << " terminated" << endl;
      queue.erase(queue.begin() + j);
      j--;
    }
  }
}
