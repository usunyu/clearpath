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
	
	/**
	 * get current node's edge, another node for additional information
	 * @param node1
	 * @param node2
	 * @return
	 */
	public EdgeInfo getEdgeFromNodes(NodeInfo otherNode) {
		EdgeInfo edge = null;
		if(!isIntersect()) {	// it's not intersect, only one associate edge
			edge = getOnEdgeList().getFirst();
		}
		else {
			// get the common edge
			for(EdgeInfo e1 : getOnEdgeList()) {
				for(EdgeInfo e2 : otherNode.getOnEdgeList()) {
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
}
