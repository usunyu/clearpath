package Objects;

public class SensorInfo {
	int sensorId;
	String onStreet;
	String fromStreet;
	PairInfo node;
	int direction;
	int affected;
	
	public SensorInfo(int sensorId, String onStreet, String fromStreet, PairInfo node, int direction, int affected) {
		this.sensorId = sensorId;
		this.onStreet = onStreet;
		this.fromStreet = fromStreet;
		this.node = node;
		this.direction = direction;
		this.affected = affected;
	}
	
	public int getSensorId() { return sensorId; }
	public String getOnStreet() { return onStreet; }
	public String getFromStreet() { return fromStreet; }
	public PairInfo getNode() { return node; }
	public int getDirection() { return direction; }
	public int getAffected() {return affected; }
}
