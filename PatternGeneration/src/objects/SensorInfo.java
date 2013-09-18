package objects;

public class SensorInfo {
	int sensorId;
	String onStreet;
	String fromStreet;
	PairInfo node;
	int direction;
	
	LocationInfo location;
	
	public SensorInfo(int sensorId, String onStreet, String fromStreet, LocationInfo location, int direction) {
		this.sensorId = sensorId;
		this.onStreet = onStreet;
		this.fromStreet = fromStreet;
		this.location = location;
		this.direction = direction;
	}
	
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
	public LocationInfo getLocation() { return location; }
	public int getDirection() { return direction; }
}
