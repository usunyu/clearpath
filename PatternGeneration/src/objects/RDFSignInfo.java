package objects;

public class RDFSignInfo {
	long signId;
	long originLinkId;
	long destLinkId;
	String exitNumber;
	boolean straightOnSign;
	String textType;
	String text;
	String directionCode;
	
	public RDFSignInfo(long signId) {
		this.signId = signId;
	}
	
	public long getSignId() {
		return signId;
	}
	
	public void setOriginLinkId(long linkId) {
		originLinkId = linkId;
	}
	
	public long getOriginLinkId() {
		return originLinkId;
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

	public void setTextType(String textType) {
		this.textType = textType;
	}
	
	public String getTextType() {
		return textType;
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
