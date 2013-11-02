package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

/**
 * @data structure used to represent highway entrance
 */
class HighwayEntrance {
	long entranceNodeId;
	// path from source or destination to highway entrance or exit
	ArrayList<Long> localToHighPath;
	int cost;
	int heuristic;
	
	public HighwayEntrance(long nodeId) {
		entranceNodeId = nodeId;
	}
	
	public void setLocalToHighPath(ArrayList<Long> path) {
		localToHighPath = path;
	}

	public ArrayList<Long> getLocalToHighPath() {
		return localToHighPath;
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
	public static HashMap<String, Integer> hierarchyHashMap = new HashMap<String, Integer>();
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
		OSMOutput.generatePathKML(OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, pathNodeList);
		OSMOutput.generatePathNodeKML(OSMData.nodeHashMap, pathNodeList);
	}
	
	public static void tdsp(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		// count time
		long begintime = System.currentTimeMillis();
		//tdsp(START_NODE, END_NODE, START_TIME, nodeHashMap, adjListHashMap);
		tdspHierarchy(START_NODE, END_NODE, START_TIME, nodeHashMap, adjListHashMap, adjReverseListHashMap, nodesToEdgeHashMap);
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
				return n1.getTotalCost() - n2.getTotalCost();
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
		
		// find four entrance
		while(!openSet.isEmpty() && highwayEntranceMap.size() < 4) {
			// remove current from openset
			current = openSet.poll();
			long nodeId = current.getNodeId();
			// add current to closedset
			closedSet.add(nodeId);
			if(nodeId == endNode) { return null; }	// we already find destination
			// for time dependent routing
			int timeIndex;
			if(!exit) {
				timeIndex = startTime + current.getCost() / 60 / TIME_INTERVAL;
				if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = TIME_RANGE - 1;
			}
			else {
				timeIndex = startTime - current.getCost() / 60 / TIME_INTERVAL;
				if(timeIndex < 0) {
					timeIndex = 0;
				}
			}
			
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null) continue;	// this node cannot go anywhere
			int arriveTime = current.getCost();
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
				int costTime = arriveTime + travelTime;
				// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
				int heuristicTime =  estimateHeuristic(toNodeId, endNode, nodeHashMap);
				int totalCostTime = costTime + heuristicTime;
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
					NodeInfoHelper entrance = current;
					while(entrance.getParentId() != 0) {
						entrancePathNodeList.add(entrance.getNodeId());	// add end node
						entrance = nodeHelperCache.get(entrance.getParentId());
					}
					if(!exit) { 
						Collections.reverse(entrancePathNodeList);
					}
					HighwayEntrance highwayEntrance = new HighwayEntrance(nodeId);
					highwayEntrance.setCost(current.getCost());
					highwayEntrance.setHeuristic(current.getHeuristic());
					highwayEntrance.setLocalToHighPath(entrancePathNodeList);
					highwayEntranceMap.put(nodeId, highwayEntrance);
					break;	// not transversal other connected node from current
				}
			}
		}
		return highwayEntranceMap;
	}
	
	public static int tdspHierarchy(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap, 
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap,
			HashMap<String, EdgeInfo> nodesToEdgeHashMap) {
		System.out.println("start finding the path...");
		if(!nodeHashMap.containsKey(startNode) || !nodeHashMap.containsKey(endNode)) {
			System.err.println("cannot find start or end node!");
			return -1;
		}
		
		if (startNode == endNode) {
			System.out.println("start node is the same as end node.");
			return 0;
		}
		HashMap<Long, HighwayEntrance> entranceMap = searchHighwayEntrance(startNode, endNode, startTime, nodeHashMap, nodesToEdgeHashMap, adjListHashMap, false);
		int estimateArriveTime = startTime + estimateHeuristic(startNode, endNode, nodeHashMap) / 60 / TIME_INTERVAL;
		HashMap<Long, HighwayEntrance> exitMap	= searchHighwayEntrance(endNode, startNode, estimateArriveTime, nodeHashMap, nodesToEdgeHashMap, adjReverseListHashMap, true);
		
		if(entranceMap == null || exitMap == null) {	// we should use normal tdsp in this situation
			return tdsp(startNode, endNode, startTime, nodeHashMap, adjListHashMap);
		}
		
		// test
		generateEntranceExitKML(startNode, endNode, entranceMap, exitMap, nodeHashMap);
		
		int totalCost = Integer.MAX_VALUE;
		long finalEntrance = 0;
		long finalExit = 0;
		
		// iterate each entrance
		for(long entranceId : entranceMap.keySet()) {
			PriorityQueue<NodeInfoHelper> openSet = new PriorityQueue<NodeInfoHelper>( 5000, new Comparator<NodeInfoHelper>() {
				public int compare(NodeInfoHelper n1, NodeInfoHelper n2) {
					return n1.getTotalCost() - n2.getTotalCost();
				}
			});
			HashSet<Long> closedSet = new HashSet<Long>();
			HashMap<Long, NodeInfoHelper> nodeHelperCache = new HashMap<Long, NodeInfoHelper>();
			// initial
			NodeInfoHelper current = new NodeInfoHelper(entranceId);
			current.setCost(0);
			current.setHeuristic(estimateHeuristic(entranceId, endNode, nodeHashMap));
			openSet.offer(current);	// push the start node
			nodeHelperCache.put(current.getNodeId(), current);	// add cache
			HighwayEntrance entrance = entranceMap.get(entranceId);
			
			LinkedList<NodeInfoHelper> exitNodeList = new LinkedList<NodeInfoHelper>();
			while (!openSet.isEmpty()) {
				// remove current from openset
				current = openSet.poll();
				long nodeId = current.getNodeId();
				if(exitMap.containsKey(nodeId)) {	// find exit
					exitNodeList.add(current);
					//if(exitNodeList.size() == 4) break;	// find all four exits
					break;
				}
				// for time dependent routing, also need add the time from source to entrance
				int timeIndex = startTime + entrance.getCost() / 60 / TIME_INTERVAL + current.getCost() / 60 / TIME_INTERVAL;
				if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
					timeIndex = TIME_RANGE - 1;
				LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
				if(adjNodeList == null) continue;	// this node cannot go anywhere
				int arriveTime = current.getCost();
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
					int costTime = arriveTime + travelTime;
					// tentative_f_score := tentative_g_score + heuristic_cost_estimate(neighbor, goal)
					int heuristicTime =  estimateHeuristic(toNodeId, endNode, nodeHashMap);
					int totalCostTime = costTime + heuristicTime;
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
					int newCost = node.getCost() + entranceMap.get(entranceId).getCost() + exitMap.get(exitId).getCost();
					
					if(newCost < totalCost) {	// find less cost path
						totalCost = newCost;
						pathNodeList = new ArrayList<Long>();
						while(node.getParentId() != 0) {
							pathNodeList.add(node.getNodeId());
							node = nodeHelperCache.get(node.getParentId());
						}
						Collections.reverse(pathNodeList);
						finalEntrance = entranceId;
						finalExit = exitId;
					}
				}
			}
		}
		
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
		
		return totalCost;
	}
	
	public static void generateEntranceExitKML(long start, long end, HashMap<Long, HighwayEntrance> entranceMap, 
			HashMap<Long, HighwayEntrance> exitMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate entrance exit kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.entranceExitFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
			NodeInfo startNode = nodeHashMap.get(start);
			NodeInfo endNode = nodeHashMap.get(end);
			
			nodeList.add(startNode);
			nodeList.add(endNode);
			
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
	
	public static int estimateHeuristic(long curNode, long endNode, HashMap<Long, NodeInfo> nodeHashMap) {
		NodeInfo cur = nodeHashMap.get(curNode);
		NodeInfo end = nodeHashMap.get(endNode);
		double distance = Distance.calculateDistance(cur.getLocation(), end.getLocation());
		return (int) (distance / 60 * OSMParam.SECOND_PER_HOUR);
	}
	
	// using A* algorithm
	// http://en.wikipedia.org/wiki/A*_search_algorithm
	public static int tdsp(long startNode, long endNode, int startTime, HashMap<Long, NodeInfo> nodeHashMap,
			HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("start finding the path...");
		
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
				return n1.getTotalCost() - n2.getTotalCost();
			}
		});
		HashSet<Long> closedSet = new HashSet<Long>();
		HashMap<Long, NodeInfoHelper> nodeHelperCache = new HashMap<Long, NodeInfoHelper>();
		
		// initial
		NodeInfoHelper current = new NodeInfoHelper(startNode);
		current.setCost(0);
		current.setHeuristic(estimateHeuristic(startNode, endNode, nodeHashMap));
		openSet.offer(current);	// push the start node
		nodeHelperCache.put(current.getNodeId(), current);	// add cache
		
		int totalCost = -1;
		
		while(!openSet.isEmpty()) {
			// remove current from openset
			current = openSet.poll();
			
			//if(!transversalSet.contains(current.getNodeId()))
			//	transversalSet.add(current.getNodeId());
			
			long nodeId = current.getNodeId();
			// add current to closedset
			closedSet.add(nodeId);
			if(nodeId == endNode) {	// find the destination
				totalCost = current.getCost();
				break;
			}
			// for time dependent routing
			int timeIndex = startTime + current.getCost() / 60 / TIME_INTERVAL;
			if (timeIndex > TIME_RANGE - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = TIME_RANGE - 1;
			LinkedList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null) continue;	// this node cannot go anywhere
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
		
		current = nodeHelperCache.get(endNode);
		while(current.getParentId() != 0) {
			pathNodeList.add(current.getNodeId());	// add end node
			current = nodeHelperCache.get(current.getParentId());
		}
		Collections.reverse(pathNodeList);	// reverse the path list
		
		//OSMOutput.generateTransversalNodeKML(transversalSet, nodeHashMap);
		
		System.out.println("find the path successful!");
		return totalCost;
	}
}
