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
#include <sstream>

using namespace std;

void initialize();
void printStatus();
void runCommand(string command);
void createProcess(int pid, int burst);
void burstDelete();
vector<string> seperate(string str, char delim);

struct Process{
  int pid;
  int burst;
  int eventID;
  vector<Process> children;
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

vector<string> seperate(string str, char delim){
  vector<string> temp;
  stringstream ss(str);
  string token;
  while(getline(ss, token, delim)){
    temp.push_back(token);
  }
  return temp;
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
    if (current.pid != 0){
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
      else {
	quantum--;
      }
    }
    break;
  case 'W': 
    current.burst--;
    burstDelete();
    quantum = 3;
    current.eventID = stoi(function(command, 2));
    wq.push_back(current);
    cout << "PID " << current.pid << " " << current.burst
    	 << " placed on Wait Queue" << endl;
    current = (rq.size() != 0) ? rq[0] : init;
    if (current.pid != 0)
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
      if (rq.size() != 0)
	current = rq[0];
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
  vector<int> toBeDeleted;
  if(current.burst == 0) {
    cout << "PID " << current.pid << " " <<
      current.burst << " terminated" << endl;
    for (int i = 0; i < current.children.size(); i++) {
      for (int j = 0; j < rq.size(); j++){
	if(rq.at(j).pid == current.children.at(i).pid){
	  cout << "PID " << rq.at(j).pid << " " <<
	    rq.at(j).burst << " terminated" << endl;
	  toBeDeleted.push_back(rq.at(j).pid);
	}
      }
      while(toBeDeleted.size() != 0){
	int i = 0;
	if (rq.at(i).pid == toBeDeleted[0]){
	  rq.erase(rq.begin()+i);
	  toBeDeleted.erase(toBeDeleted.begin());
	  i--;
	}
	i++;
      }
      for (int k = 0; k < wq.size(); k++){
	if(rq.at(k).pid == current.children.at(i).pid){
	  cout << "PID " << wq.at(k).pid << " " <<
	    wq.at(k).burst << " terminated" << endl;
	  toBeDeleted.push_back(rq.at(k).pid);
	}
      }
      while(toBeDeleted.size() != 0){
	int i = 0;
	if (wq.at(i).pid == toBeDeleted[0]){
	  wq.erase(wq.begin()+i);
	  toBeDeleted.erase(toBeDeleted.begin());
	  i--;
	}
	i++;
      }
    }
    while(current.children.size() != 0) 
      current.children.pop_back();
    current = (rq.size() != 0) ? rq[0] : init;
  }
}
void createProcess(int pid, int burst){
  Process newProcess = {pid, burst, 0};
  current.children.push_back(newProcess);
  rq.push_back(newProcess);
  cout << "PID " << pid << " " << burst << " placed on Ready Queue" << endl;
  if (current.pid == 0) {
      current = newProcess;
      rq.pop_back();
      quantum = 3;
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
