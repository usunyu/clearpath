package objects;

public class RDFSignInfo {
	long signId;
	long origLinkId;
	long destLinkId;
	String exitNumber;
	boolean straightOnSign;
	String text;
	String directionCode;
	
	public RDFSignInfo(long signId) {
		this.signId = signId;
	}
	
	public long getSignId() {
		return signId;
	}
	
	public void setOrigLinkId(long linkId) {
		origLinkId = linkId;
	}
	
	public long getOrigLinkId() {
		return origLinkId;
	}
	
	public void setDestLinkid(long linkId) {
		destLinkId = linkId;
	}
	
	public long getDestLinkId(long linkId) {
		return linkId;
	}
	
	public void setExitNumber(String exitNumber) {
		this.exitNumber = exitNumber;
	}
	
	public String getExitNumber() {
		return exitNumber;
	}
	
	public void setStraightOnSign(boolean straightOnSign) {
		this.straightOnSign = straightOnSign;
	}
	
	public boolean isStraightOnSign() {
		return straightOnSign;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setDirectionCode(String directionCode) {
		this.directionCode = directionCode;
	}
	
	public String getDirectionCode() {
		return directionCode;
	}
}
