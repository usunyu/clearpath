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

	public static double getTravelSpeed(EdgeInfo edgeInfo) {
		// initial
		double speed  = (double) 5 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);	// feet/second
		// define all kinds of highway type link's speed
		if (edgeInfo.getHighway().equals(OSMParam.MOTORWAY) || edgeInfo.getHighway().equals(OSMParam.TRUNK)) {
			speed = (double) 60 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.MOTORWAY_LINK) || edgeInfo.getHighway().equals(OSMParam.TRUNK_LINK) ) {
			speed = (double) 55 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.RESIDENTIAL) || edgeInfo.getHighway().equals(OSMParam.CYCLEWAY) ||
				edgeInfo.getHighway().equals(OSMParam.TURNING_CIRCLE)) {
			speed = (double) 10 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.UNKNOWN_HIGHWAY) || edgeInfo.getHighway().equals(OSMParam.UNCLASSIFIED) || 
				edgeInfo.getHighway().equals(OSMParam.TRACK) || edgeInfo.getHighway().equals(OSMParam.CONSTRUCTION) || 
				edgeInfo.getHighway().equals(OSMParam.PROPOSED) || edgeInfo.getHighway().equals(OSMParam.ROAD) || 
				edgeInfo.getHighway().equals(OSMParam.ABANDONED) || edgeInfo.getHighway().equals(OSMParam.SCALE)) {
			speed = (double) 5 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.TERTIARY)) {
			speed = (double) 20 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.TERTIARY_LINK)) {
			speed = (double) 15 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.SECONDARY)) {
			speed = (double) 30 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.SECONDARY_LINK)) {
			speed = (double) 25 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.PRIMARY)) {
			speed = (double) 35 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.PRIMARY_LINK)) {
			speed = (double) 30 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		return speed;
	}
	
	public static int getTravelTime(EdgeInfo edgeInfo) {
		// initial
		double speed  = getTravelSpeed(edgeInfo);
		int travelTime = 1;	// second
		travelTime = (int) Math.round(edgeInfo.getDistance() / speed * OSMParam.MILLI_PER_SECOND);
		// travelTime cannot be zero
		if (travelTime <= 0) {
			travelTime = 1;
		}
		return travelTime;
	}
	
	public static boolean isFix(EdgeInfo edgeInfo) {
		boolean isFix = true;
		if (edgeInfo.getHighway().equals(OSMParam.MOTORWAY) || edgeInfo.getHighway().equals(OSMParam.TRUNK) ||
				edgeInfo.getHighway().equals(OSMParam.MOTORWAY_LINK) || edgeInfo.getHighway().equals(OSMParam.TRUNK_LINK)) {
			isFix = false;
		}
		return isFix;
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
			
			int travelTime = getTravelTime(edgeInfo);
			boolean isFix = isFix(edgeInfo);
			
			addNodeToAdjListHashMap(startNode, endNode, isFix, travelTime, adjListHashMap);
			
			if(!isOneway) {
				addNodeToAdjListHashMap(endNode, startNode, isFix, travelTime, adjListHashMap);
			}
		}
		System.out.println("build adjlist finish!");
	}

}
