package objects;

public class RDFNodeInfo {
	long nodeId;
	LocationInfo location;

	public RDFNodeInfo(long nodeId, LocationInfo location) {
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
