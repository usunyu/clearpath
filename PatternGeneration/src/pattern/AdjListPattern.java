package pattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class AdjListPattern {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFileCA = "CA_Link.txt";
	// for write node file
	static String nodeFileCA = "CA_Node.txt";
	/**
	 * @param link
	 */
	static ArrayList<CALinkInfo> CALinkList = new ArrayList<CALinkInfo>();
	/**
	 * @param node
	 */
	static ArrayList<CANodeInfo> CANodeList = new ArrayList<CANodeInfo>();
	static HashMap<Integer, ArrayList<Integer>> AdjList = new HashMap<Integer, ArrayList<Integer>>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFileCA();
		readLinkFileCA();
		buildAdjList();
	}

	private static void buildAdjList() {
		System.out.println("build adj list...");
		for(int i = 0; i < CALinkList.size(); i++) {
			
		}
		System.out.println("build adj list finish!");
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
				int networkId = Integer.parseInt(nodes[1]);
				int linkClass = Integer.parseInt(nodes[2]);
				boolean rampFlag = getBooleanFromStr(nodes[3]);
				boolean internalFlag = getBooleanFromStr(nodes[4]);
				boolean activeFlag = getBooleanFromStr(nodes[5]);
				int fromNodeIdNew = Integer.parseInt(nodes[6]);
				int toNodeIdNew = Integer.parseInt(nodes[7]);
				double linkLengthKm = Double.parseDouble(nodes[8]);
				int primaryRoadwayId = Integer.parseInt(nodes[9]);
				String linkDesc = nodes[10];
				String fromDesc = nodes[11];
				String toDesc = nodes[12];
				double speedLimitKmh = Double.parseDouble(nodes[13]);
				PairInfo startLoc = getPairFromStr(nodes[14]);
				PairInfo endLoc = getPairFromStr(nodes[15]);
				PairInfo minLoc = getPairFromStr(nodes[16]);
				PairInfo maxLoc = getPairFromStr(nodes[17]);
				ArrayList<PairInfo> pathPoints = getPairListFromStr(nodes[18]);
				double fromProjCompassAngle = Double.parseDouble(nodes[19]);
				double toProjCompassAngle = Double.parseDouble(nodes[20]);
				String sourceId = nodes[21];
				String sourceRef = nodes[22];
				String tmcCode = nodes[23];

				CALinkInfo CALink = new CALinkInfo(linkId, networkId,
					linkClass, rampFlag, internalFlag, activeFlag,
					fromNodeIdNew, toNodeIdNew, linkLengthKm, primaryRoadwayId,
					linkDesc, fromDesc, toDesc, speedLimitKmh, startLoc,
					endLoc, minLoc, maxLoc, pathPoints,
					fromProjCompassAngle, toProjCompassAngle, sourceId,
					sourceRef, tmcCode);

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
