package map;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import object.*;

public class OSMGenerateKMLMap {

	/**
	 * @param file
	 */
	static String root = "file";
	static String nodeFile = "osm_node.txt";
	static String wayFile = "osm_way.txt";
	static String kmlFile = "osm_map.kml";
	/**
	 * @param node
	 */
	static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		readWayFile();
		generateKML();
	}
	
	public static void generateKML() {
		System.out.println("generate kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for (int i = 0; i < wayArrayList.size(); i++) {
				debug++;
				WayInfo wayInfo = wayArrayList.get(i);
				long wayId = wayInfo.getWayId();
				int version = wayInfo.getVersion();
				String name = wayInfo.getName();
				ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
				
				String kmlStr = "<Placemark><name>Way:" + wayId + "</name>";
				kmlStr += "<description>";
				kmlStr += "version:" + version + "\r\n";
				kmlStr += "name:" + name + "\r\n";
				kmlStr += "ref:\r\n";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					long NodeId = localNodeArrayList.get(j);
					kmlStr += NodeId + "\r\n";
				}
				kmlStr += "\r\n";
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					NodeInfo nodeInfo = nodeHashMap.get(localNodeArrayList.get(j));
					LocationInfo location = nodeInfo.getLocation();
					kmlStr += location.getLongitude() + "," + location.getLatitude() + ",0 ";
				}
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<width>1</width>";
				kmlStr += "</LineStyle></Style></Placemark>\n";
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generateKML: debug code: " + debug);
		}
		System.out.println("generate kml finish!");
	}
	
	public static void readWayFile() {
		System.out.println("read way file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + wayFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] splitted = strLine.split("\\|\\|");
				int wayId = Integer.parseInt(splitted[0]);
				int version = Integer.parseInt(splitted[1]);
				String name = splitted[2];
				String nodeListStr = splitted[3];
				String[] nodeList = nodeListStr.split(",");
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>(); 
				for(int i = 0; i < nodeList.length; i++) {
					long nodeId = Long.parseLong(nodeList[i]);
					localNodeArrayList.add(nodeId);
				}
				WayInfo wayInfo = new WayInfo(wayId, version, name, localNodeArrayList);
				wayArrayList.add(wayInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readWayFile: debug code: " + debug);
		}
		System.out.println("read way file finish!");
	}
	
	public static void readNodeFile() {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] splitted = strLine.split("\\|\\|");
				long nodeId = Long.parseLong(splitted[0]);
				String locationStr = splitted[1];
				String[] location = locationStr.split(",");
				double latitude = Double.parseDouble(location[0]);
				double longitude = Double.parseDouble(location[1]);
				LocationInfo locationInfo = new LocationInfo(latitude, longitude);
				NodeInfo nodeInfo = new NodeInfo(nodeId, locationInfo);
				nodeArrayList.add(nodeInfo);
				nodeHashMap.put(nodeId, nodeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readNodeFile: debug code: " + debug);
		}
		System.out.println("read node file finish!");
	}
}
