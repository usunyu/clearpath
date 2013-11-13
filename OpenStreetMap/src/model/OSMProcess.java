package model;

import java.text.*;
import java.util.*;

import global.*;
import object.*;

public class OSMProcess {
	public static NodeInfo searchNodeByLocation(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, LinkedList<NodeInfo>> nodeLocationGridMap, LocationInfo location) {
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		DecimalFormat df=new DecimalFormat("0.0");
		String latLonId = df.format(lat) + OSMParam.COMMA + df.format(lon);
		LinkedList<NodeInfo> nodeList = nodeLocationGridMap.get(latLonId);
		// get the nearest node
		double minDis = Double.MAX_VALUE;
		NodeInfo nearestNode = null;
		for(NodeInfo nearNode : nodeList) {
			double dis = Geometry.calculateDistance(location, nearNode.getLocation());
			if(dis < minDis) {
				minDis = dis;
				nearestNode = nearNode;
			}
		}
		return nearestNode;
	}
}
