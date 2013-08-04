package input;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class InputFileGenerationCA {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFileCA = "CA_Link.txt";
	// for write node file
	static String nodeFileCA = "CA_Node.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName = "GNDEMO";
	static String password = "GNDEMO";
	static Connection connHome = null;
	/**
	 * @param link
	 */
	static ArrayList<CALinkInfo> CALinkList = new ArrayList<CALinkInfo>();
	/**
	 * @param node
	 */
	static ArrayList<CANodeInfo> CANodeList = new ArrayList<CANodeInfo>();
	static HashMap<Integer, Integer> oldToNewNodeMap = new HashMap<Integer, Integer>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//fetchNodeCA();
		//writeNodeFileCA();
		
		readNodeFileCA();
		
		fetchLinkCA();
		writeLinkFileCA();
	}

	private static void writeLinkFileCA() {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFileCA);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < CALinkList.size(); i++) {
				CALinkInfo link = CALinkList.get(i);

				int linkId = link.getLinkId();
				int networkId = link.getNetworkId();
				int linkClass = link.getLinkClass();
				boolean rampFlag = link.getRampFlag();
				boolean internalFlag = link.getInternalFlag();
				boolean activeFlag = link.getActiveFlag();
				int fromNodeId = link.getFromNodeId();
				int toNodeId = link.getToNodeId();
				double linkLengthKm = link.getLinkLengthKm();
				int primaryRoadwayId = link.getPrimaryRoadwayId();
				String linkDesc = link.getLinkDesc();
				String fromDesc = link.getFromDesc();
				String toDesc = link.getToDesc();
				double speedLimitKmh = link.getSpeedLimitKmh();
				PairInfo startLoc = link.getStartLoc();
				PairInfo endLoc = link.getEndLoc();
				PairInfo minLoc = link.getMinLoc();
				PairInfo maxLoc = link.getMaxLoc();
				ArrayList<PairInfo> pathPoints = link.getPathPoints();
				// String encodedPolyline;
				double fromProjCompassAngle = link.getFromProjCompassAngle();
				double toProjCompassAngle = link.getToProjCompassAngle();
				String sourceId = link.getSourceId();
				String sourceRef = link.getSourceRef();
				String tmcCode = link.getTmcCode();

				String startLocStr = startLoc.getLati() + ","
						+ startLoc.getLongi();
				String endLocStr = endLoc.getLati() + "," + endLoc.getLongi();
				String minLocStr = minLoc.getLati() + "," + minLoc.getLongi();
				String maxLocStr = maxLoc.getLati() + "," + maxLoc.getLongi();

				String pathPointsStr = "";
				for (int j = 0; j < pathPoints.size(); j++) {
					PairInfo pair = pathPoints.get(j);
					if (j == 0)
						pathPointsStr += pair.getLati() + "," + pair.getLongi();
					else
						pathPointsStr += ";" + pair.getLati() + "," + pair.getLongi();
				}

				String strLine = linkId + "||" + networkId + "||" + linkClass + "||" + rampFlag + "||" + internalFlag + "||" + activeFlag + "||";
				strLine += fromNodeId + "||" + toNodeId + "||" + linkLengthKm + "||" + primaryRoadwayId + "||" + linkDesc + "||";
				strLine += fromDesc + "||" + toDesc + "||" + speedLimitKmh + "||" + startLocStr + "||" + endLocStr + "||";
				strLine += minLocStr + "||" + maxLocStr + "||" + pathPointsStr + "||" + fromProjCompassAngle + "||" + toProjCompassAngle + "||";
				strLine += sourceId + "||" + sourceRef + "||" + tmcCode + "\r\n";

				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}

	private static void fetchLinkCA() {
		System.out.println("fetch link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT * FROM gn_links WHERE tmc_code IS NOT NULL";

			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				int linkId = res.getInt(1);
				int networkId = res.getInt(2);
				int linkClass = res.getInt(3);
				boolean rampFlag = res.getBoolean(4);
				boolean internalFlag = res.getBoolean(5);
				boolean activeFlag = res.getBoolean(6);
				int fromNodeId = res.getInt(7);
				int toNodeId = res.getInt(8);
				int fromNodeIdNew = oldToNewNodeMap.get(fromNodeId);
				int toNodeIdNew = oldToNewNodeMap.get(toNodeId);

				double linkLengthKm = res.getDouble(9);
				int primaryRoadwayId = res.getInt(10);
				String linkDesc = res.getString(11);
				String fromDesc = res.getString(12);
				String toDesc = res.getString(13);
				double speedLimitKmh = res.getDouble(14);

				double startLat = res.getDouble(15);
				double startLng = res.getDouble(16);
				PairInfo startLoc = new PairInfo(startLat, startLng);

				double endLat = res.getDouble(17);
				double endLng = res.getDouble(18);
				PairInfo endLoc = new PairInfo(endLat, endLng);

				double minLat = res.getDouble(19);
				double minLng = res.getDouble(20);
				PairInfo minLoc = new PairInfo(minLat, minLng);

				double maxLat = res.getDouble(21);
				double maxLng = res.getDouble(22);
				PairInfo maxLoc = new PairInfo(maxLat, maxLng);

				Clob pathPointsClob = res.getClob(23);
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
				double fromProjCompassAngle = res.getDouble(25);
				double toProjCompassAngle = res.getDouble(26);
				String sourceId = res.getString(27);
				String sourceRef = res.getString(28);
				String tmcCode = transTMCCode(res.getString(29));

				CALinkInfo CALink = new CALinkInfo(linkId, networkId,
						linkClass, rampFlag, internalFlag, activeFlag,
						fromNodeIdNew, toNodeIdNew, linkLengthKm, primaryRoadwayId,
						linkDesc, fromDesc, toDesc, speedLimitKmh, startLoc,
						endLoc, minLoc, maxLoc, pathPoints,
						fromProjCompassAngle, toProjCompassAngle, sourceId,
						sourceRef, tmcCode);

				CALinkList.add(CALink);

				if (debug % 1000 == 0)
					System.out.println("record " + debug + " finish!");
				
				if(debug == 10000)
					break;
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
	
	private static String transTMCCode(String oldTMC) {
		String newTMC = oldTMC;
		String sign = newTMC.substring(3, 4);
		if(sign.equals("P"))
			newTMC = oldTMC.substring(0, 3) + "+" + oldTMC.substring(4);
		else if(sign.equals("N"))
			newTMC = oldTMC.substring(0, 3) + "-" + oldTMC.substring(4);
		return newTMC;
	}
	
	private static void readNodeFileCA() {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFileCA);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|\\|");
				int nodeId = Integer.parseInt(nodes[0]);
				int newNodeId = Integer.parseInt(nodes[1]);
				int networkId = Integer.parseInt(nodes[2]);
				String nodeType = nodes[3];
				int minLinkClass = Integer.parseInt(nodes[4]);
				String nodeName = nodes[5];
				String locationStr = nodes[6];
				String[] locNode = locationStr.split(",");
				double lat = Double.parseDouble(locNode[0]);
				double lng = Double.parseDouble(locNode[1]);
				PairInfo location = new PairInfo(lat, lng);
				String sourceId1 = nodes[7];
				String sourceRef1 = nodes[8];
				
				CANodeInfo CANode = new CANodeInfo(nodeId, newNodeId,
						networkId, nodeType, minLinkClass, nodeName, location,
						sourceId1, sourceRef1);
				
				CANodeList.add(CANode);
				oldToNewNodeMap.put(nodeId, newNodeId);
				if (debug % 100000 == 0)
					System.out.println("record " + debug + " finish!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + debug);
		}
		System.out.println("read node file finish!");
	}
	
	private static void writeNodeFileCA() {
		System.out.println("write node file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + nodeFileCA);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < CANodeList.size(); i++) {
				CANodeInfo CANode = CANodeList.get(i);
				
				int nodeId = CANode.getNodeId();
				int newNodeId = CANode.getNewNodeId();
				int networkId = CANode.getNetworkId();
				String nodeType = CANode.getNodeType();
				int minLinkClass = CANode.getMinLinkClass();
				String nodeName = CANode.getNodeName();
				PairInfo location = CANode.getLocation();
				String locationStr = location.getLati() + "," + location.getLongi();
				String sourceId1 = CANode.getSourceId1();
				String sourceRef1 = CANode.getSourceRef1();
				
				String strLine = nodeId + "||" + newNodeId + "||" + networkId + "||" + nodeType + "||" + minLinkClass + "||";
				strLine += nodeName + "||" + locationStr + "||" + sourceId1 + "||" + sourceRef1 + "\r\n";
				
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write node file finish!");
	}

	private static void fetchNodeCA() {
		System.out.println("fetch node...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT * FROM gn_nodes";

			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			int nodeNo = 0;

			while (res.next()) {
				debug++;

				int nodeId = res.getInt(1);
				int newNodeId = nodeNo++;
				int networkId = res.getInt(2);
				String nodeType = res.getString(3);
				int minLinkClass = res.getInt(4);
				String nodeName = res.getString(5);
				double lat = res.getDouble(6);
				double lng = res.getDouble(7);
				PairInfo location = new PairInfo(lat, lng);
				String sourceId1 = res.getString(8);
				String sourceRef1 = res.getString(9);

				CANodeInfo CANode = new CANodeInfo(nodeId, newNodeId,
						networkId, nodeType, minLinkClass, nodeName, location,
						sourceId1, sourceRef1);
				
				CANodeList.add(CANode);
				
				if (debug % 1000 == 0)
					System.out.println("record " + debug + " finish!");
			}

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
