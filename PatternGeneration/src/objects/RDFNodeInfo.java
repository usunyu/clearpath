package objects;

public class RDFNodeInfo {
	long nodeId;
	LocationInfo location;
	
	// for routing
	int cost;
	boolean visited;
	long parentId;

	public RDFNodeInfo(long nodeId) {
		this.nodeId = nodeId;
	}

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

	public void setLocation(LocationInfo location) {
		this.location = location;
	}
	
	public void prepareRoute() {
		this.cost = Integer.MAX_VALUE;
		this.visited = false;
		this.parentId = -1;
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
}
