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
	long responseTime;
	long responseTimeH;
	
	public Report(int cost, int costH, long responseTime, long responseTimeH, long start, long end) {
		this.cost = cost;
		this.costH = costH;
		this.responseTime = responseTime;
		this.responseTimeH = responseTimeH;
		this.start = start;
		this.end = end;
	}
}

public class CompareTdspTdspHTimeCost {
	
	public static void main(String[] args) {
		// config
		OSMParam.paramConfig(OSMMain.osm);
		// input
		OSMInput.readNodeFile(OSMData.nodeHashMap);
		OSMInput.readEdgeFile(OSMData.edgeHashMap, OSMData.nodesToEdgeHashMap);
		OSMInput.readAdjList(OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		OSMRouting.initialHierarchy(OSMRouting.hierarchyHashMap);
		// for now, just use node exist in adjlist
		for(long nodeId : OSMData.adjListHashMap.keySet()) {
			NodeInfo nodeInfo = OSMData.nodeHashMap.get(nodeId);
			OSMData.nodeArrayList.add(nodeInfo);
		}
		
		int startTime = 10;
		// test for routing
		int size = OSMData.nodeArrayList.size();
		ArrayList<Report> reportList = new ArrayList<Report>();
		System.out.println("Test for tdsp and tdsph...");
		for(int i = 0; i < 100; i++) {
			long startNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			long endNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			
			System.out.println("start node: " + startNode);
			System.out.println("end node: " + endNode);
			
			long begintime, endtime, costtime, costtimeH;
			
			begintime = System.currentTimeMillis();
			int cost = OSMRouting.tdsp(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.adjListHashMap);
			endtime = System.currentTimeMillis();
			costtime = endtime - begintime;
			begintime = System.currentTimeMillis();
			int costH = OSMRouting.tdspHierarchy(startNode, endNode, startTime, OSMData.nodeHashMap, OSMData.adjListHashMap, OSMData.adjReverseListHashMap, OSMData.nodesToEdgeHashMap);
			endtime = System.currentTimeMillis();
			costtimeH = endtime - begintime;
			Report r = new Report(cost, costH, costtime, costtimeH, startNode, endNode);
			reportList.add(r);
		}
		// write report
		int debug = 0;
		long totalCost = 0;
		long totalCostH = 0;
		long totalResponse = 0;
		long totalResponseH = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.costReportFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Start Node:\tEnd Node:\tTdsp Response(ms):\tTdspH Response(ms):\tTdsp Cost(m):\tTdspH Cost(m):" + OSMParam.LINEEND);
			for(int i = 0; i < reportList.size(); i++) {
				Report r = reportList.get(i);
				String strLine = r.start + "\t" + r.end + "\t" + r.responseTime + "\t\t\t\t\t" + r.responseTimeH + "\t\t\t\t\t" + r.cost/60 + "\t\t\t\t\t" + r.costH/60 + OSMParam.LINEEND;
				totalCost += r.cost/60;
				totalCostH += r.costH/60;
				totalResponse += r.responseTime;
				totalResponseH += r.responseTimeH;
				out.write(strLine);
			}
			out.write("Tdsp Total Response(ms):\tTdspH Total Response(ms):\tTdsp Total Cost:\tTdspH Total Cost:" + OSMParam.LINEEND);
			out.write(totalResponse + "\t\t\t\t\t" + totalResponseH + "\t\t\t\t\t" + totalCost + "\t\t\t\t" + totalCostH + OSMParam.LINEEND);
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("main: debug code: " + debug);
		}
	}
}
