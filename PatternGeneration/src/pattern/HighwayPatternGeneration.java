package pattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class HighwayPatternGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	static String highwayLinkFile = "Highway_Link.txt";
	static String highwayLinkKML = "Highway_Link.kml";
	static String highwaySensorKML = "Highway_Sensor.kml";
	static String allHighwaySensorKML = "All_Highway_Sensor.kml";
	static String averageSpeedFile = "Average_Speed.txt";
	static String highwayPatternKML = "Highway_Pattern.kml";
	/**
	 * @param arguments
	 */
	static double searchDistance = 0.15;
	static double corssSearchDistance = 0.15;
	static double noDirSearchDistance = 0.01;
	static int devide = 10;
	static int thirdRoundTime = 3;
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param link
	 */
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static HashMap<Integer, LinkInfo> linkMap = new HashMap<Integer, LinkInfo>();
	/**
	 * @param sensor
	 */
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static ArrayList<SensorInfo> matchSensorList = new ArrayList<SensorInfo>();
	static HashSet<Integer> checkMatchSensor = new HashSet<Integer>();
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();
	/**
	 * @param node
	 */
	static HashMap<Integer, PairInfo> nodePositionMap = new HashMap<Integer, PairInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Integer, ArrayList<Integer>> adjNodeList = new HashMap<Integer, ArrayList<Integer>>();
	// two nodes decide one link
	static HashMap<String, LinkInfo> nodeToLink = new HashMap<String, LinkInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readLinkFile();
		fetchSensor();
		matchLinkSensor();
	}
	
	private static void firstRoundMatch() {
		System.out.println("first round...");
		for (int i = 0; i < linkList.size(); i++) {
			LinkInfo link = linkList.get(i);
			ArrayList<PairInfo> nodeList = link.getNodeList();
			for (double step = searchDistance / devide; step < searchDistance; step += step) {
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node1 = nodeList.get(j);
					for (int k = 0; k < sensorList.size(); k++) {
						SensorInfo sensor = sensorList.get(k);
						PairInfo node2 = sensor.getNode();
						// same direction
						String allDir = link.getAllDir();
						String[] dirList = allDir.split(",");
						for (int d = 0; d < dirList.length; d++) {
							if (sensor.getDirection() == Integer.parseInt(dirList[d])) {
								double distance = DistanceCalculator.CalculationByDistance(node1, node2);
								// in the search area
								if (distance < step) {
									// match sensor
									if (!link.containSensor(sensor))
										link.addSensor(sensor);
									if (!checkMatchSensor.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										checkMatchSensor.add(sensor.getSensorId());
									}
								}
							}
						}
					}
				}
			}
			if (i % 1000 == 0 || i == linkList.size())
				System.out.println((float) i / linkList.size() * 100 + "%");
		}
		System.out.println("first round finish!");
	}

	private static void secondRoundMatch() {
		System.out.println("second round...");
		for (int i = 0; i < linkList.size(); i++) {
			LinkInfo link = linkList.get(i);
			if (link.getSensorList().size() > 0)
				continue;
			ArrayList<PairInfo> nodeList = link.getNodeList();
			for (double step = corssSearchDistance / devide; step < searchDistance; step += step) {
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node1 = nodeList.get(j);
					for (int k = 0; k < sensorList.size(); k++) {
						SensorInfo sensor = sensorList.get(k);
						PairInfo node2 = sensor.getNode();
						// same direction
						String allDir = link.getAllDir();
						String[] dirList = allDir.split(",");
						for (int d = 0; d < dirList.length; d++) {
							// 0 : 3, 1 : 2
							if ((sensor.getDirection() == 0 && Integer.parseInt(dirList[d]) == 3)
									|| (sensor.getDirection() == 3 && Integer.parseInt(dirList[d]) == 0)
									|| (sensor.getDirection() == 1 && Integer.parseInt(dirList[d]) == 2)
									|| (sensor.getDirection() == 2 && Integer.parseInt(dirList[d]) == 1)) {
								double distance = DistanceCalculator.CalculationByDistance(node1, node2);
								// in the search area
								if (distance < step) {
									// match sensor
									if (!link.containSensor(sensor))
										link.addSensor(sensor);
									if (!checkMatchSensor.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										checkMatchSensor.add(sensor.getSensorId());
									}
								}
							}
						}
					}
				}
			}
			if (i % 1000 == 0 || i == linkList.size())
				System.out.println((float) i / linkList.size() * 100 + "%");
		}
		System.out.println("second round finish!");
	}

	private static void thirdRoundMatch() {
		System.out.println("third round...");
		for (int i = 0; i < linkList.size(); i++) {
			LinkInfo link = linkList.get(i);
			ArrayList<SensorInfo> linkSensorList = link.getSensorList();
			if (linkSensorList.size() == 0)
				continue;
			int refNode = link.getStartNode();
			ArrayList<Integer> refAdjList = adjNodeList.get(refNode);
			for (int j = 0; j < refAdjList.size(); j++) {
				String nodesStr = refNode + "," + refAdjList.get(j);
				PairInfo nearNode = nodePositionMap.get(refAdjList.get(j));
				if (nodeToLink.containsKey(nodesStr)) {
					LinkInfo nearLink = nodeToLink.get(nodesStr);
					if (nearLink.getSensorList().size() == 0) {
						// match
						SensorInfo nearestSensor = linkSensorList.get(0);
						double minDis = DistanceCalculator.CalculationByDistance(nearNode, nearestSensor.getNode());
						for (int k = 1; k < linkSensorList.size(); k++) {
							SensorInfo otherSensor = linkSensorList.get(k);
							double dis = DistanceCalculator.CalculationByDistance(nearNode, otherSensor.getNode());
							if (dis < minDis) {
								nearestSensor = otherSensor;
							}
						}
						nearLink.addSensor(nearestSensor);
					}
				}
			}

			int nrefNode = link.getEndNode();
			ArrayList<Integer> nrefAdjList = adjNodeList.get(nrefNode);
			for (int j = 0; j < nrefAdjList.size(); j++) {
				String nodesStr = nrefNode + "," + nrefAdjList.get(j);
				PairInfo nearNode = nodePositionMap.get(nrefAdjList.get(j));
				if (nodeToLink.containsKey(nodesStr)) {
					LinkInfo nearLink = nodeToLink.get(nodesStr);
					if (nearLink.getSensorList().size() == 0) {
						// match
						SensorInfo nearestSensor = linkSensorList.get(0);
						double minDis = DistanceCalculator.CalculationByDistance(nearNode, nearestSensor.getNode());
						for (int k = 1; k < linkSensorList.size(); k++) {
							SensorInfo otherSensor = linkSensorList.get(k);
							double dis = DistanceCalculator.CalculationByDistance(nearNode, otherSensor.getNode());
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

	private static void forthRoundMatch() {
		System.out.println("forth round...");
		for (int i = 0; i < linkList.size(); i++) {
			LinkInfo link = linkList.get(i);
			if (link.getSensorList().size() > 0)
				continue;
			ArrayList<PairInfo> nodeList = link.getNodeList();
			for (double step = noDirSearchDistance / devide; step < searchDistance; step += step) {
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node1 = nodeList.get(j);
					for (int k = 0; k < sensorList.size(); k++) {
						SensorInfo sensor = sensorList.get(k);
						PairInfo node2 = sensor.getNode();
						double distance = DistanceCalculator.CalculationByDistance(node1, node2);
						// in the search area
						if (distance < step) {
							// match sensor
							if (!link.containSensor(sensor))
								link.addSensor(sensor);
							if (!checkMatchSensor.contains(sensor.getSensorId())) {
								matchSensorList.add(sensor);
								checkMatchSensor.add(sensor.getSensorId());
							}
						}
					}
				}
			}
			if (i % 1000 == 0 || i == linkList.size())
				System.out.println((float) i / linkList.size() * 100 + "%");
		}
		System.out.println("forth round finish!");
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
	
	private static void fetchSensor() {
		System.out.println("fetch sensor...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from highway_congestion_config ";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				if (!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet,
							fromStreet, node, direction);
					checkSensor.add(sensorId);
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
	
	private static void readLinkFile() {
		System.out.println("read link file...");
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + highwayLinkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(";");
				int linkId = Integer.parseInt(nodes[0]);
				String dir = nodes[1];
				String name = nodes[2];
				int funClass = Integer.parseInt(nodes[3]);
				String nodeListString = nodes[4];
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				String[] nodeString = nodeListString.split(":");
				for (int i = 0; i < nodeString.length; i++) {
					String[] loc = nodeString[i].split(",");
					PairInfo node = new PairInfo(Double.parseDouble(loc[1]), Double.parseDouble(loc[0]));
					nodeList.add(node);
				}
				int speedCat = Integer.parseInt(nodes[5]);
				String dirTravel = nodes[6];
				int refNode = Integer.parseInt(nodes[7]);
				int nrefNode = Integer.parseInt(nodes[8]);

				if (adjNodeList.containsKey(refNode)) {
					ArrayList<Integer> tempList = adjNodeList.get(refNode);
					if (!tempList.contains(nrefNode))
						tempList.add(nrefNode);
				} else {
					ArrayList<Integer> newList = new ArrayList<Integer>();
					newList.add(nrefNode);
					adjNodeList.put(refNode, newList);
				}

				if (adjNodeList.containsKey(nrefNode)) {
					ArrayList<Integer> tempList = adjNodeList.get(nrefNode);
					if (!tempList.contains(refNode))
						tempList.add(refNode);
				} else {
					ArrayList<Integer> newList = new ArrayList<Integer>();
					newList.add(refNode);
					adjNodeList.put(nrefNode, newList);
				}

				if (!nodePositionMap.containsKey(refNode))
					nodePositionMap.put(refNode, nodeList.get(0));
				if (!nodePositionMap.containsKey(nrefNode))
					nodePositionMap.put(nrefNode, nodeList.get(1));

				LinkInfo link = new LinkInfo(linkId, funClass, name, refNode, nrefNode, nodeList, dirTravel, speedCat, dir);
				linkList.add(link);
				linkMap.put(linkId, link);

				String nodesStr1 = refNode + "," + nrefNode;
				String nodesStr2 = nrefNode + "," + refNode;
				if (!nodeToLink.containsKey(nodesStr1))
					nodeToLink.put(nodesStr1, link);
				if (!nodeToLink.containsKey(nodesStr2))
					nodeToLink.put(nodesStr2, link);

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read link file finish!");
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
