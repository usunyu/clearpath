package highwayPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class highwayPatternGeneration {

	/**
	 * @param args
	 */
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<Integer> linkIdList = new ArrayList<Integer>();
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static HashMap<Integer, LinkInfo> linkMap = new HashMap<Integer, LinkInfo>();
	static HashMap<Integer, PairInfo> nodePosition = new HashMap<Integer, PairInfo>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchLinkId();
		fetchLink();
		generateLinkKML();
		writeLinkFile();
		fetchSensor();
	}

	private static void fetchSensor() {
		System.out.println("fetch Sensor...");

		System.out.println("fetch sensor finish!");
	}

	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter("Highway_Link_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				int intId = link.getIntLinkId();
				SensorInfo sensor = link.getSensor();
				String sensorStr = "NULL";
				ArrayList<PairInfo> nodeList = link.getNodeList();
				if (sensor != null) {
					sensorStr = String.valueOf(sensor.getSensorId());
				}

				String kmlStr = "<Placemark><name>Link:" + intId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				kmlStr += "Start:" + link.getStartNode();
				kmlStr += "End:" + link.getEndNode();
				kmlStr += ", Direction:" + link.getAllDir();
				// kmlStr += ", StreetName:" + link.getSt_name();
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
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

	private static void writeLinkFile() {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter("Highway_Link_List.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				ArrayList<PairInfo> nodeList = link.getNodeList();
				int num = nodeList.size();
				String str = Integer.toString(link.getIntLinkId()) + "|" + link.getAllDir() + "|" + link.getSt_name() + "|" + link.getFunc_class() + 
						"|" + nodeList.get(0).getLongi() + "," + nodeList.get(0).getLati() + 
						"|" + nodeList.get(num - 1).getLongi() + "," + nodeList.get(num - 1).getLati() + 
						"|" + link.getSpeedCat() + "|" + link.getStartNode() + "|" + link.getEndNode() + "\r\n";
				out.write(str);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}

	private static void fetchLink() {
		System.out.println("fetch link...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();

			for (int i = 0; i < linkIdList.size(); i++) {

				int linkId = linkIdList.get(i);
				sql = "select * from streets_dca1_new where link_id = '"
						+ linkId + "'";

				pstatement = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				res = pstatement.executeQuery();
				
				res.next();
				
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT) res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for (int k = 0; k < geom.getNumPoints(); k++) {
					double latitude = geomPoints[k * 2 + 1];
					double longitude = geomPoints[k * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}

				int speedCat = res.getInt(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);
				
				while (res.next()) {
					stName = stName + "," + res.getString(3);
				}

				int num = nodeList.size();

				if (!nodePosition.containsKey(refNode))
					nodePosition.put(refNode, nodeList.get(0));
				if (!nodePosition.containsKey(nrefNode))
					nodePosition.put(nrefNode, nodeList.get(num - 1));

				if (dirTravel.equals("B")) {
					int dir1 = DistanceCalculator.getDirection(
							nodeList.get(0), nodeList.get(num - 1));
					int dir2 = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String dir = dir1 + "," + dir2;
					LinkInfo link = new LinkInfo(linkId, func_class,
							stName, refNode, nrefNode, nodeList, dirTravel,
							speedCat, dir);
					linkList.add(link);
					linkMap.put(linkId, link);
				} else if (dirTravel.equals("T")) {
					int dir = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class,
							stName, refNode, nrefNode, nodeList, dirTravel,
							speedCat, sDir);
					linkList.add(link);
					linkMap.put(linkId, link);
				} else {
					int dir = DistanceCalculator.getDirection(
							nodeList.get(0), nodeList.get(num - 1));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class,
							stName, refNode, nrefNode, nodeList, dirTravel,
							speedCat, sDir);
					linkList.add(link);
					linkMap.put(linkId, link);
				}

				if (i % 250 == 0) {
					res.close();
					pstatement.close();
					con.close();

					con = getConnection();

					System.out.println((double) i / linkIdList.size() * 100 + "%");
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("fetch link finish!");
	}

	public static void fetchLinkId() {
		System.out.println("fetch link id...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();

			sql = "SELECT distinct dc.link_id FROM streets_dca1_new dc, zlevels_new z1, zlevels_new z2"
					+ " where z1.node_id !=0 and z2.node_id !=0 "
					+ " and Ref_In_Id = z1.node_id and nRef_In_Id = z2.node_id "
					+ " and func_class IN (1,2)" + " order by dc.link_id";

			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {

				int linkId = res.getInt(1);
				linkIdList.add(linkId);

			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch link id finish!");
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
