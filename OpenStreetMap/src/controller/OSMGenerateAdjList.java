package controller;

import java.util.*;

import model.*;

import global.*;
import object.*;

public class OSMGenerateAdjList {

	public static void run(String[] args) {
		main(args);
	}

	public static void main(String[] args) {
		buildAdjList(OSMData.nodeHashMap, OSMData.edgeHashMap, OSMData.adjList, OSMData.nodesToEdge);
		OSMParam.paramConfig(args[0]);
		OSMOutput.generateAdjList(OSMData.nodeHashMap, OSMData.adjList, OSMData.nodesToEdge);
	}

	public static void buildAdjList(HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, EdgeInfo> edgeHashMap, HashMap<Long, ArrayList<Long>> adjList, HashMap<String, EdgeInfo> nodesToEdge) {
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
