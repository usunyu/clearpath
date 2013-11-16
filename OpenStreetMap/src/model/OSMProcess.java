package model;

import java.text.*;
import java.util.*;

import global.*;
import object.*;

public class OSMProcess {

	/**
	 * get speed by edge info
	 * @param edgeInfo
	 * @return
	 */
	public static double getTravelSpeed(EdgeInfo edgeInfo) {
		// initial
		double speed  = (double) 10 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);	// feet/second
		// define all kinds of highway type link's speed
		if (edgeInfo.getHighway().equals(OSMParam.MOTORWAY)) {
			speed = (double) 60 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(edgeInfo.getHighway().equals(OSMParam.TRUNK)) {
			speed = (double) 50 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.MOTORWAY_LINK)) {
			speed = (double) 30 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(edgeInfo.getHighway().equals(OSMParam.TRUNK_LINK)) {
			speed = (double) 25 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.CYCLEWAY) || edgeInfo.getHighway().equals(OSMParam.TURNING_CIRCLE) ||
				edgeInfo.getHighway().equals(OSMParam.TRACK) || edgeInfo.getHighway().equals(OSMParam.PROPOSED) ||
				edgeInfo.getHighway().equals(OSMParam.ROAD) || edgeInfo.getHighway().equals(OSMParam.SCALE)) {
			speed = (double) 10 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.UNKNOWN_HIGHWAY) || edgeInfo.getHighway().equals(OSMParam.CONSTRUCTION) || 
				edgeInfo.getHighway().equals(OSMParam.ABANDONED)) {
			speed = (double) 5 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(edgeInfo.getHighway().equals(OSMParam.RESIDENTIAL)) {
			speed = (double) 20 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(edgeInfo.getHighway().equals(OSMParam.UNCLASSIFIED)) {
			speed = (double) 25 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.TERTIARY)) {
			speed = (double) 35 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.TERTIARY_LINK)) {
			speed = (double) 30 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.SECONDARY)) {
			speed = (double) 40 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.SECONDARY_LINK)) {
			speed = (double) 35 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.PRIMARY)) {
			speed = (double) 45 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (edgeInfo.getHighway().equals(OSMParam.PRIMARY_LINK)) {
			speed = (double) 40 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		return speed;
	}
	
	/**
	 * get travel time by edge info
	 * @param edgeInfo
	 * @return
	 */
	public static int getTravelTime(EdgeInfo edgeInfo) {
		// initial
		double speed  = getTravelSpeed(edgeInfo);
		int travelTime = 1;	// second
		travelTime = (int) Math.round(edgeInfo.getDistance() / speed * OSMParam.MILLI_PER_SECOND);
		// travelTime cannot be zero
		if (travelTime <= 0) {
			travelTime = 1;
		}
		return travelTime;
	}
	
	/**
	 * whether the edge is fix by edge info
	 * @param edgeInfo
	 * @return
	 */
	public static boolean isFixEdge(EdgeInfo edgeInfo) {
		boolean isFix = true;
		if (edgeInfo.getHighway().equals(OSMParam.MOTORWAY) || edgeInfo.getHighway().equals(OSMParam.TRUNK) ||
				edgeInfo.getHighway().equals(OSMParam.MOTORWAY_LINK) || edgeInfo.getHighway().equals(OSMParam.TRUNK_LINK)) {
			isFix = false;
		}
		return isFix;
	}
	
	/**
	 * get the node1's edge, node2 for additional infomation
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static EdgeInfo getEdgeFromNodes(NodeInfo node1, NodeInfo node2) {
		EdgeInfo edge = null;
		if(!node1.isIntersect()) {
			edge = node1.getOnEdgeList().getFirst();
		}
		else {
			// get the common edge
			for(EdgeInfo e1 : node1.getOnEdgeList()) {
				for(EdgeInfo e2 : node2.getOnEdgeList()) {
					if(e1.getId() == e2.getId()) {
						edge = e1;
						break;
					}
				}
				if(edge != null) break;
			}
		}
		return edge;
	}
	
	/**
	 * search the nearest node based on lot and lon
	 * @param nodeHashMap
	 * @param nodeLocationGridMap
	 * @param location
	 * @return
	 */
	public static NodeInfo searchNodeByLocation(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, LinkedList<NodeInfo>> nodeLocationGridMap, LocationInfo location) {
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		DecimalFormat df=new DecimalFormat("0.0");
		String latLonId = df.format(lat) + OSMParam.COMMA + df.format(lon);
		LinkedList<NodeInfo> nodeList = nodeLocationGridMap.get(latLonId);
		// get the nearest node
		double minDis = Double.MAX_VALUE;
		NodeInfo nearestNode = null;
		if(nodeList == null) {	// need to expand
			Stack<String> latLonIdStack = new Stack<String>();
			// case 1
			double newLat = lat - 0.1;
			double newLon = lon - 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// case 2
			newLat = lat + 0.1;
			newLon = lon - 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// case 3
			newLat = lat + 0.1;
			newLon = lon + 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// case 4
			newLat = lat - 0.1;
			newLon = lon + 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// calculate
			while(latLonIdStack.isEmpty()) {
				latLonId = latLonIdStack.pop();
				nodeList = nodeLocationGridMap.get(latLonId);
				if(nodeList == null) continue;
				for(NodeInfo nearNode : nodeList) {
					double dis = Geometry.calculateDistance(location, nearNode.getLocation());
					if(dis < minDis) {
						minDis = dis;
						nearestNode = nearNode;
					}
				}
			}
		}
		else {
			for(NodeInfo nearNode : nodeList) {
				double dis = Geometry.calculateDistance(location, nearNode.getLocation());
				if(dis < minDis) {
					minDis = dis;
					nearestNode = nearNode;
				}
			}
		}
		return nearestNode;
	}
	
	/**
	 * we can find edge by node
	 * @param nodeHashMap
	 * @param edgeHashMap
	 */
	public static void addOnEdgeToNode(HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, EdgeInfo> edgeHashMap) {
		for(EdgeInfo edge : edgeHashMap.values()) {
			LinkedList<Long> nodeList = edge.getNodeList();
			for(long nodeId : nodeList) {
				NodeInfo node = nodeHashMap.get(nodeId);
				node.addOnEdge(edge);
			}
		}
	}
}
