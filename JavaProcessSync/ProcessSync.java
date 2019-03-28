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
    public static PrintWriter writer;
    
    public static void main(String [] args) throws java.io.IOException {
	if (args.length != 2){
	    System.out.printf("USAGE: %s <input_file> <quantum_size>\n",args[0]);
	    System.exit(0);
	}
        writer = new PrintWriter("test3_Q10.txt", "UTF-8");
        quantumInit = Integer.parseInt(args[1]);
	quantum = quantumInit;
	String command;
	File inputFile = new File(args[0]);

	    BufferedReader br = new BufferedReader(new FileReader(inputFile));
	    if (inputFile.isFile() && inputFile.canRead()){
		initialize();
		printStatus();
		while((command = br.readLine()) != null){
		    writer.println(command);
		    writer.flush();
		    // try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("filename.txt"), "utf-8"))) {
		    // 	writer.write("something");
		    // }
		    //System.out.println(command);
		    runCommand(command);
		    printRunning();
		    printStatus();
		}
	    }
	// } catch (FileNotFoundException e) {
	//     e.printStackTrace();
	// }
	writer.close();
    }

    public static void runCommand(String command){
	switch(command.charAt(0)) {
	case 'C':
	    createProcess(getDigit(command, "first"), getDigit(command, "second"));
	    break;
	case 'D':
	    if (current.getPID() != 0){
		int deletePID = getDigit(command, "first");
		if (current.getPID() == deletePID) {
		    writer.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
		    writer.flush();
		    burstDelete(current);
		    resetCurrent();
		    quantum = quantumInit;
		    break;
		}
		else if (current.getChildrenPID().contains(deletePID)){
		    int index = current.getChildrenPID().indexOf(deletePID);
		    burstDelete(current.getChildren().get(index));
		}
		runCommand("I");
	    }
	    break;
	case 'I':
	    if (current.getPID() != 0){
		if (current.getBurst() > 0){
		    current.setBurst(current.getBurst()-1);
		    if (current.getBurst() == 0){
			writer.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
			writer.flush();
			burstDelete(current);
			resetCurrent();
			quantum = quantumInit;
			break;
		    }
		}
		quantum = quantum - 1;
		if (quantum == 0){
		    quantum = quantumInit;
		    rq.add(current);
		    writer.printf("PID %d %d placed on Ready Queue\n", current.getPID(), current.getBurst());
		    writer.flush();
		    current = rq.get(0);
		    rq.remove(0);
		}
	    }
	break;
	case 'W':
	    if (current.getPID() != 0){
		current.setBurst(current.getBurst()-1);
		if (current.getBurst() == 0){
		    writer.printf("PID %d %d terminated\n", current.getPID(), current.getBurst());
		    writer.flush();
		    burstDelete(current);
		    resetCurrent();
		}
		else {
		    current.setEventID(getDigit(command, "first"));
		    wq.add(current);
		    writer.printf("PID %d %d placed on Wait Queue \n", current.getPID(), current.getBurst());
		    writer.flush();
		    resetCurrent();
		}
		quantum = quantumInit;
	    }
        break;
	case 'E':
	    runCommand("I");
	    for (int i = 0; i < wq.size(); i++){
		if (wq.get(i).getEventID() == getDigit(command, "first")){
		    rq.add(wq.get(i));
		    writer.printf("PID %d %d placed on Ready Queue\n", wq.get(i).getPID(), wq.get(i).getBurst());
		    writer.flush();
		    wq.remove(i);
		}
	    }
	break;
	case 'X':
	    writer.println("Current state of simulation:");
	    writer.flush();
	    printRunning();
	    printStatus();
	    System.exit(0);
	break;
	}
    }
    
    public static void resetCurrent(){
	if (rq.size() != 0){
	    current = rq.get(0);
	    rq.remove(0);
	}
	else {
	    current = init;
	}
    }

    public static void terminate(ArrayList<Process> queue, int pidToDelete) {
	for (int j = 0; j < queue.size(); j++) {
	    if(queue.get(j).getPID() == pidToDelete) {
	        writer.printf("PID %d %d terminated\n", queue.get(j).getPID(), queue.get(j).getBurst());
		writer.flush();
		queue.remove(j);
		break;
	    }
	}
    }

    public static int getDigit(String command, String index){
	int res = 0;
        Scanner sc = new Scanner(command);
	res = sc.useDelimiter("\\D+").nextInt();
	if (index.equals("second")){
	    res = sc.useDelimiter("\\D+").nextInt();
	}
	return res;
    }
    
    public static void initialize(){
        writer.println("PID 0 running");
	writer.flush();
	current = init;
    }

    public static void printRunning(){
	if(current.getPID() != 0){
	    writer.printf("PID %d %d running with %d left\n", current.getPID(), current.getBurst(), quantum);
	    writer.flush();
	}
	else {
	    initialize();
	}
    }
    
    public static void printStatus(){
        writer.print("Ready Queue: ");
	writer.flush();
	if (rq.size() != 0){
	    for(int i = 0; i < rq.size(); i++){
	        writer.printf("PID %d %d ", rq.get(i).getPID(), rq.get(i).getBurst());
		writer.flush();
	    }
	}
	writer.println();
	writer.flush();
	writer.print("Wait Queue: ");
	writer.flush();
	if (wq.size() != 0){
	    for(int i = 0; i < wq.size(); i++){
	        writer.printf("PID %d %d %d ", wq.get(i).getPID(), wq.get(i).getBurst(), wq.get(i).getEventID());
		writer.flush();
	    }
	}
        writer.println();
	writer.flush();
    }

    public static void burstDelete(Process parent) {
	for(int i = 0; i < parent.getChildren().size(); i++) {
	    Process current_parent = parent.getChildren().get(i);
	    if (current_parent.getChildren().size() > 0) {
		for (int j = current_parent.getChildren().size()-1; j >= 0; j--){
		    burstDelete(current_parent.getChildren().get(j));
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
        
    public static void createProcess(int pid, int burst) {
	Process newProcess = new Process(pid, burst, 0);
        writer.printf("PID %d %d placed on Ready Queue\n", pid, burst);
	writer.flush();
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
