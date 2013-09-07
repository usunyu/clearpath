package path;

import java.util.*;
import java.util.Map.*;
import java.io.*;

import object.*;
import library.*;

public class OSMRouting {

	/**
	 * @param param
	 */
	static long startNode = 1474993500;
	static long endNode = 122657380;
	static int startTime = 10;
	static int timeInterval = 15;
	static int timeRange = 60;
	/**
	 * @param file
	 */
	static String root = "file";
	static String adjlistFile = "osm_adjlist.txt";
	static String nodeFile = "osm_node.txt";
	static String kmlFile = "osm_path.kml";
	/**
	 * @param node
	 */
	static HashMap<Long, NodeInfo> nodeHashMap = new HashMap<Long, NodeInfo>();
	// help for routing
	static HashMap<Long, NodeAssistInfo> nodeAssistMap = new HashMap<Long, NodeAssistInfo>();
	/**
	 * @param graph
	 */
	static HashMap<Long, ArrayList<ToNodeInfo>> adjListHashMap = new HashMap<Long, ArrayList<ToNodeInfo>>();
	/**
	 * @param path
	 */
	static ArrayList<Long> pathNodeList = new ArrayList<Long>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		buildList();
		readNodeFile();
		tdsp(startNode, endNode, startTime);
		generatePathKML();
	}
	
	public static void generatePathKML() {
		System.out.println("generate path kml...");
		
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			long lastNodeId = 0;
			for (int i = 0; i < pathNodeList.size(); i++) {
				debug++;
				
				if(i == 0) {
					lastNodeId = pathNodeList.get(i);
					continue;
				}
				
				long nodeId = pathNodeList.get(i);
				
				NodeInfo lastNode = nodeHashMap.get(lastNodeId);
				NodeInfo currentNode = nodeHashMap.get(nodeId);
				
				String kmlStr = "<Placemark>";
				kmlStr += "<description>";
				kmlStr += "start:" + lastNodeId + "\r\n";
				kmlStr += "end:" + nodeId + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				kmlStr += lastNode.getLocation().getLongitude() + "," + lastNode.getLocation().getLatitude() + ",0 ";
				kmlStr += currentNode.getLocation().getLongitude() + "," + currentNode.getLocation().getLatitude() + ",0 ";
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<color>#FF00FF14</color>";
				kmlStr += "<width>3</width>";
				kmlStr += "</LineStyle></Style></Placemark>\n";
				out.write(kmlStr);
				
				lastNodeId = nodeId;
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generateKML: debug code: " + debug);
		}
		
		System.out.println("generate path kml finish!");
	}

	public static void tdsp(long startNode, long endNode, int startTime) {
		System.out.println("start finding the path...");
		PriorityQueue<NodeAssistInfo> priorityQ = new PriorityQueue<NodeAssistInfo>(
				20, new Comparator<NodeAssistInfo>() {
			public int compare(NodeAssistInfo n1, NodeAssistInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		int adjlistSize = adjListHashMap.keySet().size();
		
		boolean[] visited = new boolean[adjlistSize];
		for(int i = 0; i < adjlistSize; i++)
			visited[i] = false;
		
		NodeAssistInfo current = nodeAssistMap.get(startNode);	// get start node
		if(current == null) {
			System.err.println("cannot find start node, program exit!");
			System.exit(-1);
		}
		
		current.setCost(0);	// set start cost to 0
		
		priorityQ.offer(current);
		
		while ((current = priorityQ.poll()) != null) {
			long nodeId = current.getNodeId();
			
			if(nodeId == endNode)	// find the end
				break;
			
			int timeIndex = startTime + current.getCost() / 60 / timeInterval;
			
			if (timeIndex > timeRange - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = timeRange - 1;
			
			ArrayList<ToNodeInfo> adjNodeList = adjListHashMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(ToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				NodeAssistInfo toNodeAssist = nodeAssistMap.get(toNodeId);
				
				if(toNodeAssist.isVisited())	// if the node is visited, we bypass it
					continue;
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeAssist.getCost()) {
					toNodeAssist.setCost(totalTime);
					toNodeAssist.setParentId(nodeId);
					priorityQ.offer(toNodeAssist);
				}
			}
		}
		
		if (startNode == endNode)
			System.out.println("start node is the same as end node.");
		else {
			current = nodeAssistMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node, program exit!");
				System.exit(-1);
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeAssistMap.get(current.getParentId());
				if(current == null) {
					System.err.println("cannot find intermediate node, program exit!");
					System.exit(-1);
				}
				pathNodeList.add(current.getNodeId());	// add intermediate node
			}
			
			if(current.getParentId() == -1) {
				System.err.println("cannot find the path, program exit!");
				System.exit(-1);
			}
			
			if(current.getParentId() == startNode)
				pathNodeList.add(startNode);	// add start node
			
			Collections.reverse(pathNodeList);	// reverse the path list
		}
		
		System.out.println("find the path successful!");
	}

	public static void buildList() {
		System.out.println("loading adjlist file: " + adjlistFile);

		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/"
					+ adjlistFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph, please wait...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				if (debug % 1000 == 0)
					System.out.println("completed " + debug + "lines.");

				String[] splitStr = strLine.split("\\|\\|");
				long startNode = Long.parseLong(splitStr[0].substring(1));

				ArrayList<ToNodeInfo> toNodeList = new ArrayList<ToNodeInfo>();
				String[] nodeListStr = splitStr[1].split(";");
				for (int i = 0; i < nodeListStr.length; i++) {
					String nodeStr = nodeListStr[i];
					long toNode = Long.parseLong(nodeStr.substring(
							nodeStr.indexOf('n') + 1, nodeStr.indexOf('(')));
					String fixStr = nodeStr.substring(nodeStr.indexOf('(') + 1,
							nodeStr.indexOf(')'));
					ToNodeInfo toNodeInfo;
					if (fixStr.equals("F")) { // fixed
						int travelTime = Integer.parseInt(nodeStr.substring(nodeStr.indexOf(':') + 1));
						toNodeInfo = new ToNodeInfo(toNode, travelTime);
					} else { // variable
						String timeListStr = nodeStr.substring(nodeStr
								.indexOf(':') + 1);
						String[] timeValueStr = timeListStr.split(",");
						int[] travelTimeArray = new int[timeValueStr.length];
						for (int j = 0; j < timeValueStr.length; j++)
							travelTimeArray[j] = Integer.parseInt(timeValueStr[j]);
						toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
					}
					toNodeList.add(toNodeInfo);
				}
				adjListHashMap.put(startNode, toNodeList);
			}

			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("buildList: debug code: " + debug);
		}
		System.out.println("building list finish!");
	}

	public static void readNodeFile() {
		System.out.println("load node file: " + nodeFile);
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("reading node info, please wait...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] splitted = strLine.split("\\|\\|");
				long nodeId = Long.parseLong(splitted[0]);
				String locationStr = splitted[1];
				String[] location = locationStr.split(",");
				double latitude = Double.parseDouble(location[0]);
				double longitude = Double.parseDouble(location[1]);
				LocationInfo locationInfo = new LocationInfo(latitude,
						longitude);
				NodeInfo nodeInfo = new NodeInfo(nodeId, locationInfo);
				nodeHashMap.put(nodeId, nodeInfo);
				NodeAssistInfo nodeCost = new NodeAssistInfo(nodeId);
				nodeAssistMap.put(nodeId, nodeCost);
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
