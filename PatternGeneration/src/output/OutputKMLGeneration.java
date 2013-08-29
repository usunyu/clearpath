package output;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class OutputKMLGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFileCA = "CA_Link.txt";
	// for write node file
	static String nodeFileCA = "CA_Node.txt";
	// for write kml file
	static String kmlFileCA = "CA_Link.kml";
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
		readNodeFileCA();
		readLinkFileCA();
		generateKMLCA();
	}

	private static void generateKMLCA() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlFileCA);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < CALinkList.size(); i++) {
				CALinkInfo link = CALinkList.get(i);
				int linkId = link.getLinkId();
				//int networkId = link.getNetworkId();
				int linkClass = link.getLinkClass();
				//boolean rampFlag = link.getRampFlag();
				//boolean internalFlag = link.getInternalFlag();
				//boolean activeFlag = link.getActiveFlag();
				int fromNodeId = link.getFromNodeId();
				int toNodeId = link.getToNodeId();
				//double linkLengthKm = link.getLinkLengthKm();
				//int primaryRoadwayId = link.getPrimaryRoadwayId();
				//String linkDesc = link.getLinkDesc();
				//String fromDesc = link.getFromDesc();
				//String toDesc = link.getToDesc();
				//double speedLimitKmh = link.getSpeedLimitKmh();
				//PairInfo startLoc = link.getStartLoc();
				//PairInfo endLoc = link.getEndLoc();
				//PairInfo minLoc = link.getMinLoc();
				//PairInfo maxLoc = link.getMaxLoc();
				ArrayList<PairInfo> pathPoints = link.getPathPoints();
				// String encodedPolyline;
				//double fromProjCompassAngle = link.getFromProjCompassAngle();
				//double toProjCompassAngle = link.getToProjCompassAngle();
				//String sourceId = link.getSourceId();
				//String sourceRef = link.getSourceRef();
				String tmcCode = link.getTmcCode();

				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				// kmlStr += "Network:" + networkId + "\r\n";
				kmlStr += "Class:" + linkClass + "\r\n";
				// kmlStr += "Ramp:" + rampFlag + "\r\n";
				// kmlStr += "Internal:" + internalFlag + "\r\n";
				// kmlStr += "Active:" + activeFlag + "\r\n";
				kmlStr += "FromNode:" + fromNodeId + "\r\n";
				kmlStr += "ToNode:" + toNodeId + "\r\n";
				// kmlStr += "Length(Km):" + linkLengthKm + "\r\n";
				// kmlStr += "Primary:" + primaryRoadwayId + "\r\n";
				// kmlStr += "LinkDesc:" + linkDesc + "\r\n";
				// kmlStr += "FromDesc:" + fromDesc + "\r\n";
				// kmlStr += "ToDesc:" + toDesc + "\r\n";
				// kmlStr += "SpeedLimit(Km/h):" + speedLimitKmh + "\r\n";
				// kmlStr += "FromAngle:" + fromProjCompassAngle + "\r\n";
				// kmlStr += "ToAngle:" + fromProjCompassAngle + "\r\n";
				// kmlStr += "SourceId:" + sourceId + "\r\n";
				// kmlStr += "SourceRef:" + sourceRef + "\r\n";
				kmlStr += "TMCCode:" + tmcCode;
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < pathPoints.size(); j++) {
					PairInfo node = pathPoints.get(j);
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

	private static void readLinkFileCA() {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkFileCA);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|\\|");
				int linkId = Integer.parseInt(nodes[0]);
				//int networkId = Integer.parseInt(nodes[1]);
				int linkClass = Integer.parseInt(nodes[1]);
				//boolean rampFlag = getBooleanFromStr(nodes[3]);
				//boolean internalFlag = getBooleanFromStr(nodes[4]);
				//boolean activeFlag = getBooleanFromStr(nodes[5]);
				int fromNodeIdNew = Integer.parseInt(nodes[2]);
				int toNodeIdNew = Integer.parseInt(nodes[3]);
				//double linkLengthKm = Double.parseDouble(nodes[8]);
				//int primaryRoadwayId = Integer.parseInt(nodes[9]);
				//String linkDesc = nodes[10];
				//String fromDesc = nodes[11];
				//String toDesc = nodes[12];
				//double speedLimitKmh = Double.parseDouble(nodes[13]);
				PairInfo startLoc = getPairFromStr(nodes[4]);
				PairInfo endLoc = getPairFromStr(nodes[5]);
				//PairInfo minLoc = getPairFromStr(nodes[16]);
				//PairInfo maxLoc = getPairFromStr(nodes[17]);
				ArrayList<PairInfo> pathPoints = getPairListFromStr(nodes[6]);
				//double fromProjCompassAngle = Double.parseDouble(nodes[19]);
				//double toProjCompassAngle = Double.parseDouble(nodes[20]);
				//String sourceId = nodes[21];
				//String sourceRef = nodes[22];
				String tmcCode = nodes[7];

				CALinkInfo CALink = new CALinkInfo(linkId, linkClass, fromNodeIdNew, toNodeIdNew, startLoc, endLoc, pathPoints, tmcCode);

				CALinkList.add(CALink);
				
				if (debug % 100000 == 0)
					System.out.println("record " + debug + " finish!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + debug);
		}
		System.out.println("read link file finish!");
	}

	private static ArrayList<PairInfo> getPairListFromStr(String str) {
		String[] nodes = str.split(";");
		ArrayList<PairInfo> pairList = new ArrayList<PairInfo>();
		for (int i = 0; i < nodes.length; i++) {
			PairInfo pair = getPairFromStr(nodes[i]);
			pairList.add(pair);
		}
		return pairList;
	}

	private static PairInfo getPairFromStr(String str) {
		String[] nodes = str.split(",");
		PairInfo pair = new PairInfo(Double.parseDouble(nodes[0]), Double.parseDouble(nodes[1]));
		return pair;
	}

	private static boolean getBooleanFromStr(String str) {
		if(str.equals("true"))
			return true;
		else
			return false;
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
				//int networkId = Integer.parseInt(nodes[2]);
				//String nodeType = nodes[3];
				//int minLinkClass = Integer.parseInt(nodes[4]);
				//String nodeName = nodes[5];
				String locationStr = nodes[2];
				String[] locNode = locationStr.split(",");
				double lat = Double.parseDouble(locNode[0]);
				double lng = Double.parseDouble(locNode[1]);
				PairInfo location = new PairInfo(lat, lng);
				//String sourceId1 = nodes[7];
				//String sourceRef1 = nodes[8];
				
				CANodeInfo CANode = new CANodeInfo(nodeId, newNodeId, location);
				
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
}
