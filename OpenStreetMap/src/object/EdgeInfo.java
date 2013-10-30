package object;

import java.util.*;

public class EdgeInfo {
	long wayId;
	int edgeId;
	String name;
	String highway;
	LinkedList<Long> nodeList;
	int distance;
	
	public EdgeInfo(long wayId, int edgeId, String name, String highway, LinkedList<Long> nodeList, int distance) {
		this.wayId = wayId;
		this.edgeId = edgeId;
		this.name = name;
		this.highway = highway;
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

	public String getHighway() {
		return highway;
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
}
