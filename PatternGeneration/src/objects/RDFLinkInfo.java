package objects;

import java.util.*;

public class RDFLinkInfo {
	long linkId;
	String streetName;
	long refNodeId;
	long nonRefNodeId;
	int functionalClass;
	String direction;
	boolean ramp;
	boolean tollway;
	boolean carpool;
	int speedCategory;
	LinkedList<LocationInfo> pointsList;
	
	public RDFLinkInfo(long linkId) {
		this.linkId = linkId;
	}
	
	public RDFLinkInfo(long linkId, long refNodeId, long nonRefNodeId) {
		this.linkId = linkId;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
	}
	
	public RDFLinkInfo(long linkId, String streetName, long refNodeId, long nonRefNodeId, int functionalClass, 
			String direction, boolean ramp, boolean tollway, boolean carpool, int speedCategory) {
		this.linkId = linkId;
		this.streetName = streetName;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
		this.functionalClass = functionalClass;
		this.direction = direction;
		this.ramp = ramp;
		this.tollway = tollway;
		this.carpool = carpool;
		this.speedCategory = speedCategory ;
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
	
	public String getDirection() {
		return direction;
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
