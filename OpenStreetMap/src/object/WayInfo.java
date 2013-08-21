package object;

import java.util.*;

public class WayInfo {
	long wayId;
	int version;
	String name;
	ArrayList<Long> nodeArrayList;
	
	public WayInfo(long wayId, int version, String name, ArrayList<Long> nodeArrayList) {
		this.wayId = wayId;
		this.version = version;
		this.name = name;
		this.nodeArrayList = nodeArrayList;
	}
	
	public long getWayId() {
		return wayId;
	}
	
	public int getVersion() {
		return version;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Long> getNodeArrayList() {
		return nodeArrayList;
	}
}
