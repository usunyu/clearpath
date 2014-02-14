package process;

import java.io.*;
import java.sql.*;
import java.util.*;

import data.*;
import objects.*;
import function.*;

public class RDFAdjListPattern {

	/**
	 * @param file
	 */
	static String root 			= "file";
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
	
	public static void main(String[] args) {
		//RDFInput.readNodeFile(RDFData.nodeMap);
		
		//RDFInput.readLinkFile(RDFData.linkMap, RDFData.nodeMap, nodeToLink);
		//RDFInput.readLinkGeometry(RDFData.linkMap);
		
		//RDFInput.fetchSensor(RDFData.sensorMatchMap);
		//RDFInput.readMatchSensor(RDFData.linkMap, RDFData.sensorMatchMap);
		
		fetchSensorPattern(8, 0, RDFData.sensorMatchMap);
		
		createPattern(RDFData.linkMap);
		createAdjList(0, RDFData.nodeMap, RDFData.nodeAdjList, RDFData.nodeToLinkMap);
	}
	
	private static void createAdjList(int day, HashMap<Long, RDFNodeInfo> nodeMap, HashMap<Long, LinkedList<Long>> nodeAdjList,
			HashMap<String, RDFLinkInfo> nodeToLinkMap) {
		System.out.println("create adj list...");
		try {
			String[] file = adjListFile.split("\\.");
			adjListFile = file[0] + "_" + days[day] + "." + file[1];
			FileWriter fstream = new FileWriter(root + "/" + adjListFile);
			BufferedWriter out = new BufferedWriter(fstream);

			for(RDFNodeInfo nodeInfo : nodeMap.values()) {
				long nodeId = nodeInfo.getNodeId();
				String strLine = "n" + nodeId + VERTICAL;
				
				LinkedList<Long> toList = nodeAdjList.get(nodeId);
				if(toList == null || toList.isEmpty()) {
					System.err.println("warning: " + nodeId + " does not have adj node");
					continue;
				}
				
				for(long toNodeId : toList) {
					String nodesStr = nodeId + SEPARATION + toNodeId;
					RDFLinkInfo link = nodeToLinkMap.get(nodesStr);
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
	
	private static void createPattern(HashMap<Long, RDFLinkInfo> linkMap) {
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
	
	private static void fetchSensorByQuery(String query, int month, int day, HashMap<Integer, SensorInfo> sensorMatchMap) {
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
				if(!sensorMatchMap.containsKey(sensorId))
					System.out.println("did not find sensor in the hashmap");
				
				SensorInfo sensor = sensorMatchMap.get(sensorId);
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
	
	private static void fetchSensorPattern(int month, int day, HashMap<Integer, SensorInfo> sensorMatchMap) {
		System.out.println("fetch sensor pattern...");
		int debug = 0;
		try {
			// build sql query
			StringBuffer sensorStringBuffer = new StringBuffer();
			int i = 0;
			for(SensorInfo sensor : sensorMatchMap.values()) {
				debug++;
				if(i++ == 0)
					sensorStringBuffer.append(sensor.getSensorId());
				else
					sensorStringBuffer.append(", " + sensor.getSensorId());
				
				if(i % 500 == 0) {	// query will throw exception if too long
					String nodeQuery = sensorStringBuffer.toString();
					fetchSensorByQuery(nodeQuery, month, day, sensorMatchMap);
					i = 0;
					sensorStringBuffer = new StringBuffer();
				}
				
				if(debug % 1000 == 0)
					System.out.println((double)debug / sensorMatchMap.size() * 100 + "% finish!");
			}
			if(!sensorStringBuffer.toString().equals("")) {	// process rest
				String nodeQuery = sensorStringBuffer.toString();
				fetchSensorByQuery(nodeQuery, month, day, sensorMatchMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchSensorPattern: debug code: " + debug);
		}
		System.out.println("fetch sensor pattern finish!");		
	}
}
