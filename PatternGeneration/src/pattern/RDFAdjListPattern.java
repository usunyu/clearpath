package pattern;

import java.io.*;
import java.sql.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import objects.*;

public class RDFAdjListPattern {

	/**
	 * @param file
	 */
	static String root 				= "file";
	static String linkFile 			= "RDF_Link.txt";
	static String nodeFile 			= "RDF_Node.txt";
	static String matchSensorFile	= "RDF_Sensor_Match.txt";
	static String adjListFile		= "RDF_AdjList.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param args
	 */
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	static double[] speedCategory = {-1, 80, 65, 55, 41, 31, 21, 15, 6};
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static LinkedList<RDFNodeInfo> nodeList = new LinkedList<RDFNodeInfo>();
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param sensor
	 */
	static LinkedList<SensorInfo> matchSensorList = new LinkedList<SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	/**
	 * @param pattern
	 */
	static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	static HashMap<String, RDFLinkInfo> nodeToLink = new HashMap<String, RDFLinkInfo>();
	
	public static void main(String[] args) {
		readNodeFile();
		readLinkFile();
		fetchSensor();
		readMatchSensor();
		fetchSensorPattern(8, 0);
		buildAdjList();
		createPattern();
		createAdjList(0);
	}
	
	private static void createAdjList(int day) {
		System.out.println("create adj list...");
		try {
			String[] file = adjListFile.split("\\.");
			adjListFile = file[0] + "_" + days[day] + "." + file[1];
			FileWriter fstream = new FileWriter(root + "/" + adjListFile);
			BufferedWriter out = new BufferedWriter(fstream);
			ListIterator<RDFNodeInfo> nodeIterator = nodeList.listIterator();
			while(nodeIterator.hasNext()) {
				RDFNodeInfo nodeInfo = nodeIterator.next();
				long nodeId = nodeInfo.getNodeId();
				String strLine = "n" + nodeId + "|";
				
				ArrayList<Long> toList = adjList.get(nodeId);
				if(toList == null || toList.isEmpty()) {
					System.err.println("warning: " + nodeId + " does not have adj node");
					continue;
				}
				
				for(int j = 0; j < toList.size(); j++) {
					long toNodeId = toList.get(j);
					String nodesStr = nodeId + "," + toNodeId;
					RDFLinkInfo link = nodeToLink.get(nodesStr);
					if(link == null) {
						System.err.println(nodesStr + " no associated link found");
						continue;
					}
					int[] pattern = link.getPattern();
					if(link.getFunctionalClass() == 5) {
						strLine += "n" + toNodeId + "(F):" + pattern[0] + ";";
					}
					else {
						strLine += "n" + toNodeId + "(V):";
						for(int i = 0; i < 60; i++) {
							strLine += pattern[i] + ",";
						}
						strLine += ";";
					}
				}
				strLine += "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("create adj list finish!");
	}
	
	private static void createPattern() {
		System.out.println("create pattern...");
		int debug = 0;
		ListIterator<RDFLinkInfo> linkIterator = linkList.listIterator();
		try {
			while(linkIterator.hasNext()) {
				debug++;
				RDFLinkInfo link = linkIterator.next();

				int[] pattern;
				// if the link is highway
				if(link.getFunctionalClass() == 1 || link.getFunctionalClass() == 2)
					pattern = getHighwayPattern(link);
				else 	// the link is arterial
					pattern = getArterialPattern(link);
				link.setPattern(pattern);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("createPattern: debug code: " + debug);
		}
		System.out.println("create pattern finish!");		
	}
	
	private static int[] getArterialPattern(RDFLinkInfo link) {
		int[] pattern;
		double dis = getLength(link.getPointsList());
		boolean fixed = false;
		if(link.getFunctionalClass() == 5)
			fixed = true;
		if(fixed) {
			double speed = getSpeed(link);
			pattern = new int[1];
			pattern[0] = (int) Math.round(dis / speed * 60 * 60);
		}
		else {
			pattern = new int[60];
			for(int i = 0; i < 60; i++) {
				double speed = getSpeed(link, i);
				pattern[i] = (int) Math.round( dis / speed * 60 * 60 );
			}
		}
		return pattern;
	}
	
	private static int[] getHighwayPattern(RDFLinkInfo link) {
		int[] pattern = new int[60];
		double dis = getLength(link.getPointsList());
		LinkedList<SensorInfo> sensorList = link.getSensorList();
		if(sensorList != null && sensorList.size() != 0) {
			ListIterator<SensorInfo> sensorIt = sensorList.listIterator();
			int[] count = new int[60];
			double[] speedPattern = new double[60];
			while(sensorIt.hasNext()) {
				SensorInfo sensor = sensorIt.next();
				double[] speedList = sensor.getPattern();
				if(speedList == null) // don't have valid speed pattern
					continue;
				for(int i = 0; i < 60; i++) {
					double speed = speedList[i];
					if(speed > 0) {	// count this speed
						speedPattern[i] += speed;
						count[i]++;
					}
				}
			}
			for(int i = 0; i < 60; i++) {
				// calculate average speed
				if(count[i] > 0)
					speedPattern[i] /= count[i];
			}
			for(int i = 0; i < 60; i++) {
				double speed;
				if(speedPattern[i] > 0)
					speed = speedPattern[i];
				else
					speed = getSpeed(link, i);
				pattern[i] = (int) Math.round( dis / speed * 60 * 60 );
			}
		}
		else {
			for(int i = 0; i < 60; i++) {
				double speed = getSpeed(link, i);
				pattern[i] = (int) Math.round( dis / speed * 60 * 60 );
			}
		}
		return pattern;
	}
	
	private static double getSpeed(RDFLinkInfo link) {
		int funcClass = link.getFunctionalClass();
		int speedCat = link.getSpeedCategory();
		double penalty = 1.;
		if(funcClass == 3 || funcClass == 4 || funcClass == 5)
			penalty *= 0.75;
		double originalSpeed = speedCategory[speedCat];
		return penalty * originalSpeed;
	}
	
	private static double getSpeed(RDFLinkInfo link, int index) {
		int funcClass = link.getFunctionalClass();
		int speedCat = link.getSpeedCategory();
		double penalty = 1.;
		if ((index >= 3 && index <= 12) || (index >= 43 && index <= 52))
			penalty *= 0.9;
		if(funcClass == 3 || funcClass == 4 || funcClass == 5)
			penalty *= 0.75;
		double originalSpeed = speedCategory[speedCat];
		//if (funcClass == 5)
		//	originalSpeed = 8;
		return penalty * originalSpeed;
	}
	
	private static double getLength(LinkedList<LocationInfo> pointsList) {
		ListIterator<LocationInfo> pointIt = pointsList.listIterator();
		LocationInfo lastLoc = null;
		double dis = 0;
		while(pointIt.hasNext()) {
			LocationInfo location = pointIt.next();
			if(lastLoc == null) {
				lastLoc = location;
				continue;
			}
			dis += Geometry.calculateDistance(lastLoc, location);
			lastLoc = location;
		}
		return dis;
	}
	
	private static void buildAdjList() {
		System.out.println("build adj list...");
		ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
		while(iterator.hasNext()) {
			RDFLinkInfo link = iterator.next();
			
			long refNode = link.getRefNodeId();
			long nonRefNode = link.getNonRefNodeId();
			String travelDir = link.getTravelDirection();
			if(travelDir.equals("T")) {	//from nonref to ref
				if(!adjList.containsKey(nonRefNode)) {
					ArrayList<Long> toList = new ArrayList<Long>();
					toList.add(refNode);
					adjList.put(nonRefNode, toList);
				}
				else {
					ArrayList<Long> toList = adjList.get(nonRefNode);
					toList.add(refNode);
				}
			}
			else if(travelDir.equals("F")) { //from ref to nonref
				if(!adjList.containsKey(refNode)) {
					ArrayList<Long> toList = new ArrayList<Long>();
					toList.add(nonRefNode);
					adjList.put(refNode, toList);
				}
				else {
					ArrayList<Long> toList = adjList.get(refNode);
					toList.add(nonRefNode);
				}
			}
			else if(travelDir.equals("B")) { // bi-direction
				if(!adjList.containsKey(nonRefNode)) {
					ArrayList<Long> toList = new ArrayList<Long>();
					toList.add(refNode);
					adjList.put(nonRefNode, toList);
				}
				else {
					ArrayList<Long> toList = adjList.get(nonRefNode);
					toList.add(refNode);
				}
				if(!adjList.containsKey(refNode)) {
					ArrayList<Long> toList = new ArrayList<Long>();
					toList.add(nonRefNode);
					adjList.put(refNode, toList);
				}
				else {
					ArrayList<Long> toList = adjList.get(refNode);
					toList.add(nonRefNode);
				}
			}
			else {
				System.err.println("undefine travel direction!");
			}
			
		}
		System.out.println("build adj list finish!");
	}
	
	private static void fetchSensorByQuery(String query, int month, int day) {
		String tableName;
		switch(month) {
			case 7:
				tableName = "highway_averages_august_clean";
				break;
			case 8:
				tableName = "highway_averages_sep_clean";
				break;
			default:
				tableName = "null";
				break;
		}
		
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			
			sql = "SELECT link_id, speed, time FROM " + tableName + " WHERE day='" + days[day] + "' AND link_id IN ( " + query + " )";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt("link_id");
				if(!sensorMap.containsKey(sensorId))
					System.out.println("did not find sensor in the hashmap");
				
				SensorInfo sensor = sensorMap.get(sensorId);
				double speed = res.getDouble("speed");
				String time = res.getString("time");
				int index = UtilClass.getIndex(time);
				sensor.addPattern(speed, index);
			}
			
			res.close();
			pstatement.close();
			con.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void fetchSensorPattern(int month, int day) {
		System.out.println("fetch sensor pattern...");
		// make sensorMap just contain matchedSenor
		sensorMap = new HashMap<Integer, SensorInfo>();	// free old memory
		ListIterator<SensorInfo> msIt = matchSensorList.listIterator();
		while(msIt.hasNext()) {
			SensorInfo sensor = msIt.next();
			sensorMap.put(sensor.getSensorId(), sensor);
		}
		int debug = 0;
		try {
			msIt = matchSensorList.listIterator();
			// build sql query
			StringBuffer sensorStringBuffer = new StringBuffer();
			int i = 0;
			while(msIt.hasNext()) {
				debug++;
				SensorInfo sensor = msIt.next();
				if(i++ == 0)
					sensorStringBuffer.append(sensor.getSensorId());
				else
					sensorStringBuffer.append(", " + sensor.getSensorId());
				
				if(i % 500 == 0) {	// query will throw exception if too long
					String nodeQuery = sensorStringBuffer.toString();
					fetchSensorByQuery(nodeQuery, month, day);
					i = 0;
					sensorStringBuffer = new StringBuffer();
				}
				
				if(debug % 1000 == 0)
					System.out.println((double)debug / matchSensorList.size() * 100 + "% finish!");
			}
			if(!sensorStringBuffer.toString().equals("")) {	// process rest
				String nodeQuery = sensorStringBuffer.toString();
				fetchSensorByQuery(nodeQuery, month, day);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchSensorPattern: debug code: " + debug);
		}
		System.out.println("fetch sensor pattern finish!");		
	}
	
	private static void readMatchSensor() {
		System.out.println("read match sensor...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + matchSensorFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|");
				
				long 	linkId 		= Long.parseLong(nodes[0]);
				String[] sensorStr	= nodes[1].split(",");
				
				RDFLinkInfo RDFLink = linkMap.get(linkId);
				for(int i = 0; i < sensorStr.length; i++) {
					int sensorId = Integer.parseInt(sensorStr[i]);
					SensorInfo sensor 	= sensorMap.get(sensorId);
					RDFLink.addSensor(sensor);
					
					if(!matchSensorList.contains(sensor))
						matchSensorList.add(sensor);
				}

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readMatchSensor: debug code: " + debug);
		}
		System.out.println("read match sensor finish!");
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
				
				if(!sensorMap.containsKey(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, location, direction);
					sensorMap.put(sensorId, sensorInfo);
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
				String 	direction 		= nodes[5];
				int 	speedCategory 	= Integer.parseInt(nodes[6]);
				boolean ramp 			= nodes[7].equals("T") ? true : false;
				boolean tollway 		= nodes[8].equals("T") ? true : false;
				boolean carpool 		= nodes[9].equals("T") ? true : false;
				
				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, streetName, refNodeId, nonRefNodeId, functionalClass, direction, ramp, tollway, carpool, speedCategory );
				
				LinkedList<LocationInfo> pointsList = new LinkedList<LocationInfo>();
				String[] pointsListStr		= nodes[10].split(";");
				for(int i = 0; i < pointsListStr.length; i++) {
					String[] locStr = pointsListStr[i].split(",");
					double lat = Double.parseDouble(locStr[0]);
					double lon = Double.parseDouble(locStr[1]);
					int z = Integer.parseInt(locStr[2]);
					LocationInfo loc = new LocationInfo(lat, lon, z);
					pointsList.add(loc);
				}
				
				RDFLink.setPointsList(pointsList);
				
				linkList.add(RDFLink);
				linkMap.put(linkId, RDFLink);
				
				if(direction.equals("T")) {
					String nodeStr = nonRefNodeId + "," + refNodeId;
					nodeToLink.put(nodeStr, RDFLink);
				}
				else if(direction.equals("F")) {
					String nodeStr = refNodeId + "," + nonRefNodeId;
					nodeToLink.put(nodeStr, RDFLink);
				}
				else if(direction.equals("B")) {
					String nodeStr = nonRefNodeId + "," + refNodeId;
					nodeToLink.put(nodeStr, RDFLink);
					nodeStr = refNodeId + "," + nonRefNodeId;
					nodeToLink.put(nodeStr, RDFLink);
				}
				else {
					System.err.println("direction undefined");
				}

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
				
				nodeList.add(RDFNode);
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
