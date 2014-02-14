package data;

import java.util.*;

import objects.*;

public class RDFData {
	/**
	 * @param node
	 */
	public static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param link
	 */
	public static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param sensor
	 */
	public static HashMap<Integer, SensorInfo> sensorMatchMap = new HashMap<Integer, SensorInfo>();
	/**
	 * @param connect
	 */
	public static HashMap<Long, LinkedList<Long>> nodeAdjList = new HashMap<Long, LinkedList<Long>>();
	// two nodes decide one link
	public static HashMap<String, RDFLinkInfo> nodeToLinkMap = new HashMap<String, RDFLinkInfo>();
	/**
	 * @param carpool
	 */
	public static HashSet<Long> carpoolManualSet = new HashSet<Long>();
}
