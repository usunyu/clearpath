package global;

import java.util.*;

import object.*;

public class OSMData {
	/**
	 * @param node
	 */
	public static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	public static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	public static HashMap<String, LinkedList<NodeInfo>> nodeLocationGridMap = new HashMap<String, LinkedList<NodeInfo>>();
	/**
	 * @param way
	 */
	public static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	public static HashMap<Long, WayInfo> wayHashMap = new HashMap<Long, WayInfo>();
	/**
	 * @param edge
	 */
	public static HashMap<Long, EdgeInfo> edgeHashMap = new HashMap<Long, EdgeInfo>();
	public static ArrayList<String> edgeHighwayTypeList = new ArrayList<String>();
	/**
	 * @param adjlist
	 */
	public static HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap = new HashMap<Long, LinkedList<ToNodeInfo>>();
	public static HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap = new HashMap<Long, LinkedList<ToNodeInfo>>();
	public static HashMap<Long, ArrayList<Double>> edgePatternHashMap = new HashMap<Long, ArrayList<Double>>();
	/**
	 * @param hierarchy, type : level
	 */
	public static HashMap<String, Integer> hierarchyHashMap = new HashMap<String, Integer>();
}
