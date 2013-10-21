package path;

import java.util.*;
import java.util.Map.*;
import java.io.*;

import function.OSMInput;
import function.OSMOutput;

import object.*;
import library.*;
import main.OSMMain;

public class OSMRouting {

	/**
	 * @param param
	 */
	static long startNode 		= 122688467;
	static long endNode 		= 703503358;
	static int startTime 		= 10;
	static int timeInterval 	= 15;
	static int timeRange 		= 60;
	/**
	 * @param node
	 */
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param graph
	 */
	static HashMap<Long, ArrayList<ToNodeInfo>> adjListHashMap = new HashMap<Long, ArrayList<ToNodeInfo>>();
	/**
	 * @param path
	 */
	static ArrayList<Long> pathNodeList = new ArrayList<Long>();

	public static void main(String[] args) {
		OSMInput.paramConfig(OSMMain.osm);
		OSMInput.buildAdjList(adjListHashMap);
		OSMInput.readNodeFile(nodeHashMap);
		
		prepareRoute();
		tdsp(startNode, endNode, startTime);
		
		OSMOutput.paramConfig(OSMMain.osm);
		OSMOutput.generatePathKML(nodeHashMap, pathNodeList);
	}
	
	public static void prepareRoute() {
		for(NodeInfo node : nodeHashMap.values()) {
			node.prepareRoute();
		}
	}

	public static void tdsp(long startNode, long endNode, int startTime) {
		System.out.println("start finding the path...");
		PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
			public int compare(NodeInfo n1, NodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		NodeInfo current = nodeHashMap.get(startNode);	// get start node
		if(current == null) {
			System.err.println("cannot find start node, program exit!");
			System.exit(-1);
		}
		
		current.setCost(0);	// set start cost to 0
		
		priorityQ.offer(current);
		
		while ((current = priorityQ.poll()) != null) {
			long nodeId = current.getNodeId();
			
			if(nodeId == endNode)	// find the end
				break;
			
			int timeIndex = startTime + current.getCost() / 60 / timeInterval;
			
			if (timeIndex > timeRange - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = timeRange - 1;
			
			ArrayList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				NodeInfo toNodeInfo = nodeHashMap.get(toNodeId);
				
				if(toNodeInfo.isVisited())	// if the node is visited, we bypass it
					continue;
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeInfo.getCost()) {
					toNodeInfo.setCost(totalTime);
					toNodeInfo.setParentId(nodeId);
					priorityQ.offer(toNodeInfo);
				}
			}
		}
		
		if (startNode == endNode)
			System.out.println("start node is the same as end node.");
		else {
			current = nodeHashMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node, program exit!");
				System.exit(-1);
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeHashMap.get(current.getParentId());
				if(current == null) {
					System.err.println("cannot find intermediate node, program exit!");
					System.exit(-1);
				}
				pathNodeList.add(current.getNodeId());	// add intermediate node
			}
			
			if(current.getParentId() == -1) {
				System.err.println("cannot find the path, program exit!");
				System.exit(-1);
			}
			
			if(current.getParentId() == startNode)
				pathNodeList.add(startNode);	// add start node
			
			Collections.reverse(pathNodeList);	// reverse the path list
		}
		
		// prepare for next time
		prepareRoute();
		
		System.out.println("find the path successful!");
	}
}
