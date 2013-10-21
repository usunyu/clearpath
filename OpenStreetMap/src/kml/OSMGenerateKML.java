package kml;

import java.util.*;

import object.*;
import function.*;

public class OSMGenerateKML {

	/**
	 * @param node
	 */
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	static HashMap<Long, WayInfo> wayHashMap = new HashMap<Long, WayInfo>();
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMInput.paramConfig(args[0]);
		OSMInput.readNodeFile(nodeHashMap);
		OSMInput.readWayFile(wayHashMap);
		OSMInput.readWktsFile(wayHashMap, nodeHashMap);
		
		OSMOutput.paramConfig(args[0]);
		OSMOutput.generateWayKML(wayHashMap, nodeHashMap);
		OSMOutput.generateNodeKML(nodeHashMap);
	}
}
