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
		divideWayToEdge();
		OSMInput.addOnEdgeToNode();
		OSMParam.paramConfig();
		OSMOutput.writeEdgeFile();
		OSMOutput.writeEdgeFileBidirectional();
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
	
	/**
	 * add edge to the edgeHashMap
	 * @param wayId
	 * @param edgeId
	 * @param name
	 * @param highway
	 * @param isOneway
	 * @param nodeList
	 * @param duplicateEndEdgeMap
	 * @return
	 */
	public static int addEdgeToEdgeHashMap(long wayId, int edgeId, String name, int highwayId, boolean isOneway,
			LinkedList<Long> nodeList, HashMap<String, EdgeInfo> duplicateEndEdgeMap) {
		long id = wayId * 1000 + edgeId;
		int distance = getDistance(nodeList);
		EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highwayId, isOneway, nodeList, distance);
		String nodeStrId = edge.getStartNode() + OSMParam.COMMA + edge.getEndNode();
		// if we don't have the edge with same start and end, add it directly
		if(!duplicateEndEdgeMap.containsKey(nodeStrId)) {
			edgeId++;
			OSMData.edgeHashMap.put(id, edge);
			duplicateEndEdgeMap.put(nodeStrId, edge);
			if(!edge.isOneway()) {
				nodeStrId = edge.getEndNode() + OSMParam.COMMA + edge.getStartNode();
				duplicateEndEdgeMap.put(nodeStrId, edge);
			}
		}
		else {	// if we have the edge with same start and end, we need add a node manually to avoid error
			// if current edge can be divide two edges
			if(nodeList.size() >= 3) {
				LinkedList<Long> subNodeList1 = new LinkedList<Long>();
				LinkedList<Long> subNodeList2 = new LinkedList<Long>();
				for(long nodeId : nodeList.subList(0, 2)) subNodeList1.add(nodeId);
				for(long nodeId : nodeList.subList(1, nodeList.size())) 	subNodeList2.add(nodeId);
				edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highwayId, isOneway, subNodeList1, duplicateEndEdgeMap);
				edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highwayId, isOneway, subNodeList2, duplicateEndEdgeMap);
			}
			else {
				EdgeInfo prevEdge = duplicateEndEdgeMap.get(nodeStrId);
				long prevId = prevEdge.getId();
				// rempve the prev edge from edgeHashMap
				OSMData.edgeHashMap.remove(prevId);
				long prevWayId = prevEdge.getWayId();
				int prevEdgeId = prevEdge.getEdgeId();
				// estimate the edge total number
				int count = OSMData.wayHashMap.get(prevWayId).getNodeArrayList().size();
				LinkedList<Long> prevNodeList = prevEdge.getNodeList();
				// if prev edge can be divide to two edges
				if(prevNodeList.size() >= 3) {
					LinkedList<Long> subNodeList1 = new LinkedList<Long>();
					LinkedList<Long> subNodeList2 = new LinkedList<Long>();
					for(long nodeId : prevNodeList.subList(0, 2)) subNodeList1.add(nodeId);
					for(long nodeId : prevNodeList.subList(1, prevNodeList.size())) subNodeList2.add(nodeId);
					addEdgeToEdgeHashMap(prevWayId, prevEdgeId, name, highwayId, isOneway, subNodeList1, duplicateEndEdgeMap);
					addEdgeToEdgeHashMap(prevWayId, count, name, highwayId, isOneway, subNodeList2, duplicateEndEdgeMap);
					edgeId++;
					OSMData.edgeHashMap.put(id, edge);
					duplicateEndEdgeMap.put(nodeStrId, edge);
					if(!edge.isOneway()) {
						nodeStrId = edge.getEndNode() + OSMParam.COMMA + edge.getStartNode();
						duplicateEndEdgeMap.put(nodeStrId, edge);
					}
				}
				else {	// we can only remove an edge, chose the longer one two remove
					if(edge.getDistance() < prevEdge.getDistance()) {
						OSMData.edgeHashMap.put(id, edge);
						duplicateEndEdgeMap.put(nodeStrId, edge);
						if(!edge.isOneway()) {
							nodeStrId = edge.getEndNode() + OSMParam.COMMA + edge.getStartNode();
							duplicateEndEdgeMap.put(nodeStrId, edge);
						}
					}
				}
			}
		}
		return edgeId;
	}
	
	public static int getDistance(LinkedList<Long> nodeList) {
		long preNodeId = -1;
		double distance = 0;
		for(long nodeId : nodeList) {
			if(preNodeId != -1) {
				NodeInfo node1 = OSMData.nodeHashMap.get(preNodeId);
				NodeInfo node2 = OSMData.nodeHashMap.get(nodeId);
				distance += Geometry.calculateDistance(node1.getLocation(), node2.getLocation()) * OSMParam.FEET_PER_MILE;
			}
			preNodeId = nodeId;
		}
		return (int)Math.round(distance);
	}
	
	public static void divideWayToEdge() {
		System.out.println("divide way to edge...");
		HashMap<Long, LinkedList<Long>> adjListMap = new HashMap<Long, LinkedList<Long>>();
		// used to check if there already has an edge with same start node ane end node
		HashMap<String, EdgeInfo> duplicateEndEdgeMap = new HashMap<String, EdgeInfo>();
		// from highway type to identity the index
		HashMap<String, Integer> highwayTypeCache = new HashMap<String, Integer>();
		// build adjlist
		for(WayInfo way : OSMData.wayHashMap.values()) {
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
		for(WayInfo way : OSMData.wayHashMap.values()) {
			long wayId = way.getWayId();
			boolean isOneway = way.isOneway();
			String name = way.getName();
			String highway = way.getHighway();
			int highwayId;
			if(highwayTypeCache.containsKey(highway)) {
				highwayId = highwayTypeCache.get(highway);
			}
			else {
				highwayId = OSMData.edgeHighwayTypeList.size();
				highwayTypeCache.put(highway, highwayId);
				// add to highway list
				OSMData.edgeHighwayTypeList.add(highway);
			}
			ArrayList<Long> localNodeArrayList = way.getNodeArrayList();
			int edgeId = 1;
			LinkedList<Long> currentList = new LinkedList<Long>();
			for(long nodeId : localNodeArrayList) {
				if(currentList.size() == 0) {
					currentList.add(nodeId);
					continue;
				}
				LinkedList<Long> adjList = adjListMap.get(nodeId);
				currentList.add(nodeId);
				// has intersect with other way
				if(!checkNodeInWay(localNodeArrayList, adjList)) {
					edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highwayId, isOneway, currentList, duplicateEndEdgeMap);
					// prepare for next
					currentList = new LinkedList<Long>();
					currentList.add(nodeId);
				}
			}
			// check the dead end
			if(currentList.size() > 1) {
				edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highwayId, isOneway, currentList, duplicateEndEdgeMap);
			}
		}
		System.out.println("divide way to edge finish!");
	}
}
