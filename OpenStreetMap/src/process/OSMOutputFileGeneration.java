package process;

import data.*;
import function.*;
import object.*;

public class OSMOutputFileGeneration {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMInput.paramConfig(args[0]);
		OSMInput.readNodeFile(OSMData.nodeHashMap);
		OSMInput.readWayFile(OSMData.wayHashMap);
		OSMInput.readWayInfo(OSMData.wayHashMap);
		OSMInput.readWktsFile(OSMData.wayHashMap, OSMData.nodeHashMap);
		
		OSMOutput.paramConfig(args[0]);
		// over write the cvs file
		OSMOutput.writeNodeFile(OSMData.nodeHashMap);
		OSMOutput.writeWayFile(OSMData.wayHashMap);
		OSMOutput.writeWayInfo(OSMData.wayHashMap);
	}
}
