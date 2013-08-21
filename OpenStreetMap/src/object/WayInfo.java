package object;

import java.util.*;

public class WayInfo {
	long wayId;
	ArrayList<Long> nodeArrayList;
	
	public WayInfo(long wayId, ArrayList<Long> nodeArrayList) {
		this.wayId = wayId;
		this.nodeArrayList = nodeArrayList;
	}
	
	public long getWayId() {
		return wayId;
	}
	
	public ArrayList<Long> getNodeArrayList() {
		return nodeArrayList;
	}
}
