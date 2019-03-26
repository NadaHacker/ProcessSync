import java.util.*;
import java.lang.*;
import java.io.*;

public class ProcessSync {
    public static Process init = new Process(0, 1, 0);
    public static Process current;
    public static int quantum;
    public static List<Process> rq = new ArrayList<Process>();
    public static List<Process> wq = new ArrayList<Process>();
    
    public static void main(String [] args) throws Exception{
	if (args.length != 3){
	    System.out.println("USAGE: ");
	    System.out.println(args[0]);
	    System.out.println("<input_file <quantum_size");
	    System.exit(0);
	}
	int quantumInit = Integer.parseInt(args[2]);
	quantum = quantumInit;
	String command;
	File inputFile = new File(args[1]);
	BufferedReader br = new BufferedReader(new FileReader(inputFile));
	if (inputFile.isFile() && inputFile.canRead()){
	    System.out.println("PID 0 running");
	    current = init;
	    printStatus();
	    while((command = br.readLine()) != null){
		
	    }
	}
	
    }

    public static void printStatus(){
	System.out.println("Ready Queue: ");
	if (rq.size() != 0){
	    for(int i = 0; i < rq.size(); i++){
		System.out.printf("PID  %d  %d", rq.get(i).getPID(), rq.get(i).getBurst());
	    }
	}
	System.out.println();
	System.out.println("Wait Queue: ");
	if (wq.size() != 0){
	    for(int i = 0; i < wq.size(); i++){
	        System.out.printf("PID  %d  %d", wq.get(i).getPID(), wq.get(i).getBurst());
	    }
	}
	System.out.println();
    }
}
