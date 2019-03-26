import java.util.*;
import java.lang.*;

public class Process {
    protected int pid;
    protected int burst;
    protected int eventID;
    protected ArrayList<Process> children = new ArrayList<Process>();

    public Process(int pid, int burst, int eventID){
	this.pid = pid;
	this.burst = burst;
	this.eventID = eventID;
    }

    public int getPID() { return this.pid; }
    public int getBurst() { return this.burst; }
    public int getEventID() { return this.eventID; }
    public void setPID(int newPID) {
	this.pid = newPID;
    }
    public void setBurst(int newBurst) {
	this.burst = newBurst;
    }
    public void setEventID(int newEventID) {
	this.eventID = newEventID;
    }

    public ArrayList<Process> getChildren() { return this.children; }

    public ArrayList<Integer> getChildrenPID() {
	ArrayList<Integer> PIDs = new ArrayList<Integer>();
	for (Process child : this.children){
	    PIDs.add(child.pid);
	}
	return PIDs;
    }
}
