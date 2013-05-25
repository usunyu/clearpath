//Generate Highway_senosr_close.csv
//number of sensors
// table used

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.inputConfig;

public class CreateCSVForHighwaysFinal1to5 {
	static int numElem = 0;
	static HashMap<String, LinkInfo> links = new HashMap<String, LinkInfo>();
	static int link_count = 0;
	static String url_home = null;
	static String userName = null;
	static String password = null;
	static Connection connHome = null;
	static BufferedWriter out;
	static int noData = 0;
	static int LinkIds[] = null;
	static PairInfo pairs[] = null;
	static boolean hasEdge[] = null;
	static int Direction[] = null;
	static HashMap<String, Integer> LinkDirection = new HashMap<String, Integer>();
	@SuppressWarnings("unchecked")
	static ArrayList<Double>[] speeds = (ArrayList<Double>[]) new ArrayList[60];
	@SuppressWarnings("unchecked")
	static ArrayList<Integer>[][] sensorsGrid = (ArrayList<Integer>[][]) new ArrayList[500][500];
	@SuppressWarnings("unchecked")
	static ArrayList<Integer>[][] sensorsGridDir = (ArrayList<Integer>[][]) new ArrayList[500][500];
	static int factor_count[] = new int[500];
	static LinkInfo[] links_with_sensors = new LinkInfo[1100000];
	static ArrayList<Integer> check = new ArrayList<Integer>();
	private static int links_with_sensor_count;
	private static String FILE_LINK = null;
	// add:
	static HashMap<Long, double[]> SensorDistance = new HashMap<Long, double[]>();
	static HashMap<String, String> LinkName = new HashMap<String, String>();
	static HashMap<Integer, String> Sensor_stName = new HashMap<Integer, String>();

	public static inputConfig config = null;

	public CreateCSVForHighwaysFinal1to5() {
		;
	}

	public static void run(String[] args) throws SQLException,
			NumberFormatException, IOException {

		// getGridSensors();
		// modify_gridfile();

		// read in config info
		config = new inputConfig(args[0]);
		FILE_LINK = config.getRoot() + "/Edges.csv";
		numElem = config.getHighwayNum();
		url_home = "jdbc:oracle:thin:@" + config.getJdbc();
		userName = config.getUser();
		password = config.getPassword();
		LinkIds = new int[numElem];
		pairs = new PairInfo[numElem];
		hasEdge = new boolean[numElem];
		Direction = new int[numElem];

		System.out.println("Reading From Edge File");

		// from Edges.csv
		// links(HashMap<String, LinkInfo>):
		// LinkIdIndex(link_id+node_id(ref)+node_id(nref)):
		// LinkInfo(LinkIdIndex, func_class, st_name, node_id(ref),
		// node_id(nref), pairs, count)
		readFileInMemory_new();
		// readFileInMemory();
		// readFileinMemory_new();
		// readFileinMemory_new2();

		// System.out.println("size = "+link)
		// links_with_sensors(LinkInfo[1100000]): link has sensors
		// check(ArrayList<Integer>): total sensors
		GetLinkWithSensors();
		System.out.println("Getting Link Directions");

		// from highway_link_direction_G.csv
		// LinkDirection(HashMap<String, Integer>): link_id, direction
		getLinkDirections();
		System.out.println("Getting Sensors close to Links");

		// links(HashMap<String, LinkInfo>): add sensors
		fillAdjList_Sensor();
		GetLinkWithSensors();

		// sensorsGrid[index1][index2]: link_id
		// sensorsGridDir[index1][index2]: direction
		getGridDetailsFromFile();

		// loadSensorRoadInfo();
		// Any Direction
		// links(HashMap<String, LinkInfo>): add close sensors
		findNearbySensorsToFillRemainingLinks();
		GetLinkWithSensors();

		// links(HashMap<String, LinkInfo>): set -1 for no sensors
		add_ZeroSensorToRemainingLinks();
		GetLinkWithSensors();
		// System.out.println("After Any Grid Same Direction");

		// printFactorCount();
		// Highway_Sensor_Close.csv
		// link_id, node_id(ref)+node_id(nref), sensor_id
		// check(ArrayList<Integer>): total sensors
		writeLinksToFile();

		// writeLinksToFile_new();
		// createpattern();

	}

	public static void readFileInMemory_new() {
		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Connection con = getConnection();
			// add
			// int count1 =0,count2=0;

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String LinkId = nodes[0];
				int FuncClass = Integer.parseInt(nodes[1]);

				if (FuncClass == 1 || FuncClass == 2) {

					String st_name = nodes[2];
					String st_node = nodes[3];
					String end_node = nodes[4];
					String index = st_node.substring(1) + ""
							+ end_node.substring(1);
					String LinkIdIndex = LinkId + "" + index;
					String index2 = LinkIdIndex;
					// System.out.println(nodes[0]+","+nodes[2].substring(1)+","+nodes[3].substring(1)+","+index2);

					int i = 5, count = 0;
					PairInfo[] pairs = new PairInfo[10];
					while (i < nodes.length) {
						double lati = Double.parseDouble(nodes[i]);
						double longi = Double.parseDouble(nodes[i + 1]);
						pairs[count] = new PairInfo(lati, longi);
						count++;
						i = i + 2;
					}
					if (links.get(index2) != null)
						System.out.println(links.get(index2)
								+ "Duplicate LinkIds");

					links.put(index2, new LinkInfo(index2, FuncClass, st_name,
							st_node, end_node, pairs, count));
					LinkName.put(index2, nodes[2]);

				}
			}
			in.close();
			System.out.println("number of links = " + links.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void add_ZeroSensorToRemainingLinks() {
		int i = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
		System.out.println(keys.size());

		// Connection con = getConnection();

		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());

			if (link.getFunc_class() == 3 || link.getFunc_class() == 4
					|| link.getFunc_class() == 5) {
				i++;
				continue;
			}

			if (link.sensors.isEmpty()) {
				links.get(link.getLinkId()).sensors.add(-1); // original sensor
																// 715898
			}
			i++;
		}
	}

	private static void writeLinksToFile_new() throws IOException {
		FileWriter fstream = new FileWriter(
				"C:\\Users\\jiayunge\\Highway_Sensor_Close.csv");
		out = new BufferedWriter(fstream);
		int i = 0, count = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			/*
			 * if (!(link.sensors.isEmpty())) { links_with_sensors[count] =
			 * link;
			 * out.write(String.valueOf(link.getLinkId()).substring(0,8)+",");
			 * out.write(String.valueOf(link.getLinkId()).substring(8)+",");
			 * count++; for (int j = 0; j < link.sensors.size(); j++) {
			 * out.write(link.sensors.get(j)+","); if
			 * (!(check.contains(link.sensors.get(j)))) {
			 * check.add(link.sensors.get(j)); } } out.write("\n"); }
			 */
			if (link.sensors.isEmpty()) {
				out.write(String.valueOf(link.getLinkId()).substring(0, 8)
						+ ",");
				out.write(String.valueOf(link.getLinkId()).substring(8) + ",");
				count++;
				out.write("\n");
			}
			i++;

		}
		out.close();
		System.out.println("File Written");

	}

	public static void createpattern() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Highway_Sensor_Close.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream2 = new FileWriter(
					"C:\\Users\\jiayunge\\I-110_pattern_new_tuesday_0_test.csv");
			out = new BufferedWriter(fstream2);
			int flag = 0;
			String strLine;

			int count = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				long LinkId = Long.parseLong(nodes[0] + "" + nodes[1]);
				// choose direction
				if (LinkDirection.get(LinkId) == 1)
					continue;
				// choose direction ends
				out.write(LinkId + ",");
				int length = nodes.length - 2;
				double[][] speed_array = new double[length][60];
				for (int i = 2; i < nodes.length; i++) {
					String sql = "select speed from highway_averages where  day = 'Tuesday' and link_id = "
							+ nodes[i] + " order by time";
					Connection con = getConnection();
					PreparedStatement f = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					ResultSet rs = f.executeQuery();
					int j = 0;
					while (rs.next() && j < 60) {
						speed_array[i - 2][j] = rs.getDouble(1);
						j++;
					}
					rs.close();
					con.close();
				}

				for (int i = 0; i < 60; i++) {
					double speed = 0.;
					double distance = 0.;
					int Num_Sensor = 0;
					for (int j = 0; j < length; j++) {
						if (speed_array[j][i] != 0) {
							speed = speed + speed_array[j][i];
							Num_Sensor++;
							if (SensorDistance.get(LinkId)[j] >= 100.)
								break;
							// speed = speed +
							// speed_array[j][i]*SensorDistance.get(LinkId)[j];
							// distance = distance +
							// SensorDistance.get(LinkId)[j];
							// out.write("speed: "+(speed/(double)length)+" ,");
						}
					}

					if (speed != 0)
						out.write(speed / (double) Num_Sensor + ",");
					else
						out.write("0.,");
				}

				out.write("\r\n");
				count++;
				System.out.println((count + 1) + " roads finished");
			}

			out.close();
			System.out.println("finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void loadSensorRoadInfo() {
		try {

			Connection con = getConnection();
			String sql = "select link_id, onstreet,fromstreet from highway_congestion_config";
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = f.executeQuery();
			int count = 0;
			while (rs.next()) {
				count++;
				String stinfo = rs.getString(2) + "," + rs.getString(3);
				Sensor_stName.put(rs.getInt(1), stinfo);
			}
			System.out.println("load " + count + " sensor_stinfo");
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
	}

	/*
	 * public static void readFileinMemory_new2(){ try { FileInputStream fstream
	 * = new FileInputStream(FILE_LINK); DataInputStream in = new
	 * DataInputStream(fstream); BufferedReader br = new BufferedReader(new
	 * InputStreamReader(in)); String strLine; Connection con = getConnection();
	 * 
	 * while ((strLine = br.readLine()) != null) { String[] nodes =
	 * strLine.split(","); int LinkId = Integer.parseInt(nodes[0]); int
	 * FuncClass = Integer.parseInt(nodes[1]); if(nodes[2].endsWith("I-10")){
	 * if(FuncClass == 1 || FuncClass == 2) { String st_name = nodes[2]; String
	 * st_node = nodes[3]; String end_node = nodes[4]; String index =
	 * st_node.substring(1)+""+end_node.substring(1); String LinkIdIndex =
	 * String.valueOf(LinkId)+""+index;
	 * 
	 * String sql =
	 * "select direction from edgeinfo1234 where link_id ="+LinkIdIndex;
	 * PreparedStatement f = con.prepareStatement(sql,
	 * ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet
	 * rs = f.executeQuery(); rs.first(); int direction = rs.getInt(1);
	 * rs.close(); f.close(); // System.out.println(direction); if(direction ==
	 * 3) continue;
	 * 
	 * long index2 = Long.valueOf(LinkIdIndex);
	 * //System.out.println(nodes[0]+","
	 * +nodes[2].substring(1)+","+nodes[3].substring(1)+","+index2);
	 * 
	 * int i = 5, count = 0; PairInfo[] pairs = new PairInfo[1000]; while (i <
	 * nodes.length) { double lati = Double.parseDouble(nodes[i]); double longi
	 * = Double.parseDouble(nodes[i + 1]); pairs[count] = new PairInfo(lati,
	 * longi); count++; i = i + 2; } if(links.get(index2) != null)
	 * System.out.println(links.get(index2)+"Duplicate LinkIds");
	 * 
	 * links.put(index2, new LinkInfo(index2, FuncClass, st_name, st_node,
	 * end_node, pairs, count));
	 * 
	 * } } } in.close();
	 * System.out.println("total number of links = "+links.size()); } catch
	 * (Exception e) { e.printStackTrace(); System.err.println("Error: " +
	 * e.getMessage()); }
	 * 
	 * }
	 */

	public static void modify_gridfile() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\grid_highway.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			Connection con = getConnection();

			FileWriter fstream2 = new FileWriter(
					"C:\\Users\\jiayunge\\grid_highway_real.txt");
			out = new BufferedWriter(fstream2);
			int count = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				if (nodes.length == 2)
					out.write(strLine + '\n');
				else {
					out.write(nodes[0] + "," + nodes[1]);
					for (int i = 2; i < nodes.length; i++) {

						String sql = "select direction from highway_congestion_config where link_id ="
								+ nodes[i];
						PreparedStatement f = con.prepareStatement(sql,
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY);
						ResultSet rs = f.executeQuery();
						rs.first();
						out.write("," + nodes[i] + ","
								+ String.valueOf(rs.getInt(1)));
						f.close();
						rs.close();
					}
					out.write("\r\n");

				}
				count++;
				System.out.println("count = " + count);
			}
		} catch (Exception r) {
			r.printStackTrace();
		}

	}

	public static void getGridSensors() throws SQLException {

		try {
			FileWriter fstream = new FileWriter(
					"C:\\Users\\jiayunge\\grid_highway.txt");
			out = new BufferedWriter(fstream);

			double lat1 = 34.2824, lat2 = 33.9072, long1 = -119.3, long2 = -117.6;
			// double lat1= 34.336,lat2=33.8,long1=-119.3,long2=-117.6;
			double latstep = (lat1 - lat2) / 500.0;
			double longstep = (long2 - long1) / 500.0;
			System.out.println(latstep + " " + longstep);
			Connection con = getConnection();
			int index1 = 0, count = 0;
			for (double i = lat1; i > lat2; index1++, i = i - latstep) {
				System.out.println(index1);
				if (index1 >= 500)
					continue;
				int index2 = 0;
				for (double j = long1; j < long2; index2++, j = j + longstep) {
					System.out.print(" " + index2);
					if (index2 >= 500)
						continue;
					out.write(index1 + "," + index2);
					double lati1 = i, lati2 = i - latstep, longi1 = j, longi2 = j
							+ longstep;
					String geomQuery = "MDSYS.SDO_GEOMETRY(2003,8307,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY("
							+ longi1
							+ ","
							+ lati1
							+ ","
							+ longi2
							+ ","
							+ lati2
							+ "))";
					String sql = "select link_id from highway_congestion_config where "
							+ "SDO_relate(start_lat_long,"
							+ geomQuery
							+ ",'mask=inside')='TRUE'";
					// System.out.println(sql);

					PreparedStatement f = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					ResultSet rs = f.executeQuery();

					// sensorsGrid[index1][index2]= new ArrayList<Integer>();
					while (rs.next()) {
						count++;
						out.write("," + rs.getInt(1));
						// sensorsGrid[index1][index2].add(rs.getInt(1));
					}
					rs.close();
					f.close();
					out.write("\n");
				}
			}
			con.close();
			out.close();
			fstream.close();
			System.out.println("\n" + count);
			System.out.println("finished");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	private static void getGridDetailsFromFile() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/grid_highway.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int index1 = Integer.parseInt(nodes[0]);
				int index2 = Integer.parseInt(nodes[1]);
				sensorsGrid[index1][index2] = new ArrayList<Integer>();
				sensorsGridDir[index1][index2] = new ArrayList<Integer>();
				int i = 2;
				// System.out.println(nodes.length);
				boolean flag = false;
				while (i < nodes.length) {
					flag = true;
					// System.out.println(index1+" "+index2+" "+nodes[i]);
					sensorsGrid[index1][index2].add(Integer.parseInt(nodes[i]));
					sensorsGridDir[index1][index2].add(Integer
							.parseInt(nodes[i + 1]));
					i = i + 2;
				}
				if (flag)
					count++;
			}
			System.out.println(count + " sensor locations formed");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static void fillAdjList_Sensor() throws SQLException,
			NumberFormatException, IOException {

		System.out.println("Fill Adj List called with total count ="
				+ links.size());
		int i = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
		int c = 0, tot = 0, lnkC = 0;
		Connection con = null;
		while (i < keys.size()) {

			if (i % 400 == 0) {
				System.out.print(i + ";");
				if (i != 0)
					con.close();
				con = getConnection();
			}

			LinkInfo link = links.get(iter.next());
			if (link.getFunc_class() == 1 || link.getFunc_class() == 2) {
				// tot++;
				String sql = "SELECT LINK_ID  FROM highway_congestion_config T2 "
						+ "WHERE sdo_within_distance(T2.START_LAT_LONG,MDSYS.SDO_GEOMETRY(2002,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY("
						+ link.getNodes()[0].getQuery()
						+ ","
						+ link.getNodes()[1].getQuery()
						+ ")),'distance=200 unit=METER')='TRUE' and direction="
						+ LinkDirection.get(link.getLinkId());

				PreparedStatement f = con.prepareStatement(sql);
				ResultSet rs = f.executeQuery();
				int count = 0;
				// double[] distance = new double[60];
				// int k = 0;
				while (rs.next()) {
					// lnkC++;
					// int sensor_direction = rs.getInt(2);
					// int link_direction =
					// DistanceCalculator.getDirection(link);
					if (!link.sensors.contains(rs.getInt(1))) {
						// if(rs.getString(2).equals(LinkName.get(link.getLinkId()))||rs.getString(3).equals(LinkName.get(link.getLinkId()))){
						link.sensors.add(rs.getInt(1));
						// distance[k] = rs.getDouble(4);
						// k++;
						count++;
						// most 3 sensors ?
						if (count == 3)
							break;
						// }
					}
					// System.out.println(rs.getInt(1)+" "+rs.getDouble(3));
				}

				// SensorDistance.put(link.getLinkId(), distance);

				rs.close();
				f.close();
			}
			i++;
			// con.close();
		}
		con.close();
		System.out.println("fillng process finished");

	}

	private static void findNearbySensorsToFillRemainingLinks() {
		try {
			int i = 0;
			Set<String> keys = links.keySet();
			Iterator<String> iter = keys.iterator();
			System.out.println(keys.size());

			// Connection con = getConnection();

			while (i < keys.size()) {
				LinkInfo link = links.get(iter.next());

				if (link.getFunc_class() == 3 || link.getFunc_class() == 4
						|| link.getFunc_class() == 5) {
					i++;
					continue;
				}

				if (link.sensors.isEmpty()) {

					// Start Lat,Long
					int factor = 1;
					while (link.sensors.isEmpty() && factor < 80) {

						int idx1 = getIndex1(link.getNodes()[0].getLati());
						int idx2 = getIndex2(link.getNodes()[0].getLongi());
						for (int k1 = Math.max(0, idx1 - factor); k1 < Math.min(499, idx1 + factor); k1++) {
							for (int j1 = Math.max(0, idx2 - factor); j1 < Math.min(499, idx2 + factor); j1++) {

								if (sensorsGrid[k1][j1] != null) {
									// System.out.println("Sensors found for this link with sensor count="+
									// sensorsGrid[idx1][idx2].size());
									int numElem2 = sensorsGrid[k1][j1].size();

									// links.remove(link.getLinkId());
									for (int i1 = 0; i1 < numElem2; i1++) {
										/*
										 * String sql =
										 * "SELECT onstreet,fromstreet  FROM highway_congestion_config where link_id ="
										 * + sensorsGrid [ k1 ] [ j1 ] . get (
										 * i1 ) ; PreparedStatement f = con .
										 * prepareStatement ( sql , ResultSet .
										 * TYPE_SCROLL_INSENSITIVE , ResultSet .
										 * CONCUR_READ_ONLY ) ; ResultSet rs = f
										 * . executeQuery ( ) ; rs . first ( ) ;
										 */
										if (LinkDirection.get(link.getLinkId()) == sensorsGridDir[k1][j1].get(i1)) {
											link.sensors.add(sensorsGrid[k1][j1].get(i1));
											factor_count[factor]++;
										}
										// rs.close();
										// f.close();

									}

								}
							}
						}

						idx1 = getIndex1(link.getNodes()[1].getLati());
						idx2 = getIndex2(link.getNodes()[1].getLongi());

						for (int k1 = Math.max(0, idx1 - factor); k1 < Math.min(499, idx1 + factor); k1++) {
							for (int j1 = Math.max(0, idx2 - factor); j1 < Math.min(499, idx2 + factor); j1++) {
								// END Lat,Long
								if (sensorsGrid[k1][j1] != null) {
									// System.out.println("Sensors found for this link with sensor count="+
									// sensorsGrid[idx1][idx2].size());
									int numElem2 = sensorsGrid[k1][j1].size();
									for (int i1 = 0; i1 < numElem2; i1++) {
										/*
										 * String sql =
										 * "SELECT onstreet,fromstreet  FROM highway_congestion_config where link_id ="
										 * + sensorsGrid [ k1 ] [ j1 ] . get (
										 * i1 ) ; PreparedStatement f = con .
										 * prepareStatement ( sql , ResultSet .
										 * TYPE_SCROLL_INSENSITIVE , ResultSet .
										 * CONCUR_READ_ONLY ) ; ResultSet rs = f
										 * . executeQuery ( ) ; rs . first ( ) ;
										 */
										if (LinkDirection.get(link.getLinkId()) == sensorsGridDir[k1][j1].get(i1)) {
											link.sensors.add(sensorsGrid[k1][j1].get(i1));
											factor_count[factor]++;
										}
										// f.close();
										// rs.close();
									}

								}
							}
						}

						links.put(link.getLinkId(), link);
						factor++;
					}
				}
				i++;
				// System.out.println("num of road finished:"+i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static int getIndex2(double longi) {

		// System.out.print(longi+" ");
		double l1 = -119.3;
		double l2 = -117.6;
		if (longi < l1 || longi > l2)
			return 0;
		double step = (l2 - l1) / 500.0;
		int index = 0;
		while (l1 < longi) {
			index++;
			l1 += step;
		}
		// System.out.println(index);
		if (index >= 500)
			return 499;
		return index;
	}

	private static int getIndex1(double lati) {
		// System.out.print(lati+" ");
		double l1 = 34.2824;
		double l2 = 33.9072;
		if (lati > l1 || lati < l2)
			return 0;
		double step = (l1 - l2) / 500.0;
		int index = 0;
		while (l1 > lati) {
			index++;
			l1 -= step;
		}
		// System.out.println(index);
		if (index >= 500)
			return 499;
		return index;
	}

	private static void writeLinksToFile() throws IOException {
		FileWriter fstream = new FileWriter(config.getRoot()
				+ "/Highway_Sensor_Close.csv");
		out = new BufferedWriter(fstream);
		int i = 0, count = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			if (!(link.sensors.isEmpty())) {
				links_with_sensors[count] = link;
				out.write(String.valueOf(link.getLinkId()).substring(0, 8) + ",");
				out.write(String.valueOf(link.getLinkId()).substring(8) + ",");
				count++;
				for (int j = 0; j < link.sensors.size(); j++) {
					out.write(link.sensors.get(j) + ",");
					if (!(check.contains(link.sensors.get(j)))) {
						check.add(link.sensors.get(j));
					}
				}
				out.write("\n");
			}
			i++;

		}
		out.close();
		System.out.println("File Written");

	}

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager
					.getConnection(url_home, userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;

	}

	/*
	 * private static void readFileInMemory() {
	 * 
	 * try { FileInputStream fstream = new FileInputStream(FILE_LINK);
	 * DataInputStream in = new DataInputStream(fstream); BufferedReader br =
	 * new BufferedReader(new InputStreamReader(in)); String strLine; //add
	 * //int count1 =0,count2=0; while ((strLine = br.readLine()) != null) {
	 * String[] nodes = strLine.split(","); int LinkId =
	 * Integer.parseInt(nodes[0]); int FuncClass = Integer.parseInt(nodes[1]);
	 * 
	 * if(FuncClass == 1 || FuncClass == 2) {
	 * 
	 * 
	 * String st_name = nodes[2]; String st_node = nodes[3]; String end_node =
	 * nodes[4]; String index = st_node.substring(1)+""+end_node.substring(1);
	 * String LinkIdIndex = String.valueOf(LinkId)+""+index; long index2 =
	 * Long.valueOf(LinkIdIndex);
	 * //System.out.println(nodes[0]+","+nodes[2].substring
	 * (1)+","+nodes[3].substring(1)+","+index2);
	 * 
	 * int i = 5, count = 0; PairInfo[] pairs = new PairInfo[1000]; while (i <
	 * nodes.length) { double lati = Double.parseDouble(nodes[i]); double longi
	 * = Double.parseDouble(nodes[i + 1]); pairs[count] = new PairInfo(lati,
	 * longi); count++; i = i + 2; } if(links.get(index2) != null)
	 * System.out.println(links.get(index2)+"Duplicate LinkIds");
	 * 
	 * links.put(index2, new LinkInfo(index2, FuncClass, st_name, st_node,
	 * end_node, pairs, count)); LinkName.put(index2, nodes[2]);
	 * 
	 * } } in.close(); System.out.println("size = "+links.size()); } catch
	 * (Exception e) { e.printStackTrace(); System.err.println("Error: " +
	 * e.getMessage()); }
	 * 
	 * }
	 */
	private static void GetLinkWithSensors() {
		check.clear();
		int i = 0, count = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			if (!(link.sensors.isEmpty())) {
				links_with_sensors[count] = link;
				count++;
				for (int j = 0; j < link.sensors.size(); j++) {
					if (!(check.contains(link.sensors.get(j)))) {
						check.add(link.sensors.get(j));
					}
				}

			}
			i++;
		}
		System.out.println("Total Sensors found =" + check.size()
				+ " and total links on which sensors found=" + count);
		links_with_sensor_count = count;
	}

	private static void printFactorCount() {
		int count = 0;
		for (int i = 0; i < 500; i++) {
			count += factor_count[i];
		}

		for (int i = 0; i < 500; i++) {
			if (factor_count[i] != 0)
				System.out.println(i + "="
						+ ((double) (factor_count[i] * 100.0) / count));
		}
	}

	private static void getLinkDirections() throws SQLException {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/highway_link_direction_G.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				int direction = Integer.parseInt(nodes[1]);
				LinkDirection.put(link_id, direction);
				count++;
			}

			in.close();
			System.out.println(count + " roads direction loaded!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
