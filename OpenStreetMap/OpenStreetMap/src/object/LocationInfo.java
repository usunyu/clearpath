package object;

import global.*;
import java.text.*;
import java.util.*;

public class LocationInfo {
	double latitude, longitude;
	
	public LocationInfo(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * search the nearest node based on lot and lon
	 * @return
	 */
	public NodeInfo searchNode() {
		DecimalFormat df=new DecimalFormat("0.0");
		String latLonId = df.format(latitude) + OSMParam.COMMA + df.format(longitude);
		LinkedList<NodeInfo> nodeList = OSMData.nodeLocationGridMap.get(latLonId);
		// get the nearest node
		double minDis = Double.MAX_VALUE;
		NodeInfo nearestNode = null;
		if(nodeList == null) {	// need to expand
			Stack<String> latLonIdStack = new Stack<String>();
			double newLat, newLon;
			int expand = 5;
			// case 1
			for(int i = 0; i <= expand; i++) {
				newLat = latitude - 0.1 * i;
				for(int j = 0; j <= expand; j++) {
					newLon = longitude - 0.1 * j;
					latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
					latLonIdStack.push(latLonId);
				}
			}
			// case 2
			for(int i = 0; i <= expand; i++) {
				newLat = latitude + 0.1 * i;
				for(int j = 0; j <= expand; j++) {
					newLon = longitude - 0.1 * j;
					latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
					latLonIdStack.push(latLonId);
				}
			}
			// case 3
			for(int i = 0; i <= expand; i++) {
				newLat = latitude + 0.1 * i;
				for(int j = 0; j <= expand; j++) {
					newLon = longitude + 0.1 * j;
					latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
					latLonIdStack.push(latLonId);
				}
			}
			// case 4
			for(int i = 0; i <= 5; i++) {
				newLat = latitude - 0.1 * i;
				for(int j = 0; j <= expand; j++) {
					newLon = longitude + 0.1 * j;
					latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
					latLonIdStack.push(latLonId);
				}
			}
			// calculate
			while(!latLonIdStack.isEmpty()) {
				latLonId = latLonIdStack.pop();
				nodeList = OSMData.nodeLocationGridMap.get(latLonId);
				if(nodeList == null) continue;
				for(NodeInfo nearNode : nodeList) {
					double dis = Geometry.calculateDistance(this, nearNode.getLocation());
					if(dis < minDis) {
						minDis = dis;
						nearestNode = nearNode;
					}
				}
			}
		}
		else {
			for(NodeInfo nearNode : nodeList) {
				double dis = Geometry.calculateDistance(this, nearNode.getLocation());
				if(dis < minDis) {
					minDis = dis;
					nearestNode = nearNode;
				}
			}
		}
		return nearestNode;
	}
}
