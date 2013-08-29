package object;

import java.util.*;

public class WayInfo {
	long wayId;
	boolean isOneway;
	String name;
	ArrayList<Long> nodeArrayList;
	
	public WayInfo(long wayId, boolean isOneway, String name, ArrayList<Long> nodeArrayList) {
		this.wayId = wayId;
		this.isOneway = isOneway;
		this.name = name;
		this.nodeArrayList = nodeArrayList;
	}
	
	public long getWayId() {
		return wayId;
	}

	public boolean isOneway() {
		return isOneway;
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
