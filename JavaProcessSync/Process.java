import java.util.*;
import java.lang.*;

public class Process {
    protected int pid;
    protected int burst;
    protected int eventID;
    protected List<Integer> children = new ArrayList<Integer>();

    public Process(int pid, int burst, int eventID){
	this.pid = pid;
	this.burst = burst;
	this.eventID = eventID;
    }

    public int getPID() { return this.pid; }
    public int getBurst() { return this.burst; }
    public int getEventID() { return this.eventID; }
}
