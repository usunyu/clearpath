package objects;

import java.util.ArrayList;

public class CALinkInfo {
	int linkId;
	int networkId;
	int linkClass;
	boolean rampFlag;
	boolean internalFlag;
	boolean activeFlag;
	int fromNodeId;
	int toNodeId;
	double linkLengthKm;
	int primaryRoadwayId;
	String linkDesc;
	String fromDesc;
	String toDesc;
	double speedLimitKmh;
	PairInfo startLoc;
	PairInfo endLoc;
	PairInfo minLoc;
	PairInfo maxLoc;
	ArrayList<PairInfo> pathPoints;
	// String encodedPolyline;
	double fromProjCompassAngle;
	double toProjCompassAngle;
	String sourceId;
	String sourceRef;
	String tmcCode;

	double[] minSpeedArrayWeekday;
	double[] maxSpeedArrayWeekday;
	double[] averageSpeedArrayWeekday;
	double[] minSpeedArrayWeekend;
	double[] maxSpeedArrayWeekend;
	double[] averageSpeedArrayWeekend;

	public CALinkInfo(int linkId, int networkId, int linkClass,
			boolean rampFlag, boolean internalFlag, boolean activeFlag,
			int fromNodeId, int toNodeId, double linkLengthKm,
			int primaryRoadwayId, String linkDesc, String fromDesc,
			String toDesc, double speedLimitKmh, PairInfo startLoc,
			PairInfo endLoc, PairInfo minLoc, PairInfo maxLoc,
			ArrayList<PairInfo> pathPoints, /* String encodedPolyline, */
			double fromProjCompassAngle, double toProjCompassAngle,
			String sourceId, String sourceRef, String tmcCode) {
		this.linkId = linkId;
		this.networkId = networkId;
		this.linkClass = linkClass;
		this.rampFlag = rampFlag;
		this.internalFlag = internalFlag;
		this.activeFlag = activeFlag;
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
		this.linkLengthKm = linkLengthKm;
		this.primaryRoadwayId = primaryRoadwayId;
		this.linkDesc = linkDesc;
		this.fromDesc = fromDesc;
		this.toDesc = toDesc;
		this.speedLimitKmh = speedLimitKmh;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.minLoc = minLoc;
		this.maxLoc = maxLoc;
		this.pathPoints = pathPoints;
		// this.encodedPolyline = encodedPolyline;
		this.fromProjCompassAngle = fromProjCompassAngle;
		this.toProjCompassAngle = toProjCompassAngle;
		this.sourceId = sourceId;
		this.sourceRef = sourceRef;
		this.tmcCode = tmcCode;
	}

	public CALinkInfo(int linkId, int linkClass,
			int fromNodeId, int toNodeId, PairInfo startLoc,
			PairInfo endLoc, ArrayList<PairInfo> pathPoints, String tmcCode) {
		this.linkId = linkId;
		this.linkClass = linkClass;
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.pathPoints = pathPoints;
		this.tmcCode = tmcCode;
	}

	public void setMinSpeedArrayWeekday(double[] array) {
		minSpeedArrayWeekday = array;
	}

	public double[] getMinSpeedArrayWeekday() {
		return minSpeedArrayWeekday;
	}

	public void setMaxSpeedArrayWeekday(double[] array) {
		maxSpeedArrayWeekday = array;
	}

	public double[] getMaxSpeedArrayWeekday() {
		return maxSpeedArrayWeekday;
	}

	public void setAverageSpeedArrayWeekday(double[] array) {
		averageSpeedArrayWeekday = array;
	}

	public double[] getAverageSpeedArrayWeekday() {
		return averageSpeedArrayWeekday;
	}

	public void setMinSpeedArrayWeekend(double[] array) {
		minSpeedArrayWeekend = array;
	}

	public double[] getMinSpeedArrayWeekend() {
		return minSpeedArrayWeekend;
	}

	public void setMaxSpeedArrayWeekend(double[] array) {
		maxSpeedArrayWeekend = array;
	}

	public double[] getMaxSpeedArrayWeekend() {
		return maxSpeedArrayWeekend;
	}

	public void setAverageSpeedArrayWeekend(double[] array) {
		maxSpeedArrayWeekend = array;
	}

	public double[] getAverageSpeedArrayWeekend() {
		return maxSpeedArrayWeekend;
	}

	/**
	 * Unique identifier
	 */
	public int getLinkId() {
		return linkId;
	}

	/**
	 * Network to which this link belongs
	 */
	public int getNetworkId() {
		return networkId;
	}

	/**
	 * Functional classification of the link.
	 */
	public int getLinkClass() {
		return linkClass;
	}

	/**
	 * Indicates whether the link is a ramp (0 or 1).
	 */
	public boolean getRampFlag() {
		return rampFlag;
	}

	/**
	 * Indicates whether the link is an 'intersection internal' link (0 or 1) -
	 * needed for routing.
	 */
	public boolean getInternalFlag() {
		return internalFlag;
	}

	/**
	 * Indicates whether the link is an active (i.e. routable). Some one way
	 * streets have both directions TMC mapped.
	 */
	public boolean getActiveFlag() {
		return activeFlag;
	}

	/**
	 * Node from which this link originates.
	 */
	public int getFromNodeId() {
		return fromNodeId;
	}

	/**
	 * Node at which this link terminates.
	 */
	public int getToNodeId() {
		return toNodeId;
	}

	/**
	 * The length of the link in kilometers.
	 */
	public double getLinkLengthKm() {
		return linkLengthKm;
	}

	/**
	 * Primary roadway to which this link belongs.
	 */
	public int getPrimaryRoadwayId() {
		return primaryRoadwayId;
	}

	/**
	 * Description of the link (e.g. street name)
	 */
	public String getLinkDesc() {
		return linkDesc;
	}

	/**
	 * Description of the 'from' end of the link (e.g. cross-street name)
	 */
	public String getFromDesc() {
		return fromDesc;
	}

	/**
	 * Description of the 'to' end of the link (e.g. cross-street name)
	 */
	public String getToDesc() {
		return toDesc;
	}

	/**
	 * Speed limit of the link represented as km/h.
	 */
	public double getSpeedLimitKmh() {
		return speedLimitKmh;
	}

	/**
	 * Start Latitude, Longitude (of link origin) in decimal degrees.
	 */
	public PairInfo getStartLoc() {
		return startLoc;
	}

	/**
	 * End Latitude, Longitude (of link terminus) in decimal degrees.
	 */
	public PairInfo getEndLoc() {
		return endLoc;
	}

	/**
	 * Min Latitude, Longitude (of bounding box) in decimal degrees.
	 */
	public PairInfo getMinLoc() {
		return minLoc;
	}

	/**
	 * Max Latitude, Longitude (of bounding box) in decimal degrees.
	 */
	public PairInfo getMaxLoc() {
		return maxLoc;
	}

	/**
	 * Link path expressed as Lat,lng pairs (each pair separated by semicolons).
	 */
	public ArrayList<PairInfo> getPathPoints() {
		return pathPoints;
	}

	/**
	 * Encoded polyline string for Google Maps
	 */
	// public String getEncodedPolyline() {
	// return encodedPolyline;
	//	}

	/**
	 * What angle to project the from (start) node of this link?
	 */
	public double getFromProjCompassAngle() {
		return fromProjCompassAngle;
	}

	/**
	 * What angle to project the to (end) node of this link?
	 */
	public double getToProjCompassAngle() {
		return toProjCompassAngle;
	}

	/**
	 * Source identifier of this link
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Reference for this source identifier (e.g. Navteq, OSM, etc.)
	 */
	public String getSourceRef() {
		return sourceRef;
	}

	/**
	 * Denormalized TMC Code for this Link
	 */
	public String getTmcCode() {
		return tmcCode;
	}
}
