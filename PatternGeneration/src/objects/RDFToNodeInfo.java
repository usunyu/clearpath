package objects;

public class RDFToNodeInfo {
	long nodeId;
	int[] travelTimeArray;
	int travelTime;
	boolean fix;
	
	public RDFToNodeInfo(long nodeId, int[] travelTimeArray) {
		this.nodeId = nodeId;
		this.travelTimeArray = travelTimeArray;
		fix = false;
	}
	 
	public RDFToNodeInfo(long nodeId, int travelTime) {
		this.nodeId = nodeId;
		this.travelTime = travelTime;
		fix = true;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	public boolean isFix() {
		return fix;
	}
	
	public int[] getTravelTimeArray() {
		return travelTimeArray;
	}
	
	public int getTravelTime() {
		return travelTime;
	}
}
