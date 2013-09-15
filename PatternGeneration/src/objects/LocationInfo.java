package objects;

public class LocationInfo {
	double latitude, longitude;
	int zLevel;
	
	public LocationInfo(double latitude, double longitude, int zLevel) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.zLevel = zLevel;
	}
	
	public int getZLevel() {
		return zLevel;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
}