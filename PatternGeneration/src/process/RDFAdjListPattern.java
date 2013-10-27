package process;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;
import function.*;

public class RDFAdjListPattern {

	/**
	 * @param file
	 */
	static String root 				= "file";
	static String adjListFile		= "RDF_AdjList.csv";
	/**
	 * @param args
	 */
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	static double[] speedCategory = {-1, 80, 65, 55, 41, 31, 21, 15, 6};
	static String SEPARATION		= ",";
	static String VERTICAL		= "|";
	static String COLON			= ":";
	static String SEMICOLON		= ";";
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param sensor
	 */
	static HashMap<Integer, SensorInfo> matchSensorMap = new HashMap<Integer, SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	/**
	 * @param pattern
	 */
	static HashMap<Long, ArrayList<Long>> adjList = new HashMap<Long, ArrayList<Long>>();
	static HashMap<String, RDFLinkInfo> nodeToLink = new HashMap<String, RDFLinkInfo>();
	
	public static void main(String[] args) {
		RDFInput.readNodeFile(nodeMap);
		
		RDFInput.readLinkFile(linkMap, nodeMap, nodeToLink);
		RDFInput.readLinkGeometry(linkMap);
		
		RDFInput.fetchSensor(sensorMap);
		RDFInput.readMatchSensor(linkMap, sensorMap, matchSensorMap);
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

			for(RDFNodeInfo nodeInfo : nodeMap.values()) {
				long nodeId = nodeInfo.getNodeId();
				String strLine = "n" + nodeId + VERTICAL;
				
				ArrayList<Long> toList = adjList.get(nodeId);
				if(toList == null || toList.isEmpty()) {
					System.err.println("warning: " + nodeId + " does not have adj node");
					continue;
				}
				
				for(int j = 0; j < toList.size(); j++) {
					long toNodeId = toList.get(j);
					String nodesStr = nodeId + SEPARATION + toNodeId;
					RDFLinkInfo link = nodeToLink.get(nodesStr);
					if(link == null) {
						System.err.println(nodesStr + " no associated link found");
						continue;
					}
					int[] pattern = link.getPattern();
					if(link.getFunctionalClass() == 5) {
						strLine += "n" + toNodeId + "(F)" + COLON + pattern[0] + SEMICOLON;
					}
					else {
						strLine += "n" + toNodeId + "(V)" + COLON;
						for(int i = 0; i < 60; i++) {
							strLine += pattern[i] + SEPARATION;
						}
						strLine += SEMICOLON;
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
		try {
			for(RDFLinkInfo link : linkMap.values()) {
				debug++;

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
		double dis = getLength(link.getPointList());
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
		double dis = getLength(link.getPointList());
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
		for(RDFLinkInfo link : linkMap.values()) {
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
			con = Database.getConnection();
			
			sql = "SELECT link_id, speed, time FROM " + tableName + " WHERE day='" + days[day] + "' AND link_id IN ( " + query + " )";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt("link_id");
				if(!matchSensorMap.containsKey(sensorId))
					System.out.println("did not find sensor in the hashmap");
				
				SensorInfo sensor = matchSensorMap.get(sensorId);
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
		int debug = 0;
		try {
			// build sql query
			StringBuffer sensorStringBuffer = new StringBuffer();
			int i = 0;
			for(SensorInfo sensor : matchSensorMap.values()) {
				debug++;
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
					System.out.println((double)debug / matchSensorMap.size() * 100 + "% finish!");
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
}
