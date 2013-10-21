package function;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import object.EdgeInfo;
import object.LocationInfo;
import object.NodeInfo;
import object.WayInfo;

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
	// temp
	static String extraNodeFile;
	/**
	 * @param csv
	 */
	static String SEPARATION	= "|";
	static String COMMA			= ",";
	static String SEMICOLON		= ";";
	static String COLON			= ":";
	static String ONEDIRECT		= "O";
	static String BIDIRECT		= "B";
	static String LINEEND		= "\r\n";
	
	static String UNKNOWN_STREET 	= "Unknown Street";
	static String UNKNOWN_HIGHWAY 	= "Unknown Highway";
	
	public static void paramConfig(String name) {
		osmFile 		= name + ".osm";
		nodeCSVFile 	= name + "_node.csv";
		wayCSVFile 		= name + "_way.csv";
		wayInfoFile		= name + "_info.csv";
		wayKMLFile		= name + "_way.kml";
		nodeKMLFile		= name + "_node.kml";
		edgeCVSFile		= name + "_edge.csv";
		adjlistFile		= name + "_adjlist.csv";
		// temp
		extraNodeFile	= name + "_way_extra.csv";
	}
	
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
					double speed = 1;
					boolean isFix = false;
					if (edgeInfo.getHighway().equals("motorway")) {
						speed = (double) 60 * 5280 / (60 * 60);
					}
					if (edgeInfo.getHighway().equals("motorway_link")) {
						speed = (double) 55 * 5280 / (60 * 60);
					}
					if (edgeInfo.getHighway().equals("residential")) {
						speed = (double) 30 * 5280 / (60 * 60);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals("tertiary")) {
						speed = (double) 30 * 5280 / (60 * 60);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals("tertiary_link")) {
						speed = (double) 25 * 5280 / (60 * 60);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals("secondary")) {
						speed = (double) 35 * 5280 / (60 * 60);
					}
					if (edgeInfo.getHighway().equals("secondary_link")) {
						speed = (double) 30 * 5280 / (60 * 60);
					}
					if (edgeInfo.getHighway().equals("primary")) {
						speed = (double) 35 * 5280 / (60 * 60);
					}
					if (edgeInfo.getHighway().equals("primary_link")) {
						speed = (double) 30 * 5280 / (60 * 60);
					}
					if (edgeInfo.getHighway().equals("unclassified")) {
						speed = (double) 15 * 5280 / (60 * 60);
						isFix = true;
					}
					if (edgeInfo.getHighway().equals("null")) {
						speed = (double) 15 * 5280 / (60 * 60);
						isFix = true;
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
						
						strLine += localNodeArrayList.get(j) + "(V)" + COLON;
						for (int k = 0; k < timeList.length; k++) {
							strLine += timeList[k];
							if (k < timeList.length - 1)
								strLine += COMMA;
							else
								strLine += SEMICOLON;
						}
					} else {
						strLine += localNodeArrayList.get(j) + "(F)" + COLON;
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
