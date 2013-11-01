package model;

import java.io.*;
import java.util.*;

import global.*;
import object.*;

public class OSMOutput {
	
	public static void generateEntranceExitKML(long start, long end, HashMap<Long, HighwayEntrance> entranceMap, HashMap<Long, HighwayEntrance> exitMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate entrance exit kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.entranceExitFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
			NodeInfo startNode = nodeHashMap.get(start);
			NodeInfo endNode = nodeHashMap.get(end);
			
			nodeList.add(startNode);
			nodeList.add(endNode);
			
			for(long nodeId : entranceMap.keySet()) {
				NodeInfo node = nodeHashMap.get(nodeId);
				nodeList.add(node);
			}
			
			for(long nodeId : exitMap.keySet()) {
				NodeInfo node = nodeHashMap.get(nodeId);
				nodeList.add(node);
			}
			
			for(NodeInfo nodeInfo : nodeList) {
				debug++;
				String strLine = "<Placemark>";
				strLine += "<name>" + nodeInfo.getNodeId() + "</name>";
				strLine += "<description>";
				strLine += "Id:" + nodeInfo.getNodeId();
				strLine += "</description>";
				strLine += "<Point><coordinates>";
				strLine += nodeInfo.getLocation().getLongitude() + OSMParam.COMMA + nodeInfo.getLocation().getLatitude();
				strLine += ",0</coordinates></Point>";
				strLine += "</Placemark>";
				
				out.write(strLine);
			}
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generateKMLNode: debug code: " + debug);
		}
		System.out.println("generate entrance exit kml finish!");
	}
	
	/**
	 * generate path node kml
	 * @param nodeHashMap
	 * @param pathNodeList
	 */
	public static void generatePathNodeKML(HashMap<Long, NodeInfo> nodeHashMap, ArrayList<Long> pathNodeList) {
		System.out.println("generate path node kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.pathNodeKMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for(long nodeId : pathNodeList) {
				debug++;
				NodeInfo nodeInfo = nodeHashMap.get(nodeId);
				String strLine = "<Placemark>";
				strLine += "<name>" + nodeInfo.getNodeId() + "</name>";
				strLine += "<description>";
				strLine += "Id:" + nodeInfo.getNodeId();
				strLine += "</description>";
				strLine += "<Point><coordinates>";
				strLine += nodeInfo.getLocation().getLongitude() + OSMParam.COMMA + nodeInfo.getLocation().getLatitude() + OSMParam.COMMA + "0 ";
				strLine += "</coordinates></Point>";
				strLine += "</Placemark>";
				
				out.write(strLine);
			}
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generatePathNodeKML: debug code: " + debug);
		}
		System.out.println("generate node kml finish!");
	}
	
	/**
	 * generate path kml
	 * @param nodeHashMap
	 * @param pathNodeList
	 */
	public static void generatePathKML(HashMap<Long, NodeInfo> nodeHashMap, HashMap<String, EdgeInfo> nodesToEdgeHashMap, ArrayList<Long> pathNodeList) {
		System.out.println("generate path kml...");
		
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.pathKMLFile);
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
				
				String nodeIdStr = lastNodeId + OSMParam.COMMA + nodeId;
				
				EdgeInfo edge = nodesToEdgeHashMap.get(nodeIdStr);
				
				String kmlStr = "<Placemark>";
				kmlStr += "<name>Edge:" + edge.getId() + "</name>";
				kmlStr += "<description>";
				kmlStr += "name:" + edge.getName() + OSMParam.LINEEND;
				kmlStr += "highway:" + edge.getHighway() + OSMParam.LINEEND;
				kmlStr += "distance:" + edge.getDistance() + OSMParam.LINEEND;
				kmlStr += "oneway:" + edge.isOneway() + OSMParam.LINEEND;
				kmlStr += "start:" + lastNodeId + OSMParam.LINEEND;
				kmlStr += "end:" + nodeId + OSMParam.LINEEND;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(long n : edge.getNodeList()) {
					NodeInfo node = nodeHashMap.get(n);
					kmlStr += node.getLocation().getLongitude() + OSMParam.COMMA + node.getLocation().getLatitude() + OSMParam.COMMA + "0 ";
				}
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
			System.err.println("generatePathKML: debug code: " + debug);
		}
		
		System.out.println("generate path kml finish!");
	}
	
	/**
	 * generate adjlist
	 * @param nodeHashMap
	 * @param adjList
	 * @param nodesToEdgeHashMap
	 */
	public static void writeAdjList(HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("generate adjlist file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.adjlistFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(NodeInfo node : nodeHashMap.values()) {
				debug++;
				String strLine;
				strLine = node.getNodeId() + OSMParam.SEPARATION;

				LinkedList<ToNodeInfo> localNodeList = adjListHashMap.get(node.getNodeId());

				// this node cannot go to any other node
				if (localNodeList == null)
					continue;

				for(ToNodeInfo toNode : localNodeList) {
					boolean isFix = toNode.isFix();
					if (!isFix) {
						// assign travel time
						int[] timeList = toNode.getTravelTimeArray();
						
						strLine += toNode.getNodeId() + "(" + OSMParam.VARIABLE + ")" + OSMParam.COLON;
						for (int k = 0; k < timeList.length; k++) {
							strLine += timeList[k];
							if (k < timeList.length - 1)
								strLine += OSMParam.COMMA;
							else
								strLine += OSMParam.SEMICOLON;
						}
					} else {
						int travelTime = toNode.getTravelTime();
						strLine += toNode.getNodeId() + "(" + OSMParam.FIX + ")" + OSMParam.COLON;
						strLine += travelTime + OSMParam.SEMICOLON;
					}
				}

				strLine += OSMParam.LINEEND;
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
	
	/**
	 * write edge csv file
	 * @param edgeHashMap
	 */
	public static void writeEdgeFile(HashMap<Long, EdgeInfo> edgeHashMap) {
		System.out.println("write edge file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.edgeCVSFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (EdgeInfo edge : edgeHashMap.values()) {
				debug++;
				long wayId = edge.getWayId();
				int edgeId = edge.getEdgeId();
				String name = edge.getName();
				String highway = edge.getHighway();
				boolean isOneway = edge.isOneway();
				LinkedList<Long> nodeList = edge.getNodeList();
				String nodeListStr = null;
				for(long nodeId : nodeList) {
					if(nodeListStr == null) {
						nodeListStr = String.valueOf(nodeId);
					}
					else {
						nodeListStr += OSMParam.COMMA + nodeId;
					}
				}
				int distance = edge.getDistance();
				String strLine = wayId + OSMParam.SEPARATION + edgeId + OSMParam.SEPARATION + name + OSMParam.SEPARATION  + 
						highway + OSMParam.SEPARATION + (isOneway ? OSMParam.ONEDIRECT : OSMParam.BIDIRECT) + 
						OSMParam.SEPARATION + nodeListStr + OSMParam.SEPARATION +  distance + OSMParam.LINEEND;
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
	
	/**
	 * generate node kml
	 * @param nodeHashMap
	 */
	public static void generateNodeKML(HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate node kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.nodeKMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for(NodeInfo nodeInfo : nodeHashMap.values()) {
				debug++;
				String strLine = "<Placemark>";
				strLine += "<name>" + nodeInfo.getNodeId() + "</name>";
				strLine += "<description>";
				strLine += "Id:" + nodeInfo.getNodeId();
				strLine += "</description>";
				strLine += "<Point><coordinates>";
				strLine += nodeInfo.getLocation().getLongitude() + OSMParam.COMMA + nodeInfo.getLocation().getLatitude() + OSMParam.COMMA + "0 ";
				strLine += "</coordinates></Point>";
				strLine += "</Placemark>";
				
				out.write(strLine);
			}
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("generateKMLNode: debug code: " + debug);
		}
		System.out.println("generate node kml finish!");
	}
	
	/**
	 * generate way kml
	 * @param wayHashMap
	 * @param nodeHashMap
	 */
	public static void generateWayKML(HashMap<Long, WayInfo> wayHashMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate way kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.wayKMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for(WayInfo wayInfo : wayHashMap.values()) {
				debug++;
				long wayId = wayInfo.getWayId();
				boolean isOneway = wayInfo.isOneway();
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
				
				String kmlStr = "<Placemark><name>Way:" + wayId + "</name>";
				kmlStr += "<description>";
				kmlStr += "oneway:" + isOneway + OSMParam.LINEEND;
				if(name.contains("&"))
					name = name.replaceAll("&", "and");
				kmlStr += "name:" + name + OSMParam.LINEEND;
				kmlStr += "highway:" + highway + OSMParam.LINEEND;
				kmlStr += "ref:\r\n";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					long NodeId = localNodeArrayList.get(j);
					kmlStr += NodeId + OSMParam.LINEEND;
				}
				kmlStr += OSMParam.LINEEND;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					NodeInfo nodeInfo = nodeHashMap.get(localNodeArrayList.get(j));
					LocationInfo location = nodeInfo.getLocation();
					kmlStr += location.getLongitude() + OSMParam.COMMA + location.getLatitude() + OSMParam.COMMA + "0 ";
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
			System.err.println("generateWayKML: debug code: " + debug);
		}
		System.out.println("generate way kml finish!");
	}
	
	/**
	 * generate edge kml
	 * @param edgeHashMap
	 * @param nodeHashMap
	 */
	public static void generateEdgeKML(HashMap<Long, EdgeInfo> edgeHashMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate edge kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.edgeKMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for(EdgeInfo edgeInfo : edgeHashMap.values()) {
				debug++;
				long id = edgeInfo.getId();
				String name = edgeInfo.getName();
				String highway = edgeInfo.getHighway();
				boolean isOneway = edgeInfo.isOneway();
				LinkedList<Long> nodeList = edgeInfo.getNodeList();
				
				String kmlStr = "<Placemark><name>Edge:" + id + "</name>";
				kmlStr += "<description>";
				if(name.contains("&"))
					name = name.replaceAll("&", "and");
				kmlStr += "name:" + name + OSMParam.LINEEND;
				kmlStr += "highway:" + highway + OSMParam.LINEEND;
				kmlStr += "isOneway:" + isOneway + OSMParam.LINEEND;
				kmlStr += "ref:" + OSMParam.LINEEND;
				for(long nodeId : nodeList) {
					kmlStr += nodeId + OSMParam.LINEEND;
				}
				kmlStr += OSMParam.LINEEND;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(long nodeId : nodeList) {
					NodeInfo nodeInfo = nodeHashMap.get(nodeId);
					LocationInfo location = nodeInfo.getLocation();
					kmlStr += location.getLongitude() + OSMParam.COMMA + location.getLatitude() + OSMParam.COMMA + "0 ";
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
			System.err.println("generateEdgeKML: debug code: " + debug);
		}
		System.out.println("generate edge kml finish!");
	}
	
	/**
	 * generate highway kml
	 * @param edgeHashMap
	 * @param nodeHashMap
	 */
	public static void generateHighwayKML(HashMap<Long, EdgeInfo> edgeHashMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate highway kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.highwayKMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for(EdgeInfo edgeInfo : edgeHashMap.values()) {
				debug++;
				long wayId = edgeInfo.getWayId();
				String name = edgeInfo.getName();
				String highway = edgeInfo.getHighway();
				
				if(!highway.equals(OSMParam.MOTORWAY) && !highway.equals(OSMParam.MOTORWAY_LINK) && 
						!highway.equals(OSMParam.TRUNK) && !highway.equals(OSMParam.TRUNK_LINK)) {
					continue;
				}
				
				long start = edgeInfo.getStartNode();
				long end = edgeInfo.getEndNode();
				
				String kmlStr = "<Placemark><name>Way:" + wayId + "</name>";
				kmlStr += "<description>";
				kmlStr += "start:" + start + OSMParam.LINEEND;
				kmlStr += "end:" + end + OSMParam.LINEEND;
				if(name.contains("&"))
					name = name.replaceAll("&", "and");
				kmlStr += "name:" + name + OSMParam.LINEEND;
				kmlStr += "highway:" + highway + OSMParam.LINEEND;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				
				NodeInfo node1 = nodeHashMap.get(start);
				LocationInfo location1 = node1.getLocation();
				kmlStr += location1.getLongitude() + OSMParam.COMMA + location1.getLatitude() + OSMParam.COMMA + "0 ";
				
				NodeInfo node2 = nodeHashMap.get(end);
				LocationInfo location2 = node2.getLocation();
				kmlStr += location2.getLongitude() + OSMParam.COMMA + location2.getLatitude() + OSMParam.COMMA + "0 ";
				
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
			System.err.println("generateHighwayKML: debug code: " + debug);
		}
		System.out.println("generate highway kml finish!");
	}
	
	/**
	 * write the way info file
	 * @param wayHashMap
	 */
	public static void writeWayInfo(HashMap<Long, WayInfo> wayHashMap) {
		System.out.println("write way info file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.wayInfoFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(Long wayId : wayHashMap.keySet()) {
				debug++;
				WayInfo wayInfo = wayHashMap.get(wayId);
				HashMap<String, String> infoHashMap = wayInfo.getInfoHashMap();
				if(infoHashMap != null) {
					String strLine = String.valueOf(wayId);
					for(String key : infoHashMap.keySet()) {
						String value = infoHashMap.get(key);
						strLine += OSMParam.SEPARATION + OSMParam.SEPARATION + key + OSMParam.SEPARATION + value;
					}
					strLine += OSMParam.LINEEND;
					out.write(strLine);
				}
			}
			
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeWayInfo: debug code: " + debug);
		}
		System.out.println("write way info file finish!");
	}
	
	/**
	 * write the way file
	 * @param wayHashMap
	 */
	public static void writeWayFile(HashMap<Long, WayInfo> wayHashMap) {
		System.out.println("write way file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.wayCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(Long wayId : wayHashMap.keySet()) {
				debug++;
				WayInfo wayInfo = wayHashMap.get(wayId);
				String oneway = wayInfo.isOneway() ? OSMParam.ONEDIRECT : OSMParam.BIDIRECT;
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				String strLine = wayId + OSMParam.SEPARATION + oneway + OSMParam.SEPARATION + name + OSMParam.SEPARATION + highway + OSMParam.LINEEND;
				out.write(strLine);
			}
			
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeWayFile: debug code: " + debug);
		}
		System.out.println("write way file finish!");
	}
	
	/**
	 * write the node csv file
	 * @param nodeHashMap
	 */
	public static void writeNodeFile(HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("write node file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.nodeCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(Long nodeId : nodeHashMap.keySet()) {
				debug++;
				NodeInfo nodeInfo = nodeHashMap.get(nodeId);
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				String strLine = nodeId + OSMParam.SEPARATION + latitude + OSMParam.SEPARATION + longitude + OSMParam.LINEEND;
				
				out.write(strLine);
			}
			
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeNodeFile: debug code: " + debug);
		}
		System.out.println("write node file finish!");
	}

	/**
	 * write to CSV file from the data read from XML file 
	 * @param wayArrayList
	 * @param nodeArrayList
	 */
	public static void writeCSVFile(ArrayList<WayInfo> wayArrayList, ArrayList<NodeInfo> nodeArrayList) {
		System.out.println("write csv file...");
		int debug = 0, loop = 0;
		try {
			// write node
			FileWriter fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.nodeCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < nodeArrayList.size(); i++) {
				debug++;
				NodeInfo nodeInfo = nodeArrayList.get(i);
				long nodeId = nodeInfo.getNodeId();
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				String strLine = nodeId + OSMParam.SEPARATION + latitude + OSMParam.SEPARATION + longitude + OSMParam.LINEEND;
				
				out.write(strLine);
			}
			out.close();
			fstream.close();
			
			// write way
			fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.wayCSVFile);
			out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < wayArrayList.size(); i++) {
				debug++;
				WayInfo wayInfo = wayArrayList.get(i);
				long wayId = wayInfo.getWayId();
				String isOneway = wayInfo.isOneway() ? OSMParam.ONEDIRECT : OSMParam.BIDIRECT;
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				String strLine = wayId + OSMParam.SEPARATION + isOneway + OSMParam.SEPARATION + name + OSMParam.SEPARATION + highway;
				
				strLine += OSMParam.LINEEND;
				out.write(strLine);
			}
			out.close();
			fstream.close();
			
			// write way info
			fstream = new FileWriter(OSMParam.root + OSMParam.SEGMENT + OSMParam.wayInfoFile);
			out = new BufferedWriter(fstream);
			loop++;
			for(WayInfo wayInfo : wayArrayList) {
				debug++;
				long wayId = wayInfo.getWayId();
				HashMap<String, String> wayInfoMap = wayInfo.getInfoHashMap();
				if(wayInfoMap != null) {
					String strLine = String.valueOf(wayId);
					for(String key : wayInfoMap.keySet()) {
						String value = wayInfoMap.get(key);
						strLine += OSMParam.SEPARATION + OSMParam.SEPARATION + key + OSMParam.SEPARATION + value;
					}
					strLine += OSMParam.LINEEND;
					out.write(strLine);
				}
			}
			out.close();
			fstream.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeCSVFile: debug code: " + debug + ", in the " + loop + " loop.");
		}
		System.out.println("write csv file finish!");
	}
}
