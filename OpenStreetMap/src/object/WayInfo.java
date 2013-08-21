package object;

import java.util.*;

public class WayInfo {
	long wayId;
	String name;
	ArrayList<Long> nodeArrayList;
	
	public WayInfo(long wayId, String name, ArrayList<Long> nodeArrayList) {
		this.wayId = wayId;
		this.name = name;
		this.nodeArrayList = nodeArrayList;
	}
	
	public long getWayId() {
		return wayId;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Long> getNodeArrayList() {
		return nodeArrayList;
	}
	
	public void setNodeArrayList(ArrayList<Long> nodeArrayList) {
		this.nodeArrayList = nodeArrayList;
	}
}
