package object;

import java.util.*;

public class ToNodeInfo {
	long nodeId;
	// for seven days' pattern
	ArrayList<ArrayList<Integer>> travelTimeArray;
	int travelTime;
	int minTravelTime;
	boolean fix;
	
	public ToNodeInfo(Long nodeId, ArrayList<ArrayList<Integer>> travelTimeArray) {
		this.nodeId = nodeId;
		this.travelTimeArray = travelTimeArray;
		fix = false;
	}
	 
	public ToNodeInfo(Long nodeId, int travelTime) {
		this.nodeId = nodeId;
		this.travelTime = travelTime;
		fix = true;
	}

	public ToNodeInfo(Long nodeId) {
		this.nodeId = nodeId;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	public boolean isFix() {
		return fix;
	}

	// in millisecond
	public void setTravelTimeArray(ArrayList<ArrayList<Integer>> travelTimeArray) {
		this.travelTimeArray = travelTimeArray;
	}
	
	// in millisecond
	public ArrayList<ArrayList<Integer>> getTravelTimeArray() {
		return travelTimeArray;
	}

	// in millisecond
	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}
	
	// in millisecond
	public int getTravelTime() {
		return travelTime;
	}

	// in millisecond
	public void setMinTravelTime(int minTravelTime) {
		this.minTravelTime = minTravelTime;
	}

	// in millisecond
	public int getMinTravelTime() {
		return minTravelTime;
	}

	public int getSpecificTravelTime(int day, int time) {
		return travelTimeArray.get(day).get(time);
	}
}
