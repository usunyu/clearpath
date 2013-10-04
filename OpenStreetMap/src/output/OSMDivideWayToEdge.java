package output;

import java.io.*;
import java.util.*;

import object.*;

public class OSMDivideWayToEdge {

	/**
	 * @param file
	 */
	static String root 			= "file";
	//static String nodeFile 		= "osm_node.txt";
	//static String wayFile 		= "osm_way.txt";
	//static String edgeFile 		= "osm_edge.txt";
	
	//static String nodeFile	 	= "los_angeles_node.txt";
	//static String wayFile	 	= "los_angeles_way.txt";
	//static String edgeFile 		= "los_angeles_edge.txt";
	
	// static String nodeFile	 	= "minnesota_node.txt";
	// static String wayFile	 	= "minnesota_way.txt";
	// static String edgeFile 		= "minnesota_edge.txt";

	static String nodeFile;
	static String wayFile;
	static String edgeFile;
	/**
	 * @param node
	 */
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	/**
	 * @param edge
	 */
	static ArrayList<EdgeInfo> edgeArrayList = new ArrayList<EdgeInfo>();
	
	private static void inputArgs(String arg) {
		nodeFile 	= arg + "_node.txt";
		wayFile 	= arg + "_way.txt";
		edgeFile	= arg + "_edge.txt";
	}
	
	public static void run(String[] args) {
		inputArgs(args[0]);
		main(args);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		readWayFile();
		divideWayToEdge();
		writeEdgeFile();
	}
	
	private static String getWayEdgeId(long wayId, int edgeId) {
		String wayStr = String.valueOf(wayId);
		String edgeStr = String.valueOf(edgeId);
		while(edgeStr.length() < 4)
			edgeStr = "0" + edgeStr;
		return wayStr + edgeStr;
	}
	
	public static void writeEdgeFile() {
		System.out.println("write edge file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + edgeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < edgeArrayList.size(); i++) {
				debug++;
				EdgeInfo edgeInfo = edgeArrayList.get(i);
				long wayId = edgeInfo.getWayId();
				int edgeId = edgeInfo.getEdgeId();
				String name = edgeInfo.getName();
				String highway = edgeInfo.getHighway();
				long startNode = edgeInfo.getStartNode();
				long endNode = edgeInfo.getEndNode();
				int distance = edgeInfo.getDistance();
				
				String strLine = getWayEdgeId(wayId, edgeId) + "||" + name + "||"  + highway + "||" 
				+ startNode + "||" + endNode + "||" + distance + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeEdgeFile: debug code: " + debug);
		}
		System.out.println("write edge file finish!");
	}
	
	public static void divideWayToEdge() {
		System.out.println("divide way to edge...");
		
		for(int i = 0; i < wayArrayList.size(); i++) {
			WayInfo wayInfo = wayArrayList.get(i);
			long wayId = wayInfo.getWayId();
			boolean isOneway = wayInfo.isOneway();
			String name = wayInfo.getName();
			String highway = wayInfo.getHighway();
			ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
			int edgeId = 0;
			
			long preNodeId = 0;
			for(int j = 0; j < localNodeArrayList.size(); j++) {
				long nodeId = localNodeArrayList.get(j);
				if(j >= 1) {
					long startNode = preNodeId;
					long endNode = nodeId;
					NodeInfo nodeInfo1 = nodeHashMap.get(startNode);
					NodeInfo nodeInfo2 = nodeHashMap.get(endNode);
					double dDistance = Distance.calculateDistance(nodeInfo1.getLocation(), nodeInfo2.getLocation()) * 5280;
					int distance = (int)Math.round(dDistance);
					EdgeInfo edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, startNode, endNode, distance);
					edgeId++;
					edgeArrayList.add(edgeInfo);
					// use two edges to denote one edge here
					if(!isOneway) {
						EdgeInfo edgeInfo2 = new EdgeInfo(wayId, edgeId, name, highway, endNode, startNode, distance);
						edgeId++;
						edgeArrayList.add(edgeInfo2);
					}
				}
				preNodeId = nodeId;
			}
		}
		System.out.println("divide way to edge finish!");
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
				String highway = splitted[3];
				String nodeListStr = splitted[4];
				String[] nodeList = nodeListStr.split(",");
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>(); 
				for(int i = 0; i < nodeList.length; i++) {
					long nodeId = Long.parseLong(nodeList[i]);
					localNodeArrayList.add(nodeId);
				}
				// skip read info map
				WayInfo wayInfo = new WayInfo(wayId, isOneway, name, highway, localNodeArrayList, null);
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
