package objects;

public class RDFLinkInfo {
	long linkId;
	long refNodeId;
	long nonRefNodeId;
	int functionalClass;
	
	public RDFLinkInfo(long linkId, long refNodeId, long nonRefNodeId, int functionalClass) {
		this.linkId = linkId;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
		this.functionalClass = functionalClass;
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
