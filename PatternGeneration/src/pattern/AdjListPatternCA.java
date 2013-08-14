package pattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class AdjListPatternCA {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFileCA = "CA_Link.txt";
	// for write node file
	static String nodeFileCA = "CA_Node.txt";
	// for write adjlist
	static String adjlistFileWeekdayCA = "CA_AdjList_Weekday.txt";
	static String adjlistFileWeekendCA = "CA_AdjList_Weekend.txt";
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
	static HashMap<Integer, CALinkInfo> CALinkMap = new HashMap<Integer, CALinkInfo>();
	/**
	 * @param node
	 */
	static ArrayList<CANodeInfo> CANodeList = new ArrayList<CANodeInfo>();
	static HashMap<Integer, CANodeInfo> CANodeMap = new HashMap<Integer, CANodeInfo>();
	/**
	 * @param pattern
	 */
	static HashMap<String, double[]> tmcCacheWeekday = new HashMap<String, double[]>();
	static HashMap<String, double[]> tmcCacheWeekend = new HashMap<String, double[]>();
	/**
	 * @param connect
	 */
	static HashMap<Integer, ArrayList<Integer>> AdjList = new HashMap<Integer, ArrayList<Integer>>();
	// two nodes determine one link
	static HashMap<String, CALinkInfo> nodesToLink = new HashMap<String, CALinkInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFileCA();
		readLinkFileCA();
		fetchPattern();
		buildAdjList();
		// weekday
		createAdjList(true, 3);
		// weekend
		createAdjList(false, 3);
	}
	
	// interval: 1 for every 5 min, 3 for every 15 min
	private static void createAdjList(boolean weekday, int interval) {
		System.out.println("create adj list...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + (weekday ? adjlistFileWeekdayCA : adjlistFileWeekendCA));
			BufferedWriter out = new BufferedWriter(fstream);
			for(int i = 0; i < CANodeList.size(); i++) {
				CANodeInfo CANode = CANodeList.get(i);
				int nodeId = CANode.getNewNodeId();
				
				String strLine = "";
				ArrayList<Integer> toList = AdjList.get(nodeId);
				if(toList == null || toList.isEmpty()) {
					strLine = "NA";
				}
				else {
					for(int j = 0; j < toList.size(); j++) {
						int toNodeId = toList.get(j);
						
						String nodesStr = nodeId + "-" + toNodeId;
						CALinkInfo CALink = nodesToLink.get(nodesStr);
						
						double dis = DistanceCalculator.CalculationByDistance(CALink.getStartLoc(), CALink.getEndLoc());
						// convert mile to km
						dis *= 1.609344;
						// use 10mph and fix for linkclass = 5 
						if(CALink.getLinkClass() == 5) {
							strLine += "n" + toNodeId + "(F):";
							long costTime = Math.round(dis / 10 * 60 * 60);
							if(costTime == 0)
								costTime = 1;
							strLine += costTime + ";";
						}
						else {
							strLine += "n" + toNodeId + "(V):";
							double[] speedArray = weekday ? CALink.getAverageSpeedArrayWeekday() : CALink.getAverageSpeedArrayWeekend();
							// time from 06:00 to 20:55 , every 5 min, index from 72 to 251
							for(int t = 72; t <= 251; t += interval) {
								double speed = speedArray[t];
								// assign the 0 value
								if(speed == 0)
									speed = 50;
								long costTime = Math.round(dis / speed * 60 * 60);
								if(costTime == 0)
									costTime = 1;
								strLine += costTime;
								
								strLine += ((t + interval) > 251 ? ";" : ",");
							}
						}
					}
				}
				strLine += "\r\n";
				out.write(strLine);
				
				if(i % 10000 == 0)
					System.out.println((double)i / CANodeList.size() * 100 + "% finish!");
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("create adj list finish!");
	}
	
	private static void buildAdjList() {
		System.out.println("build adj list...");
		for(int i = 0; i < CALinkList.size(); i++) {
			CALinkInfo CALink = CALinkList.get(i);
			int fromNodeId = CALink.getFromNodeId();
			int toNodeId = CALink.getToNodeId();
			if(!AdjList.containsKey(fromNodeId)) {
				ArrayList<Integer> toList = new ArrayList<Integer>();
				toList.add(toNodeId);
				AdjList.put(fromNodeId, toList);
			}
			else {
				ArrayList<Integer> toList = AdjList.get(fromNodeId);
				toList.add(toNodeId);
			}
			String nodesStr = fromNodeId + "-" + toNodeId;
			nodesToLink.put(nodesStr, CALink);
		}
		System.out.println("build adj list finish!");
	}
	
	private static void fetchPattern() {
		System.out.println("fetch pattern...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = getConnection();
			
			for(int i = 0; i < CALinkList.size(); i++) {
				debug++;
				
				CALinkInfo CALink = CALinkList.get(i);
				String tmcCode = CALink.getTmcCode();
				
				double[] avgArrayWeekday = null;
				double[] avgArrayWeekend = null;
				
				if(tmcCacheWeekday.containsKey(tmcCode)) {// search in cache
					avgArrayWeekday = tmcCacheWeekday.get(tmcCode);
					avgArrayWeekend = tmcCacheWeekend.get(tmcCode);
				}
				else {// search in database
					sql = "SELECT * FROM tmc_monthly_5min WHERE tmc_path_id = '" + tmcCode + "'";
					
					pstatement = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					res = pstatement.executeQuery();
					
					avgArrayWeekday = new double[288];
					avgArrayWeekend = new double[288];
					
					while (res.next()) {
						int minute = shiftFromUTCtoPDT(res.getInt("minute"));
						double avgSpeed = res.getDouble("avg_speed");
						boolean weekday = res.getBoolean("weekday");
						int index = minute / 5;
						if(weekday) {
							// weekday
							avgArrayWeekday[index] = avgSpeed;
						}
						else {
							// weekend
							avgArrayWeekend[index] = avgSpeed;
						}
					}
					tmcCacheWeekday.put(tmcCode, avgArrayWeekday);
					tmcCacheWeekend.put(tmcCode, avgArrayWeekend);
				}
				
				CALink.setAverageSpeedArrayWeekday(avgArrayWeekday);
				CALink.setAverageSpeedArrayWeekend(avgArrayWeekend);
				
				if (i % 250 == 0) {
					// reconnect
					res.close();
					pstatement.close();
					con.close();
					con = getConnection();
					
					System.out.println((double)i / CALinkList.size() * 100 + "% finish!");
				}
				
			}
			
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + debug);
		}
		System.out.println("fetch pattern finish!");
	}
	
	private static int shiftFromUTCtoPDT(int utc) {
		return (utc - 420 + 1440) % 1440;
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
				//boolean rampFlag = getBooleanFromStr(nodes[2]);
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

				//CALinkInfo CALink = new CALinkInfo(linkId, networkId,
				//	linkClass, rampFlag, internalFlag, activeFlag,
				//	fromNodeIdNew, toNodeIdNew, linkLengthKm, primaryRoadwayId,
				//	linkDesc, fromDesc, toDesc, speedLimitKmh, startLoc,
				//	endLoc, minLoc, maxLoc, pathPoints,
				//	fromProjCompassAngle, toProjCompassAngle, sourceId,
				//	sourceRef, tmcCode);
				
				CALinkInfo CALink = new CALinkInfo(linkId, linkClass, fromNodeIdNew, toNodeIdNew, 
						startLoc, endLoc,  pathPoints, tmcCode);

				CALinkList.add(CALink);
				CALinkMap.put(linkId, CALink);
				
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
				
				//CANodeInfo CANode = new CANodeInfo(nodeId, newNodeId,
				//	networkId, nodeType, minLinkClass, nodeName, location,
				//	sourceId1, sourceRef1);
				
				CANodeInfo CANode = new CANodeInfo(nodeId, newNodeId, location);
				
				CANodeList.add(CANode);
				CANodeMap.put(newNodeId, CANode);
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
