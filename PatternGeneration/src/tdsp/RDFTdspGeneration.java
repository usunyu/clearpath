package tdsp;

import java.io.*;
import java.text.*;
import java.util.*;

import library.*;

import objects.*;

public class RDFTdspGeneration {

	/**
	 * @param file
	 */
	static String root 				= "file";
	static String linkFile 			= "RDF_Link.csv";
	static String linkGeometryFile	= "RDF_Link_Geometry.csv";
	static String linkLaneFile		= "RDF_Link_Lane.csv";
	static String nodeFile 			= "RDF_Node.csv";
	static String adjListFile		= "RDF_AdjList.csv";
	static String pathKMLFile 		= "RDF_Path.kml";
	/**
	 * @param args
	 */
	static long startNode 		= 49304020;
	static long endNode 		= 958285311;
	//static long startNode 	= 49253316;
	//static long endNode 		= 49250620;
	static int startTime 		= 10;
	static int timeInterval 	= 15;
	static int timeRange 		= 60;
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	static String SEPARATION		= ",";
	static String VERTICAL		= "|";
	static String COLON			= ":";
	static String SEMICOLON		= ";";
	static String UNKNOWN 			= "Unknown Street";
	static String YES				= "Y";
	static String NO				= "N";
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param graph
	 */
	static HashMap<Long, ArrayList<RDFToNodeInfo>> adjListMap = new HashMap<Long, ArrayList<RDFToNodeInfo>>();
	/**
	 * @param route
	 */
	static HashMap<String, RDFLinkInfo> nodeToLink = new HashMap<String, RDFLinkInfo>();
	/**
	 * @param path
	 */
	static ArrayList<Long> pathNodeList = new ArrayList<Long>();
	
	public static void main(String[] args) {
		readNodeFile();
		
		readLinkFile();
		readLinkGeometry();
		readLinkLane();
		
		buildAdjList(0);
		tdspGreedy(startNode, endNode, startTime);
		//tdspAStar(startNode, endNode, startTime);
		generatePathKML();
		turnByTurn();
	}
	
	// private static String getUniformSTName(String stName) {
	// 	String[] namePart = stName.split(";");
	// 	int index = 0;
	// 	boolean find = false;
		
	// 	if(namePart.length > 1) {
	// 		for(; index < namePart.length; index++) {
	// 			for(int i = 0; i < namePart[index].length(); i++) {
	// 				if(Character.isDigit(namePart[index].charAt(i))) {
	// 					find = true;
	// 					break;
	// 				}
	// 			}
	// 			if(find)
	// 				break;
	// 		}
	// 	}
	// 	if(index == namePart.length)
	// 		stName = namePart[0];
	// 	else
	// 		stName = namePart[index];
	// 	return stName;
	// }
	
	public static void turnByTurn() {
		System.out.println("turn by turn...");
		long preNodeId = -1;
		String preStName = "";
		int preDirIndex = -1;
		double distance = 0;
		DecimalFormat df = new DecimalFormat("#0.0");
		boolean onRamp = false;
		boolean preOnRamp = false;
		for(int i = 0; i < pathNodeList.size(); i++) {
			if(i == 0) {
				preNodeId = pathNodeList.get(i);
				continue;
			}
			
			long curNodeId = pathNodeList.get(i);
			String nodeStr = preNodeId + "," + curNodeId;
			RDFLinkInfo linkInfo = nodeToLink.get(nodeStr);
			
			RDFNodeInfo preNode = nodeMap.get(preNodeId);
			RDFNodeInfo curNode = nodeMap.get(curNodeId);
			
			int curDirIndex = Geometry.getDirectionIndex(preNode.getLocation(), curNode.getLocation());
			
			String curStName = linkInfo.getBaseName();
			
			//curStName = getUniformSTName(curStName);
			
			onRamp = linkInfo.isRamp();
			
			if(i == 1) {
				preStName = curStName;
				distance = curDirIndex;
			}
			
			// no turn need, cumulative distance
			if(Geometry.isSameDirection(curDirIndex, preDirIndex) && preStName.equals(curStName)) {
				if(onRamp != preOnRamp) {
					if(onRamp) {	// take ramp
						System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStName + " for " + df.format(distance) + " miles.");
					}
					else {	// exit ramp
						System.out.println("Take ramp onto " + curStName + ".");
					}
					distance = 0;
				}
			}
			else if(!preStName.equals(curStName) && Geometry.isSameDirection(curDirIndex, preDirIndex)) {	// change road
				if(onRamp != preOnRamp) {
					if(onRamp) {	// take ramp
						System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStName + " for " + df.format(distance) + " miles.");
					}
					else {	// exit ramp
						System.out.println("Take ramp onto " + curStName + ".");
					}
				}
				else {
					System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStName + " for " + df.format(distance) + " miles.");
				}
				distance = 0;
			}
			else if(preStName.equals(curStName) && !Geometry.isSameDirection(curDirIndex, preDirIndex)) {	// change direction
				if(onRamp != preOnRamp) {
					if(onRamp) {	// take ramp
						System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStName + " for " + df.format(distance) + " miles.");
					}
					else {	// exit ramp
						System.out.println("Take ramp onto " + curStName + ".");
					}
					distance = 0;
				}
			}
			else {	// change direction and road
				if(onRamp != preOnRamp) {
					if(onRamp) {	// take ramp
						System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStName + " for " + df.format(distance) + " miles.");
					}
					else {	// exit ramp
						System.out.println("Take ramp onto " + curStName + ".");
					}
				}
				else {
					System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStName + " for " + df.format(distance) + " miles.");
					int turn = Geometry.getTurn(preDirIndex, curDirIndex);
					if(turn == Geometry.LEFT)
						System.out.println("Turn left on to " + curStName + ".");
					if(turn == Geometry.RIGHT)
						System.out.println("Turn right on to " + curStName + ".");
				}
				
				distance = 0;
			}
			distance += Geometry.calculateDistance(linkInfo.getPointList());
			
			// arrive destination
			if(i == pathNodeList.size() - 1) {
				System.out.println("Go straight on " + curStName + " for " + df.format(distance) + " miles.");
				System.out.println("Arrive destination.");
			}
				
			preNodeId = curNodeId;
			preStName = curStName;
			preDirIndex = curDirIndex;
			preOnRamp = onRamp;
		}
		System.out.println("turn by turn finish!");
	}
	
	public static void generatePathKML() {
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
				
				//RDFNodeInfo lastNode = nodeMap.get(lastNodeId);
				//RDFNodeInfo currentNode = nodeMap.get(nodeId);
				
				RDFLinkInfo link = nodeToLink.get(lastNodeId + SEPARATION + nodeId);
				
				String baseName 	= link.getBaseName();
				int functionalClass = link.getFunctionalClass();
				String travelDirection 	= link.getTravelDirection();
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpool 	= link.isCarpool();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				String kmlStr = "<Placemark><name>Link:" + link.getLinkId() + "</name>";
				kmlStr += "<description>";
				kmlStr += "Street:" + baseName + "\r\n";

				kmlStr += "Funclass:" + functionalClass + "\r\n";
				kmlStr += "Travel:" + travelDirection + "\r\n";
				kmlStr += "Speedcat:" + speedCategory + "\r\n";
				
				kmlStr += "Carpool:" + carpool + "\r\n";
				kmlStr += "Ramp:" + ramp + "\r\n";
				kmlStr += "Tollway:" + tollway + "\r\n";
				
				kmlStr += "Start:" + lastNodeId + "\r\n";
				kmlStr += "End:" + nodeId + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(LocationInfo location : pointsList) {
					kmlStr += location.getLongitude() + "," + location.getLatitude() + "," + location.getZLevel() + " ";
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
	
	public static void tdspGreedy(long startNode, long endNode, int startTime) {
		System.out.println("start finding the path...");
		PriorityQueue<RDFNodeInfo> priorityQ = new PriorityQueue<RDFNodeInfo>(
				20, new Comparator<RDFNodeInfo>() {
			public int compare(RDFNodeInfo n1, RDFNodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		RDFNodeInfo current = nodeMap.get(startNode);	// get start node
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
			
			ArrayList<RDFToNodeInfo> adjNodeList = adjListMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(RDFToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				RDFNodeInfo toNodeRoute = nodeMap.get(toNodeId);
				
				if(toNodeRoute.isVisited())	// if the node is visited, we bypass it
					continue;
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeRoute.getCost()) {
					toNodeRoute.setCost(totalTime);
					toNodeRoute.setParentId(nodeId);
					priorityQ.offer(toNodeRoute);
				}
			}
		}
		
		if (startNode == endNode)
			System.out.println("start node is the same as end node.");
		else {
			current = nodeMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node, program exit!");
				System.exit(-1);
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeMap.get(current.getParentId());
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
	
	public static void tdspAStar(long startNode, long endNode, int startTime) {
		System.out.println("start finding the path...");
		
		PriorityQueue<RDFNodeInfo> priorityQ = new PriorityQueue<RDFNodeInfo>(
				20, new Comparator<RDFNodeInfo>() {
			public int compare(RDFNodeInfo n1, RDFNodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		RDFNodeInfo current = nodeMap.get(startNode);	// get start node
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
			
			ArrayList<RDFToNodeInfo> adjNodeList = adjListMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(RDFToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				RDFNodeInfo toNodeRoute = nodeMap.get(toNodeId);
				
				if(toNodeRoute.isVisited())	// if the node is visited, we bypass it
					continue;
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeRoute.getCost()) {
					toNodeRoute.setCost(totalTime);
					toNodeRoute.setParentId(nodeId);
					priorityQ.offer(toNodeRoute);
				}
			}
		}
		
		if (startNode == endNode)
			System.out.println("start node is the same as end node.");
		else {
			current = nodeMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node, program exit!");
				System.exit(-1);
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeMap.get(current.getParentId());
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
		System.out.println("start finding the path finish!");
	}
	
	public static void buildAdjList(int day) {
		String[] file = adjListFile.split("\\.");
		String tempAdjListFile = file[0] + "_" + days[day] + "." + file[1];
		
		System.out.println("loading adjlist file: " + tempAdjListFile);
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + tempAdjListFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph, please wait...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				if (debug % 100000 == 0)
					System.out.println("completed " + debug + " lines.");

				String[] splitStr = strLine.split("\\" + VERTICAL);
				long startNode = Long.parseLong(splitStr[0].substring(1));

				ArrayList<RDFToNodeInfo> toNodeList = new ArrayList<RDFToNodeInfo>();
				String[] nodeListStr = splitStr[1].split(SEMICOLON);
				for (int i = 0; i < nodeListStr.length; i++) {
					String nodeStr = nodeListStr[i];
					long toNode = Long.parseLong(nodeStr.substring(nodeStr.indexOf('n') + 1, nodeStr.indexOf('(')));
					String fixStr = nodeStr.substring(nodeStr.indexOf('(') + 1, nodeStr.indexOf(')'));
					RDFToNodeInfo toNodeInfo;
					if (fixStr.equals("F")) { // fixed
						int travelTime = Integer.parseInt(nodeStr.substring(nodeStr.indexOf(':') + 1));
						toNodeInfo = new RDFToNodeInfo(toNode, travelTime);
					} else { // variable
						String timeListStr = nodeStr.substring(nodeStr.indexOf(':') + 1);
						String[] timeValueStr = timeListStr.split(SEPARATION);
						int[] travelTimeArray = new int[timeValueStr.length];
						for (int j = 0; j < timeValueStr.length; j++)
							travelTimeArray[j] = Integer.parseInt(timeValueStr[j]);
						toNodeInfo = new RDFToNodeInfo(toNode, travelTimeArray);
					}
					toNodeList.add(toNodeInfo);
				}
				adjListMap.put(startNode, toNodeList);
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
	
	private static void readLinkLane() {
		System.out.println("read link lane...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkLaneFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				long linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo	link	= linkMap.get(linkId);
				for(int i = 1; i < nodes.length; i += 4) {
					long laneId		= Long.parseLong(nodes[i]);
					String travelDirection = nodes[i + 1];
					int laneType	= Integer.parseInt(nodes[i + 2]);
					int accessId 	= Integer.parseInt(nodes[i + 3]);
					RDFLaneInfo	lane = new RDFLaneInfo(laneId, travelDirection, laneType, accessId);

					link.addLane(lane);
				}
			}
			br.close();
			in.close();
			fstream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("readLinkLane: debug code: " + debug);
		}
		System.out.println("read link lane finish!");
	}
	
	private static void readLinkGeometry() {
		System.out.println("read link geometry...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkGeometryFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				long linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo	link	= linkMap.get(linkId);
				for(int i = 1; i < nodes.length; i+=3) {
					double lat		= Double.parseDouble(nodes[i]);
					double lon		= Double.parseDouble(nodes[i + 1]);
					int zLevel	= Integer.parseInt(nodes[i + 2]);
					LocationInfo loc = new LocationInfo(lat, lon, zLevel);
					link.addPoint(loc);
				}
			}
			br.close();
			in.close();
			fstream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("readLinkGeometry: debug code: " + debug);
		}
		System.out.println("read link geometry finish!");
	}
	
	private static void readLinkFile() {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				
				long 	linkId 			= Long.parseLong(nodes[0]);
				long 	refNodeId 		= Long.parseLong(nodes[1]);
				long 	nonRefNodeId 	= Long.parseLong(nodes[2]);
				String 	baseName 		= nodes[3];
				int		accessId		= Integer.parseInt(nodes[4]);
				int 	functionalClass = Integer.parseInt(nodes[5]);
				int 	speedCategory 	= Integer.parseInt(nodes[6]);
				String 	travelDirection = nodes[7];
				boolean ramp 			= nodes[8].equals(YES) ? true : false;
				boolean tollway 		= nodes[9].equals(YES) ? true : false;
				
				
				RDFLinkInfo link = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
				
				link.setBaseName(baseName);
				link.setAccessId(accessId);
				link.setFunctionalClass(functionalClass);
				link.setSpeedCategory(speedCategory);
				link.setTravelDirection(travelDirection);
				link.setRamp(ramp);
				link.setTollway(tollway);
				
				// add direction
				RDFNodeInfo refNode = nodeMap.get(refNodeId);
				RDFNodeInfo nonRefNode = nodeMap.get(nonRefNodeId);
				if(travelDirection.equals("T")) {
					int direction = Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation());
					link.addDirection(direction);
					nodeToLink.put(nonRefNodeId + SEPARATION + refNodeId, link);
				}
				else if(travelDirection.equals("F")) {
					int direction = Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation());
					link.addDirection(direction);
					nodeToLink.put(refNodeId + SEPARATION + nonRefNodeId, link);
				}
				else if(travelDirection.equals("B")) {
					int direction = Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation());
					link.addDirection(direction);
					direction = Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation());
					link.addDirection(direction);
					nodeToLink.put(nonRefNodeId + SEPARATION + refNodeId, link);
					nodeToLink.put(refNodeId + SEPARATION + nonRefNodeId, link);
				}
				
				linkMap.put(linkId, link);

			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readLinkFile: debug code: " + debug);
		}
		System.out.println("read link file finish!");
	}

	private static void readNodeFile() {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				
				long 			nodeId 	= Long.parseLong(nodes[0]);
				double			lat		= Double.parseDouble(nodes[1]);
				double			lon		= Double.parseDouble(nodes[2]);
				int 			zLevel	= Integer.parseInt(nodes[3]);
				
				LocationInfo 	location= new LocationInfo(lat, lon, zLevel);
				
				RDFNodeInfo node = new RDFNodeInfo(nodeId, location);
				node.prepareRoute();
				nodeMap.put(nodeId, node);
			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readNodeFile: debug code: " + debug);
		}
		System.out.println("read node file finish!");
	}
}
