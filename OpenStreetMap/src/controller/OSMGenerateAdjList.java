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
		buildAdjList(OSMData.nodeHashMap, OSMData.edgeHashMap, OSMData.adjListHashMap);
		OSMParam.paramConfig(args[0]);
		OSMOutput.writeAdjList(OSMData.nodeHashMap, OSMData.adjListHashMap);
	}
	
	public static void addNodeToAdjListHashMap(long startNode, long endNode, boolean isFix, int travelTime,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		ToNodeInfo toNodeInfo;
		if (!isFix) {
			// assign travel time
			int[] timeList = new int[60];
			for (int k = 0; k < timeList.length; k++) {
				timeList[k] = travelTime;
			}
			toNodeInfo = new ToNodeInfo(endNode, timeList);
		} else {
			toNodeInfo = new ToNodeInfo(endNode, travelTime);
		}
		
		if (!adjListHashMap.containsKey(startNode)) {
			LinkedList<ToNodeInfo> adjNodeList = new LinkedList<ToNodeInfo>();
			adjNodeList.add(toNodeInfo);
			adjListHashMap.put(startNode, adjNodeList);
		} else {
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(startNode);
			adjNodeList.add(toNodeInfo);
		}
	}

	public static void buildAdjList(HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, EdgeInfo> edgeHashMap, 
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("build adjlist file...");
		/* build adjlist using edge */
		for(EdgeInfo edgeInfo : edgeHashMap.values()) {
			long startNode = edgeInfo.getStartNode();
			long endNode = edgeInfo.getEndNode();
			boolean isOneway = edgeInfo.isOneway();
			
			int travelTime = OSMProcess.getTravelTime(edgeInfo);
			boolean isFix = OSMProcess.isFixEdge(edgeInfo);
			
			addNodeToAdjListHashMap(startNode, endNode, isFix, travelTime, adjListHashMap);
			
			if(!isOneway) {
				addNodeToAdjListHashMap(endNode, startNode, isFix, travelTime, adjListHashMap);
			}
		}
		System.out.println("build adjlist finish!");
	}

}
