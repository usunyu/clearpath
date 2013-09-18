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
	boolean carpool;
	int speedCategory;
	LinkedList<LocationInfo> pointsList;
	
	String allDirection;
	
	LinkedList<SensorInfo> matchSensorList;
	
	public RDFLinkInfo(long linkId) {
		this.linkId = linkId;
	}
	
	public RDFLinkInfo(long linkId, long refNodeId, long nonRefNodeId) {
		this.linkId = linkId;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
	}
	
	public RDFLinkInfo(long linkId, String streetName, long refNodeId, long nonRefNodeId, int functionalClass, 
			String travelDirection, boolean ramp, boolean tollway, boolean carpool, int speedCategory) {
		this.linkId = linkId;
		this.streetName = streetName;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
		this.functionalClass = functionalClass;
		this.travelDirection = travelDirection;
		this.ramp = ramp;
		this.tollway = tollway;
		this.carpool = carpool;
		this.speedCategory = speedCategory ;
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
	
	public boolean isCarpool() {
		return carpool;
	}
	
	public int getSpeedCategory() {
		return speedCategory;
	}
}
