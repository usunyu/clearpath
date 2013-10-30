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
	
	public static boolean checkNodeInWay(ArrayList<Long> nodesInWay, LinkedList<Long> nodeCheckList) {
		for(long nodeId : nodeCheckList) {
			if(!nodesInWay.contains(nodeId)) {
				return false;
			}
		}
		return true;
	}
	
	public static void addNodeToAdjListMap(HashMap<Long, LinkedList<Long>> adjListMap, long node1, long node2) {
		LinkedList<Long> adjList;
		if(adjListMap.containsKey(node1)) {
			adjList = adjListMap.get(node1);
		}
		else {
			adjList = new LinkedList<Long>();
			adjListMap.put(node1, adjList);
		}
		adjList.add(node2);
	}
	
	public static void addEdgeToEdgeHashMap(long wayId, int edgeId, String name, String highway, 
			LinkedList<Long> nodeList, int distance, HashMap<Long, EdgeInfo> edgeHashMap) {
		long id = wayId * 1000 + edgeId;
		EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highway, nodeList, distance);
		edgeHashMap.put(id, edge);
	}
	
	public static int getDistance(LinkedList<Long> nodeList, HashMap<Long, NodeInfo> nodeHashMap) {
		long preNodeId = -1;
		double distance = 0;
		for(long nodeId : nodeList) {
			if(preNodeId != -1) {
				NodeInfo node1 = nodeHashMap.get(preNodeId);
				NodeInfo node2 = nodeHashMap.get(nodeId);
				distance += Distance.calculateDistance(node1.getLocation(), node2.getLocation()) * OSMParam.FEET_PER_MILE;
			}
			preNodeId = nodeId;
		}
		return (int)Math.round(distance);
	}
	
	public static void divideWayToEdge(HashMap<Long, WayInfo> wayHashMap, HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, EdgeInfo> edgeHashMap) {
		System.out.println("divide way to edge...");
		HashMap<Long, LinkedList<Long>> adjListMap = new HashMap<Long, LinkedList<Long>>();
		// build adjlist
		for(WayInfo way : wayHashMap.values()) {
			ArrayList<Long> localNodeArrayList = way.getNodeArrayList();
			long preNodeId = -1;
			for(long nodeId : localNodeArrayList) {
				if(preNodeId != -1) {
					addNodeToAdjListMap(adjListMap, preNodeId, nodeId);
					// use two edges to denote bidirection edge here
					addNodeToAdjListMap(adjListMap, nodeId, preNodeId);
				}
				preNodeId = nodeId;
			}
		}
		// divide way to edge
		for(WayInfo way : wayHashMap.values()) {
			long wayId = way.getWayId();
			boolean isOneway = way.isOneway();
			String name = way.getName();
			String highway = way.getHighway();
			ArrayList<Long> localNodeArrayList = way.getNodeArrayList();
			int edgeId = 0;
			long preNodeId = -1;
			LinkedList<Long> currentList = new LinkedList<Long>();
			for(long nodeId : localNodeArrayList) {
				if(preNodeId == -1) {
					// start
					preNodeId = nodeId;
					currentList.add(nodeId);
				}
				else {
					LinkedList<Long> adjList = adjListMap.get(nodeId);
					// all connect nodes are in the way
					if(checkNodeInWay(localNodeArrayList, adjList)) {
						currentList.add(nodeId);
					}
					else {
						currentList.add(nodeId);
						int distance = getDistance(currentList, nodeHashMap);
						addEdgeToEdgeHashMap(wayId, edgeId++, name, highway, currentList, distance, edgeHashMap);
						if(!isOneway) { // bidirection
							LinkedList<Long> reverseList = new LinkedList<Long>(currentList);
							Collections.reverse(reverseList);
							addEdgeToEdgeHashMap(wayId, edgeId++, name, highway, reverseList, distance, edgeHashMap);
						}
						// prepare for next
						currentList = new LinkedList<Long>();
						currentList.add(nodeId);
					}
					preNodeId = nodeId;
				}
			}
		}
		System.out.println("divide way to edge finish!");
	}
}
