package Arterial;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class arterialPatternGeneration {
	// parameter

	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashMap<Long, ArrayList<LinkInfo>> edgeMap = new HashMap<Long, ArrayList<LinkInfo>>();

	public static void main(String[] args) {
		fetchSensor();
		generateSensorKML();
		fetchEdge();
		generateEdgeKML();
	}

	public static void generateEdgeKML() {
		Connection con = null;
		String sql = null;
		PreparedStatement pstatement = null;
		ResultSet res = null;
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			System.out.println("generate arterial edge kml...");
			fstream = new FileWriter("Arterial_Edges_List.kml");
			out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			con = getConnection();
			sql = "select distinct dc.link_id from streets_dca1_new dc, zlevels_new z1, zlevels_new z2 where "
					+ "z1.node_id !=0 and z2.node_id !=0 and ref_in_id = z1.node_id and nref_in_id = z2.node_id "
					+ "order by dc.link_id";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				long linkId = res.getLong(1);
				ArrayList<LinkInfo> linkList = edgeMap.get(linkId);
				for (int i = 0; i < linkList.size(); i++) {
					LinkInfo link = linkList.get(i);
					long id = link.getLinkId();
					ArrayList<PairInfo> nodeList = link.getNodeList();
					String kmlStr = "<Placemark><name>LinkId:" + id + "</name>";
					kmlStr += "<description>Direction:" + link.getDirection();
					kmlStr += ", DirTravel:" + link.getDirTravel();
					kmlStr += ", StreetName:" + link.getStreetName();
					kmlStr += ", FuncClass:" + link.getFuncClass();
					kmlStr += ", SpeedCat:" + link.getSpeedCat()
							+ "</description>";
					kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
					for (int j = 0; j < nodeList.size(); j++) {
						PairInfo node = nodeList.get(j);
						kmlStr += node.getLongi() + "," + node.getLati()
								+ ",0 ";
					}
					kmlStr += "</coordinates></LineString></Placemark>\n";
					out.write(kmlStr);
				}
			}
			out.write("</Document></kml>");
			res.close();
			pstatement.close();
			con.close();
			out.close();
			fstream.close();
			System.out.println("generate arterial edge kml finish!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static void fetchEdge() {
		Connection con = null;
		String sql1 = null, sql2 = null;
		PreparedStatement pstatement1 = null, pstatement2 = null;
		ResultSet res1 = null, res2 = null;
		try {
			System.out.println("fetch arterial edges...");
			con = getConnection();
			sql1 = "select distinct dc.link_id from streets_dca1_new dc, zlevels_new z1, zlevels_new z2 where "
					+ "z1.node_id !=0 and z2.node_id !=0 and ref_in_id = z1.node_id and nref_in_id = z2.node_id "
					+ "order by dc.link_id";
			pstatement1 = con.prepareStatement(sql1,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			System.out.println("execute query...");
			res1 = pstatement1.executeQuery();
			res1.last();
			int sum = res1.getRow();
			res1.beforeFirst();
			int no = 0;
			DecimalFormat df = new DecimalFormat();
			df.applyPattern("#0.00");
			while (res1.next()) {
				no++;
				long linkId = res1.getLong(1);
				System.out.println("processing no." + no + "link, "
						+ df.format((double) no / sum * 100) + "%");
				sql2 = "select * from streets_dca1_new where link_id ="
						+ linkId;
				pstatement2 = con.prepareStatement(sql2,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				res2 = pstatement2.executeQuery();
				int count = 0;
				String dirTravel = null;
				String stName = null;
				int funcClass = 0;
				ArrayList<PairInfo> nodeList = null;
				int speedCat = 0;
				int refNode = 0;
				int nrefNode = 0;
				while (res2.next()) {
					if (count == 0) {
						dirTravel = res2.getString(2);
						stName = res2.getString(3);
						funcClass = res2.getInt(4);
						// handle for geom point
						STRUCT st = (STRUCT) res2.getObject(5);
						JGeometry geom = JGeometry.load(st);
						double[] geomPoints = geom.getOrdinatesArray();
						nodeList = new ArrayList<PairInfo>();
						for (int i = 0; i < geom.getNumPoints(); i++) {
							double latitude = geomPoints[i * 2 + 1];
							double longitude = geomPoints[i * 2 + 0];
							PairInfo node = new PairInfo(latitude, longitude);
							nodeList.add(node);
						}
						speedCat = res2.getInt(6);
						refNode = res2.getInt(7);
						nrefNode = res2.getInt(8);
					} else {
						// more than one street name
						stName = stName + ";" + res2.getString(3);
					}
					count++;
				}
				ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
				if (dirTravel.equals("B")) {
					int direction = DistanceCalculator.getDirection(
							nodeList.get(0), nodeList.get(nodeList.size() - 1));
					LinkInfo link = new LinkInfo(linkId, funcClass, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							direction);
					linkList.add(link);
					direction = DistanceCalculator.getDirection(
							nodeList.get(nodeList.size() - 1), nodeList.get(0));
					link = new LinkInfo(linkId, funcClass, stName, refNode,
							nrefNode, nodeList, dirTravel, speedCat, direction);
					linkList.add(link);

				} else if (dirTravel.equals("T")) {
					int direction = DistanceCalculator.getDirection(
							nodeList.get(nodeList.size() - 1), nodeList.get(0));
					LinkInfo link = new LinkInfo(linkId, funcClass, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							direction);
					linkList.add(link);
				} else {
					int direction = DistanceCalculator.getDirection(
							nodeList.get(0), nodeList.get(nodeList.size() - 1));
					LinkInfo link = new LinkInfo(linkId, funcClass, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							direction);
					linkList.add(link);
				}
				edgeMap.put(linkId, linkList);
				res2.close();
				pstatement2.close();
			}
			res1.close();
			pstatement1.close();
			con.close();
			System.out.println("fetch arterial edges finish!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static String getDirectionStr(int code) {
		String dir = "";
		switch (code) {
		case 0:
			dir = "North";
			break;
		case 1:
			dir = "South";
			break;
		case 2:
			dir = "East";
			break;
		case 3:
			dir = "West";
			break;
		default:
			break;
		}
		return dir;
	}

	private static void generateSensorKML() {
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			System.out.println("generate arterial sensor kml...");
			fstream = new FileWriter("Arterial_Sensors_List.kml");
			out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < sensorList.size(); i++) {
				SensorInfo sensor = sensorList.get(i);
				int id = sensor.getSensorId();
				double lati = sensor.getNode().getLati();
				double longi = sensor.getNode().getLongi();
				out.write("<Placemark><name>" + id
						+ "</name><description>Onstreet:"
						+ sensor.getOnStreet() + ", Fromstreet: "
						+ sensor.getFromStreet() + ", Direction:"
						+ getDirectionStr(sensor.getDirection())
						+ ", Affected:" + sensor.getAffected()
						+ "</description><Point><coordinates>" + longi + ","
						+ lati + ",0</coordinates></Point></Placemark>");
			}
			out.write("</Document></kml>");
			out.close();
			System.out.println("generate arterial sensor kml finish!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void fetchSensor() {
		Connection con = null;
		String sql = null;
		PreparedStatement pstatement = null;
		ResultSet res = null;
		try {
			System.out.println("fetch arterial sensor...");
			con = getConnection();
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction, affected_numberof_lanes from arterial_congestion_config";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			System.out.println("execute query...");
			res = pstatement.executeQuery();
			res.last();
			int sum = res.getRow();
			res.beforeFirst();
			int no = 0;
			DecimalFormat df = new DecimalFormat();
			df.applyPattern("#0.00");
			while (res.next()) {
				no++;
				System.out.println("processing no." + no + "sensor, "
						+ df.format((double) no / sum * 100) + "%");
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				int affected = res.getInt(6);
				SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet,
						fromStreet, node, direction, affected);
				sensorList.add(sensorInfo);
			}
			System.out.println("fetch arterial sensor finish!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Connection getConnection() {
		try {
			System.out.println("connecting to database...");
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager.getConnection(urlHome, userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;

	}
}
