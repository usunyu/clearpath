package controller;

import java.util.*;

import global.*;
import object.*;
import main.*;
import model.*;

public class OSMRouting {

	/**
	 * @param param
	 */
	static long START_NODE 		= 187143552;
	static long END_NODE 		= 699922975;
	static int START_TIME 		= 10;
	static int TIME_INTERVAL 	= 15;
	static int TIME_RANGE 		= 60;
	/**
	 * @param path
	 */
	// highway type : level
	static HashMap<String, Integer> hierarchyHashMap = new HashMap<String, Integer>();
	static ArrayList<Long> pathNodeList = new ArrayList<Long>();

	public static void main(String[] args) {
		// config
		OSMParam.paramConfig(OSMMain.osm);
		// input
		OSMInput.readNodeFile(OSMData.nodeHashMap);
		OSMInput.readEdgeFile(OSMData.edgeHashMap, OSMData.nodesToEdgeHashMap);
		OSMInput.readAdjList(OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		// test
		//OSMOutput.generateHighwayKML(edgeHashMap, nodeHashMap);
		// initial hierarchy level
		initialHierarchy();
		prepareRoute(OSMData.nodeHashMap);
		
		tdsp(OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		
		// output
		OSMOutput.generatePathKML(OSMData.nodeHashMap, pathNodeList);
		OSMOutput.generatePathNodeKML(OSMData.nodeHashMap, pathNodeList);
	}
	
	public static void tdsp(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		// count time
		long begintime = System.currentTimeMillis();
		//tdsp(START_NODE, END_NODE, START_TIME, nodeHashMap, adjListHashMap);
		tdspHierarchy(START_NODE, END_NODE, START_TIME, nodeHashMap, nodesToEdgeHashMap, adjListHashMap, adjReverseListHashMap);
		long endtime = System.currentTimeMillis();
		long costTime = (endtime - begintime);
		System.out.println("tdsp cost: " + costTime + " ms");
	}
	
	public static void initialHierarchy() {
		hierarchyHashMap.put(OSMParam.MOTORWAY, 1);
		hierarchyHashMap.put(OSMParam.MOTORWAY_LINK, 1);
		hierarchyHashMap.put(OSMParam.TRUNK, 1);
		hierarchyHashMap.put(OSMParam.TRUNK_LINK, 1);
		hierarchyHashMap.put(OSMParam.PRIMARY, 2);
		// link can be use to connect the highway
		hierarchyHashMap.put(OSMParam.PRIMARY_LINK, 1);
		hierarchyHashMap.put(OSMParam.SECONDARY, 3);
		hierarchyHashMap.put(OSMParam.SECONDARY_LINK, 1);
		hierarchyHashMap.put(OSMParam.TERTIARY, 4);
		hierarchyHashMap.put(OSMParam.TERTIARY_LINK, 1);
		hierarchyHashMap.put(OSMParam.RESIDENTIAL, 5);
		hierarchyHashMap.put(OSMParam.CYCLEWAY, 5);
		hierarchyHashMap.put(OSMParam.TURNING_CIRCLE, 5);
		hierarchyHashMap.put(OSMParam.TRACK, 6);
		hierarchyHashMap.put(OSMParam.CONSTRUCTION, 6);
		hierarchyHashMap.put(OSMParam.PROPOSED, 6);
		hierarchyHashMap.put(OSMParam.ROAD, 6);
		hierarchyHashMap.put(OSMParam.ABANDONED, 6);
		hierarchyHashMap.put(OSMParam.SCALE, 6);
		hierarchyHashMap.put(OSMParam.UNCLASSIFIED, 6);
	}
	
	public static void prepareRoute(HashMap<Long, NodeInfo> map) {
		for(NodeInfo node : map.values()) {
			node.prepareRoute();
		}
	}
	
	public static void prepareRoute(Stack<NodeInfo> stack) {
		while(!stack.isEmpty()) {
			NodeInfo node = stack.pop();
			node.prepareRoute();
		}
	}
	
	public static HashMap<Long, HighwayEntrance> searchHighwayExit(long startNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap, 
			HashMap<String, EdgeInfo> nodesToEdgeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
		HashMap<Long, HighwayEntrance> highwayExitMap = new HashMap<Long, HighwayEntrance>();
		PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
			public int compare(NodeInfo n1, NodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		int debug = 0;
		try {
			NodeInfo current = nodeHashMap.get(endNode);	// get start node
			nodeStack.push(current);
			if(current == null) {
				System.err.println("cannot find start node!");
				return null;
			}
			
			current.setCost(0);	// set start cost to 0
			
			priorityQ.offer(current);
			
			// find four exit
			while ((current = priorityQ.poll()) != null && highwayExitMap.size() < 4) {
				debug++;

				long nodeId = current.getNodeId();
				
				if(nodeId == startNode) {	// we already find source
					return null;
				}
				
				LinkedList<ToNodeInfo> adjNodeList = adjReverseListHashMap.get(nodeId);
				if(adjNodeList == null)
					continue;
				
				int arrTime = current.getCost();
				
				for(ToNodeInfo fromNode : adjNodeList) {
					long fromNodeId = fromNode.getNodeId();
					
					NodeInfo fromNodeInfo = nodeHashMap.get(fromNodeId);
					
					// if(fromNodeInfo.isVisited())	// if the node is visited, we bypass it
					// 	continue;

					nodeStack.push(fromNodeInfo);

					// fromNodeInfo.setVisited();
					
					String nodeIdKey = fromNodeId + OSMParam.COMMA + nodeId;
					EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
					
					String highway = edge.getHighway();
					int hierarchy = 6;
					if(hierarchyHashMap.containsKey(highway)) {
						hierarchy = hierarchyHashMap.get(highway);
					}
					
					int travelTime;
					if(fromNode.isFix())	// fix time
						travelTime = fromNode.getTravelTime();
					else	// fetch from time array
						travelTime = fromNode.getTravelTimeArray()[30];	// use mid value
					
					// if we find a node with updated distance, just insert it to the priority queue
					// even we pop out another node with same id later, we know that it was visited and will ignore it
					int totalTime = arrTime + travelTime;
					if (totalTime < fromNodeInfo.getCost()) {
						fromNodeInfo.setCost(totalTime);
						fromNodeInfo.setParentId(nodeId);
						if(hierarchy != 1)
							priorityQ.offer(fromNodeInfo);
					}
					
					if(hierarchy == 1) {	// find one highway exit
						ArrayList<Long> path = new ArrayList<Long>();
						NodeInfo entrance = current;
						path.add(entrance.getNodeId());
						while(entrance.getParentId() != -1) {
							entrance = nodeHashMap.get(entrance.getParentId());
							if(entrance == null) {
								System.err.println("cannot find intermediate node!");
								return null;
							}
							path.add(entrance.getNodeId());	// add intermediate node
						}

						HighwayEntrance highwayExit = new HighwayEntrance(nodeId);
						highwayExit.setLocalToHighPath(path);
						highwayExit.setCost(fromNodeInfo.getCost());
						highwayExitMap.put(nodeId, highwayExit);
					}
				}
			}
			
			prepareRoute(nodeStack);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("searchHighwayExit: debug code " + debug);
		}
		return highwayExitMap;
	}
	
	public static HashMap<Long, HighwayEntrance> searchHighwayEntrance(long startNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap, 
			HashMap<String, EdgeInfo> nodesToEdgeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
		HashMap<Long, HighwayEntrance> highwayEntranceMap = new HashMap<Long, HighwayEntrance>();
		
		PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
			public int compare(NodeInfo n1, NodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		NodeInfo current = nodeHashMap.get(startNode);	// get start node
		nodeStack.push(current);
		if(current == null) {
			System.err.println("cannot find start node!");
			return null;
		}
		
		current.setCost(0);	// set start cost to 0
		
		priorityQ.offer(current);
		
		// find four entrance
		while ((current = priorityQ.poll()) != null && highwayEntranceMap.size() < 4) {
			long nodeId = current.getNodeId();
			
			if(nodeId == endNode) {	// we already find destination
				return null;
			}
			
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				NodeInfo toNodeInfo = nodeHashMap.get(toNodeId);
				
				// if(toNodeInfo.isVisited())	// if the node is visited, we bypass it
				// 	continue;
				
				nodeStack.push(toNodeInfo);

				// toNodeInfo.setVisited();
				
				String nodeIdKey = nodeId + OSMParam.COMMA + toNodeId;
				EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
				
				String highway = edge.getHighway();
				int hierarchy = 6;
				if(hierarchyHashMap.containsKey(highway)) {
					hierarchy = hierarchyHashMap.get(highway);
				}
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[30];	// use mid value
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeInfo.getCost()) {
					toNodeInfo.setCost(totalTime);
					toNodeInfo.setParentId(nodeId);
					if(hierarchy != 1) // we don't search highway
						priorityQ.offer(toNodeInfo);
				}
				
				if(hierarchy == 1) {	// find one highway entrance
					ArrayList<Long> path = new ArrayList<Long>();
					NodeInfo entrance = current;
					path.add(entrance.getNodeId());
					while(entrance.getParentId() != -1) {
						entrance = nodeHashMap.get(entrance.getParentId());
						if(entrance == null) {
							System.err.println("cannot find intermediate node!");
							return null;
						}
						path.add(entrance.getNodeId());	// add intermediate node
					}

					Collections.reverse(path);
					HighwayEntrance highwayEntrance = new HighwayEntrance(nodeId);
					highwayEntrance.setCost(toNodeInfo.getCost());
					highwayEntrance.setLocalToHighPath(path);
					highwayEntranceMap.put(nodeId, highwayEntrance);
				}
			}
		}
		
		prepareRoute(nodeStack);
		
		return highwayEntranceMap;
	}
	
	public static int tdspHierarchy(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap, 
			HashMap<String, EdgeInfo> nodesToEdgeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		System.out.println("start finding the path...");
		
		HashMap<Long, HighwayEntrance> entranceMap = searchHighwayEntrance(startNode, endNode, nodeHashMap, nodesToEdgeHashMap, adjListHashMap);
		HashMap<Long, HighwayEntrance> exitMap	= searchHighwayExit(startNode, endNode, nodeHashMap, nodesToEdgeHashMap, adjReverseListHashMap);
		
		if(entranceMap == null || exitMap == null) {	// we should use normal tdsp in this situation
			return tdsp(startNode, endNode, startTime, nodeHashMap, adjListHashMap);
		}
		
		// test
		OSMOutput.generateEntranceExitKML(startNode, endNode, entranceMap, exitMap, nodeHashMap);
		
		int totalCost = Integer.MAX_VALUE;
		long finalEntrance = -1;
		long finalExit = -1;
		
		// iterate each entrance
		for(long entranceId : entranceMap.keySet()) {
			PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
				public int compare(NodeInfo n1, NodeInfo n2) {
					return n1.getCost() - n2.getCost();
				}
			});
			
			Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
			NodeInfo current = nodeHashMap.get(entranceId);	// get start node
			nodeStack.push(current);
			if(current == null) {
				System.err.println("cannot find entrance node!");
				prepareRoute(nodeStack);
				return -1;
			}
			
			current.setCost(0);	// set start cost to 0
			
			priorityQ.offer(current);
			
			LinkedList<NodeInfo> exitNodeList = new LinkedList<NodeInfo>();
			while ((current = priorityQ.poll()) != null) {
				long nodeId = current.getNodeId();
				
				if(exitMap.get(nodeId) != null) {	// find exit
					exitNodeList.add(current);
					if(exitNodeList.size() == 4)
						break;
				}
				
				int timeIndex = startTime + current.getCost() / 60 / TIME_INTERVAL;
				
				if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = TIME_RANGE - 1;
				
				LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
				if(adjNodeList == null)
					continue;
				
				int arrTime = current.getCost();
				
				for(ToNodeInfo toNode : adjNodeList) {
					long toNodeId = toNode.getNodeId();
					
					NodeInfo toNodeInfo = nodeHashMap.get(toNodeId);
					
					// if(toNodeInfo.isVisited())	// if the node is visited, we bypass it
					// 	continue;
					nodeStack.push(toNodeInfo);
					
					// toNodeInfo.setVisited();
					
					String nodeIdKey = nodeId + OSMParam.COMMA + toNodeId;
					EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
					
					String highway = edge.getHighway();
					int hierarchy = 6;
					if(hierarchyHashMap.containsKey(highway)) {
						hierarchy = hierarchyHashMap.get(highway);
					}
					if(hierarchy != 1)	// we keep on in highway
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
			
			if(exitNodeList.size() > 0) {
				for(NodeInfo exit : exitNodeList) {
					long exitId = exit.getNodeId();
					NodeInfo cur = exit;
					int cost1 = entranceMap.get(entranceId).getCost();
					int cost2 = exitMap.get(exitId).getCost();
					int newCost = cur.getCost() + entranceMap.get(entranceId).getCost() + exitMap.get(exitId).getCost();
					
					if(newCost < totalCost) {	// find less cost path
						totalCost = newCost;
						pathNodeList = new ArrayList<Long>();
						if(entranceId == exitId) {
							System.out.println("entrance node is the same as exit node.");
							prepareRoute(nodeStack);
							return 0;
						}
						else {
							pathNodeList.add(exitId);
							while(cur.getParentId() != -1) {
								cur = nodeHashMap.get(cur.getParentId());
								if(cur == null) {
									System.err.println("cannot find intermediate node!");
									prepareRoute(nodeStack);
									return -1;
								}
								pathNodeList.add(cur.getNodeId());	// add intermediate node
							}
							Collections.reverse(pathNodeList);
						}
						finalEntrance = entranceId;
						finalExit = exitId;
					}
				}
			}
			// prepare for next time
			prepareRoute(nodeStack);
		}
		
		HighwayEntrance entrance = entranceMap.get(finalEntrance);
		HighwayEntrance exit = exitMap.get(finalExit);
		if(entrance != null && exit != null) {
			ArrayList<Long> entrancePath = entrance.getLocalToHighPath();
			// remove the duplicate entrance
			entrancePath.remove(entrancePath.size() - 1);
			ArrayList<Long> exitPath = exit.getLocalToHighPath();
			// remove the duplicate exit
			pathNodeList.remove(pathNodeList.size() - 1);
			// add entrance and exit path
			pathNodeList.addAll(0, entrancePath);
			pathNodeList.addAll(pathNodeList.size(), exitPath);
			System.out.println("find the path successful!");
		}
		else {
			System.err.println("cannot find the entrance!");
			return -1;
		}
		return totalCost;
	}

	public static int tdsp(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("start finding the path...");
		PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
			public int compare(NodeInfo n1, NodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
		NodeInfo current = nodeHashMap.get(startNode);	// get start node
		nodeStack.push(current);
		if(current == null) {
			System.err.println("cannot find start node!");
			prepareRoute(nodeStack);
			return -1;
		}
		
		current.setCost(0);	// set start cost to 0
		
		priorityQ.offer(current);
		
		int totalCost = -1;
		
		while ((current = priorityQ.poll()) != null) {
			long nodeId = current.getNodeId();
			
			if(nodeId == endNode) {	// find the end
				totalCost = current.getCost();
				break;
			}
			
			int timeIndex = startTime + current.getCost() / 60 / TIME_INTERVAL;
			
			if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = TIME_RANGE - 1;
			
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				NodeInfo toNodeInfo = nodeHashMap.get(toNodeId);
				
				// if(toNodeInfo.isVisited())	// if the node is visited, we bypass it
				// 	continue;
				
				nodeStack.push(toNodeInfo);

				// toNodeInfo.setVisited();
				
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
		
		if (startNode == endNode) {
			System.out.println("start node is the same as end node.");
			prepareRoute(nodeStack);
			return 0;
		}
		else {
			current = nodeHashMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node!");
				prepareRoute(nodeStack);
				return -1;
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeHashMap.get(current.getParentId());
				if(current == null) {
					System.err.println("cannot find intermediate node!");
					prepareRoute(nodeStack);
					return -1;
				}
				pathNodeList.add(current.getNodeId());	// add intermediate node
			}
			
			if(current.getParentId() == -1) {
				System.err.println("cannot find the path!");
				prepareRoute(nodeStack);
				return -1;
			}
			
			if(current.getParentId() == startNode)
				pathNodeList.add(startNode);	// add start node
			
			Collections.reverse(pathNodeList);	// reverse the path list
		}
		
		// prepare for next time
		prepareRoute(nodeStack);
		
		System.out.println("find the path successful!");
		return totalCost;
	}
}
