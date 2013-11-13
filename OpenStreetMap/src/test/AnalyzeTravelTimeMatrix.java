package test;

import global.*;
import object.*;
import main.OSMMain;
import model.*;

import java.util.*;
import java.io.*;

public class AnalyzeTravelTimeMatrix {
	
	public static LinkedList<NodeInfo> nodeAnalyzeList = new LinkedList<NodeInfo>();

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
		
		readLocationsWithLatLongs();
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
				nodeAnalyzeList.add(node);
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
