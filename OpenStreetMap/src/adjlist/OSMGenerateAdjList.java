package adjlist;

import java.util.*;

import object.*;
import function.*;

public class OSMGenerateAdjList {
	/**
	 * @param node
	 */
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	// static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	/**
	 * @param edge
	 */
	static HashMap<Long, EdgeInfo> edgeHashMap = new HashMap<Long, EdgeInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	static HashMap<String, EdgeInfo> nodesToEdge = new HashMap<String, EdgeInfo>();

	public static void run(String[] args) {
		main(args);
	}

	public static void main(String[] args) {
		OSMInput.paramConfig(args[0]);
		OSMInput.readNodeFile(nodeHashMap);
		OSMInput.readEdgeFile(edgeHashMap);
		buildAdjList();
		OSMOutput.paramConfig(args[0]);
		OSMOutput.generateAdjList(nodeHashMap, adjList, nodesToEdge);
	}

	public static void buildAdjList() {
		System.out.println("build adjlist file...");
		/* build adjlist using edge */
		for(EdgeInfo edgeInfo : edgeHashMap.values()) {
			long startNode = edgeInfo.getStartNode();
			long endNode = edgeInfo.getEndNode();
			String nodeIdString = startNode + "," + endNode;
			nodesToEdge.put(nodeIdString, edgeInfo);
			if (!adjList.containsKey(startNode)) {
				ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
				adjNodeArrayList.add(endNode);
				adjList.put(startNode, adjNodeArrayList);
			} else {
				ArrayList<Long> adjNodeArrayList = adjList.get(startNode);
				adjNodeArrayList.add(endNode);
			}
		}

		/* build adjlist using way */
		//for (int i = 0; i < wayArrayList.size(); i++) {
		//	WayInfo wayInfo = wayArrayList.get(i);
		//	boolean isOneway = wayInfo.isOneway();
		//	ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
		//	long preNodeId = 0;
		//	for (int j = 0; j < localNodeArrayList.size(); j++) {
		//		long nodeId = localNodeArrayList.get(j);
		//		// build adjlist
		//		if (j >= 1) {
		//			if (!adjList.containsKey(preNodeId)) {
		//				ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
		//				adjNodeArrayList.add(nodeId);
		//				adjList.put(preNodeId, adjNodeArrayList);
		//			} else {
		//				ArrayList<Long> adjNodeArrayList = adjList
		//						.get(preNodeId);
		//				adjNodeArrayList.add(nodeId);
		//			}
		//			if (!isOneway) {
		//				if (!adjList.containsKey(nodeId)) {
		//					ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
		//					adjNodeArrayList.add(preNodeId);
		//					adjList.put(nodeId, adjNodeArrayList);
		//				} else {
		//					ArrayList<Long> adjNodeArrayList = adjList
		//							.get(nodeId);
		//					adjNodeArrayList.add(preNodeId);
		//				}
		//			}
		//		}
		//		preNodeId = nodeId;
		//	}
		//}
		System.out.println("build adjlist finish!");
	}

}
