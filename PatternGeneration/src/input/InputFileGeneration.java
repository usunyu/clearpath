package input;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class InputFileGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write average file for cube
	static String averageSpeedFile = "Average_Speed.txt";
	// for write highway link file
	static String highwayLinkFile = "Highway_Link.txt";
	// for write arterial link file
	static String arterialLinkFile = "Arterial_Link.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param arguments
	 */
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	/**
	 * @param link
	 */
	static ArrayList<Integer> linkIdList = new ArrayList<Integer>();
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static HashMap<Integer, LinkInfo> linkMap = new HashMap<Integer, LinkInfo>();
	/**
	 * @param pattern
	 */
	static ArrayList<Integer> sensorIdList = new ArrayList<Integer>();
	/**
	 * @param pattern
	 */
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();
	static HashMap<Integer, double[]> sensorSpeed15Pattern = new HashMap<Integer, double[]>();
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* write link file */
		// true: highway, false: arterial
		//fetchLinkId(true);
		//fetchLink();
		//writeLinkFile(true);
		
		int month = 9; // October
		
		/* write average file for cube */
		for(int i = 0; i < days.length; i++)
			writeAverageCube(month, i);
		/* test */
		//writeAverageCube(8, 4);
		
		/* change the interval to 15 min */
		for(int i = 0; i < days.length; i++) {
			// September
			System.out.println("change the interval for " + months[month] + ", " + days[i] + "...");
			readAverageCube(month, i);
			changeInterval();
			writeAverage15Cube(month, i);
			renameAverageFile(month, i);
			System.out.println("change the interval for " + months[month] + ", " + days[i] + " finish!");
		}
		/* test */
		//readAverageCube(8, 4);
		//changeInterval();
		//writeAverage15Cube(8, 4);
		//renameAverageFile(8, 4);
	}
	
	private static void renameAverageFile(int month, int day) {
		System.out.println("rename average file...");
		File file = new File(root + "/" + months[month] + "_" + days[day] + "_15_" + averageSpeedFile);
		if (!file.exists() || file.isDirectory()) {
			System.err.println("file does not exist: " + file);
			return;
		}
		File newFile = new File(root + "/" + months[month] + "_" + days[day] + "_" + averageSpeedFile);
		// Delete old file if it already exist
		if(newFile.exists())
			newFile.delete();
		//Rename
		if (file.renameTo(newFile)) {
			System.out.println("rename average file finish!");
		} else {
			System.err.println("error renmaing file");
		}
	}
	
	private static void writeAverage15Cube(int month, int day) {
		System.out.println("write average speed for " + months[month] + ", " + days[day] + "...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + months[month] + "_" + days[day] + "_15_" + averageSpeedFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(int i = 0; i < sensorIdList.size(); i++) {
				int sensorId = sensorIdList.get(i);
				double[] speed15Array = sensorSpeed15Pattern.get(sensorId);
				for(int j = 0; j < speed15Array.length; j++) {
					
					String strLine = sensorId + ";" + speed15Array[j] + ";" + UtilClass.getStartTime(j) + "\r\n";
					out.write(strLine);
				}
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write average speed for " + months[month] + ", " + days[day] + " finish!");
	}
	
	private static void changeInterval() {
		System.out.println("change time interval...");
		int error = 0;
		try {
			for(int i = 0; i < sensorIdList.size(); i++) {
				error++;
				int sensorId = sensorIdList.get(i);
				double[] speedArray = sensorSpeedPattern.get(sensorId);
				double[] speed15Array;
				if(!sensorSpeed15Pattern.containsKey(sensorId))
					speed15Array = new double[60];
				else
					speed15Array = sensorSpeed15Pattern.get(sensorId);
				// from 06:00 to 20:45
				for(int j = 72, k = 0; j < 250; j += 3, k++) {
					speed15Array[k] = speedArray[j];
				}
				// fill near time less than 10 min
				for(int k = 0; k < speed15Array.length; k++) {
					if(speed15Array[k] == 0) {
						int oIndex = 72 + k * 3;
						for(int step = 1; step < 3; step++) {
							int lIndex = oIndex - step;
							int rIndex = oIndex + step;
							if(speedArray[lIndex] != 0 && speedArray[rIndex] != 0) {
								speed15Array[k] = (speedArray[lIndex] + speedArray[rIndex]) / 2;
								break;
							}
							else if(speedArray[lIndex] != 0) {
								speed15Array[k] = speedArray[lIndex];
								break;
							}
							else if(speedArray[rIndex] != 0) {
								speed15Array[k] = speedArray[rIndex];
								break;
							}
						}
					}
				}
				if(!sensorSpeed15Pattern.containsKey(sensorId))
					sensorSpeed15Pattern.put(sensorId, speed15Array);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + error);
		}
		System.out.println("change time interval finish!");
	}
	
	private static void readAverageCube(int month, int day) {
		System.out.println("read average file...");
		// initial again
		sensorIdList = new ArrayList<Integer>();
		sensorSpeedPattern = new HashMap<Integer, double[]>();
		sensorSpeed15Pattern = new HashMap<Integer, double[]>();
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + months[month] + "_" + days[day] + "_" + averageSpeedFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(";");
				int sensorId = Integer.parseInt(nodes[0]);
				
				if(!sensorIdList.contains(sensorId))
					sensorIdList.add(sensorId);
				
				double speed = Double.parseDouble(nodes[1]);
				String time = nodes[2];

				if (sensorSpeedPattern.containsKey(sensorId)) {
					double[] tempArray = sensorSpeedPattern.get(sensorId);
					int index = UtilClass.getFullTimeIndex(time);
					if (index >= 0 && index <= 287)
						tempArray[index] = speed;
				} else {
					double[] newArray = new double[288];
					int index = UtilClass.getFullTimeIndex(time);
					if (index >= 0 && index <= 287)
						newArray[index] = speed;
					sensorSpeedPattern.put(sensorId, newArray);
				}
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read average file finish!");
	}
	
	private static void writeLinkFile(boolean isHighway) {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + (isHighway ? highwayLinkFile : arterialLinkFile));
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				ArrayList<PairInfo> nodeList = link.getNodeList();
				int num = nodeList.size();
				String str = Integer.toString(link.getLinkId()) + ";";
				str = str + link.getAllDir() + ";" + link.getStreetName() + ";";
				str = str + link.getFuncClass() + ";";
				String nodeListString = "";
				for (int j = 0; j < num; j++) {
					String nodeString = nodeList.get(j).getLongi() + "," + nodeList.get(j).getLati();
					if (nodeListString.equals(""))
						nodeListString = nodeString;
					else
						nodeListString = nodeListString + ":" + nodeString;
				}
				str = str + nodeListString + ";";
				str = str + link.getSpeedCat() + ";";
				str = str + link.getDirTravel() + ";";
				str = str + link.getStartNode() + ";";
				str = str + link.getEndNode() + "\r\n";
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
				sql = "SELECT * FROM streets_dca1_new WHERE link_id = '" + linkId + "'";

				pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

				LinkInfo link = null;
				if (dirTravel.equals("B")) {
					int dir1 = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
					int dir2 = DistanceCalculator.getDirection(nodeList.get(num - 1), nodeList.get(0));
					String dir = dir1 + "," + dir2;
					link = new LinkInfo(linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, dir);
					linkList.add(link);
					linkMap.put(linkId, link);
				} else if (dirTravel.equals("T")) {
					int dir = DistanceCalculator.getDirection(nodeList.get(num - 1), nodeList.get(0));
					String sDir = String.valueOf(dir);
					link = new LinkInfo(linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, sDir);
					linkList.add(link);
					linkMap.put(linkId, link);
				} else {
					int dir = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
					String sDir = String.valueOf(dir);
					link = new LinkInfo(linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, sDir);
					linkList.add(link);
					linkMap.put(linkId, link);
				}

				// reconnect
				if (i % 250 == 0) {
					res.close();
					pstatement.close();
					con.close();
					con = getConnection();
					System.out.println((double) i / linkIdList.size() * 100 + "% finish!");
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
	
	public static void fetchLinkId(boolean isHighway) {
		System.out.println("fetch link id...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();

			sql = "SELECT distinct dc.link_id FROM streets_dca1_new dc, zlevels_new z1, zlevels_new z2"
					+ " WHERE z1.node_id !=0 AND z2.node_id !=0 "
					+ " AND Ref_In_Id = z1.node_id AND nRef_In_Id = z2.node_id "
					+ " AND func_class IN " + (isHighway ? "(1,2)" : "(3,4,5)")
					+ " ORDER BY dc.link_id";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
	
	private static void writeAverageCube(int month, int day) {
		System.out.println("write average speed for " + months[month] + ", " + days[day] + "...");
		int error = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + months[month] + "_" + days[day] + "_" + averageSpeedFile);
			BufferedWriter out = new BufferedWriter(fstream);

			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			
			sql = "SELECT * FROM DING.HIGHWAY_AVERAGES3_CUBE WHERE month = '" + months[month] + "' AND day = '" + days[day] + "' ORDER BY link_id, time";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			int i = 0;
			while (res.next()) {
				error++;
				String strId = res.getString(1);
				if (!isNumeric(strId))
					continue;
				int sensorId = res.getInt(1);
				double speed = res.getDouble(2);
				String time = res.getString(5);

				String strLine = sensorId + ";" + speed + ";" + time + "\r\n";
				out.write(strLine);
				
				if(i % 10000 == 0)
					System.out.println("No." + i + " finish!");
				i++;
			}

			out.close();

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + error);
		}
		System.out.println("write average speed for " + months[month] + ", " + days[day] + " finish!");
	}
	
	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
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
