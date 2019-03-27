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
		//System.out.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
		burstDelete(current);
		if (rq.size() != 0){
		current = rq.get(0);
		rq.remove(0);
		}
		else {
		    current = init;
		}
	    }
	    //System.out.println();
	    //System.out.println();
	    //System.out.printf("current pid: %d\n", current.getPID());
	    else if (current.getChildrenPID().contains(deletePID)){
		System.out.printf("deletePID: %d\n",deletePID);
		checkDeleteQueue(rq, deletePID);
		checkDeleteQueue(wq, deletePID);
		// if (rq.size() != 0){
		//     current = rq.get(0);
		//     rq.remove(0);
		// }
		// else {
		//     current = init;
		// }
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
			break;
		    }
		}
		quantum = quantum - 1;
		if (quantum == 0){
		    quantum = quantumInit;
		    //if (current.getPID() != 0) {
			rq.add(current);
			System.out.printf("PID %d %d placed on Ready Queue\n", current.getPID(), current.getBurst());
			current = rq.get(0);
			rq.remove(0);
			//}
		}
	    }
	break;
	case 'W':
	    if (current.getPID() != 0){
		current.setBurst(current.getBurst()-1);
		if (current.getBurst() == 0){
		    burstDelete(current);
		    System.out.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
		    if (rq.size() != 0){
			    current = rq.get(0);
			    rq.remove(0);
			}
			else {
			    current = init;
			}
		}
		else {
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
		quantum = quantumInit;
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
	    //System.out.println(queue.size());
	    if(queue.get(j).getPID() == i){//current.getChildren().get(i).getPID()){
		//System.out.println("hi");
		//System.out.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
		System.out.printf("PID %d %d terminated\n", queue.get(j).getPID(), queue.get(j).getBurst());
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
	//System.out.println(parent.getPID());
	for(int i = 0; i < parent.getChildren().size(); i++) {
	    Process current_parent = parent.getChildren().get(i);
	    //System.out.printf("current parent: %d\n",current_parent.getPID());
	    if (current_parent.getChildren().size() > 0) {
		// for(int j = 0; j < current_parent.getChildren().size(); j++) {
		//     burstDelete(parent.getChildren().get(j));
		//     System.out.println("hi");
		// }
		for (int j = current_parent.getChildren().size()-1; j >= 0; j--){
		    // System.out.printf("pid deleting: %d\n",parent.getChildren().get(j).getPID());
		    //System.out.printf("j: %d\n",j);
		    //System.out.println(current_parent.getChildren().get(j).getPID());
		    //System.out.println("hi");
		    burstDelete(current_parent.getChildren().get(j));
		    //System.out.println("hi");
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
	//System.out.printf("PID %d should be deleted\n",pidToDelete);
	for (int j = 0; j < queue.size(); j++) {
	    //System.out.printf("current queue pid: %d, pidToDelete: %d\n", queue.get(j).getPID(), pidToDelete);
	    if(queue.get(j).getPID() == pidToDelete) {
		System.out.printf("PID %d %d terminated\n", queue.get(j).getPID(), queue.get(j).getBurst());
		queue.remove(j);
		//	System.out.printf("j: %d and j-1: %d \n", j, j-1);
		//j--;
		//System.out.println(j);
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
