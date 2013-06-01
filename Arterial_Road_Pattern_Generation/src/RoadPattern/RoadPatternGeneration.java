package RoadPattern;

import java.io.*;
import java.sql.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class RoadPatternGeneration {

	/**
	 * @param args
	 */
	// street
	static String street = "FIGUEROA";
	// parameter
	static double searchDistance = 0.1;
	static int devide = 1000;
	// database
	static String root = "/Users/Sun/Documents/workspace/CleanPath/GeneratedFile";
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<LinkInfo> edgeList = new ArrayList<LinkInfo>();
	static HashMap<Integer, Boolean> checkEdge = new HashMap<Integer, Boolean>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashMap<Integer, Boolean> checkSensor = new HashMap<Integer, Boolean>();
	static HashMap<String, PatternPairInfo> patternMap = new HashMap<String, PatternPairInfo>();
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static ArrayList<LinkInfo> searchList = new ArrayList<LinkInfo>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchSensor();
		
		fetchEdge();

		matchEdgeSensor();
		
		// readFileToMemory();
		// searchStreet();
		// createPattern();
	}
	
	private static void matchEdgeSensor() {
		System.out.println("Match Sensors for " + street);
		
		int notFind = 0;
		int find = 0;
		// first round
		for(int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			System.out.println("processing link id: " + link.getIntLinkId());
			ArrayList<PairInfo> nodeList = link.getNodeList();
			boolean findSensor = false;
			if(nodeList.size() > 2) {
				// examine the intermediate node first
				System.out.println("has more than 2 nodes");
				for(double step = searchDistance / devide; step < searchDistance; step += step) {
					// skip first one and last one
					for(int j = 1; j < nodeList.size() - 1; j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for(int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator.CalculationByDistance(node1, node2);
							if(distance < step) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
								find++;
								System.out.println("find sensor " + sensor.getSensorId() + " for link " + link.getLinkId());
								break;
							}
						}
						if(findSensor)
							break;
					}
					if(findSensor)
						break;
				}
			}
			
			if(!findSensor) {
				// examine the vertex
				System.out.println("has 2 nodes");
				for(double step = searchDistance / devide; step < searchDistance; step += step) {
					for(int j = 0; j < nodeList.size(); j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for(int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator.CalculationByDistance(node1, node2);
							if(distance < step) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
								find++;
								System.out.println("find sensor " + sensor.getSensorId() + " for link " + link.getLinkId());
								break;
							}
						}
						if(findSensor)
							break;
					}
					if(findSensor)
						break;
				}
			}
			
			if(!findSensor) {
				notFind++;
				System.out.println("cannot find sensor for link " + link.getIntLinkId());
			}
		}
		
		// second round
		for(int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			if(link.getSensor() == null) {
				System.out.println("processing link id: " + link.getIntLinkId());
				int startNode = link.getStartNode();
				int endNode = link.getEndNode();
				
			}
		}
		
		System.out.println("Match Sensors Success, " + find + " link has sensor, " + notFind + " link has no sensor");
	}
	
	private static void fetchEdge() {
		try {
			System.out.println("Fetch Edge for " + street);
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			
			sql = "select * from streets_dca1_new where upper(st_name) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int linkId = res.getInt(1);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT)res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for(int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = geomPoints[i * 2 + 1];
					double longitude = geomPoints[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}
				
				String speedCar = res.getString(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);
				LinkInfo link = new LinkInfo(linkId, func_class, stName, refNode, nrefNode, nodeList);
				if(checkEdge.get(linkId) == null) {
					checkEdge.put(linkId, true);
					edgeList.add(link);
				}
			}
			res.close();
			pstatement.close();
			con.close();
			
			System.out.println("Fetch Edge Success!");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void fetchSensor() {
		try {
			System.out.println("Fetch Sensor for " + street);
			
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			// assume this is sensing the specified street
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from arterial_congestion_config " + 
			"where upper(onstreet) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				if(checkSensor.get(sensorId) == null) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, node, direction);
					checkSensor.put(sensorId, true);
					sensorList.add(sensorInfo);
				}
			}
			
			// this maybe sensing the specified street
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from arterial_congestion_config " +
			"where upper(fromstreet) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				if(checkSensor.get(sensorId) == null) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, node, direction);
					checkSensor.put(sensorId, true);
					sensorList.add(sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
			
			System.out.println("Fetch Sensor Success!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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

	private static void createPattern() {
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			System.out.println("create pattern for " + street + " start");
			fstream = new FileWriter("Pattern_" + street + ".txt");
			out = new BufferedWriter(fstream);
			int sum = searchList.size();
			for (int i = 0; i < sum; i++) {
				LinkInfo link = searchList.get(i);
				String patternId = link.getStart_node() + link.getEnd_node();
				PatternPairInfo patternInfo = patternMap.get(patternId);
				String link_id = link.getPureLinkId();
				String st_node = link.getStart_node();
				String ed_node = link.getEnd_node();
				System.out.println("Link_ID:" + link_id + " St_Node:" + st_node
						+ " Ed_Node:" + ed_node);
				out.write("Link_ID:" + link_id + " St_Node:" + st_node
						+ " Ed_Node:" + ed_node + "\n");
				double distance = DistanceCalculator.CalculationByDistance(
						link.getNodes()[0], link.getNodes()[1]);
				int[] intervals = patternInfo.getIntervals();
				for (int j = 0; j < 60 && j < intervals.length; j++) {
					double speed = (double) Math.round(distance / intervals[j]
							* 60 * 60 * 1000 * 100) / 100;
					System.out.print(UtilClass.getStartTime(j) + "-"
							+ UtilClass.getEndTime(j) + ":" + speed + ", ");
					out.write(UtilClass.getStartTime(j) + "-"
							+ UtilClass.getEndTime(j) + ":" + speed + ", ");
				}
				System.out.println();
				out.write("\n");
			}

			out.close();
			fstream.close();
			System.out.println("create pattern for " + street + " finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void searchStreet() {
		System.out.println("search " + street + " start");
		int sum = linkList.size();
		for (int i = 0; i < sum; i++) {
			LinkInfo link = linkList.get(i);
			if (link.getSt_name().contains(street)) {
				// if(link.getLinkId().contains("24009477"))
				// System.out.println(link.toString());
				searchList.add(link);
			}
			if (i % 10000 == 0)
				System.out.println((double) i / sum * 100 + "%");
		}
		System.out.println("search " + street + " finished! find "
				+ searchList.size() + " links");
	}

	private static void readFileToMemory() {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;

		try {
			System.out.println("read from AdjList_Monday.txt start");
			fstream = new FileInputStream(root + "/AdjList_Monday.txt");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			int lineNum = 0;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (strLine.equals("NA")) {
					lineNum++;
					continue;
				}
				String[] nodes = strLine.split(";");
				for (int i = 0; i < nodes.length; i++) {
					// info[0]: n1(V) info[1]: 7931,7931,7931,7931......
					String[] info = nodes[i].split(":");
					// info2[0]: n1 info2[1]: V)
					String[] info2 = info[0].split("\\(");
					int node1 = lineNum;
					int node2 = Integer.parseInt(info2[0].substring(1));
					int[] intervals = new int[60];
					// String flag = info2[1].substring(0, 1);
					// if(flag == "V") { } else { }
					String[] intervalS = info[1].split(",");
					int[] interval = new int[60];
					int intervalNum = 0;
					while (intervalNum < 60 && intervalNum < intervalS.length) {
						interval[intervalNum] = Integer
								.parseInt(intervalS[intervalNum]);
						intervalNum++;
					}
					PatternPairInfo pattern = new PatternPairInfo(node1, node2,
							interval);
					String patternId = "n" + node1 + "n" + node2;
					patternMap.put(patternId, pattern);
				}
				lineNum++;
				if (lineNum % 10000 == 0)
					System.out.print("read line " + lineNum + "\r");
			}
			br.close();
			in.close();
			fstream.close();
			System.out.println("read " + lineNum
					+ " lines from AdjList_Monday.txt finished!");

			System.out.println("read from Edges.csv start");
			fstream = new FileInputStream(root + "/Edges.csv");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			lineNum = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				int func_class = Integer.parseInt(nodes[1]);
				String st_name = nodes[2];
				String st_node = nodes[3];
				String ed_node = nodes[4];
				String linkIndexId = link_id + st_node.substring(1)
						+ ed_node.substring(1);
				PairInfo[] pairs = new PairInfo[2];
				pairs[0] = new PairInfo(Double.parseDouble(nodes[5]),
						Double.parseDouble(nodes[6]));
				pairs[1] = new PairInfo(Double.parseDouble(nodes[7]),
						Double.parseDouble(nodes[8]));
				LinkInfo link = new LinkInfo(linkIndexId, link_id, func_class,
						st_name, st_node, ed_node, pairs, 2);
				linkList.add(link);

				lineNum++;
				if (lineNum % 10000 == 0)
					System.out.print("read line " + lineNum + "\r");
			}
			br.close();
			in.close();
			fstream.close();
			System.out.println("read " + lineNum
					+ " lines from Edges.csv finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
