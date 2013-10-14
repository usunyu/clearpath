package objects;

import java.util.*;

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
	
	public static double calculateDistance(LinkedList<LocationInfo> pointsList) {
		ListIterator<LocationInfo> iterator = pointsList.listIterator();
		LocationInfo preLocation = null;
		double distance = 0;
		while(iterator.hasNext()) {
			LocationInfo location = iterator.next();
			if(preLocation == null) {
				preLocation = location;
				continue;
			}
			distance += calculateDistance(preLocation, location);
			preLocation = location;
		}
		return distance;
	}

	public static double getAngel(LocationInfo start, LocationInfo end) {
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

		return direction;
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
	
	public static int getDirectionIndex(LocationInfo start, LocationInfo end) {
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
		
		if(direction >= 0 && direction < 22.5)
			return 0;
		else if(direction >= 22.5 && direction < 45)
			return 1;
		else if(direction >= 45 && direction < 67.5)
			return 2;
		else if(direction >= 67.5 && direction < 90)
			return 3;
		else if(direction >= 90 && direction < 112.5)
			return 4;
		else if(direction >= 112.5 && direction < 135)
			return 5;
		else if(direction >= 135 && direction < 157.5)
			return 6;
		else if(direction >= 157.5 && direction < 180)
			return 7;
		else if(direction >= 180 && direction < 202.5)
			return 8;
		else if(direction >= 202.5 && direction < 225)
			return 9;
		else if(direction >= 225 && direction < 247.5)
			return 10;
		else if(direction >= 247.5 && direction < 270)
			return 11;
		else if(direction >= 270 && direction < 292.5)
			return 12;
		else if(direction >= 292.5 && direction < 315)
			return 13;
		else if(direction >= 315 && direction < 337.5)
			return 14;
		else if(direction >= 337.5 && direction < 360)
			return 15;

		return -1; // worng case
	}
	
	public static int getTurn(int dirIndex1, int dirIndex2) {
		int turn = -1;
		int indexDif = Math.abs(dirIndex2 - dirIndex1);
		if(indexDif >= 4 && indexDif < 8) {
			if(dirIndex2 > dirIndex1)
				turn = RIGHT;
			else
				turn = LEFT;
		}
		else if(indexDif == 8) {
			turn = UTURN;
		}
		else if(indexDif > 8 && indexDif <= 12) {
			if(dirIndex2 > dirIndex1)
				turn = LEFT;
			else
				turn = RIGHT;
		}
		return turn;
	}
	
	public static boolean isSameDirection(int dir1, int dir2) {
		int dirDif = Math.abs(dir1 - dir2);
		if(dirDif <= 2 || dirDif >= 14)
			return true;
		return false;
	}
	
	public static String getDirectionStr(int dirIndex) {
		String dirStr = "";
		if(dirIndex == 15 || dirIndex == 0)
			dirStr = "north";
		else if(dirIndex == 1 || dirIndex == 2)
			dirStr = "northeast";
		else if(dirIndex == 3 || dirIndex == 4)
			dirStr = "east";
		else if(dirIndex == 5 || dirIndex == 6)
			dirStr = "eastsouth";
		else if(dirIndex == 7 || dirIndex == 8)
			dirStr = "south";
		else if(dirIndex == 9 || dirIndex == 10)
			dirStr = "southwest";
		else if(dirIndex == 11 || dirIndex == 12)
			dirStr = "west";
		else if(dirIndex == 13 || dirIndex == 14)
			dirStr = "westnorth";
		return dirStr;
	}
}
