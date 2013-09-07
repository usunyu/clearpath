package object;

public class NodeInfo {
	long nodeId;
	LocationInfo location;
	
	public NodeInfo(long nodeId, LocationInfo location) {
		this.nodeId = nodeId;
		this.location = location;
	}

	public long getNodeId() {
		return nodeId;
	}

	public LocationInfo getLocation() {
		return location;
	}

}
