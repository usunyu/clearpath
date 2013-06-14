package Objects;

public class DistanceCalculator {

	static public double Radius;
	static public int NORTH = 0;
	static public int SOUTH = 1;
	static public int WEST = 3;
	static public int EAST = 2;

	public static double CalculationByDistance(PairInfo StartP, PairInfo EndP) {
		Radius = 6371 * 0.621371192;
		double lat1 = StartP.getLati();
		double lat2 = EndP.getLati();
		double lon1 = StartP.getLongi();
		double lon2 = EndP.getLongi();
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return Radius * c;
	}

	public static void main(String args[]) {
		System.out.println(CalculationByDistance(new PairInfo(34.28215,
				-118.74368), new PairInfo(34.28214, -118.73976)));
	}
	
	public static int getDirection(PairInfo startNode, PairInfo endNode) {
		double latitude1 = startNode.getLati() * Math.PI / 180.;// coordinate1.Latitude.ToRadian();
		double latitude2 = endNode.getLati() * Math.PI / 180.;// coordinate2.Latitude.ToRadian();
		double longitudeDifference = (endNode.getLongi() - startNode.getLongi())
				* Math.PI / 180.;// (coordinate2.Longitude -
									// coordinate1.Longitude).ToRadian();
		double y = Math.sin(longitudeDifference) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2)
				* Math.cos(longitudeDifference);

		// return Math.atan2(y, x);
		double direction = (Math.atan2(y, x) * 180. / Math.PI + 360) % 360;

		if ((direction >= 0. && direction <= 45.)
				|| (direction > 315. && direction <= 360.))
			return NORTH; // dir = "N";
		else if (direction > 45. && direction <= 135.)
			return EAST;// dir = "E";
		else if (direction > 135. && direction <= 225.)
			return SOUTH;// dir = "S";
		else if (direction > 225. && direction <= 315.)
			return WEST;// dir = "W";

		return -1; // worng case
	}

	public static int getDirection(LinkInfo linkFound) {
		PairInfo[] pairs = linkFound.getNodes();
		PairInfo startNode = pairs[0];
		PairInfo endNode = pairs[linkFound.numPairs - 1];

		double latitude1 = startNode.getLati() * Math.PI / 180.;// coordinate1.Latitude.ToRadian();
		double latitude2 = endNode.getLati() * Math.PI / 180.;// coordinate2.Latitude.ToRadian();
		double longitudeDifference = (endNode.getLongi() - startNode.getLongi())
				* Math.PI / 180.;// (coordinate2.Longitude -
									// coordinate1.Longitude).ToRadian();
		double y = Math.sin(longitudeDifference) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2)
				* Math.cos(longitudeDifference);

		// return Math.atan2(y, x);
		double direction = (Math.atan2(y, x) * 180. / Math.PI + 360) % 360;

		if ((direction >= 0. && direction <= 45.)
				|| (direction > 315. && direction <= 360.))
			return NORTH; // dir = "N";
		else if (direction > 45. && direction <= 135.)
			return EAST;// dir = "E";
		else if (direction > 135. && direction <= 225.)
			return SOUTH;// dir = "S";
		else if (direction > 225. && direction <= 315.)
			return WEST;// dir = "W";

		return -1; // worng case

	}
}
