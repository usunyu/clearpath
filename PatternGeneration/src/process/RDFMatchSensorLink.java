package process;

import java.io.*;
import java.sql.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import objects.*;

public class RDFMatchSensorLink {

	/**
	 * @param file
	 */
	static String root 				= "file";
	static String linkFile			= "RDF_Link.txt";
	static String nodeFile			= "RDF_Node.txt";
	static String kmlLinkFile		= "RDF_Link.kml";
	static String sensorMatchFile	= "RDF_Sensor_Match.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param arguments
	 */
	static double searchDistance = 0.15;
	static double corssSearchDistance = 0.15;
	static double noDirSearchDistance = 0.01;
	static int devide = 10;
	static int thirdRoundTime = 3;
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Long, LinkedList<Long>> adjNodeList = new HashMap<Long, LinkedList<Long>>();
	// two nodes decide one link
	static HashMap<String, RDFLinkInfo> nodeToLink = new HashMap<String, RDFLinkInfo>();
	/**
	 * @param sensor
	 */
	static HashSet<Integer> sensorDuplicate = new HashSet<Integer>();
	static HashSet<Integer> matchSensorDuplicate = new HashSet<Integer>();
	static LinkedList<SensorInfo> sensorList = new LinkedList<SensorInfo>();
	static LinkedList<SensorInfo> matchSensorList = new LinkedList<SensorInfo>();
	
	public static void main(String[] args) {
		readNodeFile();
		readLinkFile();
		//generateLinkKML();
		fetchSensor();
		matchLinkSensor();
		writeSensorMatch();
	}
	
	private static void writeSensorMatch() {
		System.out.println("write sensor match...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + sensorMatchFile);
			BufferedWriter out = new BufferedWriter(fstream);
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while (iterator.hasNext()) {
				RDFLinkInfo RDFLink = iterator.next();
				LinkedList<SensorInfo> sensorList = RDFLink.getSensorList();
				if(sensorList == null || sensorList.size() == 0)
					continue;
				ListIterator<SensorInfo> sIt = sensorList.listIterator();
				String sensorStr = "null";
				int i = 0;
				while(sIt.hasNext()) {
					SensorInfo sensor = sIt.next();
					if(i++ == 0)
						sensorStr = String.valueOf(sensor.getSensorId());
					else
						sensorStr += "," + sensor.getSensorId();
				}
				long linkId = RDFLink.getLinkId();
				String strLine = linkId + "|" + sensorStr + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write sensor match finish!");
	}
	
	private static void matchLinkSensor() {
		// 3 round match algorithm
		System.out.println("match sensors to links...");
		// 1) match same direction
		firstRoundMatch();
		// 2) match direction 0 to 3, 1 to 2
		secondRoundMatch();
		// 3) match nearest link
		for (int i = 0; i < thirdRoundTime; i++)
			thirdRoundMatch();
		// 4) match smallest distance
		// forthRoundMatch();
		System.out.println("match Sensors finish!");
	}
	
	private static void firstRoundMatch() {
		System.out.println("first round...");
		ListIterator<RDFLinkInfo> linkIterator = linkList.listIterator();
		int debug = 0;
		while(linkIterator.hasNext()) {
			debug++;
			RDFLinkInfo link = linkIterator.next();
			// just match highway
			if(link.getFunctionalClass() != 1 && link.getFunctionalClass() != 2)
				continue;
			LinkedList<LocationInfo> pointsList = link.getPointsList();
			for (double step = searchDistance / devide; step < searchDistance; step += step) {
				ListIterator<LocationInfo> pointIterator = pointsList.listIterator();
				while(pointIterator.hasNext()) {
					LocationInfo nLoc = pointIterator.next();
					ListIterator<SensorInfo> sensorIterator = sensorList.listIterator();
					while(sensorIterator.hasNext()) {
						SensorInfo sensor = sensorIterator.next();
						LocationInfo sLoc = sensor.getLocation();
						// same direction
						/**
						 * need fix
						 */
						//String allDir = link.getAllDirection();
						String allDir = "";
						String[] dirList = allDir.split(",");
						for (int d = 0; d < dirList.length; d++) {
							if (sensor.getDirection() == Integer.parseInt(dirList[d])) {
								double distance = Geometry.calculateDistance(nLoc, sLoc);
								// in the search area
								if (distance < step) {
									// match sensor
									if (!link.containsSensor(sensor))
										link.addSensor(sensor);
									if (!matchSensorDuplicate.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										matchSensorDuplicate.add(sensor.getSensorId());
									}
								}
							}
						}
					}
				}
			}
			if (debug % 1000 == 0 || debug == linkList.size())
				System.out.println((float) debug / linkList.size() * 100 + "%");
		}
		System.out.println("first round finish!");
	}
	
	private static void secondRoundMatch() {
		System.out.println("second round...");
		ListIterator<RDFLinkInfo> linkIterator = linkList.listIterator();
		int debug = 0;
		while(linkIterator.hasNext()) {
			debug++;
			RDFLinkInfo link = linkIterator.next();
			// just match highway
			if(link.getFunctionalClass() != 1 && link.getFunctionalClass() != 2)
				continue;
			LinkedList<LocationInfo> pointsList = link.getPointsList();
			for (double step = corssSearchDistance / devide; step < searchDistance; step += step) {
				ListIterator<LocationInfo> pointIterator = pointsList.listIterator();
				while(pointIterator.hasNext()) {
					LocationInfo nLoc = pointIterator.next();
					ListIterator<SensorInfo> sensorIterator = sensorList.listIterator();
					while(sensorIterator.hasNext()) {
						SensorInfo sensor = sensorIterator.next();
						LocationInfo sLoc = sensor.getLocation();
						// same direction
						/**
						 * need fix
						 */
						//String allDir = link.getAllDirection();
						String allDir = "";
						String[] dirList = allDir.split(",");
						for (int d = 0; d < dirList.length; d++) {
							// 0 : 3, 1 : 2
							if ((sensor.getDirection() == 0 && Integer.parseInt(dirList[d]) == 3)
									|| (sensor.getDirection() == 3 && Integer.parseInt(dirList[d]) == 0)
									|| (sensor.getDirection() == 1 && Integer.parseInt(dirList[d]) == 2)
									|| (sensor.getDirection() == 2 && Integer.parseInt(dirList[d]) == 1)) {
								double distance = Geometry.calculateDistance(nLoc, sLoc);
								// in the search area
								if (distance < step) {
									// match sensor
									if (!link.containsSensor(sensor))
										link.addSensor(sensor);
									if (!matchSensorDuplicate.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										matchSensorDuplicate.add(sensor.getSensorId());
									}
								}
							}
						}
					}
				}
			}
			if (debug % 1000 == 0 || debug == linkList.size())
				System.out.println((float) debug / linkList.size() * 100 + "%");
		}
		System.out.println("second round finish!");
	}
	
	private static void thirdRoundMatch() {
		System.out.println("third round...");
		ListIterator<RDFLinkInfo> linkIterator = linkList.listIterator();
		int debug = 0;
		while(linkIterator.hasNext()) {
			debug++;
			RDFLinkInfo link = linkIterator.next();
			// just match highway
			if(link.getFunctionalClass() != 1 && link.getFunctionalClass() != 2)
				continue;
			LinkedList<SensorInfo> linkSensorList = link.getSensorList();
			if(linkSensorList == null || linkSensorList.size() == 0)
				continue;
			long nodeId1 = link.getRefNodeId();
			LinkedList<Long> adjList1 = adjNodeList.get(nodeId1);
			ListIterator<Long> adjIterator1 = adjList1.listIterator();
			while(adjIterator1.hasNext()) {
				long nodeId2 = adjIterator1.next();
				String nodesStr = nodeId1 + "," + nodeId2;
				RDFNodeInfo nearNode = nodeMap.get(nodeId2);
				if (nodeToLink.containsKey(nodesStr)) {
					RDFLinkInfo nearLink = nodeToLink.get(nodesStr);
					if (nearLink.getSensorList() == null || nearLink.getSensorList().size() == 0) {
						// match
						ListIterator<SensorInfo> sensorIterator = linkSensorList.listIterator();
						SensorInfo nearestSensor = sensorIterator.next();
						double minDis = Geometry.calculateDistance(nearNode.getLocation(), nearestSensor.getLocation());
						while(sensorIterator.hasNext()) {
							SensorInfo otherSensor = sensorIterator.next();
							double dis = Geometry.calculateDistance(nearNode.getLocation(), otherSensor.getLocation());
							if (dis < minDis) {
								nearestSensor = otherSensor;
							}
						}
						nearLink.addSensor(nearestSensor);
					}
				}
			}
			
			long nodeId3 = link.getNonRefNodeId();
			LinkedList<Long> adjList2 = adjNodeList.get(nodeId3);
			ListIterator<Long> adjIterator2 = adjList1.listIterator();
			while(adjIterator2.hasNext()) {
				long nodeId4 = adjIterator2.next();
				String nodesStr = nodeId3 + "," + nodeId4;
				RDFNodeInfo nearNode = nodeMap.get(nodeId4);
				if (nodeToLink.containsKey(nodesStr)) {
					RDFLinkInfo nearLink = nodeToLink.get(nodesStr);
					if (nearLink.getSensorList() == null || nearLink.getSensorList().size() == 0) {
						// match
						ListIterator<SensorInfo> sensorIterator = linkSensorList.listIterator();
						SensorInfo nearestSensor = sensorIterator.next();
						double minDis = Geometry.calculateDistance(nearNode.getLocation(), nearestSensor.getLocation());
						while(sensorIterator.hasNext()) {
							SensorInfo otherSensor = sensorIterator.next();
							double dis = Geometry.calculateDistance(nearNode.getLocation(), otherSensor.getLocation());
							if (dis < minDis) {
								nearestSensor = otherSensor;
							}
						}
						nearLink.addSensor(nearestSensor);
					}
				}
			}
		}
		System.out.println("third round finish!");
	}
	
	private static void fetchSensor() {
		System.out.println("fetch sensor...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "SELECT link_id, onstreet, fromstreet, start_lat_long, direction FROM highway_congestion_config ";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				LocationInfo location = new LocationInfo(geom.getPoint()[1], geom.getPoint()[0], 0);
				int direction = res.getInt(5);
				if (!sensorDuplicate.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, location, direction);
					sensorDuplicate.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch sensor finish!");
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
				String[] nodes = strLine.split("\\|");
				
				long 			nodeId 	= Long.parseLong(nodes[0]);
				String[]		locStr 	= nodes[1].split(",");
				int 			zLevel	= Integer.parseInt(nodes[2]);
				LocationInfo 	location= new LocationInfo(Double.parseDouble(locStr[0]), Double.parseDouble(locStr[1]), zLevel);
				
				RDFNodeInfo RDFNode = new RDFNodeInfo(nodeId, location);
				
				nodeMap.put(nodeId, RDFNode);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
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
				String[] nodes = strLine.split("\\|");
				
				long 	linkId 			= Long.parseLong(nodes[0]);
				String 	streetName 		= nodes[1];
				long 	refNodeId 		= Long.parseLong(nodes[2]);
				long 	nonRefNodeId 	= Long.parseLong(nodes[3]);
				int 	functionalClass = Integer.parseInt(nodes[4]);
				String 	travelDirection 		= nodes[5];
				int 	speedCategory 	= Integer.parseInt(nodes[6]);
				boolean ramp 			= nodes[7].equals("Y") ? true : false;
				boolean tollway 		= nodes[8].equals("Y") ? true : false;
				boolean carpoolRoad		= nodes[9].equals("Y") ? true : false;
				boolean carpools		= nodes[10].equals("Y") ? true : false;
				boolean expressLane		= nodes[11].equals("Y") ? true : false;
				
				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, streetName, refNodeId, nonRefNodeId, 
						functionalClass, travelDirection, ramp, tollway, carpoolRoad, speedCategory, 
						carpools, expressLane);
				
				LinkedList<LocationInfo> pointsList = new LinkedList<LocationInfo>();
				String[] pointsListStr		= nodes[12].split(";");
				for(int i = 0; i < pointsListStr.length; i++) {
					String[] locStr = pointsListStr[i].split(",");
					double lat = Double.parseDouble(locStr[0]);
					double lon = Double.parseDouble(locStr[1]);
					int z = Integer.parseInt(locStr[2]);
					LocationInfo loc = new LocationInfo(lat, lon, z);
					pointsList.add(loc);
				}
				RDFLink.setPointsList(pointsList);
				
				// add direction
				RDFNodeInfo refNode = nodeMap.get(refNodeId);
				RDFNodeInfo nonRefNode = nodeMap.get(nonRefNodeId);
				String allDir = "";
				if(travelDirection.equals("T"))
					allDir = String.valueOf(Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation()));
				else if(travelDirection.equals("F"))
					allDir = String.valueOf(Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation()));
				else if(travelDirection.equals("B")) {
					allDir = String.valueOf(Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation()));
					allDir += "," + String.valueOf(Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation()));
				}
				/**
				 * need fix
				 */
				//RDFLink.setAllDirection(allDir);
				
				linkList.add(RDFLink);
				
				// add connect
				if (adjNodeList.containsKey(refNodeId)) {
					LinkedList<Long> tempList = adjNodeList.get(refNodeId);
					if (!tempList.contains(nonRefNodeId))
						tempList.add(nonRefNodeId);
				} else {
					LinkedList<Long> newList = new LinkedList<Long>();
					newList.add(nonRefNodeId);
					adjNodeList.put(refNodeId, newList);
				}

				if (adjNodeList.containsKey(nonRefNodeId)) {
					LinkedList<Long> tempList = adjNodeList.get(nonRefNodeId);
					if (!tempList.contains(refNodeId))
						tempList.add(refNodeId);
				} else {
					LinkedList<Long> newList = new LinkedList<Long>();
					newList.add(refNodeId);
					adjNodeList.put(nonRefNodeId, newList);
				}
				
				String nodesStr1 = refNodeId + "," + nonRefNodeId;
				String nodesStr2 = nonRefNodeId + "," + refNodeId;
				if (!nodeToLink.containsKey(nodesStr1))
					nodeToLink.put(nodesStr1, RDFLink);
				if (!nodeToLink.containsKey(nodesStr2))
					nodeToLink.put(nodesStr2, RDFLink);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
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
	
	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlLinkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo link 	= iterator.next();
				long linkId 		= link.getLinkId();
				String streetName 	= link.getStreetName();
				long refNodeId 		= link.getRefNodeId();
				long nonRefNodeId 	= link.getNonRefNodeId();
				/**
				 * need fix
				 */
				//String allDirection		= link.getAllDirection();
				String allDirection	= "";
				int functionalClass = link.getFunctionalClass();
				String travelDirection 	= link.getTravelDirection();
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpoolRoad 	= link.isCarpoolRoad();
				boolean carpools 	= link.isCarpools();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointsList();
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Name:" 		+ streetName + "\r\n";
				kmlStr += "Ref:" 			+ refNodeId + "\r\n";
				kmlStr += "Nonref:" 		+ nonRefNodeId + "\r\n";
				kmlStr += "AllDir:" 		+ allDirection + "\r\n";
				kmlStr += "Class:" 		+ functionalClass + "\r\n";
				kmlStr += "Category:" 	+ speedCategory + "\r\n";
				kmlStr += "TraDir:" 		+ travelDirection + "\r\n";
				kmlStr += "Ramp:" 		+ ramp + "\r\n";
				kmlStr += "Tollway:" 	+ tollway + "\r\n";
				kmlStr += "CarpoolRoad:" 	+ carpoolRoad + "\r\n";
				kmlStr += "Carpools:" 	+ carpools + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				ListIterator<LocationInfo> pIterator = pointsList.listIterator();
				while(pIterator.hasNext()) {
					LocationInfo loc = pIterator.next();
					kmlStr += loc.getLongitude()+ "," + loc.getLatitude()+ "," + loc.getZLevel() + " ";
				}
				kmlStr += "</coordinates></LineString></Placemark>\n";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}
	
	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager.getConnection(urlHome, userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;

	}
}
