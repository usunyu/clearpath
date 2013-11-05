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
	
	/**
	 * add edge to the edgeHashMap
	 * @param wayId
	 * @param edgeId
	 * @param name
	 * @param highway
	 * @param isOneway
	 * @param nodeList
	 * @param duplicateEndEdgeMap
	 * @param nodeHashMap
	 * @param wayHashMap
	 * @param edgeHashMap
	 * @return
	 */
	public static int addEdgeToEdgeHashMap(long wayId, int edgeId, String name, String highway, boolean isOneway,
			LinkedList<Long> nodeList, HashMap<String, EdgeInfo> duplicateEndEdgeMap, 
			HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, WayInfo> wayHashMap, 
			HashMap<Long, EdgeInfo> edgeHashMap) {
		long id = wayId * 1000 + edgeId;
		int distance = getDistance(nodeList, nodeHashMap);
		EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highway, isOneway, nodeList, distance);
		String nodeStrId = edge.getStartNode() + OSMParam.COMMA + edge.getEndNode();
		// if we don't have the edge with same start and end, add it directly
		if(nodeStrId.equals("187491982,187492007")) {
			System.out.println();
		}
		if(!duplicateEndEdgeMap.containsKey(nodeStrId)) {
			edgeId++;
			edgeHashMap.put(id, edge);
			duplicateEndEdgeMap.put(nodeStrId, edge);
		}
		else {	// if we have the edge with same start and end, we need add a node manually to avoid error
			// if current edge can be divide two edges
			if(nodeList.size() >= 3) {
				LinkedList<Long> subNodeList1 = new LinkedList<Long>();
				LinkedList<Long> subNodeList2 = new LinkedList<Long>();
				for(long nodeId : nodeList.subList(0, 2)) subNodeList1.add(nodeId);
				for(long nodeId : nodeList.subList(1, nodeList.size())) 	subNodeList2.add(nodeId);
				edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highway, isOneway, subNodeList1, duplicateEndEdgeMap, nodeHashMap, wayHashMap, edgeHashMap);
				edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highway, isOneway, subNodeList2, duplicateEndEdgeMap, nodeHashMap, wayHashMap, edgeHashMap);
			}
			else {
				EdgeInfo prevEdge = duplicateEndEdgeMap.get(nodeStrId);
				long prevId = prevEdge.getId();
				// rempve the prev edge from edgeHashMap
				edgeHashMap.remove(prevId);
				long prevWayId = prevEdge.getWayId();
				int prevEdgeId = prevEdge.getEdgeId();
				// estimate the edge total number
				int count = wayHashMap.get(prevWayId).getNodeArrayList().size();
				LinkedList<Long> prevNodeList = prevEdge.getNodeList();
				// if prev edge can be divide to two edges
				if(prevNodeList.size() >= 3) {
					LinkedList<Long> subNodeList1 = new LinkedList<Long>();
					LinkedList<Long> subNodeList2 = new LinkedList<Long>();
					for(long nodeId : prevNodeList.subList(0, 2)) subNodeList1.add(nodeId);
					for(long nodeId : prevNodeList.subList(1, prevNodeList.size())) subNodeList2.add(nodeId);
					addEdgeToEdgeHashMap(prevWayId, prevEdgeId, name, highway, isOneway, subNodeList1, duplicateEndEdgeMap, nodeHashMap, wayHashMap, edgeHashMap);
					addEdgeToEdgeHashMap(prevWayId, count, name, highway, isOneway, subNodeList2, duplicateEndEdgeMap, nodeHashMap, wayHashMap, edgeHashMap);
					edgeId++;
					edgeHashMap.put(id, edge);
					duplicateEndEdgeMap.put(nodeStrId, edge);
				}
				else {	// we can only remove an edge, chose the longer one two remove
					if(edge.getDistance() < prevEdge.getDistance()) {
						edgeHashMap.put(id, edge);
						duplicateEndEdgeMap.put(nodeStrId, edge);
					}
				}
			}
		}
		return edgeId;
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
		// used to check if there already has an edge with same start node ane end node
		HashMap<String, EdgeInfo> duplicateEndEdgeMap = new HashMap<String, EdgeInfo>();
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
						edgeId = addEdgeToEdgeHashMap(wayId, edgeId, name, highway, isOneway, currentList, duplicateEndEdgeMap, nodeHashMap, wayHashMap, edgeHashMap);
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
