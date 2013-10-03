package objects;

import java.util.*;

public class RDFLinkInfo {
	long linkId;
	String baseName;
	LinkedList<String> streetNameList;
	LinkedList<String> additionalNameList;
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
	
	int accessId;
	
	LinkedList<Integer> allDirection;
	
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
	
	public int getAccessId() {
		return accessId;
	}
	
	public void setAccessId(int accessId) {
		this.accessId = accessId;
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
	
	public void setAllDirection(LinkedList<Integer> allDirection) {
		this.allDirection = allDirection;
	}
	
	public LinkedList<Integer> getAllDirection() {
		return allDirection;
	}
	
	public void addPoint(LocationInfo point) {
		if(pointsList == null)
			pointsList = new LinkedList<LocationInfo>();
		pointsList.add(point);
	}
	
	public LinkedList<LocationInfo> getPointsList() {
		return pointsList;
	}
	
	public long getLinkId() {
		return linkId;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getBaseName() {
		return baseName;
	}

	public void addStreetName(String streetName, boolean isNameOnRoadsign) {
		if(isNameOnRoadsign) {
			if(streetNameList == null) {
				streetNameList = new LinkedList<String>();
			}
			streetNameList.add(streetName);
		}
		else {
			if(additionalNameList == null) {
				additionalNameList = new LinkedList<String>();
			}
			additionalNameList.add(streetName);
		}
	}
	
	public LinkedList<String> getStreetNameList() {
		return streetNameList;
	}
	
	public LinkedList<String> getAdditionalNameList() {
		return additionalNameList;
	}
	
	public long getRefNodeId() {
		return refNodeId;
	}
	
	public void setRefNodeId(long refNodeId) {
		this.refNodeId = refNodeId;
	}
	
	public long getNonRefNodeId() {
		return nonRefNodeId;
	}
	
	public void setNonRefNodeId(long nonRefNodeId) {
		this.nonRefNodeId = nonRefNodeId;
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
	
	public void  setTravelDirection(String travelDirection) {
		this.travelDirection = travelDirection;
	}
	
	public boolean isRamp() {
		return ramp;
	}
	
	public void setRamp(boolean ramp) {
		this.ramp = ramp;
	}
	
	public boolean isTollway() {
		return tollway;
	}
	
	public void setTollway(boolean tollway) {
		this.tollway = tollway;
	}
	
	public boolean isCarpoolRoad() {
		return carpoolRoad;
	}
	
	public void setCarpoolRoad(boolean carpoolRoad) {
		this.carpoolRoad = carpoolRoad;
	}
	
	public boolean isCarpools() {
		return carpools;
	}
	
	public void setCarpools(boolean carpools) {
		this.carpools = carpools;
	}
	
	public boolean isExpressLane() {
		return expressLane;
	}
	
	public void isExpressLane(boolean expressLane) {
		this.expressLane = expressLane;
	}
	
	public int getSpeedCategory() {
		return speedCategory;
	}
	
	public void setSpeedCategory(int speedCategory) {
		this.speedCategory = speedCategory;
	}
}
