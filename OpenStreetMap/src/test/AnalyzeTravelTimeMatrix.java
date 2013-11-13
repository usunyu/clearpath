package test;

import global.*;
import object.*;
import main.OSMMain;
import model.*;

import java.util.*;
import java.io.*;

import controller.OSMRouting;

class AnalyzeReport {
	int startIndex;
	int endIndex;
	int timeIndex;
	int travelTime;
	
	public AnalyzeReport(int startIndex, int endIndex, int timeIndex, int travelTime) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.timeIndex = timeIndex;
		this.travelTime = travelTime;
	}
}

public class AnalyzeTravelTimeMatrix {
	
	public static ArrayList<Long> nodeAnalyzeList = new ArrayList<Long>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// config
		OSMParam.paramConfig(OSMMain.osm);
		// input
		OSMInput.readNodeFile(OSMData.nodeHashMap);
		OSMInput.readEdgeFile(OSMData.edgeHashMap, OSMData.nodesToEdgeHashMap);
		OSMInput.readAdjList(OSMData.adjListHashMap, OSMData.adjReverseListHashMap);
		OSMInput.readNodeLocationGrid(OSMData.nodeHashMap, OSMData.nodeLocationGridMap);
		OSMProcess.addOnEdgeToNode(OSMData.nodeHashMap, OSMData.edgeHashMap);
		
		readLocationsWithLatLongs();
		
		ArrayList<AnalyzeReport> reportList = new ArrayList<AnalyzeReport>();
		
		for(int i = 0; i < nodeAnalyzeList.size(); i++) {
			for(int j = 0; j < nodeAnalyzeList.size(); j++) {
				if(j == i) continue;
				for(int t = 0; t < 60; t++) {
					double cost = OSMRouting.routingAStar(nodeAnalyzeList.get(i), nodeAnalyzeList.get(j), t, OSMData.nodeHashMap, OSMData.adjListHashMap);
					AnalyzeReport ar = new AnalyzeReport(i, j, t, (int)cost);
					reportList.add(ar);
				}
			}
		}
		int debug = 0;
		// write report
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.analyzeReportFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Soruce:\tDestination:\tDeparture_Time:\tTotal_Travel_Time(seconds):" + OSMParam.LINEEND);
			for(int i = 0; i < reportList.size(); i++) {
				debug++;
				AnalyzeReport r = reportList.get(i);
				String strLine = (r.startIndex + 1) + "\t" + (r.endIndex + 1) + "\t" + TimeInfo.getStartTime(r.timeIndex) +  "\t" + r.travelTime + OSMParam.LINEEND;
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("main: debug code: " + debug);
		}
	}
	
	public static void readLocationsWithLatLongs() {
		System.out.println("read locations with lat longs file...");
		int debug = 0;
		try {
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.locationsWithLatLongsFile;
			OSMInput.checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\t");
				int size = nodes.length;
				String[] latLon = nodes[size - 1].split(", ");
				double lat = Double.parseDouble(latLon[0]);
				double lon = Double.parseDouble(latLon[1]);
				LocationInfo loc = new LocationInfo(lat, lon);
				NodeInfo node = OSMProcess.searchNodeByLocation(OSMData.nodeHashMap, OSMData.nodeLocationGridMap, loc);
				nodeAnalyzeList.add(node.getNodeId());
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readLocationsWithLatLongs: debug code: " + debug);
		}
		System.out.println("read locations with lat longs file finish!");
	}
}
