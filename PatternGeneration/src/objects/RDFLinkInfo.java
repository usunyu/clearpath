package objects;

public class RDFLinkInfo {
	long linkId;
	long refNodeId;
	long nonRefNodeId;
	
	public RDFLinkInfo(long linkId, long refNodeId, long nonRefNodeId) {
		this.linkId = linkId;
		this.refNodeId = refNodeId;
		this.nonRefNodeId = nonRefNodeId;
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
}
