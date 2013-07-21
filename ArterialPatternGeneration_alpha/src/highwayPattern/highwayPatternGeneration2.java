package highwayPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class highwayPatternGeneration2 {

	/**
	 * @param args
	 */
	// param
	static String highwayName = "CA-91";
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday",
			"Friday", "Saturday", "Sunday" };
	// file
	static String root = "file";
	static String generatedFileRoot = "../GeneratedFile";
	static String edgeFile = "Edges.csv";
	static String highwaySensorFile = "Highway_Sensor_Close.csv";
	static String highwayLinkKML = "Highway_Link.kml";
	static String highwaySensorKML = "Highway_Sensor.kml";
	static String highwayPatternKML = "Highway_Pattern.kml";
	static String averageSpeedFile = "Average_Speed_List.txt";
	// data struct
	// link
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static ArrayList<LinkInfo> pathList = new ArrayList<LinkInfo>();
	static HashMap<String, LinkInfo> linkMap = new HashMap<String, LinkInfo>();
	// sensor
	static ArrayList<Integer> allSensorList = new ArrayList<Integer>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readLinkFile();
		// findLinkPath();
		fetchSensor();
		matchLinkSensor();
		// findSensorPath();
		findAllSensorPath();
		// generateLinkKML();
		generateAllLinkKML();
		// generateSensorKML();
		generateAllSensorKML();
		// readAverageFile();
		// for(int i = 0; i < 60; i++)
		// generatePatternKML(i);
	}

	private static void readAverageFile() {
		System.out.println("read average file...");
		try {
			FileInputStream fstream = new FileInputStream(root + "/"
					+ averageSpeedFile);
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
					int index = UtilClass.getIndex(time);
					if (index >= 0 && index <= 59)
						tempArray[index] = speed;
				} else {
					double[] newArray = new double[60];
					int index = UtilClass.getIndex(time);
					if (index >= 0 && index <= 59)
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

	private static void generatePatternKML(int timeIndex) {
		System.out.println("generate pattern...");
		try {
			FileWriter fstream = new FileWriter(generatedFileRoot + "/"
					+ highwayName + "/" + highwayName + "_"
					+ UtilClass.getStartTime(timeIndex) + "_"
					+ highwayPatternKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for (int i = 0; i < pathList.size(); i++) {

				LinkInfo link = pathList.get(i);
				ArrayList<Integer> sensorList = link.getSensors();

				String linkId = link.getLinkId();
				String sensorStr = "null";

				ArrayList<double[]> speedArrayList = new ArrayList<double[]>();

				boolean noSensor = false;

				for (int j = 0; j < sensorList.size(); j++) {
					if (j == 0) {
						int sensorId = sensorList.get(j);
						if (sensorId == -1) {
							noSensor = true;
							break;
						}
						double[] speedArray = sensorSpeedPattern.get(sensorId);
						speedArrayList.add(speedArray);
						sensorStr = String.valueOf(sensorId);
					} else {
						int sensorId = sensorList.get(j);
						if (sensorId == -1) {
							noSensor = true;
							break;
						}
						sensorStr = sensorStr + ","
								+ String.valueOf(sensorList.get(j));
						double[] speedArray = sensorSpeedPattern.get(sensorId);
						speedArrayList.add(speedArray);
					}
				}

				double[] finalSpeedArray = new double[60];
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

				if (!noSensor)
					for (int j = 0; j < finalSpeedArray.length; j++) {
						if (j == 0)
							patternStr = "\r\n" + UtilClass.getStartTime(j)
									+ " : " + finalSpeedArray[j];
						else {
							patternStr = patternStr + "\r\n"
									+ UtilClass.getStartTime(j) + " : "
									+ finalSpeedArray[j];
						}
					}

				String colorStr = "#FFFFFFFF";
				if (!noSensor)
					colorStr = getColor(finalSpeedArray[timeIndex]);

				PairInfo[] nodeList = link.getNodes();
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				kmlStr += patternStr;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.length; j++) {
					PairInfo node = nodeList[j];
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

	private static void findAllSensorPath() {
		System.out.println("generate sensor path...");
		for (int i = 0; i < linkList.size(); i++) {
			LinkInfo link = linkList.get(i);
			ArrayList<Integer> sensors = link.getSensors();
			for (int j = 0; j < sensors.size(); j++) {
				if (!allSensorList.contains(sensors.get(j)))
					allSensorList.add(sensors.get(j));
			}
		}
		System.out.println("generate sensor path finish!");
	}

	private static void findSensorPath() {
		System.out.println("generate sensor path...");
		for (int i = 0; i < pathList.size(); i++) {
			LinkInfo link = pathList.get(i);
			ArrayList<Integer> sensors = link.getSensors();
			for (int j = 0; j < sensors.size(); j++) {
				if (!allSensorList.contains(sensors.get(j)))
					allSensorList.add(sensors.get(j));
			}
		}
		System.out.println("generate sensor path finish!");
	}

	private static void generateAllSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(generatedFileRoot + "/"
					+ highwaySensorKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < allSensorList.size(); i++) {
				SensorInfo sensor = sensorMap.get(allSensorList.get(i));
				if (sensor == null)
					continue;
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

	private static void generateSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(generatedFileRoot + "/"
					+ highwayName + "_" + highwaySensorKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < allSensorList.size(); i++) {
				SensorInfo sensor = sensorMap.get(allSensorList.get(i));
				if (sensor == null)
					continue;
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
		System.out.println("fetch sensor...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from highway_congestion_config ";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
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

				SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet,
						fromStreet, node, direction);
				if (!sensorMap.containsKey(sensorId)) {
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

	private static void generateAllLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(generatedFileRoot + "/"
					+ highwayLinkKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				if(link.getFunc_class() != 1 && link.getFunc_class() != 2) {
					continue;
				}
				String linkId = link.getLinkId();

				ArrayList<Integer> sensorList = link.getSensors();
				String sensorStr = "null";

				for (int j = 0; j < sensorList.size(); j++)
					if (j == 0) {
						sensorStr = String.valueOf(sensorList.get(j));
					} else
						sensorStr = sensorStr + ","
								+ String.valueOf(sensorList.get(j));

				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				// kmlStr += ", Name:" + link.getSt_name();
				// kmlStr += ", Start:" + link.getStartNode();
				// kmlStr += ", End:" + link.getEndNode();
				// kmlStr += ", Dir:" + link.getAllDir();
				// kmlStr += ", Func:" + link.getFunc_class();
				// kmlStr += ", Speed:" + link.getSpeedCat();
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				PairInfo[] nodes = link.getNodes();
				kmlStr += nodes[0].getLongi() + "," + nodes[0].getLati()
						+ ",0 ";
				kmlStr += nodes[1].getLongi() + "," + nodes[1].getLati()
						+ ",0 ";
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<width>2</width>";
				kmlStr += "</LineStyle></Style>";
				kmlStr += "</Placemark>\n";

				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}

	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(generatedFileRoot + "/"
					+ highwayName + "_" + highwayLinkKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < pathList.size(); i++) {
				LinkInfo link = pathList.get(i);
				String linkId = link.getLinkId();

				ArrayList<Integer> sensorList = link.getSensors();
				String sensorStr = "null";

				for (int j = 0; j < sensorList.size(); j++)
					if (j == 0) {
						sensorStr = String.valueOf(sensorList.get(j));
					} else
						sensorStr = sensorStr + ","
								+ String.valueOf(sensorList.get(j));

				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				kmlStr += ", Name:" + link.getSt_name();
				// kmlStr += ", Start:" + link.getStartNode();
				// kmlStr += ", End:" + link.getEndNode();
				// kmlStr += ", Dir:" + link.getAllDir();
				// kmlStr += ", Func:" + link.getFunc_class();
				// kmlStr += ", Speed:" + link.getSpeedCat();
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				PairInfo[] nodes = link.getNodes();
				kmlStr += nodes[0].getLongi() + "," + nodes[0].getLati()
						+ ",0 ";
				kmlStr += nodes[1].getLongi() + "," + nodes[1].getLati()
						+ ",0 ";
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<width>2</width>";
				kmlStr += "</LineStyle></Style>";
				kmlStr += "</Placemark>\n";

				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}

	private static void findLinkPath() {
		System.out.println("find link path...");
		for (int i = 0; i < linkList.size(); i++) {
			LinkInfo link = linkList.get(i);
			if (checkName(link.getSt_name())) {
				pathList.add(link);
			}
		}
		System.out.println("find link path finish!");
	}

	private static boolean checkName(String name) {
		String[] nodes = name.split(";");
		for (int i = 0; i < nodes.length; i++) {
			String[] subNodes = nodes[i].split(" ");
			for (int j = 0; j < subNodes.length; j++) {
				if (subNodes[j].equals(highwayName))
					return true;
			}
		}
		return false;
	}

	public static void readLinkFile() {
		System.out.println("read link file...");
		try {
			FileInputStream fstream = new FileInputStream(generatedFileRoot
					+ "/" + edgeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String linkId = nodes[0];
				int funClass = Integer.parseInt(nodes[1]);
				String name = nodes[2];
				String refId = nodes[3].substring(1);
				String nrefId = nodes[4].substring(1);
				String idStr = linkId + refId + nrefId;
				PairInfo[] pairNodes = new PairInfo[2];
				pairNodes[0] = new PairInfo(Double.parseDouble(nodes[5]),
						Double.parseDouble(nodes[6]));
				pairNodes[1] = new PairInfo(Double.parseDouble(nodes[7]),
						Double.parseDouble(nodes[8]));

				LinkInfo link = new LinkInfo(idStr, linkId, funClass, name,
						refId, nrefId, pairNodes, 2);
				linkList.add(link);
				linkMap.put(idStr, link);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read link file finish!");
	}

	public static void matchLinkSensor() {
		System.out.println("match link sensor...");
		try {
			FileInputStream fstream = new FileInputStream(generatedFileRoot
					+ "/" + highwaySensorFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String linkId = nodes[0];
				String nodeId = nodes[1];
				String id = linkId + nodeId;

				ArrayList<Integer> sensorList = new ArrayList<Integer>();
				for (int i = 2; i < nodes.length; i++) {
					int sensor = Integer.parseInt(nodes[i]);
					if (!sensorList.contains(sensor)) {
						sensorList.add(sensor);
					}
				}

				LinkInfo link = linkMap.get(id);
				link.setSensors(sensorList);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("match link sensor finish!");
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
