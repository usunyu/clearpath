package highwayPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import org.jfree.ui.tabbedui.RootEditor;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class EdgeSensorKML {

	/**
	 * @param args
	 */
	// file
	static String root = "file";
	// highway name
	static String highwayName = "I-10";
	static double searchDistance = 0.05;
	static int devide = 10;
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static HashMap<Integer, LinkInfo> linkMap = new HashMap<Integer, LinkInfo>();
	static HashMap<Integer, PairInfo> nodePosition = new HashMap<Integer, PairInfo>();

	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static ArrayList<SensorInfo> matchSensorList = new ArrayList<SensorInfo>();
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static HashSet<Integer> checkMatchSensor = new HashSet<Integer>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchLink();
		fetchSensor();
		matchLinkSensor();
		generateSensorKML();
		generateLinkKML();
	}

	private static void matchLinkSensor() {
		System.out.println("match sensors to links...");
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
						for(int d = 0; d < dirList.length; d++) {
							if(sensor.getDirection() == Integer.parseInt(dirList[d])) {
								double distance = DistanceCalculator.CalculationByDistance(node1, node2);
								// in the search area
								if (distance < step) {
									// match sensor
									if(!link.containSensor(sensor))
										link.addSensor(sensor);
									if(!checkMatchSensor.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										checkMatchSensor.add(sensor.getSensorId());
									}
								}
							}
						}
					}
				}
			}
			System.out.println((float)i / linkList.size() * 100 + "%");
		}
		System.out.println("match Sensors finish!");
	}

	private static void generateSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + highwayName + "_Sensors_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < matchSensorList.size(); i++) {
				SensorInfo sensor = matchSensorList.get(i);
				int id = sensor.getSensorId();
				double lati = sensor.getNode().getLati();
				double longi = sensor.getNode().getLongi();
				out.write("<Placemark><name>" + id + "</name><description>On:"
						+ sensor.getOnStreet() + ", From: "
						+ sensor.getFromStreet() + ", Dir:"
						+ sensor.getDirection()
						+ "</description><Point><coordinates>" + longi + ","
						+ lati + ",0</coordinates></Point></Placemark>");
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate sensor kml finish!");
	}

	private static void fetchSensor() {
		System.out.println("fetch Sensor...");
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
				// System.out.println("fetching sensor " + sensorId);
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

	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + highwayName + "_Link_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				int intId = link.getIntLinkId();
				ArrayList<SensorInfo> sensorList = link.getSensorList();
				String sensorStr = "";
				ArrayList<PairInfo> nodeList = link.getNodeList();
				if(sensorList.size() > 0) {
					for(int j = 0; j < sensorList.size(); j++) {
						sensorStr = sensorStr + "," + String.valueOf(sensorList.get(j).getSensorId());
					}
				}
				else {
					sensorStr = "null";
				}

				String kmlStr = "<Placemark><name>Link:" + intId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				kmlStr += ", Start:" + link.getStartNode();
				kmlStr += ", End:" + link.getEndNode();
				kmlStr += ", Dir:" + link.getAllDir();
				kmlStr += ", Name:" + link.getSt_name();
				kmlStr += ", FClass:" + link.getFunc_class();
				kmlStr += ", SCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
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

	private static void fetchLink() {
		System.out.println("fetch link...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();

			sql = "SELECT * FROM streets_dca1_new WHERE st_name = '"
					+ highwayName + "'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int linkId = res.getInt(1);
				// System.out.println("fetching link " + linkId);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT) res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for (int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = geomPoints[i * 2 + 1];
					double longitude = geomPoints[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}

				int speedCat = res.getInt(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);

				int num = nodeList.size();

				if (!nodePosition.containsKey(refNode))
					nodePosition.put(refNode, nodeList.get(0));
				if (!nodePosition.containsKey(nrefNode))
					nodePosition.put(nrefNode, nodeList.get(num - 1));

				if (dirTravel.equals("B")) {
					int dir1 = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					int dir2 = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String dir = dir1 + "," + dir2;
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							dir);
					linkList.add(link);
					linkMap.put(linkId, link);
				} else if (dirTravel.equals("T")) {
					int dir = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							sDir);
					linkList.add(link);
					linkMap.put(linkId, link);
				} else {
					int dir = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							sDir);
					linkList.add(link);
					linkMap.put(linkId, link);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch link finish!");
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