package controller;

import model.*;
import global.*;
import object.*;

public class OSMOutputFileGeneration {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMParam.paramConfig(args[0]);
		
		OSMInput.readNodeFile(OSMData.nodeHashMap);
		OSMInput.readWayFile(OSMData.wayHashMap);
		OSMInput.readWayInfo(OSMData.wayHashMap);
		OSMInput.readWktsFile(OSMData.wayHashMap, OSMData.nodeHashMap);
		
		// over write the cvs file
		OSMOutput.writeNodeFile(OSMData.nodeHashMap);
		OSMOutput.writeWayFile(OSMData.wayHashMap);
		OSMOutput.writeWayInfo(OSMData.wayHashMap);
	}
}
