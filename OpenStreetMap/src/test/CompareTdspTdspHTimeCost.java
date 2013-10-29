package test;

import global.*;
import main.*;
import model.*;
import controller.*;
import object.*;

public class CompareTdspTdspHTimeCost {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// config
		OSMParam.paramConfig(OSMMain.osm);
		// input
		OSMInput.readNodeFile(OSMData.nodeHashMap, OSMData.nodeArrayList);
		OSMInput.readEdgeFile(OSMData.edgeHashMap, OSMData.nodesToEdgeHashMap);
		OSMInput.readAdjList(OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		int startTime = 10;
		// test for routing
		int size = OSMData.nodeArrayList.size();
		for(int i = 0; i < 1000; i++) {
			long startNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			long endNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			int cost = OSMRouting.tdsp(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.adjListHashMap);
			int costH = OSMRouting.tdspHierarchy(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
			
		}
	}

}
