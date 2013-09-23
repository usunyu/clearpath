package objects;

public class Geometry {
	static public double Radius = 6371 * 0.621371192;
	static public int NORTH = 0;
	static public int SOUTH = 1;
	static public int WEST = 3;
	static public int EAST = 2;
	
	static public int LEFT = 0;
	static public int RIGHT = 1;
	static public int UTURN = 2;

	public static double calculateDistance(LocationInfo start, LocationInfo end) {
		double lat1 = start.getLatitude();
		double lat2 = end.getLatitude();
		double lon1 = start.getLongitude();
		double lon2 = end.getLongitude();
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return Radius * c;
	}
	
	public static int getDirection(LocationInfo start, LocationInfo end) {
		double latitude1 = start.getLatitude() * Math.PI / 180.;
		// coordinate1.Latitude.ToRadian();
		double latitude2 = end.getLatitude() * Math.PI / 180.;
		// coordinate2.Latitude.ToRadian();
		double longitudeDifference = (end.getLongitude() - start.getLongitude()) * Math.PI / 180.;
		// (coordinate2.Longitude - coordinate1.Longitude).ToRadian();
		double y = Math.sin(longitudeDifference) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2)
				* Math.cos(longitudeDifference);

		// return Math.atan2(y, x);
		double direction = (Math.atan2(y, x) * 180. / Math.PI + 360) % 360;

		if ((direction >= 0. && direction <= 45.) || (direction > 315. && direction <= 360.))
			return NORTH; // dir = "N";
		else if (direction > 45. && direction <= 135.)
			return EAST;// dir = "E";
		else if (direction > 135. && direction <= 225.)
			return SOUTH;// dir = "S";
		else if (direction > 225. && direction <= 315.)
			return WEST;// dir = "W";

		return -1; // worng case
	}
	
	public static int getTurn(int dir1, int dir2) {
		int turn = -1;
		switch(dir1) {
			case 0:	// NORTH
				switch(dir2) {
				case 1:
					turn = UTURN;
					break;
				case 2:
					turn = RIGHT;
					break;
				case 3:
					turn = LEFT;
					break;
				}
				break;
			case 1:	// SOUTH
				switch(dir2) {
				case 0:
					turn = UTURN;
					break;
				case 2:
					turn = LEFT;
					break;
				case 3:
					turn = RIGHT;
					break;
				}
				break;
			case 2:	// EAST
				switch(dir2) {
				case 0:
					turn = LEFT;
					break;
				case 1:
					turn = RIGHT;
					break;
				case 3:
					turn = UTURN;
					break;
				}
				break;
			case 3:	// WEST
				switch(dir2) {
				case 0:
					turn = RIGHT;
					break;
				case 1:
					turn = LEFT;
					break;
				case 2:
					turn = UTURN;
					break;
				}
				break;
		}
		return turn;
	}
}
