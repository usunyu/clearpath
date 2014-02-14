package object;

import java.util.*;

public class WayInfo {
	long wayId;
	boolean isOneway;
	String name;
	String highway;
	ArrayList<Long> nodeArrayList;
	HashMap<String, String> infoHashMap;
	
	public WayInfo(long wayId, boolean isOneway, String name, String highway) {
		this.wayId = wayId;
		this.isOneway = isOneway;
		this.name = name;
		this.highway = highway;
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

	public String getHighway() {
		return highway;
	}
	
	public ArrayList<Long> getNodeArrayList() {
		return nodeArrayList;
	}
	
	public void setNodeArrayList(ArrayList<Long> nodeArrayList) {
		this.nodeArrayList = nodeArrayList;
	}

	public HashMap<String, String> getInfoHashMap() {
		return infoHashMap;
	}

	public void setInfoHashMap(HashMap<String, String> infoHashMap) {
		this.infoHashMap = infoHashMap;
	}
}
