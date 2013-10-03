package objects;

public class RDFLaneInfo {
	long laneId;
	String travelDirection;
	int laneType;
	int accessId;
	
	public RDFLaneInfo(long laneId, String travelDirection, int laneType, int accessId) {
		this.laneId = laneId;
		this.travelDirection = travelDirection;
		this.laneType = laneType;
		this.accessId = accessId;
	}
	
	public long getLaneId() {
		return laneId;
	}
	
	public String getTravelDirection() {
		return travelDirection;
	}
	
	public int getLaneType() {
		return laneType;
	}
	
	public int getAccessId() {
		return accessId;
	}
}
