package objects;

import java.util.*;

public class RDFLinkInfo {
	long linkId;
	String streetName;
	long refNodeId;
	long nonRefNodeId;
	int functionalClass;
	String travelDirection;
	boolean ramp;
	boolean tollway;
	boolean carpoolRoad;
	boolean carpools;
	boolean expressLane;
	int speedCategory;
	LinkedList<LocationInfo> pointsList;
	
	String allDirection;
	
	LinkedList<SensorInfo> matchSensorList;
	
	int[] pattern;
	
	public RDFLinkInfo(long linkId) {
		this.linkId = linkId;
	}
	
	public RDFLinkInfo(long linkId, long refNodeId, long nonRefNodeId) {
		this.linkId = linkId;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
	}
	
	public RDFLinkInfo(long linkId, String streetName, long refNodeId, long nonRefNodeId, int functionalClass, 
			String travelDirection, boolean ramp, boolean tollway, boolean carpoolRoad, int speedCategory, 
			boolean carpools, boolean expressLane) {
		this.linkId = linkId;
		this.streetName = streetName;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
		this.functionalClass = functionalClass;
		this.travelDirection = travelDirection;
		this.ramp = ramp;
		this.tollway = tollway;
		this.carpoolRoad = carpoolRoad;
		this.speedCategory = speedCategory;
		this.carpools = carpools;
		this.expressLane = expressLane;
	}
	
	public int[] getPattern() {
		return pattern;
	}
	
	public void setPattern(int[] pattern) {
		this.pattern = pattern;
	}
	
	public boolean containsSensor(SensorInfo sensor) {
		if(matchSensorList == null)
			return false;
		ListIterator<SensorInfo> sensorIterator = matchSensorList.listIterator();
		while(sensorIterator.hasNext()) {
			SensorInfo containedSensor = sensorIterator.next();
			if(containedSensor.getSensorId() == sensor.getSensorId())
				return true;
		}
		return false;
	}

	public void addSensor(SensorInfo sensor) {
		if(matchSensorList == null)
			matchSensorList = new LinkedList<SensorInfo>();
		matchSensorList.add(sensor);
	}

	public LinkedList<SensorInfo> getSensorList() {
		return matchSensorList;
	}
	
	public void setAllDirection(String allDirection) {
		this.allDirection = allDirection;
	}
	
	public String getAllDirection() {
		return allDirection;
	}
	
	public void setPointsList(LinkedList<LocationInfo> pointsList) {
		this.pointsList = pointsList;
	}
	
	public LinkedList<LocationInfo> getPointsList() {
		return pointsList;
	}
	
	public long getLinkId() {
		return linkId;
	}

	public void addStreetName(String secondName) {
		streetName += ";" + secondName;
	}
	
	public String getStreetName() {
		return streetName;
	}
	
	public long getRefNodeId() {
		return refNodeId;
	}
	
	public long getNonRefNodeId() {
		return nonRefNodeId;
	}
	
	public void setFunctionalClass(int functionalClass) {
		this.functionalClass = functionalClass;
	}
	
	public int getFunctionalClass() {
		return functionalClass;
	}
	
	public String getTravelDirection() {
		return travelDirection;
	}
	
	public boolean isRamp() {
		return ramp;
	}
	
	public boolean isTollway() {
		return tollway;
	}
	
	public boolean isCarpoolRoad() {
		return carpoolRoad;
	}
	
	public boolean isCarpools() {
		return carpools;
	}
	
	public boolean isExpressLane() {
		return expressLane;
	}
	
	public int getSpeedCategory() {
		return speedCategory;
	}
}
