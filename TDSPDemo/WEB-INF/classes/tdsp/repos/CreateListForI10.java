package tdsp.repos;

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

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.UtilClass;
import Objects_repos.AdjList;

public class CreateListForI10 {

	static int numI10 = 264;
	static HashMap<Integer, LinkInfo> links = new HashMap<Integer, LinkInfo>();
	static int link_count = 0;
	static String url_home = "jdbc:oracle:thin:@localhost:1522:xe2";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	static BufferedWriter out;
	static int noData = 0;
	static int LinkIds[] = new int[numI10];
	static PairInfo pairs[] = new PairInfo[numI10];
	static boolean hasEdge[] = new boolean[numI10];
	static int Direction[] = new int[numI10];
	@SuppressWarnings("unchecked")
	static ArrayList<Double>[] speeds = (ArrayList<Double>[]) new ArrayList[60];
	static LinkInfo[] links_with_sensors = new LinkInfo[9208];
	static ArrayList<Integer> check = new ArrayList<Integer>();
	private static int links_with_sensor_count;

	public static void main(String[] args) throws SQLException,
			NumberFormatException, IOException {

		init();
		readFileInMemory();
		fillAdjList();
		GetLinkWithSensors();
		createPatterns();

	}

	private static void init() {
		for (int i = 0; i < 60; i++)
			speeds[i] = new ArrayList<Double>();
	}

	private static void OutputResults() {
		for (int i = 0; i < numI10; i++)
			if (!(hasEdge[i]))
				System.out.println(LinkIds[i]);

	}

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			Connection connHome = DriverManager.getConnection(url_home,
					userName, password);
			return connHome;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return connHome;

	}

	private static void readFileInMemory() {

		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\links_all.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int LinkId = Integer.parseInt(nodes[0]);
				int FuncClass = Integer.parseInt(nodes[1]);
				String st_node = nodes[2];
				String end_node = nodes[3];
				int i = 4, count = 0;
				PairInfo[] pairs = new PairInfo[100];
				while (i < nodes.length) {
					double lati = Double.parseDouble(nodes[i]);
					double longi = Double.parseDouble(nodes[i + 1]);
					pairs[count] = new PairInfo(lati, longi);
					count++;
					i = i + 2;
				}
				links.put(LinkId, new LinkInfo(LinkId, FuncClass, st_node,
						end_node, pairs, count));

			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static void fillAdjList() throws SQLException,
			NumberFormatException, IOException {

		System.out.println("Fill Adj List called with total index i ="
				+ link_count);

		String sql = "select link_id,start_lat_long,direction from highway_congestion_config where ONSTREET = 'I-10'";
		Connection con = getConnection();
		PreparedStatement f = con.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = f.executeQuery();

		int count = 0;
		while (rs.next()) {
			LinkIds[count] = rs.getInt(1);
			STRUCT st = (STRUCT) rs.getObject(2);
			JGeometry geom = JGeometry.load(st);
			pairs[count] = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
			Direction[count] = rs.getInt(3);
			count++;
		}
		rs.close();
		f.close();
		con.close();
		for (int i = 0; i < numI10; i++) {
			hasEdge[i] = false;

		String sql1 = "SELECT * FROM LOS_ANGELES_2 T2 WHERE SDO_NN(T2.GEOM,MDSYS.SDO_GEOMETRY(2001,8307,MDSYS.SDO_POINT_TYPE("
					+ pairs[i].getQuery()
					+ ",NULL),NULL,NULL),'sdo_num_res=1 distance = 23 unit=METER')='TRUE'";
			// System.out.println(sql1);
			Connection con2 = getConnection();
			PreparedStatement f1 = con2.prepareStatement(sql1,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet r = f1.executeQuery();
			int c = 0;
			if (rs.first()) {
				hasEdge[i] = true;

				if (r.next()) {
					int linkId = r.getInt(1);
					LinkInfo linkFound = links.get(linkId);
					links.remove(linkId);
					linkFound.sensors.add(LinkIds[i]);
					// System.out.println(linkId+" "+LinkIds[i]);
					links.put(linkId, linkFound);
					c++;

				}

			} else {
				System.out.println("Road Not found for linkid=" + LinkIds[i]);
				System.out.println(sql1);

			}
			r.close();
			f.close();
			con2.close();

		}
	}

	private static void GetLinkWithSensors() {
		int i = 0, count = 0;
		Set<Integer> keys = links.keySet();
		Iterator<Integer> iter = keys.iterator();
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
				// System.out.println(link);
			}
			i++;
		}
		System.out.println("Total Sensors found =" + check.size()
				+ " and total links on which sensors found=" + count);
		links_with_sensor_count = count;
	}

	private static void createPatterns() {
		int i = 0, j = 0;
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {

			while (i < links_with_sensor_count) {

				LinkInfo link = links_with_sensors[i];
				System.out.println(i + " " + link.toString());

				fstream = new FileWriter(
						"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\"
								+ link.getLinkId() + ".txt");
				out = new BufferedWriter(fstream);

				double distance = getDistance(link);
				System.out.println("Link Distance=" + distance);
				out.write(link.toString());
				out.write("\n");
				out.write("Link Distance=" + distance);
				out.write("\n");
				
				int count = link.sensors.size();
				j = 0;
				String query = "";
				
				while (j < count) {
					if (j != count - 1)
						query += "link_id =" + link.sensors.get(j) + " OR ";
					else
						query += "link_id =" + link.sensors.get(j);
					j++;
				}
				
				boolean flag = true;
				
				for (int k = 0; k < 60; k++) {
				
				if(flag)
				{
					String query2 ="and to_char(t2.date_and_time, 'HH24:MI')>='"+UtilClass.getStartTime(k)+"' and to_char(t2.date_and_time, 'HH24:MI')<'"+UtilClass.getEndTime(k)+"'";
				String sql = "SELECT avg(t2.SPEED) FROM HIGHWAY_CONGESTION_HISTORY_2 T2 WHERE "
						+ query+" "+query2;
				System.out.println(sql);

				Connection con = getConnection();
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet r = f.executeQuery();
				System.out.println("Executed query...waiting for results");
				if(r.first())
				{
					Double average = r.getDouble(1);
					if(average!=0.0)
					{
					System.out.println(average);
					Double TravelTime = distance / average * 60;
					out.write(k + "," + average + "," + TravelTime);
					out.write("\n");
					System.out.println("k=" + k + ", speed=" + average
							+ ", time=" + UtilClass.getStartTime(k) + "-"
							+ UtilClass.getEndTime(k) + " Travel Time(mins)="
							+ TravelTime);
					}
					else
					{
						System.out.println("Average");
						out.write("Average");
						out.write("\n");
						out.close();
						fstream.close();
						r.close();
						f.close();
						con.close();
						flag=false;
						continue;
					}
				}
				r.close();
				f.close();
				con.close();
				
				}
					else
					{
						break;
					}
				}
				
				i++;
				out.close();
				fstream.close();
				}
				
				
				
			
			System.out.println("File Written");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static double getDistance(LinkInfo link) {
		PairInfo[] pairs = link.getNodes();
		double distance = 0.0;
		for (int i = 0; i < link.getPairCount() - 1; i++) {
			distance += DistanceCalculator.CalculationByDistance(pairs[i],
					pairs[i + 1]);
		}
		return distance;
	}

	

}
