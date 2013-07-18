package highwayPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class EdgeSensorKML2 {

	/**
	 * @param args
	 */
	static String highwayName = "I-10";
	// file
	private static String fileRoot = "../GeneratedFile";
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data structure
	static HashMap<String, String> matchMap = new HashMap<String, String>();
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static HashSet<String> checkSensor = new HashSet<String>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readMatchSensor();
		fetchLink();
		generateLinkKML();
		generateSensorKML();
	}
	
	private static void generateSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(highwayName
					+ "_Sensors_List_Close.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < sensorList.size(); i++) {
				SensorInfo sensor = sensorList.get(i);
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
	
	private static void getSensor(String sensor) {
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			// assume this is sensing the specified street
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from highway_congestion_config "
					+ "where link_id = '" + sensor + "'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				String sensorId = res.getString(1);
				int sensorId2 = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				if (!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId2, onStreet,
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
	}
	
	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(highwayName + "_Link_List_Close.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				int intId = link.getIntLinkId();
				String strId = String.valueOf(intId);
				String sensorStr = matchMap.get(strId);
				if(sensorStr != null) {
					String[] strList = sensorStr.split(",");
					for(int j = 0; j < strList.length; j++) {
						getSensor(strList[j]);
					}
				}
				
				System.out.println((float)i / linkList.size() * 100 + "%");
				
				ArrayList<PairInfo> nodeList = link.getNodeList();

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

			sql = "SELECT * FROM streets_dca1_new WHERE UPPER(st_name) = '"
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
				} else if (dirTravel.equals("T")) {
					int dir = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							sDir);
					linkList.add(link);
				} else {
					int dir = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							sDir);
					linkList.add(link);
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
	
	private static void readMatchSensor() {
		System.out.println("read match sensors...");
		try {
			FileInputStream fstream = new FileInputStream(fileRoot + "/Highway_Sensor_Close.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				String[] strList = strLine.split(",");
				String linkId = strList[0];
				if(linkId == "10648765")
					linkId = "106487653";
				if(linkId == "11012413")
					linkId = "110124136";
				if(linkId == "11012412")
					linkId = "110124123";
				
				if( linkId.equals("28425397") ) {
					System.out.println(matchMap.get("28425397"));
				}
					
				
				for(int i = 2; i < strList.length; i++) {
					if(matchMap.containsKey(linkId)) {
						String sensorStr = matchMap.get(linkId);
						sensorStr = sensorStr + "," + strList[i];
						matchMap.put(linkId, sensorStr);
					}
					else {
						matchMap.put(linkId, strList[i]);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read match sensors finish!");
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
