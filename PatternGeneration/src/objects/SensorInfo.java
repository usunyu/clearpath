package objects;

public class SensorInfo {
	int sensorId;
	String onStreet;
	String fromStreet;
	PairInfo node;
	int direction;
	
	public SensorInfo(int sensorId, String onStreet, String fromStreet, PairInfo node, int direction) {
		this.sensorId = sensorId;
		this.onStreet = onStreet;
		this.fromStreet = fromStreet;
		this.node = node;
		this.direction = direction;
	}
	
	public int getSensorId() { return sensorId; }
	public String getOnStreet() { return onStreet; }
	public String getFromStreet() { return fromStreet; }
	public PairInfo getNode() { return node; }
	public int getDirection() { return direction; }
}
