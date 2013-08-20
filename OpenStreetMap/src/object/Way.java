package object;

import java.util.*;

public class Way {
	int wayId;
	ArrayList<Node> nodeArrayList;
	
	public Way(int wayId, ArrayList<Node> nodeArrayList) {
		this.wayId = wayId;
		this.nodeArrayList = nodeArrayList;
	}
	
	public int getWayId() {
		return wayId;
	}
	
	public ArrayList<Node> getNodeArrayList() {
		return nodeArrayList;
	}
}
