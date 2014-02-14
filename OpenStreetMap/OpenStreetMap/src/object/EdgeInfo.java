package object;

import java.util.*;

import global.*;

public class EdgeInfo {
	long wayId;
	int edgeId;
	String name;
	int highwayId;
	boolean isOneway;
	LinkedList<Long> nodeList;
	int distance;
	
	public EdgeInfo(long wayId, int edgeId, String name, int highwayId, boolean isOneway, LinkedList<Long> nodeList, int distance) {
		this.wayId = wayId;
		this.edgeId = edgeId;
		this.name = name;
		this.highwayId = highwayId;
		this.isOneway = isOneway;
		this.nodeList = nodeList;
		this.distance = distance;
	}
	
	public long getWayId() {
		return wayId;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public long getId() {
		return wayId * 1000 + edgeId;
	}
	
	public String getName() {
		return name;
	}

	public int getHighwayId() {
		return highwayId;
	}

	public String getHighway() {
		return OSMData.edgeHighwayTypeList.get(highwayId);
	}

	public Long getStartNode() {
		return nodeList.getFirst();
	}

	public Long getEndNode() {
		return nodeList.getLast();
	}
	
	public int getDistance() {
		return distance;
	}

	public LinkedList<Long> getNodeList() {
		return nodeList;
	}

	public boolean isOneway() {
		return isOneway;
	}
	
	/**
	 * whether the edge is fix, any fix type can be add here
	 * @param edgeInfo
	 * @return
	 */
	public boolean isFix() {
		boolean isFix = true;
		if (getHighway().equals(OSMParam.MOTORWAY) || getHighway().equals(OSMParam.TRUNK) ||
				getHighway().equals(OSMParam.MOTORWAY_LINK) || getHighway().equals(OSMParam.TRUNK_LINK)) {
			isFix = false;
		}
		return isFix;
	}
	
	/**
	 * get speed by edge info, only for local(time independent)
	 * @param edgeInfo
	 * @return
	 */
	public double getTravelSpeed() {
		// initial
		double speed  = (double) 10 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);	// feet/second
		// define all kinds of highway type link's speed
		if (getHighway().equals(OSMParam.MOTORWAY)) {
			speed = (double) 60 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(getHighway().equals(OSMParam.TRUNK)) {
			speed = (double) 50 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.MOTORWAY_LINK)) {
			speed = (double) 30 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(getHighway().equals(OSMParam.TRUNK_LINK)) {
			speed = (double) 25 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.CYCLEWAY) || getHighway().equals(OSMParam.TURNING_CIRCLE) ||
				getHighway().equals(OSMParam.TRACK) || getHighway().equals(OSMParam.PROPOSED) ||
				getHighway().equals(OSMParam.ROAD) || getHighway().equals(OSMParam.SCALE)) {
			speed = (double) 10 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.UNKNOWN_HIGHWAY) || getHighway().equals(OSMParam.CONSTRUCTION) || 
				getHighway().equals(OSMParam.ABANDONED)) {
			speed = (double) 5 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(getHighway().equals(OSMParam.RESIDENTIAL)) {
			speed = (double) 20 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if(getHighway().equals(OSMParam.UNCLASSIFIED)) {
			speed = (double) 25 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.TERTIARY)) {
			speed = (double) 35 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.TERTIARY_LINK)) {
			speed = (double) 30 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.SECONDARY)) {
			speed = (double) 40 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.SECONDARY_LINK)) {
			speed = (double) 35 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.PRIMARY)) {
			speed = (double) 45 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		if (getHighway().equals(OSMParam.PRIMARY_LINK)) {
			speed = (double) 40 * OSMParam.FEET_PER_MILE / (OSMParam.SECOND_PER_HOUR);
		}
		return speed;
	}
	
	/**
	 * get travel time by edge info,  only for local(time independent)
	 * @param edgeInfo
	 * @return
	 */
	public int getTravelTime() {
		// initial
		double speed  = getTravelSpeed();
		int travelTime = 1;	// second
		travelTime = (int) Math.round(getDistance() / speed * OSMParam.MILLI_PER_SECOND);
		// travelTime cannot be zero
		if (travelTime <= 0) {
			travelTime = 1;
		}
		return travelTime;
	}
	
	/**
	 * get travel time by edge's from node and to node (time dependent)
	 * @param forward indicate calculate forward or reverse
	 * @param timeIndex
	 * @return
	 */
	public int getTravelTime(int time, int day, boolean forward) {
		int travelTime = Integer.MAX_VALUE;	// second, initial MAX
		long from, to;
		if(forward) {
			from = getStartNode();
			to = getEndNode();
		}
		else {
			from = getEndNode();
			to = getStartNode();
		}
		LinkedList<ToNodeInfo> toNodeList = OSMData.adjListHashMap.get(from);
		for(ToNodeInfo toNode : toNodeList) {
			if(toNode.getNodeId() == to) {
				if(toNode.isFix()) {
					travelTime = toNode.getTravelTime();
				}
				else {
					travelTime = toNode.getSpecificTravelTime(day, time);
				}
				break;
			}
		}
		return travelTime;
	}
	
	/**
	 * get low bound of travel time by edge's from node and to node
	 * @param forward indicate calculate forward or reverse
	 * @return
	 */
	public int getTravelTimeMin(boolean forward) {
		int travelTime = Integer.MAX_VALUE;	// second, initial MAX
		long from, to;
		if(forward) {
			from = getStartNode();
			to = getEndNode();
		}
		else {
			from = getEndNode();
			to = getStartNode();
		}
		LinkedList<ToNodeInfo> toNodeList = OSMData.adjListHashMap.get(from);
		for(ToNodeInfo toNode : toNodeList) {
			if(toNode.getNodeId() == to) {
				travelTime = toNode.getMinTravelTime();
				break;
			}
		}
		return travelTime;
	}
	
	/**
	 * get the part of edge's distance, from start to middle
	 * @param middle
	 * @return
	 */
	public int getStartDistance(long middle) {
		long preNodeId = -1;
		double distance = 0;
		for(long nodeId : nodeList) {
			if(preNodeId != -1) {
				NodeInfo node1 = OSMData.nodeHashMap.get(preNodeId);
				NodeInfo node2 = OSMData.nodeHashMap.get(nodeId);
				distance += Geometry.calculateDistance(node1.getLocation(), node2.getLocation()) * OSMParam.FEET_PER_MILE;
			}
			preNodeId = nodeId;
			if(nodeId == middle) break;
		}
		return (int)Math.round(distance);
	}
	
	/**
	 * get the part of edge's distance, from middle to end
	 * @param middle
	 * @return
	 */
	public int getEndDistance(long middle) {
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
				NodeInfo node1 = OSMData.nodeHashMap.get(preNodeId);
				NodeInfo node2 = OSMData.nodeHashMap.get(nodeId);
				distance += Geometry.calculateDistance(node1.getLocation(), node2.getLocation()) * OSMParam.FEET_PER_MILE;
			}
			preNodeId = nodeId;
		}
		return (int)Math.round(distance);
	}
}
