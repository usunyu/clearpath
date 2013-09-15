package input;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFInputFileGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFile = "RDF_Link.txt";
	// for write node file
	static String nodeFile = "RDF_Node.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName = "NAVTEQRDF";
	static String password = "NAVTEQRDF";
	static Connection connHome = null;
	/**
	 * @param node
	 */
	static ArrayList<RDFNodeInfo> nodeList = new ArrayList<RDFNodeInfo>();
	
	public static void main(String[] args) {
		//fetchNode();
		//writeNodeFile();
		
		fetchLink();
	}
	
	private static void fetchLink() {
		System.out.println("fetch link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT link_id, ref_node_id, nonref_node_id FROM rdf_link";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				int linkId = res.getInt("link_id");
				//int networkId = res.getInt(2);
				int linkClass = res.getInt("link_class");
				//boolean rampFlag = res.getBoolean(4);
				//boolean internalFlag = res.getBoolean(5);
				//boolean activeFlag = res.getBoolean(6);
				int fromNodeId = res.getInt("from_node_id");
				int toNodeId = res.getInt("to_node_id");
				int fromNodeIdNew = oldToNewNodeMap.get(fromNodeId);
				int toNodeIdNew = oldToNewNodeMap.get(toNodeId);

				//double linkLengthKm = res.getDouble(9);
				//int primaryRoadwayId = res.getInt(10);
				//String linkDesc = res.getString(11);
				//String fromDesc = res.getString(12);
				//String toDesc = res.getString(13);
				//double speedLimitKmh = res.getDouble(14);

				double startLat = res.getDouble("start_lat");
				double startLng = res.getDouble("start_lng");
				PairInfo startLoc = new PairInfo(startLat, startLng);

				double endLat = res.getDouble("end_lat");
				double endLng = res.getDouble("end_lng");
				PairInfo endLoc = new PairInfo(endLat, endLng);

				//double minLat = res.getDouble(19);
				//double minLng = res.getDouble(20);
				//PairInfo minLoc = new PairInfo(minLat, minLng);

				//double maxLat = res.getDouble(21);
				//double maxLng = res.getDouble(22);
				//PairInfo maxLoc = new PairInfo(maxLat, maxLng);

				Clob pathPointsClob = res.getClob("path_points");
				ArrayList<PairInfo> pathPoints = new ArrayList<PairInfo>();
				if (pathPointsClob != null) {
					String pathPointsStr = pathPointsClob.getSubString(1,
							(int) pathPointsClob.length());
					String[] pathPointNode = pathPointsStr.split(";");
					for (int i = 0; i < pathPointNode.length; i++) {
						String[] loc = pathPointNode[i].split(",");
						double lat = Double.parseDouble(loc[0]);
						double lng = Double.parseDouble(loc[1]);
						PairInfo pair = new PairInfo(lat, lng);
						pathPoints.add(pair);
					}
				} else {
					pathPoints.add(startLoc);
					pathPoints.add(endLoc);
				}

				// String encodedPolyline = res.getString(24);
				//double fromProjCompassAngle = res.getDouble(25);
				//double toProjCompassAngle = res.getDouble(26);
				//String sourceId = res.getString(27);
				//String sourceRef = res.getString(28);
				String tmcCode = transTMCCode(res.getString("tmc_code"));

				CALinkInfo CALink = new CALinkInfo(linkId, linkClass, fromNodeIdNew, toNodeIdNew, 
						startLoc, endLoc,  pathPoints, tmcCode);

				CALinkList.add(CALink);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
				
			}

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + debug);
		}
		System.out.println("fetch link finish!");
	}
	
	private static void writeNodeFile() {
		System.out.println("write node file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + nodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < nodeList.size(); i++) {
				RDFNodeInfo RDFNode = nodeList.get(i);
				
				long nodeId = RDFNode.getNodeId();
				PairInfo location = RDFNode.getLocation();
				String locationStr = location.getLati() + "," + location.getLongi();
				
				String strLine = nodeId + "|" + locationStr + "\r\n";
				
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write node file finish!");
	}

	private static void fetchNode() {
		System.out.println("fetch node...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT * FROM rdf_node";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				long nodeId = res.getLong("node_id");
				double lat = res.getDouble("lat") / 100000;
				double lng = res.getDouble("lon") / 100000;
				PairInfo location = new PairInfo(lat, lng);

				RDFNodeInfo RDFNode = new RDFNodeInfo(nodeId, location);
				
				nodeList.add(RDFNode);
				
				if (debug % 100000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + debug);
		}
		System.out.println("fetch node finish!");
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




