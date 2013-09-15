package objects;

import java.util.*;

public class RDFLinkInfo {
	long linkId;
	long refNodeId;
	long nonRefNodeId;
	int functionalClass;
	LinkedList<LocationInfo> pointsList;
	
	public RDFLinkInfo(long linkId, long refNodeId, long nonRefNodeId/*, int functionalClass*/) {
		this.linkId = linkId;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
		//this.functionalClass = functionalClass;
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
	
	public long getRefNodeId() {
		return refNodeId;
	}
	
	public long getNonRefNodeId() {
		return nonRefNodeId;
	}
	
	public int getFunctionalClass() {
		return functionalClass;
	}
}
