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
#include <vector>

using namespace std;

void initialize();
void printStatus();
void runCommand(string command);
void createProcess(int pid, int burst);

struct Process{
  int pid;
  int burst;
};

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
    while(1){
      getline(inputFile, command);
      cout << command << endl;
      runCommand(command);
    }
  }
}

void runCommand(string command){
  switch (command[0])
  {
  case 'C':
    createProcess(command[2]-'0', command[4]-'0');
    break;
  case 'D':
    ;
    break;
  case 'I':
    ;
    break;
  case 'W':
    ;
    break;
  case 'E':
    ;
    break;
  case 'X':
    ;
    break;
  }
}

void createProcess(int pid, int burst){
  Process newProcess = {pid, burst};
  rq.push_back(newProcess);
}

void initialize(){
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
      cout << "PID " << wq.at(i).pid << " " << wq.at(i).burst << " ";
    }
  }
  cout << endl;
}
