package controller;

import java.io.*;
import java.text.*;
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
	
	double cost;
	double heuristic;
	long parentId;
	
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
}

/**
 * @data structure used to represent highway entrance
 */
class HighwayEntrance {
	long entranceNodeId;
	// path from source or destination to highway entrance or exit
	ArrayList<Long> localToHighPath;
	double cost;
	double heuristic;
	
	public HighwayEntrance(long nodeId) {
		entranceNodeId = nodeId;
	}
	
	public void setLocalToHighPath(ArrayList<Long> path) {
		localToHighPath = path;
	}

	public ArrayList<Long> getLocalToHighPath() {
		return localToHighPath;
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
}

public class OSMRouting {

	/**
	 * @param param
	 */
	static long START_NODE 		= 673482861;
	static long END_NODE 		= 292697402;
	static int START_TIME 		= 10;
	static int TIME_INTERVAL 	= 15;
	static int TIME_RANGE 		= 60;
	static int HEURISTIC_SPEED	= 60;
	static int ENTRANCE_NUMBER	= 4;
	/**
	 * @param path
	 */
	// highway type : level
	public static HashMap<String, Integer> hierarchyHashMap = new HashMap<String, Integer>();
	static ArrayList<Long> pathNodeList = new ArrayList<Long>();

	public static void main(String[] args) {
		// config
		OSMParam.paramConfig(OSMMain.osm);
		// input
		OSMInput.readNodeFile(OSMData.nodeHashMap);
		OSMInput.readEdgeFile(OSMData.edgeHashMap, OSMData.nodesToEdgeHashMap);
		OSMInput.readAdjList(OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		OSMInput.readNodeLocationGrid(OSMData.nodeHashMap, OSMData.nodeLocationGridMap);
		// test
		//OSMOutput.generateHighwayKML(edgeHashMap, nodeHashMap);
		
		// initial hierarchy level
		initialHierarchy(hierarchyHashMap);
		routing(OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		
		// output
		OSMOutput.generatePathKML(OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, pathNodeList);
		// OSMOutput.generatePathNodeKML(OSMData.nodeHashMap, pathNodeList);
	}
	
	public static void routing(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		// test start end node
		//OSMOutput.generateStartEndlNodeKML(START_NODE, END_NODE, nodeHashMap);
		// test count time
		long begintime = System.currentTimeMillis();
		routingAStar(START_NODE, END_NODE, START_TIME, nodeHashMap, adjListHashMap);
		//routingHierarchy(START_NODE, END_NODE, START_TIME, nodeHashMap, adjListHashMap, adjReverseListHashMap, nodesToEdgeHashMap);
		long endtime = System.currentTimeMillis();
		long costTime = (endtime - begintime);
		System.out.println("routing cost: " + costTime + " ms");
		String turnByTurn = turnByTurn(nodeHashMap, nodesToEdgeHashMap);
		System.out.println(turnByTurn);
	}
	
	public static String turnByTurn(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap) {
		String turnByTurnText = "";
		long prevNodeId = -1;
		int prevHierarchy = -1;
		int prevDirIndex = -1;
		String prevName = "";
		long distance = 0;
		boolean firstRoute = true;
		
		for(long nodeId : pathNodeList) {
			if(prevNodeId == -1) {
				prevNodeId = nodeId;
				continue;
			}
			String nodeIdStr = prevNodeId + OSMParam.COMMA + nodeId;
			EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdStr);
			String highway = edge.getHighway();
			String name = edge.getName();
			int hierarchy = 6;
			if(hierarchyHashMap.containsKey(highway)) {
				hierarchy = hierarchyHashMap.get(highway);
			}
			NodeInfo prevNodeInfo = nodeHashMap.get(prevNodeId);
			NodeInfo nodeInfo = nodeHashMap.get(nodeId);
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
			prevNodeId = nodeId;
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
	
	/**
	 * search 4 highway entrance and exit near source and destination
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param nodeHashMap
	 * @param nodesToEdgeHashMap
	 * @param adjListHashMap
	 * @param exit	search for exit if it is true
	 * @return
	 */
	public static HashMap<Long, HighwayEntrance> searchHighwayEntrance(long startNode, long endNode, int startTime, 
			HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap, 
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, boolean exit) {
		PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 500, new Comparator<NodeInfoHelper>() {
			public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
				return (int)(n1.getTotalCost() - n2.getTotalCost());
			}
		});
		HashSet<Long> closedSet = new HashSet<Long>();
		HashMap<Long, NodeInfoHelper> nodeHelperCache = new HashMap<Long, NodeInfoHelper>();
		HashMap<Long, HighwayEntrance> highwayEntranceMap = new HashMap<Long, HighwayEntrance>();
		
		// initial
		NodeInfoHelper current = new NodeInfoHelper(startNode);
		current.setCost(0);
		current.setHeuristic(estimateHeuristic(startNode, endNode, nodeHashMap));
		openSet.offer(current);	// push the start node
		nodeHelperCache.put(current.getNodeId(), current);	// add cache
		
		// find entrances until reach ENTRANCE_NUMBER
		while(!openSet.isEmpty() && highwayEntranceMap.size() < ENTRANCE_NUMBER) {
			// remove current from openset
			current = openSet.poll();
			long nodeId = current.getNodeId();
			// add current to closedset
			closedSet.add(nodeId);
			if(nodeId == endNode) { return null; }	// we already find destination
			// for time dependent routing
			int timeIndex;
			if(!exit) {
				timeIndex = startTime + (int)current.getCost() / OSMParam.SECOND_PER_MINUTE / TIME_INTERVAL;
				if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = TIME_RANGE - 1;
			}
			else {
				timeIndex = startTime - (int)current.getCost() / OSMParam.SECOND_PER_MINUTE / TIME_INTERVAL;
				if(timeIndex < 0) {
					timeIndex = 0;
				}
			}
			
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null) continue;	// this node cannot go anywhere
			double arriveTime = current.getCost();
			// for each neighbor in neighbor_nodes(current)
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				String nodeIdKey = null;
				if(!exit) { 
					nodeIdKey = nodeId + OSMParam.COMMA + toNodeId; 
				}
				else { 
					nodeIdKey = toNodeId + OSMParam.COMMA + nodeId;
				}
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
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				// tentative_g_score := g_score[current] + dist_between(current,neighbor)
				double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
				// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
				double heuristicTime =  estimateHeuristic(toNodeId, endNode, nodeHashMap);
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
					else {	// neighbor in openset
						openSet.remove(node);
					}
				}
				// neighbor need update
				if(node != null) {
					node.setCost(costTime);
					node.setHeuristic(heuristicTime);
					node.setParentId(nodeId);
					if(hierarchy != 1) {	// highway entrance, we won't transversal it again
						openSet.offer(node);	// add neighbor to openset again
					}
				}
				
				if(hierarchy == 1) {	// find one highway entrance
					ArrayList<Long> entrancePathNodeList = new ArrayList<Long>();
					long traceNodeId = current.getNodeId();
					NodeInfoHelper cur = current;
					while(traceNodeId != 0) {
						entrancePathNodeList.add(traceNodeId);	// add end node
						cur = nodeHelperCache.get(traceNodeId);
						traceNodeId = cur.getParentId();
					}
					
					if(!exit) { 
						Collections.reverse(entrancePathNodeList);
					}
					HighwayEntrance highwayEntrance = new HighwayEntrance(nodeId);
					highwayEntrance.setCost(current.getCost());
					highwayEntrance.setHeuristic(current.getHeuristic());
					highwayEntrance.setLocalToHighPath(entrancePathNodeList);
					highwayEntranceMap.put(nodeId, highwayEntrance);
				}
			}
		}
		return highwayEntranceMap;
	}
	
	/**
	 * hierarchy routing with A* algorithm 
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param nodeHashMap
	 * @param adjListHashMap
	 * @param adjReverseListHashMap
	 * @param nodesToEdgeHashMap
	 * @return
	 */
	public static double routingHierarchy(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap, 
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap,
			HashMap<String, EdgeInfo> nodesToEdgeHashMap) {
		System.out.println("start finding the path...");
		int debug = 0;
		double totalCost = Double.MAX_VALUE;
		try {
			if(!nodeHashMap.containsKey(startNode) || !nodeHashMap.containsKey(endNode)) {
				System.err.println("cannot find start or end node!");
				return -1;
			}
			
			if (startNode == endNode) {
				System.out.println("start node is the same as end node.");
				return 0;
			}
			HashMap<Long, HighwayEntrance> entranceMap = searchHighwayEntrance(startNode, endNode, startTime, nodeHashMap, nodesToEdgeHashMap, adjListHashMap, false);
			int estimateArriveTime = startTime + (int)(estimateHeuristic(startNode, endNode, nodeHashMap) / OSMParam.SECOND_PER_MINUTE / TIME_INTERVAL);
			HashMap<Long, HighwayEntrance> exitMap	= searchHighwayEntrance(endNode, startNode, estimateArriveTime, nodeHashMap, nodesToEdgeHashMap, adjReverseListHashMap, true);
			
			if(entranceMap == null || exitMap == null) {	// we should use normal tdsp in this situation
				return routingAStar(startNode, endNode, startTime, nodeHashMap, adjListHashMap);
			}
			
			// test
			//generateEntranceExitKML(entranceMap, exitMap, nodeHashMap);
			
			long finalEntrance = 0;
			long finalExit = 0;
			// test  for transversal nodes
			//HashSet<Long> transversalSet = new HashSet<Long>();
			
			// iterate each entrance
			for(long entranceId : entranceMap.keySet()) {
				PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 5000, new Comparator<NodeInfoHelper>() {
					public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
						return (int)(n1.getTotalCost() - n2.getTotalCost());
					}
				});
				HashSet<Long> closedSet = new HashSet<Long>();
				HashMap<Long, NodeInfoHelper> nodeHelperCache = new HashMap<Long, NodeInfoHelper>();
				// initial
				HighwayEntrance entrance = entranceMap.get(entranceId);
				NodeInfoHelper current = new NodeInfoHelper(entranceId);
				current.setCost(entrance.getCost());
				current.setHeuristic(estimateHeuristic(entranceId, endNode, nodeHashMap));
				openSet.offer(current);	// push the start node
				nodeHelperCache.put(current.getNodeId(), current);	// add cache
				
				
				LinkedList<NodeInfoHelper> exitNodeList = new LinkedList<NodeInfoHelper>();
				while (!openSet.isEmpty()) {
					debug++;
					// remove current from openset
					current = openSet.poll();
					long nodeId = current.getNodeId();
					// test
					//if(!transversalSet.contains(nodeId))
					//	transversalSet.add(nodeId);
					
					if(exitMap.containsKey(nodeId)) {	// find exit
						exitNodeList.add(current);
						if(exitNodeList.size() == ENTRANCE_NUMBER / 2) break;	// find all four exits
					}
					// for time dependent routing, also need add the time from source to entrance
					int timeIndex = startTime + (int)(entrance.getCost() / OSMParam.SECOND_PER_MINUTE / TIME_INTERVAL + current.getCost() / OSMParam.SECOND_PER_MINUTE / TIME_INTERVAL);
					if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
						timeIndex = TIME_RANGE - 1;
					LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
					if(adjNodeList == null) continue;	// this node cannot go anywhere
					double arriveTime = current.getCost();
					// for each neighbor in neighbor_nodes(current)
					for(ToNodeInfo toNode : adjNodeList) {
						long toNodeId = toNode.getNodeId();
						String nodeIdKey = nodeId + OSMParam.COMMA + toNodeId;
						EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdKey);
						String highway = edge.getHighway();
						int hierarchy = 6;
						if(hierarchyHashMap.containsKey(highway)) {
							hierarchy = hierarchyHashMap.get(highway);
						}
						if(hierarchy != 1) continue;	// keep on in highway
						int travelTime;
						if(toNode.isFix())	// fix time
							travelTime = toNode.getTravelTime();
						else	// fetch from time array
							travelTime = toNode.getTravelTimeArray()[timeIndex];
						// tentative_g_score := g_score[current] + dist_between(current,neighbor)
						double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
						// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
						double heuristicTime =  estimateHeuristic(toNodeId, endNode, nodeHashMap);
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
				
				if(exitNodeList.size() > 0) {
					for(NodeInfoHelper exit : exitNodeList) {
						long exitId = exit.getNodeId();
						NodeInfoHelper node = exit;
						// TODO: from exit to destination, here use reverse calculate, should modify later
						double newCost = node.getCost() + exitMap.get(exitId).getCost();
						
						if(newCost < totalCost) {	// find less cost path
							totalCost = newCost;
							pathNodeList = new ArrayList<Long>();
							long traceNodeId = node.getNodeId();
							while(traceNodeId != 0) {
								pathNodeList.add(traceNodeId);	// add end node
								node = nodeHelperCache.get(traceNodeId);
								traceNodeId = node.getParentId();
							}
							
							Collections.reverse(pathNodeList);
							finalEntrance = entranceId;
							finalExit = exitId;
						}
					}
				}
			}
			
			if(entranceMap.containsKey(finalEntrance) && exitMap.containsKey(finalExit)) {
				HighwayEntrance entrance = entranceMap.get(finalEntrance);
				HighwayEntrance exit = exitMap.get(finalExit);
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
				System.out.println("can not find the path!");
				return -1;
			}
			
			//OSMOutput.generateTransversalNodeKML(transversalSet, nodeHashMap);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("tdspHierarchy: debug code " + debug);
		}
		return totalCost;
	}
	
	public static void generateEntranceExitKML(HashMap<Long, HighwayEntrance> entranceMap, 
			HashMap<Long, HighwayEntrance> exitMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate entrance exit kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.entranceExitFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
			
			for(long nodeId : entranceMap.keySet()) {
				NodeInfo node = nodeHashMap.get(nodeId);
				nodeList.add(node);
			}
			
			for(long nodeId : exitMap.keySet()) {
				NodeInfo node = nodeHashMap.get(nodeId);
				nodeList.add(node);
			}
			
			for(NodeInfo nodeInfo : nodeList) {
				debug++;
				String strLine = "<Placemark>";
				strLine += "<name>" + nodeInfo.getNodeId() + "</name>";
				strLine += "<description>";
				strLine += "Id:" + nodeInfo.getNodeId();
				strLine += "</description>";
				strLine += "<Point><coordinates>";
				strLine += nodeInfo.getLocation().getLongitude() + OSMParam.COMMA + nodeInfo.getLocation().getLatitude();
				strLine += ",0</coordinates></Point>";
				strLine += "</Placemark>";
				
				out.write(strLine);
			}
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generateKMLNode: debug code: " + debug);
		}
		System.out.println("generate entrance exit kml finish!");
	}
	
	/**
	 * estimate heuristic, using distance/speed
	 * @param curNode
	 * @param endNode
	 * @param nodeHashMap
	 * @return
	 */
	public static double estimateHeuristic(long curNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap) {
		NodeInfo cur = nodeHashMap.get(curNode);
		NodeInfo end = nodeHashMap.get(endNode);
		double distance = Geometry.calculateDistance(cur.getLocation(), end.getLocation());
		return distance / HEURISTIC_SPEED * OSMParam.SECOND_PER_HOUR;
	}
	
	public static int getStartDistance(long middle, LinkedList<Long> nodeList, HashMap<Long, NodeInfo> nodeHashMap) {
		long preNodeId = -1;
		double distance = 0;
		for(long nodeId : nodeList) {
			if(preNodeId != -1) {
				NodeInfo node1 = nodeHashMap.get(preNodeId);
				NodeInfo node2 = nodeHashMap.get(nodeId);
				distance += Geometry.calculateDistance(node1.getLocation(), node2.getLocation()) * OSMParam.FEET_PER_MILE;
			}
			preNodeId = nodeId;
			if(nodeId == middle) break;
		}
		return (int)Math.round(distance);
	}
	
	public static int getEndDistance(long middle, LinkedList<Long> nodeList, HashMap<Long, NodeInfo> nodeHashMap) {
		long preNodeId = -1;
		double distance = 0;
		boolean start = false;
		for(long nodeId : nodeList) {
			if(nodeId == middle) {
				start = true;
			}
			if(!start) {
				continue;
			}
			if(preNodeId != -1) {
				NodeInfo node1 = nodeHashMap.get(preNodeId);
				NodeInfo node2 = nodeHashMap.get(nodeId);
				distance += Geometry.calculateDistance(node1.getLocation(), node2.getLocation()) * OSMParam.FEET_PER_MILE;
			}
			preNodeId = nodeId;
		}
		return (int)Math.round(distance);
	}
	
	public static void initialStartSet(long startNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap,
			PriorityQueue<NodeInfoHelper> openSet, HashMap<Long, NodeInfoHelper> nodeHelperCache) {
		NodeInfo start = nodeHashMap.get(startNode);
		NodeInfoHelper current;
		// initial start end set
		if(start.isIntersect()) {
			// initial
			current = new NodeInfoHelper(startNode);
			current.setCost(0);
			current.setHeuristic(estimateHeuristic(startNode, endNode, nodeHashMap));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
		}
		else {
			EdgeInfo edge = start.getOnEdgeList().getFirst();
			LinkedList<Long> nodeList = edge.getNodeList();
			int startDis = getStartDistance(startNode, nodeList, nodeHashMap);
			int endDis = getEndDistance(startNode, nodeList, nodeHashMap);
			double speed  = OSMGenerateAdjList.getTravelSpeed(edge);
			int travelTime = 1;	// second
			// for to end 1
			travelTime = (int) Math.round(startDis / speed * OSMParam.MILLI_PER_SECOND);
			current = new NodeInfoHelper(edge.getStartNode());
			current.setCost(travelTime);
			current.setHeuristic(estimateHeuristic(edge.getStartNode(), endNode, nodeHashMap));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
			// for to end 2
			travelTime = (int) Math.round(endDis / speed * OSMParam.MILLI_PER_SECOND);
			current = new NodeInfoHelper(edge.getEndNode());
			current.setCost(travelTime);
			current.setHeuristic(estimateHeuristic(edge.getEndNode(), endNode, nodeHashMap));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
		}
	}
	
	public static HashSet<Long> getEndSet(long nodeId, HashMap<Long, NodeInfo> nodeHashMap) {
		HashSet<Long> endSet = new HashSet<Long>();
		NodeInfo end = nodeHashMap.get(nodeId);
		// initial start end set
		if(end.isIntersect()) {
			endSet.add(nodeId);
		}
		else {
			endSet.add(end.getOnEdgeList().getFirst().getStartNode());
			endSet.add(end.getOnEdgeList().getFirst().getEndNode());
		}
		return endSet;
	}
	
	/**
	 * routing using A* algorithm
	 * http://en.wikipedia.org/wiki/A*_search_algorithm
	 * @param startNode
	 * @param endNode
	 * @param startTime
	 * @param nodeHashMap
	 * @param adjListHashMap
	 * @return
	 */
	public static double routingAStar(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("start finding the path...");
		int debug = 0;
		double totalCost = -1;
		try {
			// test store transversal nodes
			//HashSet<Long> transversalSet = new HashSet<Long>();
			
			if(!nodeHashMap.containsKey(startNode) || !nodeHashMap.containsKey(endNode)) {
				System.err.println("cannot find start or end node!");
				return -1;
			}
			
			if (startNode == endNode) {
				System.out.println("start node is the same as end node.");
				return 0;
			}
			
			PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 10000, new Comparator<NodeInfoHelper>() {
				public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
					return (int)(n1.getTotalCost() - n2.getTotalCost());
				}
			});
			HashSet<Long> closedSet = new HashSet<Long>();
			HashMap<Long, NodeInfoHelper> nodeHelperCache = new HashMap<Long, NodeInfoHelper>();
			
			initialStartSet(startNode, endNode, nodeHashMap, openSet, nodeHelperCache);
			HashSet<Long> endSet = getEndSet(endNode, nodeHashMap);
			NodeInfoHelper current;
			
			while(!openSet.isEmpty()) {
				// remove current from openset
				current = openSet.poll();
				
				//if(!transversalSet.contains(current.getNodeId()))
				//	transversalSet.add(current.getNodeId());
				
				long nodeId = current.getNodeId();
				// add current to closedset
				closedSet.add(nodeId);
				if(endSet.contains(nodeId)) {	// find the destination
					totalCost = current.getCost();
					break;
				}
				// for time dependent routing
				int timeIndex = startTime + (int)(current.getCost() / OSMParam.SECOND_PER_MINUTE / TIME_INTERVAL);
				if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = TIME_RANGE - 1;
				LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
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
						travelTime = toNode.getTravelTimeArray()[timeIndex];
					// tentative_g_score := g_score[current] + dist_between(current,neighbor)
					double costTime = arriveTime + (double)travelTime / OSMParam.MILLI_PER_SECOND;
					// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
					double heuristicTime =  estimateHeuristic(toNodeId, endNode, nodeHashMap);
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
			// TODO : count the to end cost
			if(totalCost != -1) {
				current = nodeHelperCache.get(endNode);
				long traceNodeId = current.getNodeId();
				while(traceNodeId != 0) {
					pathNodeList.add(traceNodeId);	// add end node
					current = nodeHelperCache.get(traceNodeId);
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
			System.err.println("tdsp: debug code " + debug);
		}
		return totalCost;
	}
}
