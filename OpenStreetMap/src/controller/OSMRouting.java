package controller;

import java.util.*;

import global.*;
import object.*;
import main.*;
import model.*;

/**
 * @data structure used in queue
 */
class NodeInfoHelper {
	long nodeId;
	
	int cost;
	int heuristic;
	long parentId;
	
	public NodeInfoHelper(long nodeId) {
		this.nodeId = nodeId;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public int getCost() {
		return cost;
	}

	public void setHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}

	public int getHeuristic() {
		return heuristic;
	}

	public int getTotalCost() {
		return cost + heuristic;
	}
	
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	public long getParentId() {
		return parentId;
	}
}

public class OSMRouting {

	/**
	 * @param param
	 */
	static long START_NODE 		= 33525194;
	static long END_NODE 		= 187954876;
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
		initialHierarchy(hierarchyHashMap);
		tdsp(OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		
		// output
		OSMOutput.generatePathKML(OSMData.nodeHashMap, pathNodeList);
		OSMOutput.generatePathNodeKML(OSMData.nodeHashMap, pathNodeList);
	}
	
	public static void tdsp(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		// count time
		long begintime = System.currentTimeMillis();
		tdsp(START_NODE, END_NODE, START_TIME, nodeHashMap, adjListHashMap);
		//tdspHierarchy(START_NODE, END_NODE, START_TIME, nodeHashMap, nodesToEdgeHashMap, adjListHashMap, adjReverseListHashMap);
		long endtime = System.currentTimeMillis();
		long costTime = (endtime - begintime);
		System.out.println("tdsp cost: " + costTime + " ms");
	}
	
	public static void initialHierarchy(HashMap<String, Integer> hierarchyHashMap) {
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
	
//	public static HashMap<Long, HighwayEntrance> searchHighwayExit(long startNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap, 
//			HashMap<String, EdgeInfo> nodesToEdgeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
//		Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
//		HashMap<Long, HighwayEntrance> highwayExitMap = new HashMap<Long, HighwayEntrance>();
//		PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
//			public int compare(NodeInfo n1, NodeInfo n2) {
//				return n1.getCost() - n2.getCost();
//			}
//		});
//		int debug = 0;
//		try {
//			NodeInfo current = nodeHashMap.get(endNode);	// get start node
//			nodeStack.push(current);
//			if(current == null) {
//				System.err.println("cannot find start node!");
//				return null;
//			}
//			
//			current.setCost(0);	// set start cost to 0
//			
//			priorityQ.offer(current);
//			
//			// find four exit
//			while ((current = priorityQ.poll()) != null && highwayExitMap.size() < 4) {
//				debug++;
//
//				long nodeId = current.getNodeId();
//				
//				if(nodeId == startNode) {	// we already find source
//					return null;
//				}
//				
//				LinkedList<ToNodeInfo> adjNodeList = adjReverseListHashMap.get(nodeId);
//				if(adjNodeList == null)
//					continue;
//				
//				int arrTime = current.getCost();
//				
//				for(ToNodeInfo fromNode : adjNodeList) {
//					long fromNodeId = fromNode.getNodeId();
//					
//					NodeInfo fromNodeInfo = nodeHashMap.get(fromNodeId);
//					
//					// if(fromNodeInfo.isVisited())	// if the node is visited, we bypass it
//					// 	continue;
//
//					nodeStack.push(fromNodeInfo);
//
//					// fromNodeInfo.setVisited();
//					
//					String nodeIdKey = fromNodeId + OSMParam.COMMA + nodeId;
//					EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
//					
//					String highway = edge.getHighway();
//					int hierarchy = 6;
//					if(hierarchyHashMap.containsKey(highway)) {
//						hierarchy = hierarchyHashMap.get(highway);
//					}
//					
//					int travelTime;
//					if(fromNode.isFix())	// fix time
//						travelTime = fromNode.getTravelTime();
//					else	// fetch from time array
//						travelTime = fromNode.getTravelTimeArray()[30];	// use mid value
//					
//					// if we find a node with updated distance, just insert it to the priority queue
//					// even we pop out another node with same id later, we know that it was visited and will ignore it
//					int totalTime = arrTime + travelTime;
//					if (totalTime < fromNodeInfo.getCost()) {
//						fromNodeInfo.setCost(totalTime);
//						fromNodeInfo.setParentId(nodeId);
//						if(hierarchy != 1)
//							priorityQ.offer(fromNodeInfo);
//					}
//					
//					if(hierarchy == 1) {	// find one highway exit
//						ArrayList<Long> path = new ArrayList<Long>();
//						NodeInfo entrance = current;
//						path.add(entrance.getNodeId());
//						while(entrance.getParentId() != -1) {
//							entrance = nodeHashMap.get(entrance.getParentId());
//							if(entrance == null) {
//								System.err.println("cannot find intermediate node!");
//								return null;
//							}
//							path.add(entrance.getNodeId());	// add intermediate node
//						}
//
//						HighwayEntrance highwayExit = new HighwayEntrance(nodeId);
//						highwayExit.setLocalToHighPath(path);
//						highwayExit.setCost(fromNodeInfo.getCost());
//						highwayExitMap.put(nodeId, highwayExit);
//					}
//				}
//			}
//			
//			prepareRoute(nodeStack);
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			System.err.println("searchHighwayExit: debug code " + debug);
//		}
//		return highwayExitMap;
//	}
//	
//	public static HashMap<Long, HighwayEntrance> searchHighwayEntrance(long startNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap, 
//			HashMap<String, EdgeInfo> nodesToEdgeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
//		Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
//		HashMap<Long, HighwayEntrance> highwayEntranceMap = new HashMap<Long, HighwayEntrance>();
//		
//		PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
//			public int compare(NodeInfo n1, NodeInfo n2) {
//				return n1.getCost() - n2.getCost();
//			}
//		});
//		
//		NodeInfo current = nodeHashMap.get(startNode);	// get start node
//		nodeStack.push(current);
//		if(current == null) {
//			System.err.println("cannot find start node!");
//			return null;
//		}
//		
//		current.setCost(0);	// set start cost to 0
//		
//		priorityQ.offer(current);
//		
//		// find four entrance
//		while ((current = priorityQ.poll()) != null && highwayEntranceMap.size() < 4) {
//			long nodeId = current.getNodeId();
//			
//			if(nodeId == endNode) {	// we already find destination
//				return null;
//			}
//			
//			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
//			if(adjNodeList == null)
//				continue;
//			
//			int arrTime = current.getCost();
//			
//			for(ToNodeInfo toNode : adjNodeList) {
//				long toNodeId = toNode.getNodeId();
//				
//				NodeInfo toNodeInfo = nodeHashMap.get(toNodeId);
//				
//				// if(toNodeInfo.isVisited())	// if the node is visited, we bypass it
//				// 	continue;
//				
//				nodeStack.push(toNodeInfo);
//
//				// toNodeInfo.setVisited();
//				
//				String nodeIdKey = nodeId + OSMParam.COMMA + toNodeId;
//				EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
//				
//				String highway = edge.getHighway();
//				int hierarchy = 6;
//				if(hierarchyHashMap.containsKey(highway)) {
//					hierarchy = hierarchyHashMap.get(highway);
//				}
//				
//				int travelTime;
//				if(toNode.isFix())	// fix time
//					travelTime = toNode.getTravelTime();
//				else	// fetch from time array
//					travelTime = toNode.getTravelTimeArray()[30];	// use mid value
//				
//				// if we find a node with updated distance, just insert it to the priority queue
//				// even we pop out another node with same id later, we know that it was visited and will ignore it
//				int totalTime = arrTime + travelTime;
//				if (totalTime < toNodeInfo.getCost()) {
//					toNodeInfo.setCost(totalTime);
//					toNodeInfo.setParentId(nodeId);
//					if(hierarchy != 1) // we don't search highway
//						priorityQ.offer(toNodeInfo);
//				}
//				
//				if(hierarchy == 1) {	// find one highway entrance
//					ArrayList<Long> path = new ArrayList<Long>();
//					NodeInfo entrance = current;
//					path.add(entrance.getNodeId());
//					while(entrance.getParentId() != -1) {
//						entrance = nodeHashMap.get(entrance.getParentId());
//						if(entrance == null) {
//							System.err.println("cannot find intermediate node!");
//							return null;
//						}
//						path.add(entrance.getNodeId());	// add intermediate node
//					}
//
//					Collections.reverse(path);
//					HighwayEntrance highwayEntrance = new HighwayEntrance(nodeId);
//					highwayEntrance.setCost(toNodeInfo.getCost());
//					highwayEntrance.setLocalToHighPath(path);
//					highwayEntranceMap.put(nodeId, highwayEntrance);
//				}
//			}
//		}
//		
//		prepareRoute(nodeStack);
//		
//		return highwayEntranceMap;
//	}
//	
//	public static int tdspHierarchy(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap, 
//			HashMap<String, EdgeInfo> nodesToEdgeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap,
//			HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
//		System.out.println("start finding the path...");
//		
//		HashMap<Long, HighwayEntrance> entranceMap = searchHighwayEntrance(startNode, endNode, nodeHashMap, nodesToEdgeHashMap, adjListHashMap);
//		HashMap<Long, HighwayEntrance> exitMap	= searchHighwayExit(startNode, endNode, nodeHashMap, nodesToEdgeHashMap, adjReverseListHashMap);
//		
//		if(entranceMap == null || exitMap == null) {	// we should use normal tdsp in this situation
//			return tdsp(startNode, endNode, startTime, nodeHashMap, adjListHashMap);
//		}
//		
//		// test
//		OSMOutput.generateEntranceExitKML(startNode, endNode, entranceMap, exitMap, nodeHashMap);
//		
//		int totalCost = Integer.MAX_VALUE;
//		long finalEntrance = -1;
//		long finalExit = -1;
//		
//		// iterate each entrance
//		for(long entranceId : entranceMap.keySet()) {
//			PriorityQueue<NodeInfo> priorityQ = new PriorityQueue<NodeInfo>( 20, new Comparator<NodeInfo>() {
//				public int compare(NodeInfo n1, NodeInfo n2) {
//					return n1.getCost() - n2.getCost();
//				}
//			});
//			
//			Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();
//			NodeInfo current = nodeHashMap.get(entranceId);	// get start node
//			nodeStack.push(current);
//			if(current == null) {
//				System.err.println("cannot find entrance node!");
//				prepareRoute(nodeStack);
//				return -1;
//			}
//			
//			current.setCost(0);	// set start cost to 0
//			
//			priorityQ.offer(current);
//			
//			LinkedList<NodeInfo> exitNodeList = new LinkedList<NodeInfo>();
//			while ((current = priorityQ.poll()) != null) {
//				long nodeId = current.getNodeId();
//				
//				if(exitMap.get(nodeId) != null) {	// find exit
//					exitNodeList.add(current);
//					if(exitNodeList.size() == 4)
//						break;
//				}
//				
//				int timeIndex = startTime + current.getCost() / 60 / TIME_INTERVAL;
//				
//				if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
//					timeIndex = TIME_RANGE - 1;
//				
//				LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
//				if(adjNodeList == null)
//					continue;
//				
//				int arrTime = current.getCost();
//				
//				for(ToNodeInfo toNode : adjNodeList) {
//					long toNodeId = toNode.getNodeId();
//					
//					NodeInfo toNodeInfo = nodeHashMap.get(toNodeId);
//					
//					// if(toNodeInfo.isVisited())	// if the node is visited, we bypass it
//					// 	continue;
//					nodeStack.push(toNodeInfo);
//					
//					// toNodeInfo.setVisited();
//					
//					String nodeIdKey = nodeId + OSMParam.COMMA + toNodeId;
//					EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
//					
//					String highway = edge.getHighway();
//					int hierarchy = 6;
//					if(hierarchyHashMap.containsKey(highway)) {
//						hierarchy = hierarchyHashMap.get(highway);
//					}
//					if(hierarchy != 1)	// we keep on in highway
//						continue;
//					
//					int travelTime;
//					if(toNode.isFix())	// fix time
//						travelTime = toNode.getTravelTime();
//					else	// fetch from time array
//						travelTime = toNode.getTravelTimeArray()[timeIndex];
//					
//					// if we find a node with updated distance, just insert it to the priority queue
//					// even we pop out another node with same id later, we know that it was visited and will ignore it
//					int totalTime = arrTime + travelTime;
//					if (totalTime < toNodeInfo.getCost()) {
//						toNodeInfo.setCost(totalTime);
//						toNodeInfo.setParentId(nodeId);
//						priorityQ.offer(toNodeInfo);
//					}
//				}
//			}
//			
//			if(exitNodeList.size() > 0) {
//				for(NodeInfo exit : exitNodeList) {
//					long exitId = exit.getNodeId();
//					NodeInfo cur = exit;
//					int newCost = cur.getCost() + entranceMap.get(entranceId).getCost() + exitMap.get(exitId).getCost();
//					
//					if(newCost < totalCost) {	// find less cost path
//						totalCost = newCost;
//						pathNodeList = new ArrayList<Long>();
//						if(entranceId == exitId) {
//							System.out.println("entrance node is the same as exit node.");
//							prepareRoute(nodeStack);
//							return 0;
//						}
//						else {
//							pathNodeList.add(exitId);
//							while(cur.getParentId() != -1) {
//								cur = nodeHashMap.get(cur.getParentId());
//								if(cur == null) {
//									System.err.println("cannot find intermediate node!");
//									prepareRoute(nodeStack);
//									return -1;
//								}
//								pathNodeList.add(cur.getNodeId());	// add intermediate node
//							}
//							Collections.reverse(pathNodeList);
//						}
//						finalEntrance = entranceId;
//						finalExit = exitId;
//					}
//				}
//			}
//			// prepare for next time
//			prepareRoute(nodeStack);
//		}
//		
//		HighwayEntrance entrance = entranceMap.get(finalEntrance);
//		HighwayEntrance exit = exitMap.get(finalExit);
//		if(entrance != null && exit != null) {
//			ArrayList<Long> entrancePath = entrance.getLocalToHighPath();
//			// remove the duplicate entrance
//			entrancePath.remove(entrancePath.size() - 1);
//			ArrayList<Long> exitPath = exit.getLocalToHighPath();
//			// remove the duplicate exit
//			pathNodeList.remove(pathNodeList.size() - 1);
//			// add entrance and exit path
//			pathNodeList.addAll(0, entrancePath);
//			pathNodeList.addAll(pathNodeList.size(), exitPath);
//			System.out.println("find the path successful!");
//		}
//		else {
//			System.err.println("cannot find the entrance!");
//			return -1;
//		}
//		return totalCost;
//	}
	
	public static int estimateHeuristic(long curNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap) {
		NodeInfo cur = nodeHashMap.get(curNode);
		NodeInfo end = nodeHashMap.get(endNode);
		double distance = Distance.calculateDistance(cur.getLocation(), end.getLocation());
		return (int) (distance / 80) * OSMParam.SECOND_PER_HOUR;
	}
	
	// using A* algorithm
	// http://en.wikipedia.org/wiki/A*_search_algorithm
	public static int tdsp(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("start finding the path...");
		
		if(!nodeHashMap.containsKey(startNode) || !nodeHashMap.containsKey(endNode)) {
			System.err.println("cannot find start or end node!");
			return -1;
		}
		
		if (startNode == endNode) {
			System.out.println("start node is the same as end node.");
			return 0;
		}
		
		PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 20, new Comparator<NodeInfoHelper>() {
			public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
				return n1.getTotalCost() - n2.getTotalCost();
			}
		});
		HashMap<Long, NodeInfoHelper> closedSet = new HashMap<Long, NodeInfoHelper>();
		
		// initial
		NodeInfoHelper current = new NodeInfoHelper(startNode);
		current.setCost(0);
		current.setHeuristic(estimateHeuristic(startNode, endNode, nodeHashMap));
		openSet.offer(current);	// push the start node
		
		int totalCost = -1;
		
		while(!openSet.isEmpty()) {
			// remove current from openset
			current = openSet.poll();
			long nodeId = current.getNodeId();
			// add current to closedset
			closedSet.put(nodeId, current);
			if(nodeId == endNode) {	// find the destination
				totalCost = current.getCost();
				break;
			}
			// for time dependent routing
			int timeIndex = startTime + current.getCost() / 60 / TIME_INTERVAL;
			if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = TIME_RANGE - 1;
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);	// this node cannot go anywhere
			if(adjNodeList == null)
				continue;
			int arriveTime = current.getCost();
			// for each neighbor in neighbor_nodes(current)
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				// tentative_g_score := g_score[current] + dist_between(current,neighbor)
				int costTime = arriveTime + travelTime;
				// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
				int heuristicTime =  estimateHeuristic(toNodeId, endNode, nodeHashMap);
				int totalCostTime = costTime + heuristicTime;
				// if neighbor in closedset and tentative_f_score >= f_score[neighbor]
				if(closedSet.containsKey(toNodeId) && closedSet.get(toNodeId).getTotalCost() <= totalCostTime) {
					continue;
				}
				NodeInfoHelper node = null;
				// tentative_f_score < f_score[neighbor]
				if(closedSet.containsKey(toNodeId) && closedSet.get(toNodeId).getTotalCost() > totalCostTime) {	// neighbor in closeset
					node = closedSet.get(toNodeId);
				}
				else  {
					for(NodeInfoHelper search : openSet) {
						if(search.getNodeId() == toNodeId) {	// neighbor in openset
							node = search;
							break;
						}
					}
					if(node == null) {	// neighbor not in openset
						node = new NodeInfoHelper(toNodeId);
						// add neighbor to openset
						openSet.offer(node);
					}
				}
				node.setCost(costTime);
				node.setHeuristic(heuristicTime);
				node.setParentId(nodeId);
			}
		}
		
		current = closedSet.get(endNode);
		while(current.getParentId() != 0) {
			pathNodeList.add(current.getNodeId());	// add end node
			current = closedSet.get(current.getParentId());
		}
		Collections.reverse(pathNodeList);	// reverse the path list
		
		System.out.println("find the path successful!");
		return totalCost;
	}
}
