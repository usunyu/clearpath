package data;

import java.util.*;

import object.*;

public class OSMData {
	/**
	 * @param node
	 */
	public static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	public static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	public static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	public static HashMap<Long, WayInfo> wayHashMap = new HashMap<Long, WayInfo>();
	/**
	 * @param edge
	 */
	public static HashMap<Long, EdgeInfo> edgeHashMap = new HashMap<Long, EdgeInfo>();
	/**
	 * @param adjlist
	 */
	public static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	public static HashMap<String, EdgeInfo> nodesToEdge = new HashMap<String, EdgeInfo>();
}
