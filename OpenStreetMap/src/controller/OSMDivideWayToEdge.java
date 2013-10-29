package controller;

import java.util.*;

import model.*;

import global.*;
import object.*;

public class OSMDivideWayToEdge {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		divideWayToEdge(OSMData.wayHashMap, OSMData.nodeHashMap, OSMData.edgeHashMap);
		OSMParam.paramConfig(args[0]);
		OSMOutput.writeEdgeFile(OSMData.edgeHashMap);
	}
	
	public static void divideWayToEdge(HashMap<Long, WayInfo> wayHashMap, HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, EdgeInfo> edgeHashMap) {
		System.out.println("divide way to edge...");
		
		for(WayInfo wayInfo : wayHashMap.values()) {
			long wayId = wayInfo.getWayId();
			boolean isOneway = wayInfo.isOneway();
			String name = wayInfo.getName();
			String highway = wayInfo.getHighway();
			ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
			int edgeId = 0;
			
			long preNodeId = 0;
			for(int j = 0; j < localNodeArrayList.size(); j++) {
				long nodeId = localNodeArrayList.get(j);
				if(j >= 1) {
					long startNode = preNodeId;
					long endNode = nodeId;
					NodeInfo nodeInfo1 = nodeHashMap.get(startNode);
					NodeInfo nodeInfo2 = nodeHashMap.get(endNode);
					// feet
					double dDistance = Distance.calculateDistance(nodeInfo1.getLocation(), nodeInfo2.getLocation()) * 5280;
					int distance = (int)Math.round(dDistance);
					EdgeInfo edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, startNode, endNode, distance);
					long id = wayId * 1000 + edgeId;
					edgeHashMap.put(id, edgeInfo);
					edgeId++;
					// use two edges to denote bidirection edge here
					if(!isOneway) {
						edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, endNode, startNode, distance);
						id = wayId * 1000 + edgeId;
						edgeHashMap.put(id, edgeInfo);
						edgeId++;
					}
				}
				preNodeId = nodeId;
			}
		}
		System.out.println("divide way to edge finish!");
	}
}
