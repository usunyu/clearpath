package object;

import java.util.*;

public class HighwayEntrance {
	long entranceNodeId;
	ArrayList<Long> localToHighPath;
	
	public HighwayEntrance(long nodeId) {
		entranceNodeId = nodeId;
	}
	
	public void setLocalToHighPath(ArrayList<Long> path) {
		localToHighPath = path;
	}

	public ArrayList<Long> getLocalToHighPath() {
		return localToHighPath;
	}
}
