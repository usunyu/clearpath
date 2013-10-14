package objects;

import java.util.ArrayList;

public class RDFSignDestInfo {
	long destLinkId;
	int destNumber;
	String exitNumber;
	boolean straightOnSign;
	ArrayList<RDFSignElemInfo> signElemList;
	
	public void addSignElem(RDFSignElemInfo signElem) {
		if(signElemList == null) {
			signElemList = new ArrayList<RDFSignElemInfo>();
		}
		signElemList.add(signElem);
	}
	
	public RDFSignDestInfo(long destLinkId, int destNumber, String exitNumber, boolean straightOnSign) {
		this.destLinkId = destLinkId;
		this.destNumber = destNumber;
		this.exitNumber = exitNumber;
		this.straightOnSign = straightOnSign;
	}
	
	public void setDestNumber(int destNumber) {
		this.destNumber = destNumber;
	}
	
	public int getDestNumber() {
		return destNumber;
	}
	
	public void setDestLinkid(long linkId) {
		destLinkId = linkId;
	}
	
	public long getDestLinkId() {
		return destLinkId;
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
}
