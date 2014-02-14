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
import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.UtilClass;
import Objects_repos.AdjList;

public class CreateListForArterials2 {

	static int numElem = 4575;
	static HashMap<Long, LinkInfo> links = new HashMap<Long, LinkInfo>();
	static HashMap<Long, LinkInfo> links2 = new HashMap<Long, LinkInfo>();
	static int link_count = 0;
	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	static BufferedWriter out;
	static int noData = 0;
	static int LinkIds[] = new int[numElem];
	static PairInfo pairs[] = new PairInfo[numElem];
	static boolean hasEdge[] = new boolean[numElem];
	static int Direction[] = new int[numElem];
	@SuppressWarnings("unchecked")
	static ArrayList<Double>[] speeds = (ArrayList<Double>[]) new ArrayList[60];
	@SuppressWarnings("unchecked")
	static ArrayList<Integer>[][] sensorsGrid = (ArrayList<Integer>[][]) new ArrayList[100][100];
	static LinkInfo[] links_with_sensors = new LinkInfo[83586];
	static ArrayList<Integer> check = new ArrayList<Integer>();
	private static int links_with_sensor_count;
	//private static String FILE_LINK ="H:\\clearp_arterial\\links_all_2.csv";
	private static String FILE_LINK = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Correct Network\\Edges.csv";
		//"H:\\clearp\\links_all.csv";

	public static void main(String[] args) throws SQLException,
			NumberFormatException, IOException {

		readFileInMemory();
		readEdgeSensors();
		GetLinkWithSensors();
		createPatterns();

	}

	
	private static void readEdgeSensors() {
			
		try {
			System.out.println("Reading Edge Sensors");
			
			FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Correct Network\\Arterial_Sensor.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if(strLine.equals(""))
					continue;
				
				String[] nodes = strLine.split(",");
				
				if(nodes.length==1)
					continue;
				long LinkId = Long.parseLong(nodes[0]+""+nodes[1]);
				int i = 2;
				ArrayList<Integer> sensors = new ArrayList<Integer>();
				while (i < nodes.length) {
					sensors.add(Integer.parseInt(nodes[i]));
					i++;
				}
				LinkInfo link = links.get(LinkId);
				link.sensors = sensors;
				links.put(LinkId, link);

			}
				in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

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
			System.out.println("Reading CSV File in Memory and creting Edge Links");
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				
				int FuncClass = Integer.parseInt(nodes[1]);
				String st_node = nodes[2];
				String end_node = nodes[3];
				long LinkId = Long.parseLong(nodes[0]+""+nodes[2].substring(1)+""+nodes[3].substring(1));
				//System.out.println(LinkId);
				int i = 4, count = 0;
				PairInfo[] pairs = new PairInfo[1000];
				while (i < nodes.length) {
					double lati = Double.parseDouble(nodes[i]);
					double longi = Double.parseDouble(nodes[i + 1]);
					pairs[count] = new PairInfo(lati, longi);
					count++;
					i = i + 2;
				}
				if(links.get(LinkId) != null)
					System.out.println("Duplicate LinkIds");
				
				links.put(LinkId, new LinkInfo(LinkId, FuncClass, st_node,
						end_node, pairs, count));

			}
			//links2 = links;
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void GetLinkWithSensors() {
		int i = 0, count = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
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

	private static void createPatterns() {
		int i = 0, j = 0;
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {

			System.out.println("Creating Patterns now");
			while (i < links_with_sensor_count) {

				if(i%200==0)
				{
					System.out.println(((double)i/links_with_sensor_count*100.0)+"% of Links Completed");
				}
				
				LinkInfo link = links_with_sensors[i];
				System.out.println(i + " " + link.toString());
				fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Correct Network\\Output_Files\\"
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
				String query = "(";
				
				while (j < count) {
					if (j != count - 1)
						query += "link_id =" + link.sensors.get(j) + " OR ";
					else
						query += "link_id =" + link.sensors.get(j);
					j++;
				}
				query+=")";
				boolean flag = true;
				Connection con = getConnection();
				System.out.println("1");
				for (int k = 0; k < 60; k++) {
				
				if(flag)
				{
										
					String query2 ="and ( to_char(t2.date_and_time,'HH24:MI') between '"+UtilClass.getStartTime(k)+"' and '"+UtilClass.getEndTime(k)+"' )";
				String sql = "SELECT avg(t2.SPEED) FROM ARTERIAL_CONGESTION_HISTORY3 T2 WHERE "
						+ query+" "+query2;
				System.out.println(sql);
				PreparedStatement f = con.prepareStatement(sql);
				ResultSet r = f.executeQuery();
				if(r.first())
				{
					Double average = r.getDouble(1);
					if(average!=0.0)
					{
					System.out.println(average);
					Double TravelTime = distance / average * 60;
					out.write(k + "," + average + "," + TravelTime);
					out.write("\n");
					System.out.println(k + "," + average + "," + TravelTime);
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
				
				
				}
					else
					{
						break;
					}
				}
				con.close();
				i++;
				out.close();
				fstream.close();
				}
				
				
				
			
			System.out.println("File Writing Complete");
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
