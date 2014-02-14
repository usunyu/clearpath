package test;

import java.io.*;
import java.util.*;

import global.*;
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
		OSMParam.paramConfig();
		// input
		OSMInput.readNodeFile();
		OSMInput.readEdgeFile();
		OSMInput.readAdjList();
		OSMInput.addOnEdgeToNode();
		OSMParam.initialHierarchy();
		// we will not use lat, lon here
		OSMRouting.setParam(0, 0, 0, 0, 10, 0);
		// for now, just use node exist in adjlist
		for(long nodeId : OSMData.adjListHashMap.keySet()) {
			NodeInfo nodeInfo = OSMData.nodeHashMap.get(nodeId);
			OSMData.nodeArrayList.add(nodeInfo);
		}
		
		int startTime = 10;
		// test for routing
		int size = OSMData.nodeArrayList.size();
		ArrayList<Report> reportList = new ArrayList<Report>();
		System.out.println("test for tdsp and tdsph...");
		int badCount = 0;
		for(int i = 0; i < 1000; i++) {
			long startNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			long endNode = OSMData.nodeArrayList.get((int)(Math.random() * size)).getNodeId();
			ArrayList<Long> pathNodeList = new ArrayList<Long>();
			//System.out.println("start node: " + startNode);
			//System.out.println("end node: " + endNode);
			
			// if start or end node is already on the highway, we skip them for now
			NodeInfo start = OSMData.nodeHashMap.get(startNode);
			NodeInfo end = OSMData.nodeHashMap.get(endNode);
			String startHighway = start.getOnEdgeList().getFirst().getHighway();
			String endHighway = end.getOnEdgeList().getFirst().getHighway();
			if(OSMData.hierarchyHashMap.containsKey(startHighway)) {
				if(OSMData.hierarchyHashMap.get(startHighway) == 1) {
					i--;
					continue;
				}
			}
			if(OSMData.hierarchyHashMap.containsKey(endHighway)) {
				if(OSMData.hierarchyHashMap.get(endHighway) == 1) {
					i--;
					continue;
				}
			}
			
			long begintime, endtime, costtime, costtimeH;
			
			begintime = System.currentTimeMillis();
			double cost = OSMRouting.routingAStar(startNode, endNode, startTime, 0, pathNodeList);
			endtime = System.currentTimeMillis();
			costtime = endtime - begintime;
			begintime = System.currentTimeMillis();
			double costH = OSMRouting.routingHierarchy(startNode, endNode, startTime, 0, pathNodeList);
			endtime = System.currentTimeMillis();
			costtimeH = endtime - begintime;
			
			if(cost < 0 || costH < 0) {
				i--;
				badCount++;
				continue;
			}
			
			Report r = new Report((int)cost, (int)costH, costtime, costtimeH, startNode, endNode);
			reportList.add(r);
			if((i + 1) % 100 == 0) {
				System.out.println((double)(i + 1) / 1000 * 100 + "%");
			}
		}
		System.out.println("finish test, bad count = " + badCount);
		// write report
		int debug = 0;
		long totalCost = 0;
		long totalCostH = 0;
		long totalResponse = 0;
		long totalResponseH = 0;
		try {
			System.out.println("writing report...");
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.costReportFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Start Node,End Node,Tdsp Response(ms),TdspH Response(ms),Tdsp Cost(m),TdspH Cost(m)" + OSMParam.LINEEND);
			for(int i = 0; i < reportList.size(); i++) {
				Report r = reportList.get(i);
				String strLine = r.start + OSMParam.COMMA + r.end + OSMParam.COMMA + r.responseTime + OSMParam.COMMA + r.responseTimeH + OSMParam.COMMA + r.cost/60 + OSMParam.COMMA + r.costH/60 + OSMParam.LINEEND;
				totalCost += r.cost/60;
				totalCostH += r.costH/60;
				totalResponse += r.responseTime;
				totalResponseH += r.responseTimeH;
				out.write(strLine);
			}
			out.write(OSMParam.LINEEND);
			out.write("Tdsp Total Response(ms),TdspH Total Response(ms),Tdsp Total Cost,TdspH Total Cost" + OSMParam.LINEEND);
			out.write(totalResponse + OSMParam.COMMA + totalResponseH + OSMParam.COMMA + totalCost + OSMParam.COMMA + totalCostH + OSMParam.LINEEND);
			out.close();
			System.out.println("done!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("main: debug code: " + debug);
		}
	}
}
