package main;

import global.*;
import object.*;
import model.*;
import controller.*;

import java.util.*;
import java.io.*;

class AnalyzeReport {
	int startIndex;
	int endIndex;
	int timeIndex;
	int dayIndex;
	int travelTime;
	
	public AnalyzeReport(int startIndex, int endIndex, int timeIndex, int dayIndex, int travelTime) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.timeIndex = timeIndex;
		this.dayIndex = dayIndex;
		this.travelTime = travelTime;
	}
}

public class TimeDependentRouting {

	public static ArrayList<Long> nodeAnalyzeList = new ArrayList<Long>();
	public static String[] days =  {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	public static boolean isNumeric(String str){
		for(int i = str.length(); --i >= 0;){
			if (!Character.isDigit(str.charAt(i))) return false;
		}
		return true;
	}
	
	public static int getDayIndex(String str) {
		for(int i = 0; i < days.length; i++) {
			if(str.equals(days[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length < 3 || args.length > 4) {
			System.err.println("usage: java -jar -Xms512m -Xmx8192m TimeDependentRouting.jar startLine location.csv result.csv day(optional)");
			System.exit(0);
		}
		// check parameter
		if(!isNumeric(args[0])) {
			System.err.println("parameter error: startLine parameter should be number");
			System.exit(0);
		}
		// set the day
		int dayIndex = -1;
		if(args.length == 4) {
			dayIndex = getDayIndex(args[3]);
			if(dayIndex == -1) {
				System.err.println("parameter error: day parameter should be Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday");
				System.exit(0);
			}
		}
		
		// TODO Auto-generated method stub
		// config
		OSMParam.paramConfig();
		// input
		OSMInput.readNodeRes();
		OSMInput.readEdgeRes();
		OSMInput.readAdjListRes();
		OSMInput.readNodeLocationGrid();
		OSMInput.addOnEdgeToNode();
		// we will not use lat, lon here
		OSMRouting.setParam(0, 0, 0, 0, 10, 0);
		
		//for(String str : OSMData.nodeLocationGridMap.keySet()) {
		//	System.out.println(str);
		//}
		ArrayList<Long> pathNodeList = new ArrayList<Long>();
		
		int startLine = Integer.parseInt(args[0]) - 1;
		readLocationsWithLatLongs(args[1]);
		
		ArrayList<AnalyzeReport> reportList = new ArrayList<AnalyzeReport>();
		int debug = 0;
		// write report
		try {
			System.out.println("writing report...");
			if(dayIndex == -1) {
				// for 7 days
				for(int day = 0; day < 7; day++) {
					// for other nodes
					for(int j = 0; j < nodeAnalyzeList.size(); j++) {
						if(j == startLine) continue;
						for(int time = 0; time < 60; time++) {
							debug++;
							double cost = OSMRouting.routingAStar(nodeAnalyzeList.get(startLine), nodeAnalyzeList.get(j), time, day, pathNodeList);
							AnalyzeReport ar = new AnalyzeReport(startLine, j, time, day, (int)cost);
							reportList.add(ar);
						}
					}
				
					FileWriter fstream = new FileWriter(args[2]);
					BufferedWriter out = new BufferedWriter(fstream);
					out.write("Soruce,Destination,Departure_Time,Day,Total_Travel_Time(seconds)" + OSMParam.LINEEND);
					for(int i = 0; i < reportList.size(); i++) {
						debug++;
						AnalyzeReport r = reportList.get(i);
						String strLine = (r.startIndex + 1) + OSMParam.COMMA + (r.endIndex + 1) + OSMParam.COMMA + 
								TimeInfo.getStartTime(r.timeIndex) +  OSMParam.COMMA + OSMParam.days[r.dayIndex] +
								OSMParam.COMMA + r.travelTime + OSMParam.LINEEND;
						out.write(strLine);
					}
					out.close();
					
					System.out.println((double)(day + 1) / 7 * 100 + "%");
				}
			}
			else {
				// for other nodes
				for(int j = 0; j < nodeAnalyzeList.size(); j++) {
					if(j == startLine) continue;
					for(int time = 0; time < 60; time++) {
						debug++;
						double cost = OSMRouting.routingAStar(nodeAnalyzeList.get(startLine), nodeAnalyzeList.get(j), time, dayIndex, pathNodeList);
						AnalyzeReport ar = new AnalyzeReport(startLine, j, time, dayIndex, (int)cost);
						reportList.add(ar);
					}
				}
			
				FileWriter fstream = new FileWriter(args[2]);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("Soruce,Destination,Departure_Time,Day,Total_Travel_Time(seconds)" + OSMParam.LINEEND);
				for(int i = 0; i < reportList.size(); i++) {
					debug++;
					AnalyzeReport r = reportList.get(i);
					String strLine = (r.startIndex + 1) + OSMParam.COMMA + (r.endIndex + 1) + OSMParam.COMMA + 
							TimeInfo.getStartTime(r.timeIndex) +  OSMParam.COMMA + OSMParam.days[r.dayIndex] +
							OSMParam.COMMA + r.travelTime + OSMParam.LINEEND;
					out.write(strLine);
				}
				out.close();
			}
			
			System.out.println("finish!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("AnalyzeTravelTimeMatrix: debug code: " + debug);
		}
	}
	
	public static void readLocationsWithLatLongs(String file) {
		System.out.println("read locations with lat longs file...");
		int debug = 0;
		try {
			OSMInput.checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				if(strLine.equals("")) continue;
				String[] nodes = strLine.split("\t");
				int size = nodes.length;
				String[] latLon = nodes[size - 1].split(", ");
				double lat = Double.parseDouble(latLon[0]);
				double lon = Double.parseDouble(latLon[1]);
				LocationInfo loc = new LocationInfo(lat, lon);
				NodeInfo node = loc.searchNode();
				if(node == null) {
					System.out.println("error: the node is out of range, line " + debug);
					System.exit(-1);
				}
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
