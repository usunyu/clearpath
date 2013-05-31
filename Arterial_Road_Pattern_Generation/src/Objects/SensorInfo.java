package Objects;

public class SensorInfo {
	int sensorId;
	String onStreet;
	String fromStreet;
	int direction;
	
	public SensorInfo(int sensorId, String onStreet, String fromStreet, int direction) {
		this.sensorId = sensorId;
		this.onStreet = onStreet;
		this.fromStreet = fromStreet;
		this.direction = direction;
	}
	
	public int getSensorId() { return sensorId; }
	public String getOnStreet() { return onStreet; }
	public String getFromStreet() { return fromStreet; }
	public int getDirection() { return direction; }
}
