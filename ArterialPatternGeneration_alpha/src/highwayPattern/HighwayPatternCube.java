package highwayPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class HighwayPatternCube {

	/**
	 * @param args
	 */
	// file
	static String root = "file";
	static String highwayLinkFile = "Highway_Link_List.txt";
	static String highwayLinkKML = "Highway_Link_List.kml";
	static String highwaySensorKML = "Highway_Sensor_List.kml";
	static String allHighwaySensorKML = "All_Highway_Sensor_List.kml";
	static String averageSpeedFile = "Average_Speed_List.txt";
	static String highwayPatternKML = "Highway_Pattern.kml";

	// database
	// static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	// link
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static HashMap<Integer, LinkInfo> linkMap = new HashMap<Integer, LinkInfo>();
	// node
	static HashMap<Integer, PairInfo> nodePositionMap = new HashMap<Integer, PairInfo>();
	// sensor
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	// connect
	// two nodes decide one link
	static HashMap<String, LinkInfo> nodeToLink = new HashMap<String, LinkInfo>();
	// adj node list
	static HashMap<Integer, ArrayList<Integer>> adjNodeList = new HashMap<Integer, ArrayList<Integer>>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readLinkFile();
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
			FileInputStream fstream = new FileInputStream(root + "/"
					+ highwayLinkFile);
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
					PairInfo node = new PairInfo(Double.parseDouble(loc[1]),
							Double.parseDouble(loc[0]));
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

				LinkInfo link = new LinkInfo(linkId, funClass, name, refNode,
						nrefNode, nodeList, dirTravel, speedCat, dir);
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
