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
	static HashMap<Long, WayInfo> wayHashMap = new HashMap<Long, WayInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		readWayFile();
		readWktsFile();
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
	
	public static void readWktsFile() {
		System.out.println("read wkts file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + wktsFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] splitted = strLine.split("\\|\\|");
				long wayId = Long.parseLong(splitted[0]);
				WayInfo wayInfo = wayHashMap.get(wayId);
				String nodeListStr = splitted[1];
				String[] nodeList = nodeListStr.split(",");
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>();
				long preNodeId = 0;
				for(int i = 0; i < nodeList.length; i++) {
					long nodeId = Long.parseLong(nodeList[i]);
					localNodeArrayList.add(nodeId);
					if(!nodeArrayList.contains(nodeId))
						nodeArrayList.add(nodeHashMap.get(nodeId));
					// build adjlist
					if(i >= 1) {
						if(!adjList.containsKey(preNodeId)) {
							ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
							adjNodeArrayList.add(nodeId);
							adjList.put(preNodeId, adjNodeArrayList);
						}
						else {
							ArrayList<Long> adjNodeArrayList = adjList.get(preNodeId);
							adjNodeArrayList.add(nodeId);
						}
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
					preNodeId = nodeId;
				}
				wayInfo.setNodeArrayList(localNodeArrayList);
				wayArrayList.add(wayInfo);
			}
			
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readWktsFile: debug code: " + debug);
		}
		System.out.println("read wkts file finish!");
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
				String name = splitted[1];
				String nodeListStr = splitted[2];
				String[] nodeList = nodeListStr.split(",");
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>(); 
				for(int i = 0; i < nodeList.length; i++) {
					long nodeId = Long.parseLong(nodeList[i]);
					localNodeArrayList.add(nodeId);
				}
				// TODO
//				WayInfo wayInfo = new WayInfo(wayId, name, localNodeArrayList);
				//wayArrayList.add(wayInfo);
//				wayHashMap.put(wayId, wayInfo);
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
