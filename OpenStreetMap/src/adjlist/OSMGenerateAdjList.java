package adjlist;

import java.io.*;
import java.util.*;

import object.*;

public class OSMGenerateAdjList {

	/**
	 * @param file
	 */
	static String root 			= "file";
	
	//static String nodeFile 		= "osm_node.txt";
	//static String wayFile 		= "osm_way.txt";
	//static String edgeFile 		= "osm_edge.txt";
	//static String adjlistFile 	= "osm_adjlist.txt";
	
	//static String nodeFile	 	= "los_angeles_node.txt";
	//static String wayFile	 	= "los_angeles_way.txt";
	//static String edgeFile 		= "los_angeles_edge.txt";
	//static String adjlistFile 	= "los_angeles_adjlist.txt";
	
	// static String nodeFile	 	= "minnesota_node.txt";
	// static String wayFile	 	= "minnesota_way.txt";
	// static String edgeFile 		= "minnesota_edge.txt";
	// static String adjlistFile 	= "minnesota_adjlist.txt";

	static String nodeFile;
	static String wayFile;
	static String edgeFile;
	static String adjlistFile;
	/**
	 * @param node
	 */
	static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	/**
	 * @param way
	 */
	//static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	/**
	 * @param edge
	 */
	static ArrayList<EdgeInfo> edgeArrayList = new ArrayList<EdgeInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	static HashMap<String, EdgeInfo> nodesToEdge = new HashMap<String, EdgeInfo>();
	
	private static void inputArgs(String arg) {
		nodeFile 	= arg + "_node.txt";
		wayFile 	= arg + "_way.txt";
		edgeFile	= arg + "_edge.txt";
		adjlistFile = arg + "_adjlist.txt";
	}
	
	public static void run(String[] args) {
		inputArgs(args[0]);
		main(args);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		//readWayFile();
		readEdgeFile();
		buildAdjList();
		generateAdjList();
	}
	
	public static void readEdgeFile() {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + edgeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] splitted = strLine.split("\\|\\|");
				long wayId = Long.parseLong(splitted[0].substring(0, splitted[0].length() - 4));
				int edgeId = Integer.parseInt(splitted[0].substring(splitted[0].length() - 4));
				String name = splitted[1];
				String highway = splitted[2];
				long startNode = Long.parseLong(splitted[3]);
				long endNode = Long.parseLong(splitted[4]);
				int distance = Integer.parseInt(splitted[5]);
				
				EdgeInfo edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, startNode, endNode, distance);
				edgeArrayList.add(edgeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readEdgeFile: debug code: " + debug);
		}
		System.out.println("read edge file finish!");
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
				
				ArrayList<Long> localNodeArrayList =  adjList.get(nodeInfo.getNodeId());
				
				// this node cannot go to any other node
				if(localNodeArrayList == null)
					continue;
				
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					
					String nodeIdString = nodeInfo.getNodeId() + "," + localNodeArrayList.get(j);
					
					EdgeInfo edgeInfo = nodesToEdge.get(nodeIdString);
					int travelTime = 1;
					double speed = 1;
					boolean isF = false;
					if(edgeInfo.getHighway().equals("motorway"))
						speed = (double) 60 * 5280 / (60 * 60);
					if(edgeInfo.getHighway().equals("motorway_link"))
						speed = (double) 55 * 5280 / (60 * 60);
					if(edgeInfo.getHighway().equals("residential")) {
						speed = (double) 30 * 5280 / (60 * 60);
						isF = true;
					}
					if(edgeInfo.getHighway().equals("tertiary")) {
						speed = (double) 30 * 5280 / (60 * 60);
						isF = true;
					}
					if(edgeInfo.getHighway().equals("tertiary_link")) {
						speed = (double) 25 * 5280 / (60 * 60);
						isF = true;
					}
					if(edgeInfo.getHighway().equals("secondary"))
						speed = (double) 35 * 5280 / (60 * 60);
					if(edgeInfo.getHighway().equals("secondary_link"))
						speed = (double) 30 * 5280 / (60 * 60);
					if(edgeInfo.getHighway().equals("primary"))
						speed = (double) 35 * 5280 / (60 * 60);
					if(edgeInfo.getHighway().equals("primary_link"))
						speed = (double) 30 * 5280 / (60 * 60);
					if(edgeInfo.getHighway().equals("unclassified")) {
						speed = (double) 15 * 5280 / (60 * 60);
						isF = true;
					}
					if(edgeInfo.getHighway().equals("null")) {
						speed = (double) 15 * 5280 / (60 * 60);
						isF = true;
					}
					
					travelTime = (int) Math.round(edgeInfo.getDistance() / speed);
					if(travelTime == 0)
						travelTime = 1;
					
					// assign travel time
					int[] timeList = new int[60];
					for(int k = 0; k < timeList.length; k++) {
						timeList[k] = travelTime;
					}
					
					if(!isF) {
						strLine += "n" + localNodeArrayList.get(j) + "(V):";
						for(int k = 0; k < timeList.length; k++) {
							strLine += timeList[k];
							if(k < timeList.length - 1)
								strLine += ",";
							else
								strLine += ";";
						}
					}
					else {
						strLine += "n" + localNodeArrayList.get(j) + "(F):";
						strLine += travelTime + ";";
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
		/* build adjlist using edge */
		for(int i = 0; i < edgeArrayList.size(); i++) {
			EdgeInfo edgeInfo = edgeArrayList.get(i);
			long startNode = edgeInfo.getStartNode();
			long endNode = edgeInfo.getEndNode();
			String nodeIdString = startNode + "," + endNode;
			nodesToEdge.put(nodeIdString, edgeInfo);
			if(!adjList.containsKey(startNode)) {
				ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
				adjNodeArrayList.add(endNode);
				adjList.put(startNode, adjNodeArrayList);
			}
			else {
				ArrayList<Long> adjNodeArrayList = adjList.get(startNode);
				adjNodeArrayList.add(endNode);
			}
		}
		
		/* build adjlist using way */
		//for(int i = 0; i < wayArrayList.size(); i++) {
		//	WayInfo wayInfo = wayArrayList.get(i);
		//	boolean isOneway = wayInfo.isOneway();
		//	ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
		//	long preNodeId = 0;
		//	for(int j = 0; j < localNodeArrayList.size(); j++) {
		//		long nodeId = localNodeArrayList.get(j);
		//		// build adjlist
		//		if(j >= 1) {
		//			if(!adjList.containsKey(preNodeId)) {
		//				ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
		//				adjNodeArrayList.add(nodeId);
		//				adjList.put(preNodeId, adjNodeArrayList);
		//			}
		//			else {
		//				ArrayList<Long> adjNodeArrayList = adjList.get(preNodeId);
		//				adjNodeArrayList.add(nodeId);
		//			}
		//			if(!isOneway) {
		//				if(!adjList.containsKey(nodeId)) {
		//					ArrayList<Long> adjNodeArrayList = new ArrayList<Long>();
		//					adjNodeArrayList.add(preNodeId);
		//					adjList.put(nodeId, adjNodeArrayList);
		//				}
		//				else {
		//					ArrayList<Long> adjNodeArrayList = adjList.get(nodeId);
		//					adjNodeArrayList.add(preNodeId);
		//				}
		//			}
		//		}
		//		preNodeId = nodeId;
		//	}
		//}
		System.out.println("build adjlist finish!");
	}
	
	//public static void readWayFile() {
	//	System.out.println("read way file...");
	//	int debug = 0;
	//	try {
	//		FileInputStream fstream = new FileInputStream(root + "/" + wayFile);
	//		DataInputStream in = new DataInputStream(fstream);
	//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
	//		String strLine;
	//		
	//		while ((strLine = br.readLine()) != null) {
	//			debug++;
	//			String[] splitted = strLine.split("\\|\\|");
	//			long wayId = Long.parseLong(splitted[0]);
	//			boolean isOneway = splitted[1].equals("O") ? true : false;
	//			String name = splitted[2];
	//			String highway = splitted[3];
	//			String nodeListStr = splitted[4];
	//			String[] nodeList = nodeListStr.split(",");
	//			ArrayList<Long> localNodeArrayList = new ArrayList<Long>(); 
	//			for(int i = 0; i < nodeList.length; i++) {
	//				long nodeId = Long.parseLong(nodeList[i]);
	//				localNodeArrayList.add(nodeId);
	//			}
	//			// skip read info map
	//			WayInfo wayInfo = new WayInfo(wayId, isOneway, name, highway, localNodeArrayList, null);
	//			wayArrayList.add(wayInfo);
	//		}
	//		br.close();
	//		in.close();
	//		fstream.close();
	//	} catch (Exception e) {
	//		// TODO: handle exception
	//		e.printStackTrace();
	//		System.err.println("readWayFile: debug code: " + debug);
	//	}
	//	System.out.println("read way file finish!");
	//}
	
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
