package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import global.*;
import main.*;
import model.*;
import controller.*;
import object.*;

class Report {
	int cost;
	int costH;
	long start;
	long end;
	
	public Report(int cost, int costH, long start, long end) {
		this.cost = cost;
		this.costH = costH;
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
		ArrayList<Report> reportList = new ArrayList<Report>();
		System.out.println("Test for tdsp and tdsph...");
		for(int i = 0; i < 1000; i++) {
			long startNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			long endNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			int cost = OSMRouting.tdsp(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.adjListHashMap);
			int costH = OSMRouting.tdspHierarchy(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.nodesToEdgeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap);

			Report r = new Report(cost, costH, startNode, endNode);

			reportList.add(r);
		}
		// write report
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.costReportFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Start Node:\tEnd Node:\tTdsp Cost:\tTdspH Cost:" + OSMParam.LINEEND);
			for(int i = 0; i < reportList.size(); i++) {
				Report r = reportList.get(i);
				String strLine = r.start + "\t" + r.end + "\t" + r.cost + "\t" + r.costH + OSMParam.LINEEND;
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("main: debug code: " + debug);
		}
	}
}
