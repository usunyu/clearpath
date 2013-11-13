package object;

import java.util.*;

public class NodeInfo {
	long nodeId;
	LocationInfo location;
	LinkedList<EdgeInfo> onEdgeList;
	
	public NodeInfo(long nodeId, LocationInfo location) {
		this.nodeId = nodeId;
		this.location = location;
		this.onEdgeList = new LinkedList<EdgeInfo>();
	}

	public boolean isIntersect() {
		if(onEdgeList.size() >= 2) {
			return true;
		}
		else {
			return false;
		}
	}

	public long getNodeId() {
		return nodeId;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public void addOnEdge(EdgeInfo edge) {
		onEdgeList.add(edge);
	}
	
	public LinkedList<EdgeInfo> getOnEdgeList() {
		return onEdgeList;
	}
}
