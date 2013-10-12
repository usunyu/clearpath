package objects;

import java.util.*;

public class RDFLinkInfo {
	long linkId;
	long refNodeId;
	long nonRefNodeId;
	
	String baseName;
	LinkedList<String> streetNameList;
	LinkedList<String> additionalNameList;
	
	int functionalClass;
	String travelDirection;
	boolean ramp;
	boolean tollway;
	boolean exitName;
	int speedCategory;
	int accessId;
	
	LinkedList<LocationInfo> pointList;
	LinkedList<RDFLaneInfo> laneList;
	LinkedList<Integer> directionList;
	LinkedList<SensorInfo> matchSensorList;
	
	int[] pattern;
	
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
	
	public void addPoint(LocationInfo point) {
		if(pointList == null)
			pointList = new LinkedList<LocationInfo>();
		pointList.add(point);
	}
	
	public LinkedList<LocationInfo> getPointList() {
		return pointList;
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
	
	public LinkedList<RDFLaneInfo> getLaneList() {
		return laneList;
	}
	
	public void addLane(RDFLaneInfo lane) {
		if(laneList == null)
			laneList = new LinkedList<RDFLaneInfo>();
		laneList.add(lane);
	}

	public void addDirection(Integer direction) {
		if(directionList == null)
			directionList = new LinkedList<Integer>();
		directionList.add(direction);
	}

	public LinkedList<Integer> getDirectionList() {
		return directionList;
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
	
	public int getSpeedCategory() {
		return speedCategory;
	}
	
	public void setSpeedCategory(int speedCategory) {
		this.speedCategory = speedCategory;
	}

	public boolean isExitName() {
		return exitName;
	}

	public void setExitName(boolean exitName) {
		if(exitName == true) {
			this.exitName = exitName;
		}
	}
	
	public boolean isCarpool() {
		if(laneList == null)
			return false;
		for(RDFLaneInfo lane : laneList) {
			if(lane.getLaneType() == 2)
				return true;
		}
		return false;
	}
}
