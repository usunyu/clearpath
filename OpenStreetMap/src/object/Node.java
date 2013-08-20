package object;

public class Node {
	int nodeId;
	Location location;
	
	public Node(int nodeId, Location location) {
		this.nodeId = nodeId;
		this.location = location;
	}

	public int getNodeId() {
		return nodeId;
	}

	public Location getLocation() {
		return location;
	}
}
