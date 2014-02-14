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
		buildAdjList();
		OSMParam.paramConfig();
		OSMOutput.writeAdjList();
	}
	
	public static void addNodeToAdjListHashMap(long startNode, long endNode, boolean isFix, int travelTime) {
		ToNodeInfo toNodeInfo;
		if (!isFix) {
			// assign travel time
			ArrayList<ArrayList<Integer>> timeArrayList = new ArrayList<ArrayList<Integer>>();
			for(int i = 0; i < 7; i++) {
				ArrayList<Integer> timeList = new ArrayList<Integer>();
				for(int k = 0; k < 60; k++) {
					timeList.add(travelTime);
				}
			}
			toNodeInfo = new ToNodeInfo(endNode, timeArrayList);
		} else {
			toNodeInfo = new ToNodeInfo(endNode, travelTime);
		}
		
		if (!OSMData.adjListHashMap.containsKey(startNode)) {
			LinkedList<ToNodeInfo> adjNodeList = new LinkedList<ToNodeInfo>();
			adjNodeList.add(toNodeInfo);
			OSMData.adjListHashMap.put(startNode, adjNodeList);
		} else {
			LinkedList<ToNodeInfo> adjNodeList = OSMData.adjListHashMap.get(startNode);
			adjNodeList.add(toNodeInfo);
		}
	}

	public static void buildAdjList() {
		System.out.println("build adjlist file...");
		/* build adjlist using edge */
		for(EdgeInfo edgeInfo : OSMData.edgeHashMap.values()) {
			long startNode = edgeInfo.getStartNode();
			long endNode = edgeInfo.getEndNode();
			boolean isOneway = edgeInfo.isOneway();
			
			int travelTime = edgeInfo.getTravelTime();
			boolean isFix = edgeInfo.isFix();
			
			addNodeToAdjListHashMap(startNode, endNode, isFix, travelTime);
			
			if(!isOneway) {
				addNodeToAdjListHashMap(endNode, startNode, isFix, travelTime);
			}
		}
		System.out.println("build adjlist finish!");
	}

}
