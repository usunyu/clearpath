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
	static String LinkFileCA = "CA_Link.txt";
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchLinkCA();
		writeLinkFileCA();
	}
	
	private static void writeLinkFileCA() {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + LinkFileCA);
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

				String startLocStr = startLoc.getLati() + "," + startLoc.getLongi();
				String endLocStr = endLoc.getLati() + "," + endLoc.getLongi();
				String minLocStr = minLoc.getLati() + "," + minLoc.getLongi();
				String maxLocStr = maxLoc.getLati() + "," + maxLoc.getLongi();

				String pathPointsStr = "";
				for(int j = 0; j < pathPoints.size(); j++) {
					PairInfo pair = pathPoints.get(j);
				}
				
				String strLine = linkId + "||" + networkId + "||" + linkClass + "||" + rampFlag + "||" + internalFlag + "||" + activeFlag + "||";
				strLine += fromNodeId + "||" + toNodeId + "||" + linkLengthKm + "||" + primaryRoadwayId + "||" + linkDesc + "||";
				strLine += fromDesc + "||" + toDesc + "||" + speedLimitKmh + "||" + startLocStr + "||" + endLocStr + "||";
				strLine += minLocStr + "||" + maxLocStr + "||";

//				String nodeListString = "";
//				for (int j = 0; j < num; j++) {
//					String nodeString = nodeList.get(j).getLongi() + "," + nodeList.get(j).getLati();
//					if (nodeListString.equals(""))
//						nodeListString = nodeString;
//					else
//						nodeListString = nodeListString + ":" + nodeString;
//				}
//				strLine = strLine + nodeListString + ";";
//				strLine = strLine + link.getSpeedCat() + ";";
//				strLine = strLine + link.getDirTravel() + ";";
//				strLine = strLine + link.getStartNode() + ";";
//				strLine = strLine + link.getEndNode() + "\r\n";
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
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT * FROM gn_links WHERE";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {

				int linkId = res.getInt(1);
				int networkId = res.getInt(2);
				int linkClass = res.getInt(3);
				boolean rampFlag = res.getBoolean(4);
				boolean internalFlag = res.getBoolean(5);
				boolean activeFlag = res.getBoolean(6);
				int fromNodeId = res.getInt(7);
				int toNodeId = res.getInt(8);
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

				String pathPointsStr = res.getString(23);
				ArrayList<PairInfo> pathPoints = new ArrayList<PairInfo>();
				String[] pathPointNode = pathPointsStr.split(";");
				for (int i = 0; i < pathPointNode.length; i++) {
					String[] loc = pathPointNode[i].split(",");
					double lat = Double.parseDouble(loc[0]);
					double lng = Double.parseDouble(loc[1]);
					PairInfo pair = new PairInfo(lat, lng);
					pathPoints.add(pair);
				}

				// String encodedPolyline = res.getString(24);
				double fromProjCompassAngle = res.getDouble(25);
				double toProjCompassAngle = res.getDouble(26);
				String sourceId = res.getString(27);
				String sourceRef = res.getString(28);
				String tmcCode = res.getString(29);

				CALinkInfo CALink = new CALinkInfo(linkId, networkId,
						linkClass, rampFlag, internalFlag, activeFlag,
						fromNodeId, toNodeId, linkLengthKm, primaryRoadwayId,
						linkDesc, fromDesc, toDesc, speedLimitKmh, startLoc,
						endLoc, minLoc, maxLoc, pathPoints,
						fromProjCompassAngle, toProjCompassAngle, sourceId,
						sourceRef, tmcCode);
				
				CALinkList.add(CALink);

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
