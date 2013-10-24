package function;

import java.io.*;
import java.util.*;

import object.*;

public class OSMOutput {
	/**
	 * @param file
	 */
	static String root = "file";
	static String osmFile;
	static String nodeCSVFile;
	static String wayCSVFile;
	static String wayInfoFile;
	static String wayKMLFile;
	static String nodeKMLFile;
	static String edgeCVSFile;
	static String adjlistFile;
	static String pathKMLFile;
	// temp
	static String extraNodeFile;
	// test
	static String entranceExitFile;
	static String pathNodeKMLFile;
	static String highwayKMLFile;
	/**
	 * @param csv
	 */
	static String SEPARATION	= "|";
	static String COMMA			= ",";
	static String SEMICOLON		= ";";
	static String COLON			= ":";
	static String ONEDIRECT		= "O";
	static String BIDIRECT		= "B";
	static String FIX			= "F";
	static String VARIABLE		= "V";
	static String LINEEND		= "\r\n";
	
	static String UNKNOWN_STREET 	= "Unknown Street";
	static String UNKNOWN_HIGHWAY 	= "Unknown Highway";
	/**
	 * @param osm
	 */
	static String MOTORWAY		= "motorway";
	static String MOTORWAY_LINK	= "motorway_link";
	static String PRIMARY		= "primary";
	static String PRIMARY_LINK	= "primary_link";
	static String SECONDARY		= "secondary";
	static String SECONDARY_LINK= "secondary_link";
	static String TERTIARY		= "tertiary";
	static String TERTIARY_LINK	= "tertiary_link";
	static String RESIDENTIAL	= "residential";
	static String CYCLEWAY		= "cycleway";
	static String PATH			= "path";
	static String TRACK			= "track";
	static String TRUNK			= "trunk";
	static String TRUNK_LINK	= "trunk_link";
	static String ROAD			= "road";
	static String PROPOSED		= "proposed";
	static String CONSTRUCTION	= "construction";
	static String ABANDONED		= "abandoned";
	static String SCALE			= "scale";
	static String UNCLASSIFIED	= "unclassified";
	/**
	 * @param const
	 */
	static int FEET_PER_MILE	= 5280;
	static int SECOND_PER_HOUR	= 3600;
	
	public static void paramConfig(String name) {
		osmFile 		= name + ".osm";
		nodeCSVFile 	= name + "_node.csv";
		wayCSVFile 		= name + "_way.csv";
		wayInfoFile		= name + "_info.csv";
		wayKMLFile		= name + "_way.kml";
		nodeKMLFile		= name + "_node.kml";
		edgeCVSFile		= name + "_edge.csv";
		adjlistFile		= name + "_adjlist.csv";
		pathKMLFile		= name + "_path.kml";
		// temp
		extraNodeFile	= name + "_way_extra.csv";
		// test
		entranceExitFile= name + "_entrance_exit.kml";
		pathNodeKMLFile	= name + "_path_node.kml";
		highwayKMLFile	= name + "_highway.kml";
	}
	
	public static void generateEntranceExitKML(long start, long end, HashMap<Long, HighwayEntrance> entranceMap, HashMap<Long, HighwayEntrance> exitMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate entrance exit kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + entranceExitFile);
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
				strLine += nodeInfo.getLocation().getLongitude() + "," + nodeInfo.getLocation().getLatitude();
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
			FileWriter fstream = new FileWriter(root + "/" + pathNodeKMLFile);
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
				strLine += nodeInfo.getLocation().getLongitude() + "," + nodeInfo.getLocation().getLatitude();
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
			System.err.println("generatePathNodeKML: debug code: " + debug);
		}
		System.out.println("generate node kml finish!");
	}
	
	/**
	 * generate path kml
	 * @param nodeHashMap
	 * @param pathNodeList
	 */
	public static void generatePathKML(HashMap<Long, NodeInfo> nodeHashMap, ArrayList<Long> pathNodeList) {
		System.out.println("generate path kml...");
		
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + pathKMLFile);
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
				kmlStr += "start:" + lastNodeId + LINEEND;
				kmlStr += "end:" + nodeId + LINEEND;
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
			System.err.println("generatePathKML: debug code: " + debug);
		}
		
		System.out.println("generate path kml finish!");
	}
	
	/**
	 * generate adjlist
	 * @param nodeHashMap
	 * @param adjList
	 * @param nodesToEdge
	 */
	public static void generateAdjList(HashMap<Long, NodeInfo> nodeHashMap, HashMap<Long, ArrayList<Long>> adjList, HashMap<String, EdgeInfo> nodesToEdge) {
		System.out.println("generate adjlist file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + adjlistFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(NodeInfo nodeInfo : nodeHashMap.values()) {
				debug++;
				String strLine;
				strLine = nodeInfo.getNodeId() + SEPARATION;

				ArrayList<Long> localNodeArrayList = adjList.get(nodeInfo.getNodeId());

				// this node cannot go to any other node
				if (localNodeArrayList == null)
					continue;

				for (int j = 0; j < localNodeArrayList.size(); j++) {

					String nodeIdString = nodeInfo.getNodeId() + "," + localNodeArrayList.get(j);

					EdgeInfo edgeInfo = nodesToEdge.get(nodeIdString);
					int travelTime = 1;
					// feet/second
					double speed = 1;
					boolean isFix = false;
					if (edgeInfo.getHighway().equals(MOTORWAY)) {
						speed = (double) 60 * FEET_PER_MILE / (SECOND_PER_HOUR);
					}
					if (edgeInfo.getHighway().equals(MOTORWAY_LINK)) {
						speed = (double) 55 * FEET_PER_MILE / (SECOND_PER_HOUR);
					}
					if (edgeInfo.getHighway().equals(RESIDENTIAL) || edgeInfo.getHighway().equals(CYCLEWAY)) {
						speed = (double) 10 * FEET_PER_MILE / (SECOND_PER_HOUR);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals(UNKNOWN_HIGHWAY) || edgeInfo.getHighway().equals(UNCLASSIFIED) || 
							edgeInfo.getHighway().equals(PATH) || edgeInfo.getHighway().equals(TRACK) || 
							edgeInfo.getHighway().equals(CONSTRUCTION) || edgeInfo.getHighway().equals(TRUNK) ||
							edgeInfo.getHighway().equals(PROPOSED) || edgeInfo.getHighway().equals(TRUNK_LINK) ||
							edgeInfo.getHighway().equals(ROAD) || edgeInfo.getHighway().equals(ABANDONED) ||
							edgeInfo.getHighway().equals(SCALE)) {
						speed = (double) 5 * FEET_PER_MILE / (SECOND_PER_HOUR);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals(TERTIARY)) {
						speed = (double) 20 * FEET_PER_MILE / (SECOND_PER_HOUR);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals(TERTIARY_LINK)) {
						speed = (double) 15 * FEET_PER_MILE / (SECOND_PER_HOUR);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals(SECONDARY)) {
						speed = (double) 30 * FEET_PER_MILE / (SECOND_PER_HOUR);
					}
					if (edgeInfo.getHighway().equals(SECONDARY_LINK)) {
						speed = (double) 25 * FEET_PER_MILE / (SECOND_PER_HOUR);
					}
					if (edgeInfo.getHighway().equals(PRIMARY)) {
						speed = (double) 35 * FEET_PER_MILE / (SECOND_PER_HOUR);
					}
					if (edgeInfo.getHighway().equals(PRIMARY_LINK)) {
						speed = (double) 30 * FEET_PER_MILE / (SECOND_PER_HOUR);
					}

					travelTime = (int) Math.round(edgeInfo.getDistance() / speed);
					if (travelTime == 0) {
						travelTime = 1;
					}

					if (!isFix) {
						// assign travel time
						int[] timeList = new int[60];
						for (int k = 0; k < timeList.length; k++) {
							timeList[k] = travelTime;
						}
						
						strLine += localNodeArrayList.get(j) + "(" + VARIABLE + ")" + COLON;
						for (int k = 0; k < timeList.length; k++) {
							strLine += timeList[k];
							if (k < timeList.length - 1)
								strLine += COMMA;
							else
								strLine += SEMICOLON;
						}
					} else {
						strLine += localNodeArrayList.get(j) + "(" + FIX + ")" + COLON;
						strLine += travelTime + SEMICOLON;
					}
				}

				strLine += LINEEND;
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
			FileWriter fstream = new FileWriter(root + "/" + edgeCVSFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (EdgeInfo edgeInfo : edgeHashMap.values()) {
				debug++;
				long wayId = edgeInfo.getWayId();
				int edgeId = edgeInfo.getEdgeId();
				String name = edgeInfo.getName();
				String highway = edgeInfo.getHighway();
				long startNode = edgeInfo.getStartNode();
				long endNode = edgeInfo.getEndNode();
				int distance = edgeInfo.getDistance();
				long id = wayId * 1000 + edgeId;
				String strLine = id + SEPARATION + name + SEPARATION  + highway + SEPARATION 
				+ startNode + SEPARATION + endNode + SEPARATION + distance + LINEEND;
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
			FileWriter fstream = new FileWriter(root + "/" + nodeKMLFile);
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
				strLine += nodeInfo.getLocation().getLongitude() + "," + nodeInfo.getLocation().getLatitude();
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
		System.out.println("generate node kml finish!");
	}
	
	/**
	 * generate way kml
	 * @param wayHashMap
	 * @param nodeHashMap
	 */
	public static void generateWayKML(HashMap<Long, WayInfo> wayHashMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("generate kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + wayKMLFile);
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
				kmlStr += "oneway:" + isOneway + LINEEND;
				if(name.contains("&"))
					name = name.replaceAll("&", "and");
				kmlStr += "name:" + name + LINEEND;
				kmlStr += "highway:" + highway + LINEEND;
				kmlStr += "ref:\r\n";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					long NodeId = localNodeArrayList.get(j);
					kmlStr += NodeId + LINEEND;
				}
				kmlStr += LINEEND;
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
			System.err.println("generateWayKML: debug code: " + debug);
		}
		System.out.println("generate way kml finish!");
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
			FileWriter fstream = new FileWriter(root + "/" + highwayKMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for(EdgeInfo edgeInfo : edgeHashMap.values()) {
				debug++;
				long wayId = edgeInfo.getWayId();
				String name = edgeInfo.getName();
				String highway = edgeInfo.getHighway();
				
				if(!highway.equals(MOTORWAY) && !highway.equals(MOTORWAY_LINK)) {
					continue;
				}
				
				long start = edgeInfo.getStartNode();
				long end = edgeInfo.getEndNode();
				
				String kmlStr = "<Placemark><name>Way:" + wayId + "</name>";
				kmlStr += "<description>";
				kmlStr += "start:" + start + LINEEND;
				kmlStr += "end:" + end + LINEEND;
				if(name.contains("&"))
					name = name.replaceAll("&", "and");
				kmlStr += "name:" + name + LINEEND;
				kmlStr += "highway:" + highway + LINEEND;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				
				NodeInfo node1 = nodeHashMap.get(start);
				LocationInfo location1 = node1.getLocation();
				kmlStr += location1.getLongitude() + "," + location1.getLatitude() + ",0 ";
				
				NodeInfo node2 = nodeHashMap.get(end);
				LocationInfo location2 = node2.getLocation();
				kmlStr += location2.getLongitude() + "," + location2.getLatitude() + ",0 ";
				
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<width>3</width>";
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
			FileWriter fstream = new FileWriter(root + "/" + wayInfoFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(Long wayId : wayHashMap.keySet()) {
				debug++;
				WayInfo wayInfo = wayHashMap.get(wayId);
				HashMap<String, String> infoHashMap = wayInfo.getInfoHashMap();
				if(infoHashMap != null) {
					String strLine = String.valueOf(wayId);
					for(String key : infoHashMap.keySet()) {
						String value = infoHashMap.get(key);
						strLine += SEPARATION + SEPARATION + key + SEPARATION + value;
					}
					strLine += LINEEND;
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
			FileWriter fstream = new FileWriter(root + "/" + wayCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(Long wayId : wayHashMap.keySet()) {
				debug++;
				WayInfo wayInfo = wayHashMap.get(wayId);
				String oneway = wayInfo.isOneway() ? ONEDIRECT : BIDIRECT;
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				String strLine = wayId + SEPARATION + oneway + SEPARATION + name + SEPARATION + highway + LINEEND;
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
			FileWriter fstream = new FileWriter(root + "/" + nodeCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(Long nodeId : nodeHashMap.keySet()) {
				debug++;
				NodeInfo nodeInfo = nodeHashMap.get(nodeId);
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				String strLine = nodeId + SEPARATION + latitude + SEPARATION + longitude + LINEEND;
				
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
	 */
	public static void writeCSVFile(ArrayList<WayInfo> wayArrayList, ArrayList<NodeInfo> nodeArrayList) {
		System.out.println("write csv file...");
		int debug = 0, loop = 0;
		try {
			// write node
			FileWriter fstream = new FileWriter(root + "/" + nodeCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < nodeArrayList.size(); i++) {
				debug++;
				NodeInfo nodeInfo = nodeArrayList.get(i);
				long nodeId = nodeInfo.getNodeId();
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				String strLine = nodeId + SEPARATION + latitude + SEPARATION + longitude + LINEEND;
				
				out.write(strLine);
			}
			out.close();
			fstream.close();
			
			// write way
			fstream = new FileWriter(root + "/" + wayCSVFile);
			out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < wayArrayList.size(); i++) {
				debug++;
				WayInfo wayInfo = wayArrayList.get(i);
				long wayId = wayInfo.getWayId();
				String isOneway = wayInfo.isOneway() ? ONEDIRECT : BIDIRECT;
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				String strLine = wayId + SEPARATION + isOneway + SEPARATION + name + SEPARATION + highway;
				
				strLine += LINEEND;
				out.write(strLine);
			}
			out.close();
			fstream.close();
			
			// write way info
			fstream = new FileWriter(root + "/" + wayInfoFile);
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
						strLine += SEPARATION + SEPARATION + key + SEPARATION + value;
					}
					strLine += LINEEND;
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
