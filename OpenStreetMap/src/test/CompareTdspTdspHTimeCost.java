package test;

import java.util.*;

import global.*;
import main.*;
import model.*;
import controller.*;
import object.*;

class Report {
	int cost;
	long start;
	long end;
	
	public Report(int cost, long start, long end) {
		this.cost = cost;
		this.start = start;
		this.end = end;
	}
}

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
		OSMRouting.initialHierarchy();
		OSMRouting.prepareRoute(OSMData.nodeHashMap);
		int startTime = 10;
		// test for routing
		int size = OSMData.nodeArrayList.size();
		ArrayList<Report> reportTdspList = new ArrayList<Report>();
		ArrayList<Report> reportTdspHList = new ArrayList<Report>();
		System.out.println("Test for tdsp and tdsph...");
		for(int i = 0; i < 1000; i++) {
			long startNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			long endNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			int cost = OSMRouting.tdsp(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.adjListHashMap);
			int costH = OSMRouting.tdspHierarchy(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
			Report r = new Report(cost, startNode, endNode);
			Report rH = new Report(costH, startNode, endNode);
			reportTdspList.add(r);
			reportTdspHList.add(rH);
		}
	}

}
