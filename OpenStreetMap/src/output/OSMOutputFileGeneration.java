package output;

import java.util.*;

import function.*;

import object.*;

public class OSMOutputFileGeneration {

	/**
	 * @param node
	 */
	public static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	public static HashMap<Long, WayInfo> wayHashMap = new HashMap<Long, WayInfo>();
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMInput.paramConfig(args[0]);
		OSMInput.readNodeFile(nodeHashMap);
		OSMInput.readWayFile(wayHashMap);
		OSMInput.readWayInfo(wayHashMap);
		OSMInput.readWktsFile(wayHashMap, nodeHashMap);
		
		OSMOutput.paramConfig(args[0]);
		// over write the cvs file
		OSMOutput.writeNodeFile(nodeHashMap);
		OSMOutput.writeWayFile(wayHashMap);
		OSMOutput.writeWayInfo(wayHashMap);
	}
}
