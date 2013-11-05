package object;

public class ToNodeInfo {
	long nodeId;
	int[] travelTimeArray;
	int travelTime;
	boolean fix;
	
	public ToNodeInfo(Long nodeId, int[] travelTimeArray) {
		this.nodeId = nodeId;
		this.travelTimeArray = travelTimeArray;
		fix = false;
	}
	 
	public ToNodeInfo(Long nodeId, int travelTime) {
		this.nodeId = nodeId;
		this.travelTime = travelTime;
		fix = true;
	}

	public ToNodeInfo(Long nodeId) {
		this.nodeId = nodeId;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	public boolean isFix() {
		return fix;
	}

	// in millisecond
	public void setTravelTimeArray(int[] travelTimeArray) {
		this.travelTimeArray = travelTimeArray;
	}
	
	// in millisecond
	public int[] getTravelTimeArray() {
		return travelTimeArray;
	}

	// in millisecond
	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}
	
	// in millisecond
	public int getTravelTime() {
		return travelTime;
	}
}
