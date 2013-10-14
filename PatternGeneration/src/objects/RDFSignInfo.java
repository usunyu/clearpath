package objects;

import java.util.*;

public class RDFSignInfo {
	long signId;
	long originLinkId;
	ArrayList<RDFSignDestInfo> signDestList;
	
	public RDFSignInfo(long signId, long originLinkId) {
		this.signId = signId;
		this.originLinkId = originLinkId;
	}
	
	public void addSignElem(RDFSignElemInfo signElem) {
		int destNumber = signElem.getDestNumber();
		RDFSignDestInfo targetSignDest = signDestList.get(destNumber - 1);
		targetSignDest.addSignElem(signElem);
	}
	
	public void addSignDest(RDFSignDestInfo signDest) {
		if(signDestList == null) {
			signDestList = new ArrayList<RDFSignDestInfo>();
		}
		boolean add = false;
		// insert sort
		for(int i = 0; i < signDestList.size(); i++) {
			RDFSignDestInfo curSignDest = signDestList.get(i);
			if(signDest.getDestNumber() < curSignDest.getDestNumber()) {
				signDestList.add(i, signDest);
				add = true;
			}
		}
		if(!add) {
			signDestList.add(signDest);
		}
	}
	
	public ArrayList<RDFSignDestInfo> getSignDestList() {
		return signDestList;
	}
	
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
}
