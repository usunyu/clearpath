package object;

import java.util.*;

public class EdgeInfo {
	long wayId;
	int edgeId;
	//boolean isOneway;
	String name;
	String highway;
	Long startNode;
	Long endNode;
	int distance;
	
	public EdgeInfo(long wayId, int edgeId, /*boolean isOneway,*/ String name, String highway, Long startNode, Long endNode, int distance) {
		this.wayId = wayId;
		this.edgeId = edgeId;
		//this.isOneway = isOneway;
		this.name = name;
		this.highway = highway;
		this.startNode = startNode;
		this.endNode = endNode;
		this.distance = distance;
	}
	
	public long getWayId() {
		return wayId;
	}

	public int getEdgeId() {
		return edgeId;
	}

	// public boolean isOneway() {
	// 	return isOneway;
	// }
	
	public String getName() {
		return name;
	}

	public String getHighway() {
		return highway;
	}

	public Long getStartNode() {
		return startNode;
	}

	public Long getEndNode() {
		return endNode;
	}
	
	public int getDistance() {
		return distance;
	}
}
