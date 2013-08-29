package adjlist;

import java.io.*;
import java.util.*;

import object.*;

public class OSMGenerateAdjList {

	/**
	 * @param file
	 */
	static String root = "file";
	static String nodeFile = "osm_node.txt";
	static String wayFile = "osm_way.txt";
	static String kmlFile = "osm_map.kml";
	static String wktsFile = "map.osm.wkts";
	static String adjlistFile = "osm_adjlist.txt";
	/**
	 * @param node
	 */
	static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		readWayFile();
		buildAdjList();
		generateAdjList();
	}
	
	public static void generateAdjList() {
		System.out.println("generate adjlist file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + adjlistFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(int i = 0; i < nodeArrayList.size(); i++) {
				debug++;
				NodeInfo nodeInfo = nodeArrayList.get(i);
				String strLine;
				strLine = "n" + nodeInfo.getNodeId() + "||";
				
				// random time value
				int[] timeList = new int[60];
				for(int j = 0; j < timeList.length; j++)
					timeList[j] = 5;
				
				ArrayList<Long> localNodeArrayList =  adjList.get(nodeInfo.getNodeId());
				
				// this node cannot go to any other node
				if(localNodeArrayList == null)
					continue;
				
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					strLine += "n" + localNodeArrayList.get(j) + "(V):";
					for(int k = 0; k < timeList.length; k++) {
						strLine += timeList[k];
						if(k < timeList.length - 1)
							strLine += ",";
						else
							strLine += ";";
					}
				}
				
				strLine += "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generateAdjList: debug code: " + debug);
		}
		System.out.println("generate adjlist file finish!");
	}
	
	public static void buildAdjList() {
		System.out.println("build adjlist file...");
		for(int i = 0; i < wayArrayList.size(); i++) {
			WayInfo wayInfo = wayArrayList.get(i);
			boolean isOneway = wayInfo.isOneway();
			ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
			long preNodeId = 0;
			for(int j = 0; j < localNodeArrayList.size(); j++) {
				long nodeId = localNodeArrayList.get(j);
				// build adjlist
				if(j >= 1) {
					if(!adjList.containsKey(preNodeId)) {
						ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
						adjNodeArrayList.add(nodeId);
						adjList.put(preNodeId, adjNodeArrayList);
					}
					else {
						ArrayList<Long> adjNodeArrayList = adjList.get(preNodeId);
						adjNodeArrayList.add(nodeId);
					}
					if(!isOneway) {
						if(!adjList.containsKey(nodeId)) {
							ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
							adjNodeArrayList.add(preNodeId);
							adjList.put(nodeId, adjNodeArrayList);
						}
						else {
							ArrayList<Long> adjNodeArrayList = adjList.get(nodeId);
							adjNodeArrayList.add(preNodeId);
						}
					}
				}
				preNodeId = nodeId;
			}
		}
		System.out.println("build adjlist finish!");
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
				long wayId = Long.parseLong(splitted[0]);
				boolean isOneway = splitted[1].equals("O") ? true : false;
				String name = splitted[2];
				String nodeListStr = splitted[3];
				String[] nodeList = nodeListStr.split(",");
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>(); 
				for(int i = 0; i < nodeList.length; i++) {
					long nodeId = Long.parseLong(nodeList[i]);
					localNodeArrayList.add(nodeId);
				}
				WayInfo wayInfo = new WayInfo(wayId, isOneway, name, localNodeArrayList);
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
				nodeHashMap.put(nodeId, nodeInfo);
				nodeArrayList.add(nodeInfo);
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
