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
		readAverageFile(7, 0);
		generatePatternKML(102, 7, 0);
	}
	
	private static void generatePatternKML(int time, int month, int day) {
		System.out.println("generate pattern...");
		int error = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + UtilClass.getFullTime(time) + "_" + months[month] + "_" + days[day] + "_" + highwayPatternKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < linkList.size(); i++) {
				error++;
				LinkInfo link = linkList.get(i);
				int intId = link.getIntLinkId();

				ArrayList<SensorInfo> localSensorList = link.getSensorList();
				String sensorStr = "null";

				ArrayList<double[]> speedArrayList = new ArrayList<double[]>();

				for (int j = 0; j < localSensorList.size(); j++) {
					if (j == 0) {
						int sensorId = localSensorList.get(j).getSensorId();
						double[] speedArray = sensorSpeedPattern.get(sensorId);
						if(speedArray != null)
							speedArrayList.add(speedArray);
						sensorStr = String.valueOf(sensorId);
					} else {
						int sensorId = localSensorList.get(j).getSensorId();
						sensorStr = sensorStr + "," + String.valueOf(sensorId);
						double[] speedArray = sensorSpeedPattern.get(sensorId);
						if(speedArray != null)
							speedArrayList.add(speedArray);
					}
				}

				double[] finalSpeedArray = new double[288];

				for (int j = 0; j < finalSpeedArray.length; j++) {
					double allSpeed = 0;
					int num = 0;
					for (int k = 0; k < speedArrayList.size(); k++) {
						double speed = speedArrayList.get(k)[j];
						if (speed > 0) {
							allSpeed += speed;
							num++;
						}
					}
					double finalSpeed = 0;

					if (allSpeed > 0) {
						finalSpeed = allSpeed / num;
						finalSpeedArray[j] = finalSpeed;
					} else {
						finalSpeedArray[j] = finalSpeed;
					}
				}

				String patternStr = "";

				if (localSensorList.size() > 0)
					for (int j = 0; j < finalSpeedArray.length; j++) {
						if (j == 0) {
							if(finalSpeedArray[j] > 0)
								patternStr = "\r\n" + UtilClass.getFullTime(j) + " : " + finalSpeedArray[j];
						}
						else {
							if(finalSpeedArray[j] > 0)
								patternStr = patternStr + "\r\n" + UtilClass.getFullTime(j) + " : " + finalSpeedArray[j];
						}
					}

				String colorStr = "#FFFFFFFF";
				if (localSensorList.size() > 0)
					colorStr = getColor(finalSpeedArray[time]);

				ArrayList<PairInfo> nodeList = link.getNodeList();
				String kmlStr = "<Placemark><name>Link:" + intId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				kmlStr += ", Name:" + link.getStreetName();
				kmlStr += patternStr;
				// kmlStr += ", Start:" + link.getStartNode();
				// kmlStr += ", End:" + link.getEndNode();
				// kmlStr += ", Dir:" + link.getAllDir();
				// kmlStr += ", Func:" + link.getFuncClass();
				// kmlStr += ", Speed:" + link.getSpeedCat();
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
				}
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<color>" + colorStr + "</color>";
				kmlStr += "<width>2</width>";
				kmlStr += "</LineStyle></Style></Placemark>\n";
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error Code: " + error);
		}
		System.out.println("generate pattern finish!");
	}
	
	private static String getColor(double speed) {
		if (speed < 10) // blue
			return "64000000";
		else if (speed >= 10 && speed < 25) // red
			return "FF1400FF";
		else if (speed >= 25 && speed < 50) // yellow
			return "FF14F0FF";
		else if (speed >= 50) // green
			return "#FF00FF14";
		return "#FFFFFFFF";
	}
	
	private static void readAverageFile(int month, int day) {
		System.out.println("read average file...");
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + months[month] + "_" + days[day] + "_" + averageSpeedFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(";");
				int sensorId = Integer.parseInt(nodes[0]);
				double speed = Double.parseDouble(nodes[1]);
				String time = nodes[2];

				if (sensorSpeedPattern.containsKey(sensorId)) {
					double[] tempArray = sensorSpeedPattern.get(sensorId);
					int index = UtilClass.getFullTimeIndex(time);
					if (index >= 0 && index <= 287)
						tempArray[index] = speed;
				} else {
					double[] newArray = new double[288];
					int index = UtilClass.getFullTimeIndex(time);
					if (index >= 0 && index <= 287)
						newArray[index] = speed;
					sensorSpeedPattern.put(sensorId, newArray);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read average file finish!");
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
