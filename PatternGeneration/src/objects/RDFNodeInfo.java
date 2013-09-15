package objects;

public class RDFNodeInfo {
	long nodeId;
	PairInfo location;

	public RDFNodeInfo(long nodeId, PairInfo location) {
		this.nodeId = nodeId;
		this.location = location;
	}

	public long getNodeId() {
		return nodeId;
	}

	public PairInfo getLocation() {
		return location;
	}
}
