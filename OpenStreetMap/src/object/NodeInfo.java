package object;

public class NodeInfo {
	long nodeId;
	LocationInfo location;

	int cost;
	boolean visited;
	long parentId;
	
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

	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public int getCost() {
		return cost;
	}

	public boolean isVisited() {
		return visited;
	}
	
	public void setVisited() {
		visited = true;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	public long getParentId() {
		return parentId;
	}

	public void prepareRoute() {
		this.cost = Integer.MAX_VALUE;
		this.visited = false;
		this.parentId = -1;
	}
}
