package object;

public class NodeAssistInfo {
	long nodeId;
	int cost;
	boolean visited;
	long parentId;
	
	public NodeAssistInfo(long nodeId) {
		this.nodeId = nodeId;
		this.cost = Integer.MAX_VALUE;
		this.visited = false;
		this.parentId = -1;
	}
	
	public long getNodeId() {
		return nodeId;
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
