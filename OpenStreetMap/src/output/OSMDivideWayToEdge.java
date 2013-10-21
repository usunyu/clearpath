package output;

import java.util.*;

import object.*;
import function.*;

public class OSMDivideWayToEdge {
	/**
	 * @param node
	 */
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	static HashMap<Long, WayInfo> wayHashMap = new HashMap<Long, WayInfo>();
	/**
	 * @param edge
	 */
	static HashMap<Long, EdgeInfo> edgeHashMap = new HashMap<Long, EdgeInfo>();
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMInput.paramConfig(args[0]);
		OSMInput.readNodeFile(nodeHashMap);
		OSMInput.readWayFile(wayHashMap);
		OSMInput.readWktsFile(wayHashMap, nodeHashMap);

		divideWayToEdge();
		OSMOutput.paramConfig(args[0]);
		OSMOutput.writeEdgeFile(edgeHashMap);
	}
	
	public static void divideWayToEdge() {
		System.out.println("divide way to edge...");
		
		for(WayInfo wayInfo : wayHashMap.values()) {
			long wayId = wayInfo.getWayId();
			boolean isOneway = wayInfo.isOneway();
			String name = wayInfo.getName();
			String highway = wayInfo.getHighway();
			ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
			int edgeId = 0;
			
			long preNodeId = 0;
			for(int j = 0; j < localNodeArrayList.size(); j++) {
				long nodeId = localNodeArrayList.get(j);
				if(j >= 1) {
					long startNode = preNodeId;
					long endNode = nodeId;
					NodeInfo nodeInfo1 = nodeHashMap.get(startNode);
					NodeInfo nodeInfo2 = nodeHashMap.get(endNode);
					// feet
					double dDistance = Distance.calculateDistance(nodeInfo1.getLocation(), nodeInfo2.getLocation()) * 5280;
					int distance = (int)Math.round(dDistance);
					EdgeInfo edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, startNode, endNode, distance);
					long id = wayId * 1000 + edgeId;
					edgeHashMap.put(id, edgeInfo);
					edgeId++;
					// use two edges to denote bidirection edge here
					if(!isOneway) {
						edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, endNode, startNode, distance);
						id = wayId * 1000 + edgeId;
						edgeHashMap.put(id, edgeInfo);
						edgeId++;
					}
				}
				preNodeId = nodeId;
			}
		}
		System.out.println("divide way to edge finish!");
	}
}
