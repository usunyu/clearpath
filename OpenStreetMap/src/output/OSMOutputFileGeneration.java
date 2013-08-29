package output;

import java.io.*;
import java.util.*;

import object.*;

public class OSMOutputFileGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	static String nodeFile = "osm_node.txt";
	static String wayFile = "osm_way.txt";
	static String wktsFile = "map.osm.wkts";
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		readWayFile();
		readWktsFile();
		overwriteNodeFile();
		overwriteWayFile();
	}
	
	public static void overwriteWayFile() {
		System.out.println("overwrite way file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + wayFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < wayArrayList.size(); i++) {
				debug++;
				WayInfo wayInfo = wayArrayList.get(i);
				long wayId = wayInfo.getWayId();
				char isOneway = wayInfo.isOneway() ? 'O' : 'B';
				String name = wayInfo.getName();
				ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
				String nodeListStr = "";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					nodeListStr += localNodeArrayList.get(j);
					if(j < localNodeArrayList.size() - 1)
						nodeListStr += ",";
				}
				String strLine = wayId + "||" + isOneway + "||" + name + "||" + nodeListStr + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("overwriteWayFile: debug code: " + debug);
		}
		System.out.println("overwrite way file finish!");
	}
	
	public static void overwriteNodeFile() {
		System.out.println("overwrite node file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + nodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < nodeArrayList.size(); i++) {
				debug++;
				NodeInfo nodeInfo = nodeArrayList.get(i);
				long nodeId = nodeInfo.getNodeId();
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				String strLine = nodeId + "||" + latitude + "," + longitude + "\r\n";
				
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("overwriteNodeFile: debug code: " + debug);
		}
		System.out.println("overwrite node file finish!");
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
				for(int i = 0; i < nodeList.length; i++) {
					long nodeId = Long.parseLong(nodeList[i]);
					NodeInfo nodeInfo = nodeHashMap.get(nodeId);
					localNodeArrayList.add(nodeId);
					if(!nodeArrayList.contains(nodeInfo)) {
						nodeArrayList.add(nodeInfo);
					}
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
				boolean isOneway = splitted[1].equals("O") ? true : false;
				String name = splitted[2];
				//String nodeListStr = splitted[2];
				//String[] nodeList = nodeListStr.split(",");
				//ArrayList<Long> localNodeArrayList = new ArrayList<Long>(); 
				//for(int i = 0; i < nodeList.length; i++) {
				//	long nodeId = Long.parseLong(nodeList[i]);
				//	localNodeArrayList.add(nodeId);
				//}
				WayInfo wayInfo = new WayInfo(wayId, isOneway, name, null);
				//wayArrayList.add(wayInfo);
				wayHashMap.put(wayId, wayInfo);
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
