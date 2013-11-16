package model;

import java.text.*;
import java.util.*;

import global.*;
import object.*;

public class OSMProcess {
	
	/**
	 * get the node1's edge, node2 for additional infomation
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static EdgeInfo getEdgeFromNodes(NodeInfo node1, NodeInfo node2) {
		EdgeInfo edge = null;
		if(!node1.isIntersect()) {
			edge = node1.getOnEdgeList().getFirst();
		}
		else {
			// get the common edge
			for(EdgeInfo e1 : node1.getOnEdgeList()) {
				for(EdgeInfo e2 : node2.getOnEdgeList()) {
					if(e1.getId() == e2.getId()) {
						edge = e1;
						break;
					}
				}
				if(edge != null) break;
			}
		}
		return edge;
	}
	
	/**
	 * search the nearest node based on lot and lon
	 * @param nodeHashMap
	 * @param nodeLocationGridMap
	 * @param location
	 * @return
	 */
	public static NodeInfo searchNodeByLocation(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, LinkedList<NodeInfo>> nodeLocationGridMap, LocationInfo location) {
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		DecimalFormat df=new DecimalFormat("0.0");
		String latLonId = df.format(lat) + OSMParam.COMMA + df.format(lon);
		LinkedList<NodeInfo> nodeList = nodeLocationGridMap.get(latLonId);
		// get the nearest node
		double minDis = Double.MAX_VALUE;
		NodeInfo nearestNode = null;
		if(nodeList == null) {	// need to expand
			Stack<String> latLonIdStack = new Stack<String>();
			// case 1
			double newLat = lat - 0.1;
			double newLon = lon - 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// case 2
			newLat = lat + 0.1;
			newLon = lon - 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// case 3
			newLat = lat + 0.1;
			newLon = lon + 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// case 4
			newLat = lat - 0.1;
			newLon = lon + 0.1;
			latLonId = df.format(newLat) + OSMParam.COMMA + df.format(newLon);
			latLonIdStack.push(latLonId);
			// calculate
			while(latLonIdStack.isEmpty()) {
				latLonId = latLonIdStack.pop();
				nodeList = nodeLocationGridMap.get(latLonId);
				if(nodeList == null) continue;
				for(NodeInfo nearNode : nodeList) {
					double dis = Geometry.calculateDistance(location, nearNode.getLocation());
					if(dis < minDis) {
						minDis = dis;
						nearestNode = nearNode;
					}
				}
			}
		}
		else {
			for(NodeInfo nearNode : nodeList) {
				double dis = Geometry.calculateDistance(location, nearNode.getLocation());
				if(dis < minDis) {
					minDis = dis;
					nearestNode = nearNode;
				}
			}
		}
		return nearestNode;
	}
	
	/**
	 * we can find edge by node
	 * @param nodeHashMap
	 * @param edgeHashMap
	 */
	public static void addOnEdgeToNode(HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, EdgeInfo> edgeHashMap) {
		for(EdgeInfo edge : edgeHashMap.values()) {
			LinkedList<Long> nodeList = edge.getNodeList();
			for(long nodeId : nodeList) {
				NodeInfo node = nodeHashMap.get(nodeId);
				node.addOnEdge(edge);
			}
		}
	}
}
