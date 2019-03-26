import java.util.*;
import java.lang.*;
import java.io.*;


public class ProcessSync {
    public static Process init = new Process(0, 1, 0);
    public static Process current;
    public static int quantumInit;
    public static int quantum;
    public static ArrayList<Process> rq = new ArrayList<Process>();
    public static ArrayList<Process> wq = new ArrayList<Process>();
    
    public static void main(String [] args) throws Exception{
	if (args.length != 2){
	    System.out.printf("USAGE: %s <input_file> <quantum_size>\n",args[0]);
	    System.exit(0);
	}
        quantumInit = Integer.parseInt(args[1]);
	quantum = quantumInit;
	String command;
	File inputFile = new File(args[0]);
	BufferedReader br = new BufferedReader(new FileReader(inputFile));
	if (inputFile.isFile() && inputFile.canRead()){
	    initialize();
	    printStatus();
	    while((command = br.readLine()) != null){
		System.out.println(command);
		runCommand(command);
		if(current.getPID() != 0){
		    System.out.printf("PID %d %d running with %d left\n", current.getPID(), current.getBurst(), quantum);
		}
		else {
		    initialize();
		}
		printStatus();
	    }
	}
    }

    public static void runCommand(String command){
	String cmd = "  ";
	switch(command.charAt(0)) {
	case 'C':
	    createProcess(getDigit(command, "first"), getDigit(command, "second"));
	    break;
	case 'D':
	    int deletePID = getDigit(command, "first");
	    if (current.getPID() == deletePID) {
		System.out.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
		burstDelete(current);
	    }
	    if (current.getChildrenPID().contains(deletePID)){
		checkDeleteQueue(rq, deletePID);
		checkDeleteQueue(wq, deletePID);
	    }
	    if (rq.size() != 0){
		current = rq.get(0);
		rq.remove(0);
	    }
	    else {
		current = init;
	    }
	    runCommand("I");
	    break;
	case 'I':
	    if (current.getPID() != 0){
		if (current.getBurst() > 0){
		    current.setBurst(current.getBurst()-1);
		    if (current.getBurst() == 0){
			System.out.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
			burstDelete(current);
			if (rq.size() != 0){
			    current = rq.get(0);
			    rq.remove(0);
			}
			else {
			    current = init;
			}
			quantum = quantumInit;
		    }
		}
		quantum = quantum - 1;
		if (quantum == 0){
		    quantum = quantumInit;
		    rq.add(current);
		    System.out.printf("PID %d %d placed on Ready Queue\n", current.getPID(), current.getBurst());
		    current = rq.get(0);
		    rq.remove(0);
		}
	    }
	break;
	case 'W':
	    if (current.getPID() != 0){
		current.setBurst(current.getBurst()-1);
		if (current.getBurst() == 0){
		    burstDelete(current);
		}
		quantum = quantumInit;
		current.setEventID(getDigit(command, "first"));
		wq.add(current);
		System.out.printf("PID %d %d placed on Wait Queue \n", current.getPID(), current.getBurst());
	        if (rq.size() != 0){
		    current = rq.get(0);
		    rq.remove(0);
		}
		else {
		    current = init;
		}
	    }
        break;
	case 'E':
	    runCommand("I");
	    for (int i = 0; i < wq.size(); i++){
		if (wq.get(i).getEventID() == getDigit(command, "first")){
		    rq.add(wq.get(i));
		    System.out.printf("PID %d %d placed on Ready Queue\n", wq.get(i).getPID(), wq.get(i).getBurst());
		    wq.remove(i);
		}
	    }
	break;
	case 'X':
	    System.out.println("Current state of simulation:");
	    if (current.getPID() != 0){
		System.out.printf("PID %d %d running with %d left\n", current.getPID(), current.getBurst(), quantum);
	    }
	    else {
		initialize();
	    }
	    printStatus();
	    System.exit(0);
	break;
	}
    }

    public static void checkDeleteQueue(ArrayList<Process> queue, int i){
	for (int j = 0; j < queue.size(); j++){
	    if(queue.get(j).getPID() == current.getChildren().get(i).getPID()){
		System.out.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
	        burstDelete(queue.get(j));
		break;
	    }
	}
    }

    public static int getDigit(String command, String index){
	// StringBuilder num = new StringBuilder("");
        // System.out.println(command.length());
    	// for (int i = index; i < command.length()-i+1; i++){
	//     System.out.print(command.charAt(i));
	//     char c = command.charAt(i);
	//     System.out.println("hi");
	//     if (c == ' ')
	// 	break;
    	//     num.append(command.charAt(i));
    	// }
    	// return num.toString();
	int res = 0;
        Scanner sc = new Scanner(command);
	//System.out.println(index);
	res = sc.useDelimiter("\\D+").nextInt();
	if (index.equals("second")){
	    //System.out.println("hi");
	    res = sc.useDelimiter("\\D+").nextInt();
	}
	// else if (index.trim().equals("second")){
	//     while (sc.hasNextInt()){
	// 	System.out.println("hi");
	// 	res = sc.useDelimiter("\\D+").nextInt();
	//     }
	// }
	return res;
    }
    
    public static void initialize(){
	System.out.println("PID 0 running");
	current = init;
    }

    public static void printStatus(){
	System.out.print("Ready Queue: ");
	if (rq.size() != 0){
	    for(int i = 0; i < rq.size(); i++){
		System.out.printf("PID %d %d ", rq.get(i).getPID(), rq.get(i).getBurst());
	    }
	}
	System.out.println();
	System.out.print("Wait Queue: ");
	if (wq.size() != 0){
	    for(int i = 0; i < wq.size(); i++){
	        System.out.printf("PID %d %d %d ", wq.get(i).getPID(), wq.get(i).getBurst(), wq.get(i).getEventID());
	    }
	}
	System.out.println();
    }

    public static void burstDelete(Process parent) {
	//System.out.println(parent.getChildren().size());
	for(int i = 0; i < parent.getChildren().size(); i++) {
	    Process current_parent = parent.getChildren().get(i);
	    if (current_parent.getChildren().size() > 0) {
		for(int j = 0; j < current_parent.getChildren().size(); j++) {
		    burstDelete(parent.getChildren().get(j));
		}
		terminate(rq, current_parent.getChildren().get(i).getPID());
		terminate(wq, current_parent.getChildren().get(i).getPID());
	    }
	    terminate(rq, current_parent.getPID());
	    terminate(wq, current_parent.getPID());
	}
	terminate(rq, parent.getPID());
	terminate(wq, parent.getPID());
    }
    
    
    public static void terminate(ArrayList<Process> queue, int pidToDelete) {
	for (int j = 0; j < queue.size(); j++) {
	    if(queue.get(j).getPID() == pidToDelete) {
		System.out.printf("PID %d %d terminated\n", queue.get(j).getPID(), queue.get(j).getBurst());
		queue.remove(j);
		break;
	    }
	}
    }
    
    
    public static void createProcess(int pid, int burst) {
	Process newProcess = new Process(pid, burst, 0);
	System.out.printf("PID %d %d placed on Ready Queue\n", pid, burst);
	current.getChildren().add(newProcess);
	rq.add(newProcess);
	if (current.getPID() == 0) {
	    current = newProcess;
	    rq.remove(rq.size() - 1);
	    quantum = quantumInit;
	} else {
	    runCommand("I");
	}
    }
}
