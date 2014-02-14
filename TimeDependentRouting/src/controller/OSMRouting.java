package controller;

import java.text.*;
import java.util.*;

import global.*;
import object.*;
import model.*;
import library.*;

class OSMRouteParam {
	/**
	 * @param param
	 */
	static double START_LAT;
	static double START_LON;
	static double END_LAT;
	static double END_LON;
	
	static int START_TIME;
	static int DAY_INDEX;
	static int TIME_INTERVAL;
	static int TIME_RANGE;
	static int HEURISTIC_SPEED;
	static int FOUND_PATH_COUNT;
}

/**
 * data structure used in queue
 */
class NodeInfoHelper {
	long nodeId;
	
	double cost;
	double heuristic;
	long parentId;
	
	// for hierarchy routing
	int currentLevel;
	
	public NodeInfoHelper(long nodeId) {
		this.nodeId = nodeId;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public double getCost() {
		return cost;
	}

	public void setHeuristic(double heuristic) {
		this.heuristic = heuristic;
	}

	public double getHeuristic() {
		return heuristic;
	}

	public double getTotalCost() {
		return cost + heuristic;
	}
	
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	public long getParentId() {
		return parentId;
	}
	
	public void setCurrentLevel(int level) {
		currentLevel = level;
	}
	
	public int getCurrentLevel() {
		return currentLevel;
	}
	
	public NodeInfoHelper getEndNodeHelper(long endNode) {
		// TODO: not time dependent now, need to modify
		NodeInfo end = OSMData.nodeHashMap.get(endNode);
		NodeInfoHelper endNodeHelper = new NodeInfoHelper(endNode);
		if(getNodeId() == end.getNodeId()) {	// no need to do in edge routing
			return this;
		}
		else {
			EdgeInfo edge = end.getOnEdgeList().getFirst();
			int distance;
			// from start to middle
			if(getNodeId() == edge.getStartNode()) {
				distance = edge.getStartDistance(endNode);
			}
			else {	// from end to middle
				distance = edge.getEndDistance(endNode);
			}
			double speed  = edge.getTravelSpeed();
			int travelTime = 1;	// second
			travelTime = (int) Math.round(distance / speed);
			endNodeHelper.setCost(getCost() + travelTime);
			endNodeHelper.setParentId(getNodeId());
		}
		return endNodeHelper;
	}
}

/**
 * forward searching thread with A*
 */
class ForwardSearching implements Runnable {
	long startNode, endNode;
	int startTime, dayIndex;
	SearchingSharing sharingData;
	HashMap<Long, NodeInfoHelper> nodeHelperCache;
	HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap;
	
	public ForwardSearching(long startNode, long endNode, int startTime, int dayIndex,
			SearchingSharing sharingData, HashMap<Long, NodeInfoHelper> nodeHelperCache) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.startTime = startTime;
		this.dayIndex = dayIndex;
		this.sharingData = sharingData;
		this.nodeHelperCache = nodeHelperCache;
		this.adjListHashMap = OSMData.adjListHashMap;
	}
	
	@Override
	public void run() {
		HashSet<Long> closedSet = new HashSet<Long>();
		PriorityQueue<NodeInfoHelper> openSet = OSMRouting.initialStartSet(startNode, endNode, startTime, dayIndex, nodeHelperCache);
		
		NodeInfoHelper current = null;
		// test
		//HashSet<Long> transversalSet = new HashSet<Long>();
		while(!openSet.isEmpty()) {
			// remove current from openset
			current = openSet.poll();
			
			long nodeId = current.getNodeId();
			
			//transversalSet.add(nodeId);
			
			// add current to closedset
			closedSet.add(nodeId);
			
			// check reverse searching covering nodes
			if(sharingData.isNodeInReverseSet(nodeId)) {
				// we found a path, stop here
				sharingData.addIntersect(nodeId);
				if(sharingData.isSearchingFinish()) break;
				else continue;
			}
			
			NodeInfo fromNodeInfo = OSMData.nodeHashMap.get(nodeId);
			// for time dependent routing in forwarding search
			int timeIndex = startTime + (int)(current.getCost() / OSMParam.SECOND_PER_MINUTE / OSMRouteParam.TIME_INTERVAL);
			if (timeIndex > OSMRouteParam.TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = OSMRouteParam.TIME_RANGE - 1;
			
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null) continue;	// this node cannot go anywhere
			double arriveTime = current.getCost();
			// for each neighbor in neighbor_nodes(current)
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				NodeInfo toNodeInfo = OSMData.nodeHashMap.get(toNodeId);
				EdgeInfo edgeInfo = fromNodeInfo.getEdgeFromNodes(toNodeInfo);
				// check if "highway" not found in param, add it
				//String highway = edgeInfo.getHighway();
				int level = 10;
				if(OSMData.hierarchyHashMap.containsKey(edgeInfo.getHighway()))
					level = OSMData.hierarchyHashMap.get(edgeInfo.getHighway());
				// 1) always level up, e.g. currently in primary, never go secondary
				//if(level > current.getCurrentLevel()) {	// do not level down
				//	continue;
				//}
				// 2) keep on highway
				if(current.getCurrentLevel() == 1 && level > 1) {
					continue;
				}
				int travelTime;
				// forward searching is time dependent
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getSpecificTravelTime(dayIndex, timeIndex);

				// tentative_g_score := g_score[current] + dist_between(current,neighbor)
				double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
				// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
				double heuristicTime =  OSMRouting.estimateHeuristic(toNodeId, endNode);
				double totalCostTime = costTime + heuristicTime;
				// if neighbor in closedset and tentative_f_score >= f_score[neighbor]
				if(closedSet.contains(toNodeId) && nodeHelperCache.get(toNodeId).getTotalCost() <= totalCostTime) {
					continue;
				}
				NodeInfoHelper node = null;
				// if neighbor not in openset or tentative_f_score < f_score[neighbor]
				if(!nodeHelperCache.containsKey(toNodeId)) {	// neighbor not in openset
					node = new NodeInfoHelper(toNodeId);
					nodeHelperCache.put(node.getNodeId(), node);
				}
				else if (nodeHelperCache.get(toNodeId).getTotalCost() > totalCostTime) {	// neighbor in openset
					node = nodeHelperCache.get(toNodeId);
					if(closedSet.contains(toNodeId)) {	// neighbor in closeset
						closedSet.remove(toNodeId);	// remove neighbor form colseset
					}
					else {
						openSet.remove(node);
					}
				}

				// neighbor need update
				if(node != null) {
					node.setCost(costTime);
					node.setHeuristic(heuristicTime);
					node.setCurrentLevel(level);
					node.setParentId(nodeId);
					openSet.offer(node);	// add neighbor to openset again
				}
			}
		}
		// test
		//OSMOutput.generateTransversalNodeKML(transversalSet, "forward_transversal");
	}
}

/**
 * reverse searching thread using A*
 */
class ReverseSearching implements Runnable {
	long startNode, endNode;
	SearchingSharing sharingData;
	HashMap<Long, NodeInfoHelper> nodeHelperCache;
	HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap;
	
	public ReverseSearching(long startNode, long endNode,
			SearchingSharing sharingData, HashMap<Long, NodeInfoHelper> nodeHelperCache) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.sharingData = sharingData;
		this.nodeHelperCache = nodeHelperCache;
		this.adjListHashMap = OSMData.adjReverseListHashMap;
	}
	
	/**
	 * using low bound for travel time, for reverse searching
	 * @param endNode
	 * @param startNode
	 * @param openSet
	 * @param nodeHelperCache
	 */
	public void initialEndSet(long startNode, long endNode, PriorityQueue<NodeInfoHelper> openSet,
			HashMap<Long, NodeInfoHelper> nodeHelperCache) {
		NodeInfo start = OSMData.nodeHashMap.get(startNode);
		NodeInfoHelper current;
		// initial start end set
		if(start.isIntersect()) {
			// initial
			current = new NodeInfoHelper(startNode);
			current.setCost(0);
			current.setCurrentLevel(10);
			current.setHeuristic(OSMRouting.estimateHeuristic(startNode, endNode));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
		}
		else {
			EdgeInfo edge = start.getOnEdgeList().getFirst();
			double travelTime = 1;	// second
			int distance; // feet
			int totalDistance = edge.getDistance(); // feet
			if(!edge.isOneway()) {
				// distance from start to middle
				distance = edge.getStartDistance(startNode);
				// get low bound of travel time
				travelTime = edge.getTravelTimeMin(false);
				travelTime *= (double)distance / totalDistance;
				current = new NodeInfoHelper(edge.getStartNode());
				current.setCost(travelTime);
				current.setCurrentLevel(10);
				current.setHeuristic(OSMRouting.estimateHeuristic(edge.getStartNode(), endNode));
				openSet.offer(current);	// push the start node
				nodeHelperCache.put(current.getNodeId(), current);	// add cache
			}
			// distance from middle to end
			distance = edge.getEndDistance(startNode);
			travelTime = edge.getTravelTimeMin(true);
			travelTime *= (double)distance / totalDistance;
			current = new NodeInfoHelper(edge.getEndNode());
			current.setCost(travelTime);
			current.setCurrentLevel(10);
			current.setHeuristic(OSMRouting.estimateHeuristic(edge.getEndNode(), endNode));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 10000, new Comparator<NodeInfoHelper>() {
			public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
				return (int)(n1.getTotalCost() - n2.getTotalCost());
			}
		});
		HashSet<Long> closedSet = new HashSet<Long>();
		initialEndSet(startNode, endNode, openSet, nodeHelperCache);
		
		NodeInfoHelper current = null;
		
		//HashSet<Long> transversalSet = new HashSet<Long>();
		
		while(!openSet.isEmpty()) {
			// remove current from openset
			current = openSet.poll();
			
			long nodeId = current.getNodeId();
			//transversalSet.add(nodeId);
			// add current to closedset
			closedSet.add(nodeId);
			
			// check if forward searching finish
			if(sharingData.isSearchingFinish()) break;
			// add to reverse cover nodes, only add the nodes on the highway
			if(current.getCurrentLevel() == 1)
				sharingData.addReverseNode(nodeId);
			
			NodeInfo fromNodeInfo = OSMData.nodeHashMap.get(nodeId);
			
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null) continue;	// this node cannot go anywhere
			double arriveTime = current.getCost();
			// for each neighbor in neighbor_nodes(current)
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				NodeInfo toNodeInfo = OSMData.nodeHashMap.get(toNodeId);
				EdgeInfo edgeInfo = fromNodeInfo.getEdgeFromNodes(toNodeInfo);
				// check if "highway" not found in param, add it
				//String highway = edgeInfo.getHighway();
				int level = 10;
				if(OSMData.hierarchyHashMap.containsKey(edgeInfo.getHighway()))
					level = OSMData.hierarchyHashMap.get(edgeInfo.getHighway());
				// 1) always level up, e.g. currently in primary, never go secondary
				//if(level > current.getCurrentLevel()) {	// do not level down
				//	continue;
				//}
				// 2) keep on highway
				if(current.getCurrentLevel() == 1 && level > 1) {
					continue;
				}
				int travelTime = toNode.getMinTravelTime();

				// tentative_g_score := g_score[current] + dist_between(current,neighbor)
				double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
				// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
				double heuristicTime =  OSMRouting.estimateHeuristic(toNodeId, endNode);
				double totalCostTime = costTime + heuristicTime;
				// if neighbor in closedset and tentative_f_score >= f_score[neighbor]
				if(closedSet.contains(toNodeId) && nodeHelperCache.get(toNodeId).getTotalCost() <= totalCostTime) {
					continue;
				}
				NodeInfoHelper node = null;
				// if neighbor not in openset or tentative_f_score < f_score[neighbor]
				if(!nodeHelperCache.containsKey(toNodeId)) {	// neighbor not in openset
					node = new NodeInfoHelper(toNodeId);
					nodeHelperCache.put(node.getNodeId(), node);
				}
				else if (nodeHelperCache.get(toNodeId).getTotalCost() > totalCostTime) {	// neighbor in openset
					node = nodeHelperCache.get(toNodeId);
					if(closedSet.contains(toNodeId)) {	// neighbor in closeset
						closedSet.remove(toNodeId);	// remove neighbor form colseset
					}
					else {
						openSet.remove(node);
					}
				}

				// neighbor need update
				if(node != null) {
					node.setCost(costTime);
					node.setHeuristic(heuristicTime);
					node.setCurrentLevel(level);
					node.setParentId(nodeId);
					openSet.offer(node);	// add neighbor to openset again
				}
			}
		}
		//OSMOutput.generateTransversalNodeKML(transversalSet, nodeHashMap, "reverse_transversal");
	}
}

/**
 * shared data
 */
class SearchingSharing {
	HashSet<Long> coveredReverseSet;
	ArrayList<Long> intersectList;
	
	public SearchingSharing() {
		coveredReverseSet = new HashSet<Long>();
		intersectList = new ArrayList<Long>();
	}
	
	/**
	 * add intersect to indicate one path has been found
	 * @param node
	 * @return true when done (found four intersects and paths)
	 */
	public void addIntersect(long node) {
		intersectList.add(node);
	}

	public ArrayList<Long> getIntersectList() {
		return intersectList;
	}

	public void addReverseNode(long node) {
		coveredReverseSet.add(node);
	}

	public boolean isNodeInReverseSet(long node) {
		if(coveredReverseSet.contains(node)) return true;
		else return false;
	}

	public boolean isSearchingFinish() {
		return intersectList.size() == OSMRouteParam.FOUND_PATH_COUNT;
	}
}

public class OSMRouting {
	
	/**
	 * hierarchy routing with A* algorithm bidirectional search
	 * reference: http://algo2.iti.kit.edu/schultes/hwy/distTableTR.pdf
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param dayIndex
	 * @param pathNodeList return the path
	 * @return
	 */
	// TODO : if start or end is already in the highway, will occur the problem, need to fix
	public static double routingHierarchy(long startNode, long endNode, int startTime, int dayIndex, 
			ArrayList<Long> pathNodeList) {
		// System.out.println("start finding the path...");
		int debug = 0;
		try {
			if(!OSMData.nodeHashMap.containsKey(startNode) || !OSMData.nodeHashMap.containsKey(endNode)) {
				System.err.println("cannot find start or end node!");
				return -1;
			}
			
			if (startNode == endNode) {
				System.out.println("start node is the same as end node.");
				return 0;
			}
			
			NodeInfo start = OSMData.nodeHashMap.get(startNode);
			NodeInfo end = OSMData.nodeHashMap.get(endNode);
			double minDistance = Geometry.calculateDistance(start.getLocation(), end.getLocation());
			if(minDistance < 5) {	// use normal A* algorithm to calculate small distance
				return routingAStar(start.getNodeId(), end.getNodeId(), OSMRouteParam.START_TIME, OSMRouteParam.DAY_INDEX, pathNodeList);
			}
			
			SearchingSharing sharingData = new SearchingSharing();
			HashMap<Long, NodeInfoHelper> nodeForwardCache = new HashMap<Long, NodeInfoHelper>();
			HashMap<Long, NodeInfoHelper> nodeReverseCache = new HashMap<Long, NodeInfoHelper>();
			ForwardSearching forwardSearching = new ForwardSearching(startNode, endNode, startTime, dayIndex, sharingData, nodeForwardCache);
			ReverseSearching reverseSearching = new ReverseSearching(endNode, startNode, sharingData, nodeReverseCache);
			// two thread run simultaneously
			Thread forwardThread = new Thread(forwardSearching);
			Thread reverseThread = new Thread(reverseSearching);
			// search forward
			forwardThread.start();
			// let forward searching for a while
			//Thread.sleep(100);
			// search reverse
			reverseThread.start();
			// waiting for thread finish
			forwardThread.join();
			reverseThread.join();
			// get the searching intersects
			ArrayList<Long> intersectList = sharingData.getIntersectList();
			// pick the least cost one according to time-dependent
			double minCost = Double.MAX_VALUE;
			ArrayList<Long> minCostPath = new ArrayList<Long>();
			for(long intersect : intersectList) {
				NodeInfoHelper current = nodeForwardCache.get(intersect);
				// cost from source to intersect
				double cost = current.getCost();
				current = nodeReverseCache.get(intersect);
				// update the reverse cost as forward cost
				current.setCost(cost);
				ArrayList<Long> reversePath = new ArrayList<Long>();
				double totalCost = Double.MAX_VALUE;
				// recalculate from intersect to destination
				while(true) {
					long nodeId = current.getNodeId();
					int timeIndex = startTime + (int)(current.getCost() / OSMParam.SECOND_PER_MINUTE / OSMRouteParam.TIME_INTERVAL);
					if (timeIndex > OSMRouteParam.TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
						timeIndex = OSMRouteParam.TIME_RANGE - 1;
					long nextNodeId = current.getParentId();
					double arriveTime = current.getCost();
					// arrive end
					if(nextNodeId == 0) {
						totalCost = arriveTime;
						break;
					}
					// add node
					reversePath.add(nextNodeId);
					// calculate cost according adjlist
					LinkedList<ToNodeInfo> adjNodeList = OSMData.adjListHashMap.get(nodeId);
					double costTime = 0;
					for(ToNodeInfo toNode : adjNodeList) {
						if(toNode.getNodeId() == nextNodeId) {
							int travelTime;
							// forward searching is time dependent
							if(toNode.isFix())	// fix time
								travelTime = toNode.getTravelTime();
							else	// fetch from time array
								travelTime = toNode.getSpecificTravelTime(dayIndex, timeIndex);
							costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
							break;
						}
					}
					current = nodeReverseCache.get(nextNodeId);
					if(costTime == 0) System.err.println("cost time cannot be zero!");
					else current.setCost(costTime);
				}
				
				// process the left nodes to real destination
				long lastNode = reversePath.get(reversePath.size() - 1); 
				if(lastNode != endNode) {
					NodeInfo last = OSMData.nodeHashMap.get(lastNode);
					NodeInfo dest = OSMData.nodeHashMap.get(endNode);
					EdgeInfo onEdge = last.getEdgeFromNodes(dest);
					current = nodeReverseCache.get(lastNode);
					int totalDistance = onEdge.getDistance();
					int distance;
					long toNodeId;
					if(onEdge.getStartNode() == lastNode) {	// from start to middle
						distance = onEdge.getStartDistance(endNode);
						toNodeId = onEdge.getEndNode();
					}
					else {	// from end to middle
						distance = onEdge.getEndDistance(endNode);
						toNodeId = onEdge.getStartNode();
					}
					LinkedList<ToNodeInfo> adjNodeList = OSMData.adjListHashMap.get(lastNode);
					double costTime = 0;
					int timeIndex = startTime + (int)(totalCost / OSMParam.SECOND_PER_MINUTE / OSMRouteParam.TIME_INTERVAL);
					if (timeIndex > OSMRouteParam.TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
						timeIndex = OSMRouteParam.TIME_RANGE - 1;
					for(ToNodeInfo toNode : adjNodeList) {
						if(toNode.getNodeId() == toNodeId) {
							int travelTime;
							// forward searching is time dependent
							if(toNode.isFix())	// fix time
								travelTime = toNode.getTravelTime();
							else	// fetch from time array
								travelTime = toNode.getSpecificTravelTime(dayIndex, timeIndex);
							costTime = (double)travelTime / OSMParam.MILLI_PER_SECOND;
							break;
						}
					}
					if(costTime != 0) {
						costTime *= (double)distance / totalDistance;
					}
					totalCost += costTime;	// add cost
					reversePath.add(endNode);	// add dest
				}
				
				// if found less cost path, build forward path
				if(totalCost < minCost) {
					ArrayList<Long> forwardPath = new ArrayList<Long>();
					minCost = totalCost;
					current = nodeForwardCache.get(intersect);
					long traceNodeId = current.getParentId();
					while(traceNodeId != 0) {
						forwardPath.add(traceNodeId);	// add node
						current = nodeForwardCache.get(traceNodeId);
						traceNodeId = current.getParentId();
					}
					Collections.reverse(forwardPath);	// reverse the path list
					// record min-cost path, combine forward path and reverse path
					minCostPath = new ArrayList<Long>();
					minCostPath.addAll(forwardPath);
					minCostPath.add(intersect);
					minCostPath.addAll(reversePath);
					// output kml
					//OSMOutput.generatePathKML(nodeHashMap, pathNodeList, "path_" + intersect);
					//ArrayList<Long> intersectNode = new ArrayList<Long>();
					//intersectNode.add(intersect);
					//OSMOutput.generatePathNodeKML(nodeHashMap, intersectNode, "intersect_" + intersect);
				}
			}
			pathNodeList.addAll(minCostPath);
			return minCost;
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("routingHierarchy: debug code " + debug);
		}
		return 0;
		
	}
	
	/**
	 * estimate heuristic, using distance/speed
	 * @param curNode
	 * @param endNode
	 * @return
	 */
	public static double estimateHeuristic(long curNode, long endNode) {
		NodeInfo cur = OSMData.nodeHashMap.get(curNode);
		NodeInfo end = OSMData.nodeHashMap.get(endNode);
		double distance = Geometry.calculateDistance(cur.getLocation(), end.getLocation());
		return distance / OSMRouteParam.HEURISTIC_SPEED * OSMParam.SECOND_PER_HOUR;
	}
	
	/**
	 * initial for routing, if the start node is an intersect, then it should already in the adjlist, than we just
	 * add it. If the start node is not intersect, then the node will not in adjlist, we need find the node's two edge
	 * entrance if the edge is bidirection or one edge entrance if the edge is oneway.
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param dayIndex
	 * @param nodeHelperCache
	 */
	public static PriorityQueue<NodeInfoHelper> initialStartSet(long startNode, long endNode, int startTime, int dayIndex,
			HashMap<Long, NodeInfoHelper> nodeHelperCache) {
		PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 10000, new Comparator<NodeInfoHelper>() {
			public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
				return (int)(n1.getTotalCost() - n2.getTotalCost());
			}
		});
		NodeInfo start = OSMData.nodeHashMap.get(startNode);
		NodeInfoHelper current;
		// initial start end set
		if(start.isIntersect()) {
			// initial
			current = new NodeInfoHelper(startNode);
			current.setCost(0);
			current.setCurrentLevel(10);
			current.setHeuristic(OSMRouting.estimateHeuristic(startNode, endNode));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
		}
		else {
			EdgeInfo edge = start.getOnEdgeList().getFirst();
			double travelTime = 1;	// second
			int distance; // feet
			int totalDistance = edge.getDistance(); // feet
			if(!edge.isOneway()) {
				// distance from start to middle
				distance = edge.getStartDistance(startNode);
				travelTime = edge.getTravelTime(startTime, dayIndex, false);
				travelTime *= (double)distance / totalDistance;
				travelTime /= OSMParam.MILLI_PER_SECOND;
				current = new NodeInfoHelper(edge.getStartNode());
				current.setCost(travelTime);
				current.setCurrentLevel(10);
				current.setHeuristic(OSMRouting.estimateHeuristic(edge.getStartNode(), endNode));
				openSet.offer(current);	// push the start node
				nodeHelperCache.put(current.getNodeId(), current);	// add cache
			}
			// distance from middle to end
			distance = edge.getEndDistance(startNode);
			travelTime = edge.getTravelTime(startTime, dayIndex, true);
			travelTime *= (double)distance / totalDistance;
			current = new NodeInfoHelper(edge.getEndNode());
			current.setCost(travelTime);
			current.setCurrentLevel(10);
			current.setHeuristic(OSMRouting.estimateHeuristic(edge.getEndNode(), endNode));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
		}
		return openSet;
	}
	
	/**
	 * initial the end node set, if the end node is intersect, add it directly
	 * or we find the end node's edge's entrance(s)
	 * @param nodeId
	 * @return
	 */
	public static HashSet<Long> initialEndSet(long nodeId) {
		HashSet<Long> endSet = new HashSet<Long>();
		NodeInfo end = OSMData.nodeHashMap.get(nodeId);
		// initial start end set
		if(end.isIntersect()) {
			endSet.add(nodeId);
		}
		else {
			EdgeInfo edge = end.getOnEdgeList().getFirst();
			if(!edge.isOneway()) {
				endSet.add(edge.getEndNode());
			}
			endSet.add(edge.getStartNode());
		}
		return endSet;
	}
	
	/**
	 * routing using A* algorithm
	 * http://en.wikipedia.org/wiki/A*_search_algorithm
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param dayIndex
	 * @param pathNodeList return path
	 * @return
	 */
	public static double routingAStar(long startNode, long endNode, int startTime, int dayIndex, ArrayList<Long> pathNodeList) {
		// System.out.println("start finding the path...");
		int debug = 0;
		double totalCost = -1;
		try {
			// test store transversal nodes
			//HashSet<Long> transversalSet = new HashSet<Long>();
			
			if(!OSMData.nodeHashMap.containsKey(startNode) || !OSMData.nodeHashMap.containsKey(endNode)) {
				System.err.println("cannot find start or end node!");
				return -1;
			}
			
			if (startNode == endNode) {
				System.out.println("start node is the same as end node.");
				return 0;
			}
			
			HashSet<Long> closedSet = new HashSet<Long>();
			HashMap<Long, NodeInfoHelper> nodeHelperCache = new HashMap<Long, NodeInfoHelper>();
			
			PriorityQueue<NodeInfoHelper> openSet = initialStartSet(startNode, endNode, startTime, dayIndex, nodeHelperCache);
			HashSet<Long> endSet = initialEndSet(endNode);
			
			NodeInfoHelper current = null;
			
			while(!openSet.isEmpty()) {
				// remove current from openset
				current = openSet.poll();
				
				//if(!transversalSet.contains(current.getNodeId()))
				//	transversalSet.add(current.getNodeId());
				
				long nodeId = current.getNodeId();
				// add current to closedset
				closedSet.add(nodeId);
				if(endSet.contains(nodeId)) {	// find the destination
					current = current.getEndNodeHelper(endNode);
					totalCost = current.getCost();
					break;
				}
				// for time dependent routing
				int timeIndex = startTime + (int)(current.getCost() / OSMParam.SECOND_PER_MINUTE / OSMRouteParam.TIME_INTERVAL);
				if (timeIndex > OSMRouteParam.TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = OSMRouteParam.TIME_RANGE - 1;
				LinkedList<ToNodeInfo> adjNodeList = OSMData.adjListHashMap.get(nodeId);
				if(adjNodeList == null) continue;	// this node cannot go anywhere
				double arriveTime = current.getCost();
				// for each neighbor in neighbor_nodes(current)
				for(ToNodeInfo toNode : adjNodeList) {
					debug++;
					long toNodeId = toNode.getNodeId();
					int travelTime;
					if(toNode.isFix())	// fix time
						travelTime = toNode.getTravelTime();
					else	// fetch from time array
						travelTime = toNode.getSpecificTravelTime(dayIndex, timeIndex);
					// tentative_g_score := g_score[current] + dist_between(current,neighbor)
					double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
					// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
					double heuristicTime =  estimateHeuristic(toNodeId, endNode);
					double totalCostTime = costTime + heuristicTime;
					// if neighbor in closedset and tentative_f_score >= f_score[neighbor]
					if(closedSet.contains(toNodeId) && nodeHelperCache.get(toNodeId).getTotalCost() <= totalCostTime) {
						continue;
					}
					NodeInfoHelper node = null;
					// if neighbor not in openset or tentative_f_score < f_score[neighbor]
					if(!nodeHelperCache.containsKey(toNodeId)) {	// neighbor not in openset
						node = new NodeInfoHelper(toNodeId);
						nodeHelperCache.put(node.getNodeId(), node);
					}
					else if (nodeHelperCache.get(toNodeId).getTotalCost() > totalCostTime) {	// neighbor in openset
						node = nodeHelperCache.get(toNodeId);
						if(closedSet.contains(toNodeId)) {	// neighbor in closeset
							closedSet.remove(toNodeId);	// remove neighbor form colseset
						}
						else {
							openSet.remove(node);
						}
					}

					// neighbor need update
					if(node != null) {
						node.setCost(costTime);
						node.setHeuristic(heuristicTime);
						node.setParentId(nodeId);
						openSet.offer(node);	// add neighbor to openset again
					}
				}
			}
			if(totalCost != -1) {
				long traceNodeId = current.getNodeId();
				pathNodeList.add(traceNodeId);	// add end node
				traceNodeId = current.getParentId();
				while(traceNodeId != 0) {
					pathNodeList.add(traceNodeId);	// add node
					current = nodeHelperCache.get(traceNodeId);
					traceNodeId = current.getParentId();
				}
				Collections.reverse(pathNodeList);	// reverse the path list
				// System.out.println("find the path successful!");
			}
			else {
				System.out.println("can not find the path!");
			}
			//OSMOutput.generateTransversalNodeKML(transversalSet, nodeHashMap);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("tdsp: debug code " + debug + ", start node " + startNode + ", end node " + endNode);
		}
		return totalCost;
	}
	
	public static FibonacciHeap<NodeInfoHelper> initialStartSet(long startNode, long endNode,
			HashMap<Long, FibonacciHeapNode<NodeInfoHelper>> nodeHelperCache) {
		FibonacciHeap<NodeInfoHelper> openSet = new FibonacciHeap<NodeInfoHelper>();
		
		NodeInfo start = OSMData.nodeHashMap.get(startNode);
		NodeInfoHelper initial;
		FibonacciHeapNode<NodeInfoHelper> fInitial;
		// initial start end set
		if(start.isIntersect()) {
			// initial
			initial = new NodeInfoHelper(startNode);
			initial.setCost(0);
			initial.setHeuristic(estimateHeuristic(startNode, endNode));
			fInitial = new FibonacciHeapNode<NodeInfoHelper>(initial);
			openSet.insert(fInitial, initial.getTotalCost());	// push the start node
			nodeHelperCache.put(initial.getNodeId(), fInitial);	// add cache
		}
		else {
			EdgeInfo edge = start.getOnEdgeList().getFirst();
			double speed  = edge.getTravelSpeed();
			int travelTime = 1;	// second
			int distance;
			if(!edge.isOneway()) {
				// distance from start to middle
				distance = edge.getStartDistance(startNode);
				travelTime = (int) Math.round(distance / speed * OSMParam.MILLI_PER_SECOND);
				initial = new NodeInfoHelper(edge.getStartNode());
				initial.setCost(travelTime);
				initial.setHeuristic(estimateHeuristic(edge.getStartNode(), endNode));
				fInitial = new FibonacciHeapNode<NodeInfoHelper>(initial);
				openSet.insert(fInitial, initial.getTotalCost());	// push the start node
				nodeHelperCache.put(initial.getNodeId(), fInitial);	// add cache
			}
			distance = edge.getEndDistance(startNode);
			travelTime = (int) Math.round(distance / speed * OSMParam.MILLI_PER_SECOND);
			initial = new NodeInfoHelper(edge.getEndNode());
			initial.setCost(travelTime);
			initial.setHeuristic(estimateHeuristic(edge.getEndNode(), endNode));
			fInitial = new FibonacciHeapNode<NodeInfoHelper>(initial);
			openSet.insert(fInitial, initial.getTotalCost());	// push the start node
			nodeHelperCache.put(initial.getNodeId(), fInitial);	// add cache
		}
		return openSet;
	}
	
	/**
	 * routing using A* algorithm with fibonacci heap
	 * basically same as routingAStar function
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param pathNodeList return path
	 * @return
	 */
	public static double routingAStarFibonacci(long startNode, long endNode, int startTime, int dayIndex, 
			ArrayList<Long> pathNodeList) {
		System.out.println("start finding the path...");
		int debug = 0;
		double totalCost = -1;
		try {
			// test store transversal nodes
			//HashSet<Long> transversalSet = new HashSet<Long>();
			
			if(!OSMData.nodeHashMap.containsKey(startNode) || !OSMData.nodeHashMap.containsKey(endNode)) {
				System.err.println("cannot find start or end node!");
				return -1;
			}
			
			if (startNode == endNode) {
				System.out.println("start node is the same as end node.");
				return 0;
			}
			
			HashSet<Long> closedSet = new HashSet<Long>();
			HashMap<Long, FibonacciHeapNode<NodeInfoHelper>> nodeHelperCache = new HashMap<Long, FibonacciHeapNode<NodeInfoHelper>>();
			
			FibonacciHeap<NodeInfoHelper> openSet = initialStartSet(startNode, endNode, nodeHelperCache);
			HashSet<Long> endSet = initialEndSet(endNode);
			NodeInfoHelper current = null;
			FibonacciHeapNode<NodeInfoHelper> fCurrent = null;
			
			while(!openSet.isEmpty()) {
				// remove current from openset
				fCurrent = openSet.min();
				openSet.removeMin();
				current = fCurrent.getData();
				
				//if(!transversalSet.contains(current.getNodeId()))
				//	transversalSet.add(current.getNodeId());
				
				long nodeId = current.getNodeId();
				// add current to closedset
				closedSet.add(nodeId);
				if(endSet.contains(nodeId)) {	// find the destination
					current = current.getEndNodeHelper(endNode);
					totalCost = current.getCost();
					break;
				}
				// for time dependent routing
				int timeIndex = startTime + (int)(current.getCost() / OSMParam.SECOND_PER_MINUTE / OSMRouteParam.TIME_INTERVAL);
				if (timeIndex > OSMRouteParam.TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = OSMRouteParam.TIME_RANGE - 1;
				LinkedList<ToNodeInfo> adjNodeList = OSMData.adjListHashMap.get(nodeId);
				if(adjNodeList == null) continue;	// this node cannot go anywhere
				double arriveTime = current.getCost();
				// for each neighbor in neighbor_nodes(current)
				for(ToNodeInfo toNode : adjNodeList) {
					debug++;
					long toNodeId = toNode.getNodeId();
					int travelTime;
					if(toNode.isFix())	// fix time
						travelTime = toNode.getTravelTime();
					else	// fetch from time array
						travelTime = toNode.getSpecificTravelTime(dayIndex, timeIndex);
					// tentative_g_score := g_score[current] + dist_between(current,neighbor)
					double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
					// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
					double heuristicTime =  estimateHeuristic(toNodeId, endNode);
					double totalCostTime = costTime + heuristicTime;
					// if neighbor in closedset and tentative_f_score >= f_score[neighbor]
					if(closedSet.contains(toNodeId) && nodeHelperCache.get(toNodeId).getData().getTotalCost() <= totalCostTime) {
						continue;
					}
					NodeInfoHelper node = null;
					FibonacciHeapNode<NodeInfoHelper> fNode = null;
					// if neighbor not in openset or tentative_f_score < f_score[neighbor]
					if(!nodeHelperCache.containsKey(toNodeId)) {	// neighbor not in openset
						// create new one
						node = new NodeInfoHelper(toNodeId);
						node.setCost(costTime);
						node.setHeuristic(heuristicTime);
						node.setParentId(nodeId);
						fNode = new FibonacciHeapNode<NodeInfoHelper>(node);
						openSet.insert(fNode, node.getTotalCost());
						nodeHelperCache.put(node.getNodeId(), fNode);
					}
					else if (nodeHelperCache.get(toNodeId).getData().getTotalCost() > totalCostTime) {	// neighbor in openset
						fNode = nodeHelperCache.get(toNodeId);
						node = fNode.getData();
						// update information
						node.setCost(costTime);
						node.setHeuristic(heuristicTime);
						node.setParentId(nodeId);
						if(closedSet.contains(toNodeId)) {	// neighbor in closeset
							closedSet.remove(toNodeId);	// remove neighbor form colseset
							openSet.insert(fNode, node.getTotalCost());
						}
						else {	// neighbor in openset, decreaseKey
							openSet.decreaseKey(fNode, node.getTotalCost());
						}
					}
				}
			}
			if(totalCost != -1) {
				long traceNodeId = current.getNodeId();
				pathNodeList.add(traceNodeId);	// add end node
				traceNodeId = current.getParentId();
				while(traceNodeId != 0) {
					pathNodeList.add(traceNodeId);	// add node
					fCurrent = nodeHelperCache.get(traceNodeId);
					current = fCurrent.getData();
					traceNodeId = current.getParentId();
				}
				Collections.reverse(pathNodeList);	// reverse the path list
				System.out.println("find the path successful!");
			}
			else {
				System.out.println("can not find the path!");
			}
			//OSMOutput.generateTransversalNodeKML(transversalSet, nodeHashMap);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("tdsp: debug code " + debug + ", start node " + startNode + ", end node " + endNode);
		}
		return totalCost;
	}
	
	/**
	 * get turn by turn text
	 * @param nodeHashMap
	 * @param hierarchyHashMap
	 * @return
	 */
	// TODO : need to fix turn by turn ramp
	public static String turnByTurn(ArrayList<Long> pathNodeList) {
		String turnByTurnText = "";
		int prevHierarchy = -1;
		int prevDirIndex = -1;
		String prevName = "";
		long distance = 0;
		boolean firstRoute = true;
		NodeInfo prevNodeInfo = null;

		if(pathNodeList == null || pathNodeList.size() == 0) {
			return getErrorText() + OSMParam.LINEEND;
		}
		
		for(long nodeId : pathNodeList) {
			if(prevNodeInfo == null) {
				prevNodeInfo = OSMData.nodeHashMap.get(nodeId);
				continue;
			}
			
			NodeInfo nodeInfo = OSMData.nodeHashMap.get(nodeId);
			EdgeInfo edge = prevNodeInfo.getEdgeFromNodes(nodeInfo);
			
			String highway = edge.getHighway();
			String name = edge.getName();
			int hierarchy = 6;
			if(OSMData.hierarchyHashMap.containsKey(highway)) {
				hierarchy = OSMData.hierarchyHashMap.get(highway);
			}

			int dirIndex = Geometry.getDirectionIndex(prevNodeInfo.getLocation(), nodeInfo.getLocation());
			// initial prev
			if(prevHierarchy == -1) {
				prevHierarchy = hierarchy;
				prevDirIndex = dirIndex;
				prevName = name;
			}
			if(prevHierarchy > 1 && hierarchy == 1) {	// from arterial to highway
				if(firstRoute) {
					// first route message
					turnByTurnText = getFirstRouteText(prevDirIndex, prevName, name) + OSMParam.LINEEND;
					firstRoute = false;
				}
				// distance message
				turnByTurnText += getDistanceText(distance) + OSMParam.LINEEND;
				turnByTurnText += getRampText(name) + OSMParam.LINEEND;
				distance = 0;
			}
			else if(prevHierarchy == 1 && hierarchy > 1) {	// from highway to arterial
				if(firstRoute) {
					// first route message
					turnByTurnText = getFirstRouteText(prevDirIndex, prevName, name) + OSMParam.LINEEND;
					firstRoute = false;
				}
				// distance message
				turnByTurnText += getDistanceText(distance) + OSMParam.LINEEND;
				turnByTurnText += getExitText(name) + OSMParam.LINEEND;
				distance = 0;
			}
			else if(prevHierarchy == 1 && hierarchy == 1) {	// on the highway
				if(!prevName.equals(name)) {	// change highway
					if(firstRoute) {
						// first route message
						turnByTurnText = getFirstRouteText(prevDirIndex, prevName, name) + OSMParam.LINEEND;
						firstRoute = false;
					}
					// distance message
					turnByTurnText += getDistanceText(distance) + OSMParam.LINEEND;
					turnByTurnText += getExitText(name) + OSMParam.LINEEND;
					distance = 0;
				}
			}
			else {	// on the arterial
				// change direction or road happen
				if(!Geometry.isSameDirection(dirIndex, prevDirIndex) || !prevName.equals(name)) {
					if(firstRoute) {
						// first route message
						turnByTurnText = getFirstRouteText(prevDirIndex, prevName, name) + OSMParam.LINEEND;
						firstRoute = false;
					}
					// distance message
					turnByTurnText += getDistanceText(distance) + OSMParam.LINEEND;
					if(!Geometry.isSameDirection(dirIndex, prevDirIndex)) {	// change direction
						turnByTurnText += getTurnText(prevDirIndex, dirIndex, prevName, name) + OSMParam.LINEEND;
					}
					else {	// change road
						turnByTurnText += getMergeText(name) + OSMParam.LINEEND;
					}
					distance = 0;
				}
			}
			prevNodeInfo = nodeInfo;
			prevHierarchy = hierarchy;
			prevDirIndex = dirIndex;
			prevName = name;
			distance += edge.getDistance();
		}
		// distance message
		turnByTurnText += getDistanceText(distance) + OSMParam.LINEEND;
		turnByTurnText += getArriveText() + OSMParam.LINEEND;
		return turnByTurnText;
	}
	
	public static String getErrorText() {
		return "Cannot find the path";
	}

	public static String getArriveText() {
		return "Arrive destination";
	}
	
	public static String getExitText(String name) {
		return "Take the exit onto " + name;
	}
	
	public static String getRampText(String name) {
		return "Take the ramp onto " + name;
	}
	
	public static String getTurnText(int prevIndex, int index, String prevName, String name) {
		int turn = Geometry.getTurn(prevIndex, index);
		if(!prevName.equals(name)) {	// also change the road
			return getDirectionText(turn) + "onto " + name;
		}
		else {
			return getDirectionText(turn) + "to stay on " + name;
		}
	}
	
	public static String getDirectionText(int turn) {
		String dirText = "";
		switch(turn) {
			case Geometry.LEFT: 
				dirText = "Turn left ";
				break;
			case Geometry.RIGHT:
				dirText = "Turn right ";
				break;
			case Geometry.SLIGHTLEFT:
				dirText = "Take slight left ";
				break;
			case Geometry.SLIGHTRIGHT:
				dirText = "Take slight right ";
				break;
			case Geometry.SHARPLEFT:
				dirText = "Take sharp  left ";
				break;
			case Geometry.SHARPRIGHT:
				dirText = "Take sharp right ";
				break;
			case Geometry.UTURN:
				dirText = "Take u-turn ";
				break;
		}
		return dirText;
	}
	
	public static String getMergeText(String name) {
		return "Merge onto " + name;
	}
	
	public static String getFirstRouteText(int prevDirIndex, String prevName, String name) {
		return "Head " + Geometry.getDirectionStr(prevDirIndex) + " on " + prevName + " toward " + name;
	}
	
	public static String getDistanceText(long distance) {
		DecimalFormat df = new DecimalFormat("#0.0");
		if(distance >= (OSMParam.FEET_PER_MILE / 10)) {
			return df.format((double)distance / OSMParam.FEET_PER_MILE) + " miles";
		}
		else {
			return distance + " feets";
		}
	}
	
	public static void tdsp() {
		// node list used for store path
		ArrayList<Long> pathNodeList = new ArrayList<Long>();
		
		// test start end node
		//OSMOutput.generateStartEndlNodeKML(START_NODE, END_NODE, nodeHashMap);
		// test count time
		long begintime = System.currentTimeMillis();
		
		LocationInfo startLoc = new LocationInfo(OSMRouteParam.START_LAT, OSMRouteParam.START_LON);
		LocationInfo endLoc = new LocationInfo(OSMRouteParam.END_LAT, OSMRouteParam.END_LON);
		NodeInfo start = startLoc.searchNode();
		NodeInfo end = endLoc.searchNode();
		
		double cost;
		// routing using A* algorithm with priority queue
		//cost = routingAStar(start.getNodeId(), end.getNodeId(), OSMRouteParam.START_TIME, OSMRouteParam.DAY_INDEX, pathNodeList);
		
		// routing using A* algorithm with fibonacci heap
		//cost = routingAStarFibonacci(start.getNodeId(), end.getNodeId(), OSMRouteParam.START_TIME, OSMRouteParam.DAY_INDEX, pathNodeList);
		
		// routing using bidirectional hierarchy search
		cost = routingHierarchy(start.getNodeId(), end.getNodeId(), OSMRouteParam.START_TIME, OSMRouteParam.DAY_INDEX, pathNodeList);
		
		long endtime = System.currentTimeMillis();
		long response = (endtime - begintime);
		System.out.println("routing cost: " + cost + " s, response time: " + response + " ms");
		
		// output kml
		OSMOutput.generatePathKML(pathNodeList);
		//OSMOutput.generatePathNodeKML(pathNodeList);
				
		String turnByTurn = turnByTurn(pathNodeList);
		System.out.println(turnByTurn);
	}

	public static void setParam(double startLat, double startLon, double endLat, double endLon, 
		int startTime, int dayIndex) {
		OSMRouteParam.START_LAT				= startLat;
		OSMRouteParam.START_LON				= startLon;
		OSMRouteParam.END_LAT				= endLat;
		OSMRouteParam.END_LON				= endLon;

		OSMRouteParam.START_TIME 			= startTime;
		OSMRouteParam.DAY_INDEX				= dayIndex;
		OSMRouteParam.TIME_INTERVAL 		= 15;
		OSMRouteParam.TIME_RANGE 			= 60;
		OSMRouteParam.HEURISTIC_SPEED		= 60;
		OSMRouteParam.FOUND_PATH_COUNT 		= 4;
	}
	
	public static void main(String[] args) {
		// set argument:
		// start lat, start lon, end lat, end lon, start time index, start day index
		setParam(34.199705, -117.3663704, 34.38999, -119.159022, 10, 0);
		
		// config
		OSMParam.paramConfig();
		// input
		OSMInput.readNodeFile();
		OSMInput.readEdgeFile();
		// for both adjListHashMap and adjReverseListHashMap
		OSMInput.readAdjList();
		OSMInput.readNodeLocationGrid();
		OSMInput.addOnEdgeToNode();
		
		// JVM memory usage
		System.out.println("JVM total memory usage: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " megabytes");
		
		// test
		//OSMOutput.generateHighwayKML(edgeHashMap, nodeHashMap);
		
		// initial hierarchy level
		OSMParam.initialHierarchy();
		
		// routing, turn by turn
		tdsp();
	}
}
