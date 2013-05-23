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
import java.util.HashMap;
import java.awt.geom.*;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.DistanceCalculator;
import Objects.nodevalue;
import Objects.Node;
import Objects.inputConfig;

//import Objects.LinkInfo;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.Map;

import library.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

public class GenerateEdgesFile {
	static BufferedWriter out;
	static String url_home = null;
	static String userName = null;
	static String password = null;
	static Connection connHome = null;
	private static String FILE_LINK = "H:\\Jiayunge\\Edges.csv";
	static int[] check = new int[80000];

	static HashMap<Integer, Integer> testmap = new HashMap<Integer, Integer>();

	static HashMap<Long, Integer> links = new HashMap<Long, Integer>();

	static HashMap<Integer, Integer> down = new HashMap<Integer, Integer>();

	static HashMap<Double, HashMap<Integer, Integer>> middle = new HashMap<Double, HashMap<Integer, Integer>>();

	static HashMap<Double, HashMap<Double, HashMap<Integer, Integer>>> top = new HashMap<Double, HashMap<Double, HashMap<Integer, Integer>>>();

	static HashMap<String, Integer> lookup = new HashMap<String, Integer>();

	static HashMap<String, String> map = new HashMap<String, String>();

	static int numElem = 2032;

	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday",
			"Friday", "Saturday", "Sunday", "All" };

	static HashMap<String, LinkInfo> links_old = new HashMap<String, LinkInfo>();
	static HashMap<String, String> linksinfo = new HashMap<String, String>();
	static HashMap<Integer, StringBuffer> sensorlinkinfo = new HashMap<Integer, StringBuffer>();
	static HashMap<Integer, String> sensorLocation = new HashMap<Integer, String>();

	static HashMap<Integer, ArrayList<nodevalue>> tdsp = new HashMap<Integer, ArrayList<nodevalue>>();
	static double[][] Gnodes = new double[496821][2];
	static double[][] locations = new double[11][2];
	static public double Radius;

	public static inputConfig config = null;

	public GenerateEdgesFile() {
		;
	}

	public static void run(String[] args) throws IOException {

		config = new inputConfig(args[0]);
		url_home = "jdbc:oracle:thin:@" + config.getJdbc();
		userName = config.getUser();
		password = config.getPassword();

		// CSV: link_id, func_class, st_name, geom.sdo_point.y(ref), geom.sdo_point.x(ref), 
		// geom.sdo_point.y(nref), geom.sdo_point.x(nref)
		input_mod(); // important

		// CSV: link_id, func_class, st_name, geom.sdo_point.y(ref), geom.sdo_point.x(ref), zlevel(ref),
		// geom.sdo_point.y(nref), geom.sdo_point.x(nref), zlevel(nref)
		input_mod_add_zlevles();

		create_nodesFile();

		changeFileName();

		AddSpeedCatToInputFile();

		AddExtraStreetNameToInputFile();

		changeFileName2();

		addDirectionAccessToInputFile();

		readFileInMemory();
		getHwyDirections();
		assignLinkDirectionsToFile();

	}

	public static void changeFileName() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges_G_12345_final.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges.csv");
			out = new BufferedWriter(fstream_out);

			while ((strLine = br.readLine()) != null) {
				out.write(strLine + "\n");

			}
			out.close();
			fstream_out.close();

			br.close();
			in.close();
			fstream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void changeFileName2() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges_G_12345_final_extraStnames.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges.csv");
			out = new BufferedWriter(fstream_out);

			while ((strLine = br.readLine()) != null) {
				out.write(strLine + "\n");

			}
			out.close();
			fstream_out.close();

			br.close();
			in.close();
			fstream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addDirectionAccessToInputFile() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges_withDA.csv");
			BufferedWriter out = new BufferedWriter(fstream_out);

			String[] buffer = new String[2];
			int counter = 0;
			int realcount = 0;
			int outputcount = 0;
			while ((strLine = br.readLine()) != null) {
				realcount++;
				buffer[counter++] = strLine;
				if (counter == 2) {
					String[] nodes1 = buffer[0].split(",");
					String[] nodes2 = buffer[1].split(",");
					if (nodes1[3].equals(nodes2[4])
							&& nodes1[4].equals(nodes2[3])) {
						out.write(buffer[0] + "," + "2\n");
						out.write(buffer[1] + "," + "2\n");
						counter = 0;
						outputcount += 2;
					} else {
						out.write(buffer[0] + "," + "1\n");
						buffer[0] = buffer[1];
						counter = 1;
						outputcount++;
						// out.write(buffer[1]+","+"1\n");
					}
				}

				System.out.println("No: " + realcount);
			}

			if (outputcount != realcount)
				out.write(buffer[0] + ",1\n");

			out.close();
			fstream_out.close();
			System.out.println("finished!!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addCarPoolToInputFile() {
		try {
			Connection con = getConnection();

			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\Edges_withCarPool_12345.csv");
			out = new BufferedWriter(fstream_out);
			int count = 0;
			while ((strLine = br.readLine()) != null) {

				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				String sql = "select ar_carpool from navtech.streets_dca1 where link_id = "
						+ link_id;
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();

				rs.first();
				String temp = rs.getString(1);
				if (temp.equals("Y"))
					out.write(strLine + "," + 1 + "\n");
				else
					out.write(strLine + "," + 0 + "\n");
				System.out.println((++count) + "roads finished!!!");
				rs.close();
				f.close();
			}

			out.close();
			System.out.println("finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void coordinates() {
		try {
			double du = 118.;
			double fen = 16.;
			double miao = 46.47;

			double result = du + fen / 60. + miao / 3600.;

			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void landmarklist() {
		try {
			FileInputStream f_stream = new FileInputStream("H:\\markpoints.txt");
			DataInputStream in = new DataInputStream(f_stream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			int count = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				// System.out.println(strLine);
				locations[count][0] = Double.parseDouble(nodes[0]);
				locations[count][1] = Double.parseDouble(nodes[1]);
				count++;
			}
			br.close();
			in.close();
			f_stream.close();

			// read in nodes info
			FileInputStream f_stream2 = new FileInputStream("H:\\Nodes.csv");
			DataInputStream in2 = new DataInputStream(f_stream2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			String strLine2;
			int i = 0;
			while ((strLine2 = br2.readLine()) != null) {
				String[] nodes = strLine2.split(",");
				Gnodes[i][0] = Double.parseDouble(nodes[1]);
				Gnodes[i][1] = Double.parseDouble(nodes[2]);
				i++;
			}
			br2.close();
			in2.close();
			f_stream2.close();

			FileWriter fstream_out = new FileWriter("H:\\markid.txt");
			out = new BufferedWriter(fstream_out);

			for (i = 0; i < count; i++) {
				out.write(findNN(locations[i][0], locations[i][1]) + "\n");
			}
			out.close();
			fstream_out.close();

			System.out.println("finished!!1");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void landMarkGeneration() {
		try {
			// read in edge info
			FileInputStream f_stream = new FileInputStream(
					"H:\\lowerBoundGraph.csv");
			DataInputStream in = new DataInputStream(f_stream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count = 0;
			while ((strLine = br.readLine()) != null) {
				if (strLine.equals("NA")) {
					count++;
					continue;
				}

				String[] nodes = strLine.split(";");
				ArrayList<nodevalue> tempA = new ArrayList<nodevalue>();
				for (int i = 0; i < nodes.length; i++) {
					String[] nodes2 = nodes[i].split(":");
					nodevalue temp = new nodevalue(Integer.parseInt(nodes2[0]),
							Integer.parseInt(nodes2[1]));
					tempA.add(temp);
				}
				tdsp.put(count, tempA);
				count++;
			}

			br.close();
			in.close();
			f_stream.close();

			// read in nodes info
			FileInputStream f_stream2 = new FileInputStream("H:\\Nodes.csv");
			DataInputStream in2 = new DataInputStream(f_stream2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			String strLine2;
			int i = 0;
			while ((strLine2 = br2.readLine()) != null) {
				String[] nodes = strLine2.split(",");
				Gnodes[i][0] = Double.parseDouble(nodes[1]);
				Gnodes[i][1] = Double.parseDouble(nodes[2]);
				i++;
			}
			br2.close();
			in2.close();
			f_stream2.close();

			FileWriter fstream_out = new FileWriter("H:\\landmark.csv");
			out = new BufferedWriter(fstream_out);

			// read in the landmark id
			ArrayList<Integer> markid = new ArrayList<Integer>();
			FileInputStream f3 = new FileInputStream("H:\\markid.txt");
			DataInputStream in3 = new DataInputStream(f3);
			BufferedReader br3 = new BufferedReader(new InputStreamReader(in3));
			String strLine3;

			while ((strLine3 = br3.readLine()) != null) {
				markid.add(Integer.parseInt(strLine3));
			}
			br3.close();
			in3.close();
			f3.close();

			// now begin to generate the output file
			int len = tdsp.size();
			int st, end;

			// System.out.println(tdsp.size());
			int pro = 0;
			for (int m = 0; m < 496821; m++) {
				out.write(m + ",");
				for (i = 0; i < markid.size(); i++) {
					int mark = markid.get(i);
					// from m to mark

					if (m == mark)
						out.write(0 + ",");
					else
						out.write(tdspquery(m, mark) + ",");
					// from mark to m
					if (m == mark)
						out.write(0 + ";");
					else
						out.write(tdspquery(mark, m) + ";");
				}
				out.write("\n");
				pro++;
				System.out.println("process: " + pro);
			}

			out.close();
			fstream_out.close();
			System.out.println("finished!!!");

			// System.out.println(tdsp.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static double distance(double stlat, double stlon, double endlat,
			double endlon) {
		Radius = 6371 * 0.621371192;
		// double lat1 = StartP.getLati();
		double lat1 = stlat;
		// double lat2 = EndP.getLati();
		double lat2 = endlat;
		// double lon1 = StartP.getLongi();
		double lon1 = stlon;
		// double lon2 = EndP.getLongi();
		double lon2 = endlon;

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return Radius * c;
	}

	public static int tdspquery(int start, int end) {
		try {
			// read in edge info

			// read in edge info ends
			int hitmark = 0;
			int len = 500000, id, arritime, w;

			FibonacciHeap<Node> priorityQ = new FibonacciHeap<Node>();
			HashMap<Integer, Node> priorityQMap1 = new HashMap<Integer, Node>();
			HashMap<Node, FibonacciHeapNode<Node>> priorityQMap2 = new HashMap<Node, FibonacciHeapNode<Node>>();

			int[] c = new int[len];
			int[] parent = new int[len];
			double shortestTime = 8000000;
			int[] nextNode = new int[len];

			double hdistance = distance(Gnodes[start][0], Gnodes[start][1],
					Gnodes[end][0], Gnodes[end][1]);
			int htime = (int) ((hdistance * 60.0 * 60.0 * 1000.0) / 60);

			for (int i = 0; i < len; i++) {
				nextNode[i] = -1;
				parent[i] = -1;
				if (i == start)
					c[i] = 0 + htime; // starting node
				else
					c[i] = 8000000; // indicating infinity
			}

			Node n, s = new Node(start, c[start], 0); // creating the starting
			// node with nodeId =
			// start and cost = 0
			// and arrival time =
			// time
			FibonacciHeapNode<Node> new_node = new FibonacciHeapNode<Node>(s);
			priorityQ.insert(new_node, s.getNodeCost()); // inserting s into the
															// priority queue
			priorityQMap1.put(s.getNodeId(), s);
			priorityQMap2.put(s, new_node);

			while (!(priorityQ.isEmpty())) { // while Q is not empty
				FibonacciHeapNode<Node> node = priorityQ.min();
				priorityQ.removeMin();
				n = node.getData();

				n = priorityQMap1.get(n.getNodeId());
				priorityQMap1.remove(n.getNodeId());
				priorityQMap2.remove(n);

				// int updTime = time;
				id = n.getNodeId();

				// if (graphTDSP[id][dayIndex] == null)
				// continue;
				if (n.getNodeId() == end) {
					hitmark = 1;
					break;
				}

				if (tdsp.containsKey(id)) {
					// HashMap<Integer, NodeValues> hmap =
					// graphTDSP[id][dayIndex].nodes;
					ArrayList<nodevalue> contnodes = tdsp.get(id);

					int i2 = 0;
					while (i2 < contnodes.size()) {
						nodevalue currentnode = contnodes.get(i2);
						int key = currentnode.getid();
						// NodeValues val = hmap.get(key);
						arritime = n.getArrTime();
						w = currentnode.getcost();

						// g
						hdistance = distance(Gnodes[key][0], Gnodes[key][1],
								Gnodes[end][0], Gnodes[end][1]);
						htime = (int) ((hdistance * 60.0 * 60.0 * 1000.0) / 60);
						// g ends

						// System.out.println("cost="+w);
						if (arritime + w + htime < c[key]) {
							c[key] = arritime + w + htime;
							parent[key] = id;
							// it = priorityQ.iterator();

							// System.out.println("Size of queue="+priorityQ.size());

							if (priorityQMap1.containsKey(key)) {
								Node node_ = priorityQMap1.get(key);
								node_.setArrTime(c[key] - htime);
								node_.setNodeCost(c[key]);
								priorityQMap1.put(key, node_);
								// priorityQ.
								priorityQ.decreaseKey(priorityQMap2.get(node_),
										node_.getNodeCost());
							} else {
								Node node2 = new Node(key, c[key], c[key]
										- htime);
								FibonacciHeapNode<Node> new_node2 = new FibonacciHeapNode<Node>(
										node2);
								priorityQ.insert(new_node2, c[key]); // arrival
																		// time
																		// =
																		// c[i]
								priorityQMap1.put(key, node2);
								priorityQMap2.put(node2, new_node2);
							}

						}

						i2++;

					}
				}
			}

			if (hitmark == 1)
				return c[end];
			else
				return -1;

		} catch (Exception e) {
			e.printStackTrace();
			// return c[end];
			return -1;
		}
	}

	static int findNN(double latitude, double longitude) {
		int NNID = 1;
		double minDistance = (latitude - Gnodes[0][0])
				* (latitude - Gnodes[0][0]) + (longitude - Gnodes[0][1])
				* (longitude - Gnodes[0][1]);
		for (int i = 1; i < Gnodes.length; i++) {
			double dist = (latitude - Gnodes[i][0]) * (latitude - Gnodes[i][0])
					+ (longitude - Gnodes[i][1]) * (longitude - Gnodes[i][1]);
			if (dist < minDistance) {
				NNID = i;
				minDistance = dist;
			}
		}
		return NNID;
	}

	public static void createLowerBoundGraph() {
		try {
			/*
			 * FileInputStream fstream = new FileInputStream(FILE_LINK);
			 * DataInputStream in = new DataInputStream(fstream); BufferedReader
			 * br = new BufferedReader(new InputStreamReader(in)); String
			 * strLine;
			 */
			// load in Monday's data
			FileInputStream f_Monday = new FileInputStream(
					"H:\\AdjList_Monday.txt");
			DataInputStream in_Monday = new DataInputStream(f_Monday);
			BufferedReader br_Monday = new BufferedReader(
					new InputStreamReader(in_Monday));
			String strLine_Monday;
			// load in Tuesday's data
			FileInputStream f_Tuesday = new FileInputStream(
					"H:\\AdjList_Tuesday.txt");
			DataInputStream in_Tuesday = new DataInputStream(f_Tuesday);
			BufferedReader br_Tuesday = new BufferedReader(
					new InputStreamReader(in_Tuesday));
			String strLine_Tuesday;
			// load in Wednesday's data
			FileInputStream f_Wednesday = new FileInputStream(
					"H:\\AdjList_Wednesday.txt");
			DataInputStream in_Wednesday = new DataInputStream(f_Wednesday);
			BufferedReader br_Wednesday = new BufferedReader(
					new InputStreamReader(in_Wednesday));
			String strLine_Wednesday;
			// load in Thursday's data
			FileInputStream f_Thursday = new FileInputStream(
					"H:\\AdjList_Thursday.txt");
			DataInputStream in_Thursday = new DataInputStream(f_Thursday);
			BufferedReader br_Thursday = new BufferedReader(
					new InputStreamReader(in_Thursday));
			String strLine_Thursday;
			// load in Friday's data
			FileInputStream f_Friday = new FileInputStream(
					"H:\\AdjList_Friday.txt");
			DataInputStream in_Friday = new DataInputStream(f_Friday);
			BufferedReader br_Friday = new BufferedReader(
					new InputStreamReader(in_Friday));
			String strLine_Friday;
			// load in saturday's data
			FileInputStream f_Saturday = new FileInputStream(
					"H:\\AdjList_Saturday.txt");
			DataInputStream in_Saturday = new DataInputStream(f_Saturday);
			BufferedReader br_Saturday = new BufferedReader(
					new InputStreamReader(in_Saturday));
			String strLine_Saturday;
			// load in sunday's data
			FileInputStream f_Sunday = new FileInputStream(
					"H:\\AdjList_Sunday.txt");
			DataInputStream in_Sunday = new DataInputStream(f_Sunday);
			BufferedReader br_Sunday = new BufferedReader(
					new InputStreamReader(in_Sunday));
			String strLine_Sunday;

			FileWriter fstream_out = new FileWriter("H:\\lowerBoundGraph.csv");
			out = new BufferedWriter(fstream_out);

			int count = 0;

			while ((strLine_Monday = br_Monday.readLine()) != null) {
				if (strLine_Monday.endsWith("NA")) {
					count++;
					System.out.println("count = " + count);
					out.write("NA\n");
					strLine_Tuesday = br_Tuesday.readLine();
					strLine_Wednesday = br_Wednesday.readLine();
					strLine_Thursday = br_Thursday.readLine();
					strLine_Friday = br_Friday.readLine();
					strLine_Saturday = br_Saturday.readLine();
					strLine_Sunday = br_Sunday.readLine();

					continue;
				}
				count++;
				System.out.println("count = " + count);
				strLine_Tuesday = br_Tuesday.readLine();
				strLine_Wednesday = br_Wednesday.readLine();
				strLine_Thursday = br_Thursday.readLine();
				strLine_Friday = br_Friday.readLine();
				strLine_Saturday = br_Saturday.readLine();
				strLine_Sunday = br_Sunday.readLine();

				String[] nodes_Monday = strLine_Monday.split(";");
				String[] nodes_Tuesday = strLine_Tuesday.split(";");
				String[] nodes_Wednesday = strLine_Wednesday.split(";");
				String[] nodes_Thursday = strLine_Thursday.split(";");
				String[] nodes_Friday = strLine_Friday.split(";");
				String[] nodes_Saturday = strLine_Saturday.split(";");
				String[] nodes_Sunday = strLine_Sunday.split(";");

				int len = nodes_Monday.length;

				for (int i = 0; i < len; i++) {

					String[] speedNodes_Monday = nodes_Monday[i].split(":")[1]
							.split(",");
					String[] speedNodes_Tuesday = nodes_Tuesday[i].split(":")[1]
							.split(",");
					String[] speedNodes_Wednesday = nodes_Wednesday[i]
							.split(":")[1].split(",");
					String[] speedNodes_Thursday = nodes_Thursday[i].split(":")[1]
							.split(",");
					String[] speedNodes_Friday = nodes_Friday[i].split(":")[1]
							.split(",");
					String[] speedNodes_Saturday = nodes_Saturday[i].split(":")[1]
							.split(",");
					String[] speedNodes_Sunday = nodes_Sunday[i].split(":")[1]
							.split(",");

					int index = nodes_Monday[i].split(":")[0].indexOf("(");
					out.write(nodes_Monday[i].split(":")[0].subSequence(1,
							index).toString()
							+ ":");

					int min = Integer.parseInt(speedNodes_Monday[0]);
					int len2 = speedNodes_Monday.length;
					for (int k = 0; k < len2; k++) {
						if (Integer.parseInt(speedNodes_Monday[k]) <= min)
							min = Integer.parseInt(speedNodes_Monday[k]);

						if (Integer.parseInt(speedNodes_Tuesday[k]) <= min)
							min = Integer.parseInt(speedNodes_Tuesday[k]);

						if (Integer.parseInt(speedNodes_Wednesday[k]) <= min)
							min = Integer.parseInt(speedNodes_Wednesday[k]);

						if (Integer.parseInt(speedNodes_Thursday[k]) <= min)
							min = Integer.parseInt(speedNodes_Thursday[k]);

						if (Integer.parseInt(speedNodes_Friday[k]) <= min)
							min = Integer.parseInt(speedNodes_Friday[k]);

						if (Integer.parseInt(speedNodes_Saturday[k]) <= min)
							min = Integer.parseInt(speedNodes_Saturday[k]);

						if (Integer.parseInt(speedNodes_Sunday[k]) <= min)
							min = Integer.parseInt(speedNodes_Sunday[k]);

					}
					out.write(min + ";");

				}

				out.write("\n");

			}

			out.close();
			fstream_out.close();

			br_Sunday.close();
			in_Sunday.close();
			f_Sunday.close();

			br_Saturday.close();
			in_Saturday.close();
			f_Saturday.close();

			br_Friday.close();
			in_Friday.close();
			f_Friday.close();

			br_Thursday.close();
			in_Thursday.close();
			f_Thursday.close();

			br_Wednesday.close();
			in_Wednesday.close();
			f_Wednesday.close();

			br_Tuesday.close();
			in_Tuesday.close();
			f_Tuesday.close();

			br_Monday.close();
			in_Monday.close();
			f_Monday.close();

			System.out.println("finished!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showaccidentlocation() {
		try {
			Connection con = getConnection();
			String sql = "select  lat, lon from event_realtime ";
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = f.executeQuery();
			// out.print("read finishedsd1234!!\n");
			int count = 0;
			while (rs.next()) {

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void test() {
		try {
			Connection con = getConnection();

			String sql = "select link_id,speed,day,time from arterial_averages3 where link_id = 10019 and day = 'Monday' order by time";

			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = f.executeQuery();
			int dayIndex = -1;
			while (rs.next()) {
				String day = rs.getString(3);
				for (int i = 0; i < days.length; i++) {
					if (days[i].equals(day))
						dayIndex = i;
				}

				int hours = Integer.parseInt(rs.getString(4).split(":")[0]);
				int minutes = Integer.parseInt(rs.getString(4).split(":")[1]);
				int timeIndex = ((hours - 6) * 4) + (minutes / 15);

				System.out.println(rs.getDouble(2) + "," + dayIndex + ","
						+ timeIndex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readFileInMemory_all() {

		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");

				int FuncClass = Integer.parseInt(nodes[1]);
				if (FuncClass == 1 || FuncClass == 2 || FuncClass == 3
						|| FuncClass == 4 || FuncClass == 5) {
					String st_node = nodes[3];
					String end_node = nodes[4];
					String LinkId = nodes[0] + "" + nodes[3].substring(1) + ""
							+ nodes[4].substring(1);

					if (linksinfo.get(LinkId) != null)
						System.out.println("Duplicate LinkIds");

					linksinfo.put(LinkId, nodes[3] + "," + nodes[5] + ","
							+ nodes[6] + "," + nodes[4] + "," + nodes[7] + ","
							+ nodes[8]);

				}
			}
			// links2 = links;
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void generateServerInputFile() {
		try {
			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Arterial_Sensor_Close.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\ServerInput.csv");
			out = new BufferedWriter(fstream_out);

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String linkid = nodes[0] + "" + nodes[1];

				for (int i = 2; i < nodes.length; i++) {
					int sensorid = Integer.parseInt(nodes[i]);
					if (sensorid == -1)
						break;
					if (sensorlinkinfo.containsKey(sensorid)) {
						sensorlinkinfo.get(sensorid).append(
								"," + linksinfo.get(linkid));
					} else
						sensorlinkinfo.put(sensorid,
								new StringBuffer(linksinfo.get(linkid)));
				}
			}

			br.close();
			in.close();
			fstream.close();

			FileInputStream fstream2 = new FileInputStream(
					"H:\\Jiayunge\\Highway_Sensor_Close.csv");
			DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			String strLine2;

			while ((strLine2 = br2.readLine()) != null) {
				String[] nodes = strLine2.split(",");
				String linkid = nodes[0] + "" + nodes[1];

				for (int i = 2; i < nodes.length; i++) {
					int sensorid = Integer.parseInt(nodes[i]);
					if (sensorid == -1)
						break;
					if (sensorlinkinfo.containsKey(sensorid)) {
						sensorlinkinfo.get(sensorid).append(
								"," + linksinfo.get(linkid));
					} else
						sensorlinkinfo.put(sensorid,
								new StringBuffer(linksinfo.get(linkid)));
				}
			}

			int i = 0;
			Set<Integer> keys = sensorlinkinfo.keySet();
			Iterator<Integer> iter = keys.iterator();
			while (i < keys.size()) {
				int sensorid = iter.next();

				out.write(sensorid + ","
						+ sensorlinkinfo.get(sensorid).toString() + "\n");

				i++;
			}
			out.close();
			fstream_out.close();
			System.out.println("haha finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ServerInputFileModify() {
		try {
			Connection con = getConnection();
			String sql = "select link_id,start_lat_long from highway_congestion_config";
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = f.executeQuery();
			while (rs.next()) {
				int sensorid = rs.getInt(1);
				STRUCT st = (STRUCT) rs.getObject(2);
				JGeometry geom = JGeometry.load(st);
				// pairs[count] = new PairInfo(geom.getPoint()[1],
				// geom.getPoint()[0]);
				double lati = geom.getPoint()[1];
				double lon = geom.getPoint()[0];
				sensorLocation.put(sensorid, lati + "," + lon);
			}
			con.close();
			con = getConnection();
			String sql2 = "select link_id,start_lat_long from arterial_congestion_config";
			PreparedStatement f2 = con.prepareStatement(sql2,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet res = f2.executeQuery();
			while (res.next()) {
				int sensorid = res.getInt(1);
				STRUCT st = (STRUCT) res.getObject(2);
				JGeometry geom = JGeometry.load(st);
				// pairs[count] = new PairInfo(geom.getPoint()[1],
				// geom.getPoint()[0]);
				double lati = geom.getPoint()[1];
				double lon = geom.getPoint()[0];
				sensorLocation.put(sensorid, lati + "," + lon);
			}

			con.close();
			System.out.println("sensor loaded!");

			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\ServerInput.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\ServerInputNew.csv");
			out = new BufferedWriter(fstream_out);
			int count = 0;
			while ((strLine = br.readLine()) != null) {

				String[] nodes = strLine.split(",");
				int sensorid = Integer.parseInt(nodes[0]);
				// int minid = 0;
				String start = "";
				String end = "";
				double dismin = Double.POSITIVE_INFINITY;
				double lat1, lat2, lon1, lon2;
				for (int i = 1; i < nodes.length; i = i + 6) {
					lat1 = Double.parseDouble(nodes[i + 1]);
					lon1 = Double.parseDouble(nodes[i + 2]);
					lat2 = Double.parseDouble(nodes[i + 4]);
					lon2 = Double.parseDouble(nodes[i + 5]);
					double sensorlat = Double.parseDouble(sensorLocation.get(
							sensorid).split(",")[0]);
					double sensorlon = Double.parseDouble(sensorLocation.get(
							sensorid).split(",")[1]);
					PairInfo p1 = new PairInfo(lat1, lon1);
					PairInfo p2 = new PairInfo(lat2, lon2);
					PairInfo sensor = new PairInfo(sensorlat, sensorlon);
					double dis1 = DistanceCalculator.CalculationByDistance(p1,
							sensor);
					double dis2 = DistanceCalculator.CalculationByDistance(p2,
							sensor);
					double dmin = dis1 > dis2 ? dis2 : dis1;
					if (dmin < dismin) {
						start = nodes[i];
						end = nodes[i + 3];
						dismin = dmin;
					}

				}

				out.write(nodes[0] + "," + start + "," + end + "\n");

			}

			out.close();
			fstream_out.close();
			br.close();
			in.close();
			fstream.close();

			System.out.println("lulu!!!");
		} catch (Exception e) {

		}
	}

	public static void AddSpeedLimitToInputFile() {
		try {
			Connection con = getConnection();

			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\Edges_WithSpeedLimit.csv");
			out = new BufferedWriter(fstream_out);
			int count = 0;
			while ((strLine = br.readLine()) != null) {

				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				String sql = "select speed_cat from streets_dca1_new where link_id = "
						+ link_id;
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();

				rs.first();
				out.write(strLine + "," + rs.getInt(1) + "\n");

				System.out.println((++count) + "roads finished!!!");
				rs.close();
				f.close();
			}

			out.close();
			System.out.println("finished!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void AddDirectionToInputFile() {
		try {
			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Edges_withDistance.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\Edges_withDirction.csv");
			out = new BufferedWriter(fstream_out);

			int number = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String LinkId = nodes[0];
				int FuncClass = Integer.parseInt(nodes[1]);
				String st_name = nodes[2];
				String st_node = nodes[3];
				String end_node = nodes[4];
				String index = st_node.substring(1) + ""
						+ end_node.substring(1);
				String LinkIdIndex = LinkId + "" + index;
				String index2 = LinkIdIndex;
				// System.out.println(nodes[0]+","+nodes[2].substring(1)+","+nodes[3].substring(1)+","+index2);

				double stLat = Double.parseDouble(nodes[5]);
				double stlon = Double.parseDouble(nodes[6]);
				double endlat = Double.parseDouble(nodes[7]);
				double endlon = Double.parseDouble(nodes[8]);

				int direction = DistanceCalculator.getDirection2(stLat, stlon,
						endlat, endlon);

				out.write(strLine + "," + direction + "\n");
				number++;
				System.out.println("number = " + number);
			}
			br.close();
			in.close();
			fstream.close();
			out.close();
			fstream_out.close();
			System.out.println("finished!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void AddDistanceToInputFile() {
		try {
			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Edges_withSpeedCat_12345.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\Edges_withDistance.csv");
			out = new BufferedWriter(fstream_out);
			int number = 0;
			while ((strLine = br.readLine()) != null) {

				String[] nodes = strLine.split(",");
				String LinkId = nodes[0];
				int FuncClass = Integer.parseInt(nodes[1]);
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
				while (i < nodes.length - 1) {
					double lati = Double.parseDouble(nodes[i]);
					double longi = Double.parseDouble(nodes[i + 1]);
					pairs[count] = new PairInfo(lati, longi);
					count++;
					i = i + 2;
				}

				double distance = DistanceCalculator.CalculationByDistance(
						pairs[0], pairs[1]);
				out.write(strLine + "," + distance + "\n");
				number++;
				System.out.println("number = " + number);
			}

			br.close();
			in.close();
			fstream.close();
			out.close();
			fstream_out.close();
			System.out.println("finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void AddSpeedCatToInputFile() {
		try {
			Connection con = getConnection();

			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges_withSpeedCat_12345.csv");
			out = new BufferedWriter(fstream_out);
			int count = 0;
			while ((strLine = br.readLine()) != null) {

				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				String sql = "select speed_cat from streets_dca1_new where link_id = "
						+ link_id;
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();

				rs.first();
				out.write(strLine + "," + rs.getInt(1) + "\n");

				System.out.println((++count) + "roads finished!!!");
				rs.close();
				f.close();
			}

			out.close();
			System.out.println("finished!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void AddRampInfo() {
		try {
			Connection con = getConnection();

			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\Edges_withRamp.csv");
			out = new BufferedWriter(fstream_out);
			int count = 0;

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");

				String sql = "select ramp from navtech.streets_dca1 where link_id = "
						+ nodes[0];
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();
				rs.first();

				out.write(strLine + "," + rs.getString(1) + "\n");

				rs.close();
				f.close();

				count++;
				System.out.println(count + "finished");
			}

			System.out.println("finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean name_match(String st_name_pre, String st_name_o) {

		String[] nodes_pre = st_name_pre.split(";");
		String[] nodes_o = st_name_o.split(";");

		for (int i = 0; i < nodes_o.length; i++)
			for (int j = 0; j < nodes_pre.length; j++) {
				if (nodes_o[i].equals(nodes_pre[j]))
					return true;
			}

		return false;

	}

	public static void AddExtraStreetNameToInputFile() {
		try {
			Connection con = getConnection();

			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges_G_12345_final_extraStnames.csv");
			out = new BufferedWriter(fstream_out);
			int count = 0;
			while ((strLine = br.readLine()) != null) {

				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				String sql = "select st_name from streets_dca1_new where link_id = "
						+ link_id;
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();
				int row_count = 0;
				while (rs.next())
					row_count++;
				if (row_count == 1)
					out.write(strLine + "\n");
				else {
					rs.beforeFirst();
					StringBuffer new_stName = new StringBuffer("");
					while (rs.next()) {
						new_stName.append(rs.getString(1) + ";");
					}
					out.write(nodes[0] + "," + nodes[1] + "," + new_stName
							+ "," + nodes[3] + "," + nodes[4] + "," + nodes[5]
							+ "," + nodes[6] + "," + nodes[7] + "," + nodes[8]
							+ "\n");

				}

				System.out.println((++count) + "roads finished!!!");
				rs.close();
				f.close();
			}

			out.close();
			System.out.println("ok!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void turn_by_turn() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\findfault_extraStname.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\trun-by-trun.txt");
			out = new BufferedWriter(fstream_out);

			String link_id = "";
			double tail_st_x = 0.;
			double tail_st_y = 0.;
			double tail_end_x = 0.;
			double tail_end_y = 0.;
			String st_name_pre = "";
			double A = 0.;
			double B = 0.;
			double C = 0.;

			int count = 0;

			while ((strLine = br.readLine()) != null) {
				count++;

				String[] nodes = strLine.split(",");
				String link_id_o = nodes[0] + "" + nodes[3].substring(1) + ""
						+ nodes[4].substring(1);
				String st_name_o = nodes[2];

				if (count == 1) {
					tail_st_x = Double.parseDouble(nodes[6]) * 100.;
					tail_st_y = Double.parseDouble(nodes[5]) * 100.;
					tail_end_x = Double.parseDouble(nodes[8]) * 100.;
					tail_end_y = Double.parseDouble(nodes[7]) * 100.;

					link_id = link_id_o;
					st_name_pre = st_name_o;
					continue;
				}

				if (link_id_o.equals(link_id)) {
					tail_st_x = Double.parseDouble(nodes[6]) * 100.;
					tail_st_y = Double.parseDouble(nodes[5]) * 100.;
					tail_end_x = Double.parseDouble(nodes[8]) * 100.;
					tail_end_y = Double.parseDouble(nodes[7]) * 100.;

					link_id = link_id_o;
					st_name_pre = st_name_o;
				}

				else if (name_match(st_name_pre, st_name_o)) {
					tail_st_x = Double.parseDouble(nodes[6]) * 100.;
					tail_st_y = Double.parseDouble(nodes[5]) * 100.;
					tail_end_x = Double.parseDouble(nodes[8]) * 100.;
					tail_end_y = Double.parseDouble(nodes[7]) * 100.;

					link_id = link_id_o;
					st_name_pre = st_name_o;
				}

				else {

					A = tail_end_y - tail_st_y;
					B = -(tail_end_x - tail_st_x);
					C = (tail_end_x - tail_st_x) * tail_st_y
							- (tail_end_y - tail_st_y) * tail_st_x;

					double x = Double.parseDouble(nodes[8]) * 100.;
					double y = Double.parseDouble(nodes[7]) * 100.;

					double result = A * x + B * y + C;
					String direction = "";

					if (result == 0)
						direction = "F";
					else if (result > 0)
						direction = "R";
					else
						direction = "L";

					double x1 = tail_end_x - tail_st_x;
					double y1 = tail_end_y - tail_st_y;
					double x2 = x - tail_end_x;
					double y2 = y - tail_end_y;

					double cosTheta = (x1 * x2 + y1 * y2)
							/ (Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2
									+ y2 * y2));

					out.write("road" + count + "   ");
					if (direction.equals("F"))
						out.write("move forward into "
								+ st_name_o.split(";")[0]);
					else if (direction.equals("R")) {
						if (cosTheta >= 0.86)
							out.write("move slight right into "
									+ st_name_o.split(";")[0]);
						else
							out.write("move right into "
									+ st_name_o.split(";")[0]);
					} else {
						if (cosTheta >= 0.86)
							out.write("move slight left into "
									+ st_name_o.split(";")[0]);
						else
							out.write("move left into "
									+ st_name_o.split(";")[0]);
					}
					out.write("\n");

					tail_st_x = Double.parseDouble(nodes[6]) * 100.;
					tail_st_y = Double.parseDouble(nodes[5]) * 100.;
					tail_end_x = Double.parseDouble(nodes[8]) * 100.;
					tail_end_y = Double.parseDouble(nodes[7]) * 100.;

					link_id = link_id_o;
					st_name_pre = st_name_o;

				}

				System.out.println(count + "roads finised");

			}
			out.close();
			System.out.println("finished!!!");
			double a = 0.004948999999947842 / 0.004965707401715878;
			System.out.println(a);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void assignLinkDirectionsToFile() {
		try {
			Set<String> keys = links_old.keySet();
			Iterator<String> iter = keys.iterator();
			System.out.println(keys.size());
			Connection con = getConnection();
			int i = 0, count = 0;
			FileWriter fstream = new FileWriter(config.getRoot()
					+ "/highway_link_direction_G.csv");
			out = new BufferedWriter(fstream);

			while (i < keys.size()) {
				if (i % 100 == 0)
					System.out.println(i);

				LinkInfo link = links_old.get(iter.next());
				if (link.getFunc_class() == 3 || link.getFunc_class() == 4
						|| link.getFunc_class() == 5) {
					i++;
					continue;
				}
				int direction = -1;
				double lati1 = link.getNodes()[0].getLati();
				double longi1 = link.getNodes()[0].getLongi();
				double lati2 = link.getNodes()[1].getLati();
				double longi2 = link.getNodes()[1].getLongi();

				// choose name
				String[] names = link.getSt_name().split(";");

				int find_mark1 = 0;
				for (int cn = 0; cn < names.length; cn++) {
					if (names[cn].contains("CA-") || names[cn].contains("I-")
							|| names[cn].contains("US-"))
						find_mark1 = 1;

				}

				int find_mark2 = 0;
				for (int cn = 0; cn < names.length; cn++) {
					if (names[cn].equals("GLENDALE FWY"))
						find_mark2 = 1;
				}

				String st_name = "";
				if (find_mark1 == 0 && find_mark2 == 0)
					st_name = names[0];

				if (find_mark1 == 1 && find_mark2 == 0) {
					for (int k = 0; k < names.length; k++) {
						if (names[k].contains("CA-") || names[k].contains("I-")
								|| names[k].contains("US-"))
							st_name = names[k];
					}
				}

				if (find_mark2 == 1)
					st_name = "GLENDALE FWY";

				// choose name ends

				String dir = map.get(st_name);
				// System.out.println(link.getSt_name()+"  "+dir);
				if (dir.equals("0,1,2,3")) {
					direction = DistanceCalculator.getDirection(link);
				} else if (dir.equals("0,1")) {
					if (lati2 > lati1) { // Going N
						direction = 0;
					} else if (lati2 < lati1) { // Going S
						direction = 1;
					} else {
						direction = DistanceCalculator.getDirection(link);
					}

				} else if (dir.equals("2,3")) {
					if (longi2 > longi1) { // Going E
						direction = 2;
					} else if (longi2 < longi1) { // Going W
						direction = 3;
					} else {
						direction = DistanceCalculator.getDirection(link);
					}
				}

				if (st_name.contains("101")) {
					if (lati1 > lati2 && longi1 > longi2 && direction == 1)
						direction = 0;
					else if (lati1 < lati2 && longi1 < longi2 && direction == 0)
						direction = 1;
					else if (direction == 2)
						direction = 1;
					else if (direction == 3)
						direction = 0;
				}

				if (st_name.equals("GLENDALE FWY")) {
					if (direction == 0)
						direction = 2;
					if (direction == 1)
						direction = 3;
				}
				/*
				 * if(link.getSt_name().equals("CA-2")){ if(direction == 0 &&
				 * lati1<lati2 && longi1<longi2) direction = 2; if(direction ==
				 * 1 && lati1>lati2 && longi1>longi2) direction = 3; }
				 */

				if (direction == -1)
					System.out.println("Alert");

				i++;
				out.write(link.getLinkId() + "," + direction + "\n");
				count++;
			}
			out.close();
			System.out.println(count);
			System.out.println("finished!!!");
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static void readFileInMemory() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges_G_12345_final_extraStnames.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String LinkId = nodes[0];
				int FuncClass = Integer.parseInt(nodes[1]);
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
				if (links_old.get(index2) != null)
					System.out.println(links_old.get(index2)
							+ "Duplicate LinkIds");

				links_old.put(index2, new LinkInfo(index2, FuncClass, st_name,
						st_node, end_node, pairs, count));

			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}

	public static void getHwyDirections() {
		map.put("107B", "0,1");
		map.put("114B", "0,1");
		map.put("24A", "0,1");
		map.put("3A", "0,1,2,3");
		map.put("3B", "0,1,2,3");
		map.put("ARTESIA FWY", "2,3");
		map.put("CA-110", "0,1");
		map.put("CA-133", "0,1");
		map.put("CA-134", "2,3"); // zui zuo ce you yi dian "0,1" bu fen
		map.put("CA-170", "0,1");
		map.put("CA-2", "0,1"); // shang wei 0, 2 xia wei 1, 3
		map.put("CA-210", "2,3");
		map.put("CA-22", "0,1"); // zuo 0 you 1 undefined
		map.put("CA-23", "0,1");
		map.put("CA-241", "0,1"); // undefined
		map.put("CA-47", "0,1"); // not correct
		map.put("CA-55", "0,1"); // undefined
		map.put("CA-57", "0,1");
		map.put("CA-60", "2,3");
		map.put("CA-73", "0,1"); // undefined

		map.put("CA-91", "2,3");
		map.put("CA-91 HOV LN", "2,3"); // undefined
		map.put("CENTURY FWY", "2,3");
		map.put("COLUMBIA ST", "2,3"); // undefined should be considered as
										// funcclass3-5
		map.put("CORONA FWY", "0,1"); // undefined
		map.put("COSTA MESA FWY", "0,1"); // undefined
		map.put("EASTERN TRANSPORTATION CORRIDOR", "0,1"); // undefined
		map.put("FOOTHILL FWY", "2,3"); // part of the vertical part are not
										// correct
		map.put("GARDEN GROVE BLVD", "0,1"); // undefined
		map.put("GARDEN GROVE FWY", "0,1"); // undefined
		map.put("GARDENA FWY", "2,3");
		map.put("GLENDALE BLVD", "0,1"); // undefined
		map.put("GLENDALE FWY", "0,1"); // special case 0->2,1->3
		map.put("GLENN ANDERSON FWY", "2,3");
		map.put("GOLDEN STATE FWY", "0,1");
		map.put("HARBOR FWY", "0,1");
		map.put("HOLLYWOOD FWY", "0,1");
		map.put("I-10", "2,3");
		map.put("I-105", "2,3");
		map.put("I-110", "0,1");
		map.put("I-110-HOV-LN", "0,1");
		map.put("I-15", "0,1"); // undefined
		map.put("I-210", "2,3");
		map.put("I-405", "0,1");
		map.put("I-5", "0,1");
		map.put("I-5-HOV-LN", "0,1"); // undefined
		map.put("I-605", "0,1");
		map.put("I-710", "0,1");
		map.put("LAGUNA FWY", "0,1"); // undefined
		map.put("LONG BEACH FWY", "0,1");
		map.put("N ALVARADO ST", "2,3"); // undefined should be func3-5
		map.put("ONTARIO FWY", "0,1"); // undefined
		map.put("ORANGE FWY", "0,1");
		map.put("ORANGE GROVE AVE", "0,1");
		map.put("PASADENA FWY", "0,1");
		map.put("POMONA FWY", "2,3");
		map.put("RIVERSIDE DR", "0,1");// undefined should be func3-5
		map.put("RIVERSIDE FWY", "2,3"); // undefined
		map.put("ROSA PARKS FWY", "2,3");
		map.put("S PASADENA AVE", "0,1"); // should be func3-5
		map.put("S ST JOHN AVE", "0,1"); // should be func3-5
		map.put("SAN BERNARDINO FWY", "2,3");
		map.put("SAN DIEGO FWY", "0,1");
		map.put("SAN GABRIEL RIVER FWY", "0,1");
		map.put("SAN JOAQUIN HILLS TRANS CORRIDOR", "0,1"); // undefined
		map.put("SANTA ANA FWY", "0,1");
		map.put("SANTA MONICA FWY", "2,3");
		map.put("THOUSAND OAKS FWY", "0,1");
		map.put("TUJUNGA AVE", "0,1"); // should be func3-5
		map.put("US HISTORIC ROUTE 66", "0,1");
		map.put("US-101", "0,1"); // special case
		map.put("VALLEY VIEW ST", "0,1"); // should be func3-5
		map.put("VENTURA FWY", "0,1,2,3");
		map.put("VICTORY BLVD", "0,1"); // should be func3-5
		map.put("VINCENT THOMAS BRG", "0,1");
		map.put("VINELAND AVE", "0,1");// should be func3-5
		map.put("W OCEAN BLVD", "0,1");
		map.put("WESTERN AVE", "2,3"); // should be func3-5
		map.put("null", "0,1,2,3");

	}

	public static void create_highway_roads() {
		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\test_roads_G.kml");
			out = new BufferedWriter(fstream_out);
			String strLine;
			out.write("<kml><Document>");
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");

				// System.out.println(strLine);
				// System.out.println(nodes[0]);
				String st_name = nodes[2];
				int FuncClass = Integer.parseInt(nodes[1]);
				if ((FuncClass == 1 || FuncClass == 2)
						&& st_name.equals("CA-2")) {
					// if(nodes[2].endsWith("I-10")||nodes[2].endsWith("I-110")){
					String st_node = nodes[3];
					String end_node = nodes[4];
					String LinkId = nodes[0] + "" + nodes[3].substring(1) + ""
							+ nodes[4].substring(1);
					// System.out.println(LinkId);
					// out.write(LinkId+"\r\n");
					out.write("<Placemark><name>"
							+ nodes[2]
							+ "</name><description>"
							+ LinkId
							+ "</description><LineString><tessellate>1</tessellate><coordinates>"
							+ nodes[6] + "," + nodes[5] + ",0 " + nodes[8]
							+ "," + nodes[7]
							+ ",0 </coordinates></LineString></Placemark>");
					// }

				}
			}
			out.write("</Document></kml>");
			out.close();
			br.close();
			System.out.println("hehe");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void create_highway_sensors() {
		try {
			FileWriter fstream = new FileWriter(
					"H:\\jiayunge\\sensors_highway.kml");
			out = new BufferedWriter(fstream);
			Connection con = getConnection();

			String sql = "select link_id,start_lat_long,direction from highway_congestion_config";
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = f.executeQuery();
			int count = 0;
			out.write("<kml><Document>");
			while (rs.next()) {
				count++;

				int LinkId = rs.getInt(1);
				STRUCT st = (STRUCT) rs.getObject(2);
				JGeometry geom = JGeometry.load(st);
				// pairs[count] = new PairInfo(geom.getPoint()[1],
				// geom.getPoint()[0]);
				double lati = geom.getPoint()[1];
				double lon = geom.getPoint()[0];
				out.write("<Placemark><name>" + LinkId
						+ " </name><Point><coordinates>" + String.valueOf(lon)
						+ "," + String.valueOf(lati)
						+ ",0</coordinates></Point></Placemark>");

				// sensorsGrid[index1][index2].add(rs.getInt(1));
			}
			out.write("</Document></kml>");
			out.close();
			f.close();
			con.close();
			System.out.println("Count = " + count);
		} catch (Exception r) {
			r.printStackTrace();
		}

	}

	public static void func1234() {

		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Edges_G_with_zelvels.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			FileWriter fstream_out1 = new FileWriter(
					"C:\\Users\\jiayunge\\Edges_G_1234_with_zlevles.csv");
			BufferedWriter out1 = new BufferedWriter(fstream_out1);

			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int funcclass = Integer.parseInt(nodes[1]);

				if (funcclass == 5)
					continue;

				out1.write(strLine + "\n");

			}
			out1.close();
			in.close();
			System.out.println("finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void create_nodesFile() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges_G_12345_with_zlevels.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			FileWriter fstream_out1 = new FileWriter(config.getRoot()
					+ "/Edges_G_12345_final.csv");
			BufferedWriter out1 = new BufferedWriter(fstream_out1);

			FileWriter fstream_out2 = new FileWriter(config.getRoot()
					+ "/Nodes_G_12345.csv");
			BufferedWriter out2 = new BufferedWriter(fstream_out2);

			String strLine;
			int count_nodes = 0;
			int count = 0;

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int mark1 = 0;
				int mark2 = 0;

				double x1 = Double.parseDouble(nodes[3]);
				double y1 = Double.parseDouble(nodes[4]);
				int z1 = Integer.parseInt(nodes[5]);
				String check1 = x1 + "," + y1 + "," + z1;

				double x2 = Double.parseDouble(nodes[6]);
				double y2 = Double.parseDouble(nodes[7]);
				int z2 = Integer.parseInt(nodes[8]);
				String check2 = x2 + "," + y2 + "," + z2;

				if (lookup.containsKey(check1))
					mark1 = 1;
				if (lookup.containsKey(check2))
					mark2 = 1;

				if (mark1 == 0) {
					out2.write("n" + count_nodes + "," + x1 + "," + y1 + "\n");
					lookup.put(check1, count_nodes);
					count_nodes++;
				}

				if (mark2 == 0) {
					out2.write("n" + count_nodes + "," + x2 + "," + y2 + "\n");
					lookup.put(check2, count_nodes);
					count_nodes++;
				}

				out1.write(nodes[0] + "," + nodes[1] + "," + nodes[2] + ",n"
						+ lookup.get(check1) + ",n" + lookup.get(check2) + ","
						+ nodes[3] + "," + nodes[4] + "," + nodes[6] + ","
						+ nodes[7] + "\n");
				count++;
				System.out.println("Count = " + count);

			}

			in.close();
			out1.close();
			out2.close();

			System.out.println("hehe");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void input_mod_add_zlevles() {
		try {
			FileInputStream fstream = new FileInputStream(config.getRoot()
					+ "/Edges_G_12345.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges_G_12345_with_zlevels.csv");
			out = new BufferedWriter(fstream_out);
			String strLine;
			int count = 0;

			Connection con = getConnection();

			long link_id_pre = -1;
			double[][] check = new double[1000][3];
			int num = 0;

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				long link_id = Long.parseLong(nodes[0]);

				if (link_id != link_id_pre) {
					String sql = "SELECT z.geom.sdo_point.y,z.geom.sdo_point.x,z.z_level FROM zlevels_new z where   z.link_id ="
							+ nodes[0];
					PreparedStatement pstatement = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					ResultSet res = pstatement.executeQuery();

					num = 0;
					while (res.next()) {
						num++;
						check[num - 1][0] = res.getDouble(1);
						check[num - 1][1] = res.getDouble(2);
						check[num - 1][2] = res.getDouble(3);
					}

					link_id_pre = link_id;

					res.close();
					pstatement.close();
				}

				int zlevel1 = 0, zlevel2 = 0;
				double x1 = Double.parseDouble(nodes[3]);
				double y1 = Double.parseDouble(nodes[4]);
				double x2 = Double.parseDouble(nodes[5]);
				double y2 = Double.parseDouble(nodes[6]);
				int c = 0;
				for (int i = 0; i < num; i++) {
					if (x1 == check[i][0] && y1 == check[i][1]) {
						zlevel1 = (int) check[i][2];
						c++;
					} else if (x2 == check[i][0] && y2 == check[i][1]) {
						zlevel2 = (int) check[i][2];
						c++;
					}

					// sucess add zlevel
					if (c == 2)
						break;
				}

				out.write(nodes[0] + "," + nodes[1] + "," + nodes[2] + ","
						+ nodes[3] + "," + nodes[4] + ","
						+ String.valueOf(zlevel1) + "," + nodes[5] + ","
						+ nodes[6] + "," + String.valueOf(zlevel2) + "\n");

				count++;

				System.out.println("count = " + count);

			}

			out.close();
			br.close();
			System.out.println("hehe");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void input_simplied() {
		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\Edges_G_simplied.csv");
			out = new BufferedWriter(fstream_out);
			String strLine;
			int count = 0;
			// out.write("<kml><Document>");
			String st_pre = "";
			String end_pre = "";

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				if (!st_pre.equals("")) {
					String st_new = nodes[3] + "," + nodes[4];
					String end_new = nodes[5] + "," + nodes[6];
					if (st_new.equals(end_pre) && end_new.equals(st_pre)) {
						st_pre = nodes[3] + "," + nodes[4];
						end_pre = nodes[5] + "," + nodes[6];
						continue;
					}
				}
				// else
				out.write(strLine + "\n");
				st_pre = nodes[3] + "," + nodes[4];
				end_pre = nodes[5] + "," + nodes[6];

				count++;
				// if(count == 500000)
				// break;
				System.out.println("count = " + count);
			}

			out.close();
			br.close();
			System.out.println("hehe");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void create_roads_temp() {
		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\full_roads_new1.kml");
			out = new BufferedWriter(fstream_out);
			String strLine;
			int count = 0;
			out.write("<kml><Document>");

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");

				int func_class = Integer.parseInt(nodes[1]);
				// if(func_class==5)
				// continue;
				long LinkId = Long.parseLong(nodes[0]);

				out.write("<Placemark><name>roads</name><description>"
						+ String.valueOf(LinkId)
						+ "</description><LineString><tessellate>1</tessellate><coordinates>"
						+ nodes[4] + "," + nodes[3] + ",0 " + nodes[6] + ","
						+ nodes[5]
						+ ",0 </coordinates></LineString></Placemark>\n");
				count++;
				if (count == 500000)
					break;
				System.out.println("count = " + count);
			}
			out.write("</Document></kml>");
			out.close();
			br.close();
			System.out.println("hehe");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void input_mod() {
		try {
			// Area of LA
			double MinLng = -118.67913;
			double MaxLng = -117.09847;
			double MinLat = 33.59831;
			double MaxLat = 34.33519;

			FileWriter fstream_out = new FileWriter(config.getRoot()
					+ "/Edges_G_12345.csv");
			BufferedWriter out = new BufferedWriter(fstream_out);

			Connection con = getConnection();

			// String sql =
			// "SELECT    dc.link_id, dc.func_class,dc.Dir_Travel, dc.st_name, dc.Speed_Cat, dc.geom  FROM streets_dca1_new dc, zlevels_new z1, zlevels_new z2 where  z1.node_id !=0 and z2.node_id !=0 and Ref_In_Id = z1.node_id and nRef_In_Id = z2.node_id order by dc.link_id   ";
			// Selesct the link_id with same zlevels
			String sql_o = "SELECT distinct dc.link_id FROM streets_dca1_new dc, zlevels_new z1, zlevels_new z2 where  z1.node_id !=0 and z2.node_id !=0 and Ref_In_Id = z1.node_id and nRef_In_Id = z2.node_id order by dc.link_id   ";

			PreparedStatement pstatement_o = con.prepareStatement(sql_o,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet res_o = pstatement_o.executeQuery();

			int count = 0;

			while (res_o.next()) {
				long link_id = res_o.getLong(1);

				if (links.containsKey(link_id))
					continue;
				else
					links.put(link_id, 1);

				// Select Info according the link_id selected
				String sql = "SELECT  dc.link_id, dc.func_class,dc.Dir_Travel, dc.st_name, dc.Speed_Cat, dc.geom  FROM streets_dca1_new dc where dc.link_id = "
						+ link_id;
				PreparedStatement pstatement = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet res = pstatement.executeQuery();
				res.first();

				int func_class = res.getInt(2);
				String Dir_Travel = res.getString(3);
				String st_name = res.getString(4);
				int speed_cat = res.getInt(5);
				STRUCT st = (STRUCT) res.getObject(6);
				JGeometry geom = JGeometry.load(st);
				Double[] point_ref = new Double[2];
				Double[] point_iref = new Double[2];
				int num_points = geom.getNumPoints();
				double[] points = geom.getOrdinatesArray();
				for (int i = 0; i < num_points - 1; i++) {
					point_ref[0] = points[i * 2 + 1];
					point_ref[1] = points[i * 2 + 0];
					point_iref[0] = points[(i + 1) * 2 + 1];
					point_iref[1] = points[(i + 1) * 2 + 0];
					if (Dir_Travel.equals("B")) {
						out.write(link_id + "," + func_class + "," + st_name
								+ "," + point_ref[0] + "," + point_ref[1] + ","
								+ point_iref[0] + "," + point_iref[1] + "\r\n");
						out.write(link_id + "," + func_class + "," + st_name
								+ "," + point_iref[0] + "," + point_iref[1]
								+ "," + point_ref[0] + "," + point_ref[1]
								+ "\r\n");
					} else if (Dir_Travel.equals("F")) {
						out.write(link_id + "," + func_class + "," + st_name
								+ "," + point_ref[0] + "," + point_ref[1] + ","
								+ point_iref[0] + "," + point_iref[1] + "\r\n");
					} else {
						out.write(link_id + "," + func_class + "," + st_name
								+ "," + point_iref[0] + "," + point_iref[1]
								+ "," + point_ref[0] + "," + point_ref[1]
								+ "\r\n");
					}

				}

				res.close();
				pstatement.close();

				count++;
				System.out.println(" roads finished:" + count);
				// if(count == 3)
				// break;

			}

			out.close();

			System.out.println("finished!!!");
			/*
			 * String st_name = res.getString(2); STRUCT st = (STRUCT)
			 * res.getObject(4); JGeometry geom = JGeometry.load(st);
			 * 
			 * 
			 * System.out.println("st_name1: "+st_name);
			 * 
			 * int num_points = geom.getNumPoints();
			 * 
			 * // System.out.println(geom.toStringFull());
			 * 
			 * 
			 * System.out.println("Number of points: "+num_points); double[]
			 * points = geom.getOrdinatesArray();
			 * System.out.println("l = "+points.length); for(int i = 0; i<
			 * points.length;i++) System.out.println(points[i]); // for(int i =
			 * 1; i<=num_points; //System.out.println(geom.toStringFull());
			 * //String input = geom.toStringFull();
			 * //System.out.println(input); //String[] nodes = input. // for(int
			 * i= 0 ;i<nodes.length;i++) // System.out.println(nodes[i]); //
			 * double x = geom.getPoint()[0]; // double y = geom.getPoint()[1];
			 * // System.out.println(y+","+x);
			 * 
			 * //}
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void findfault() {
		try {

			FileInputStream fstream = new FileInputStream(
					"H:\\jiayunge\\test.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			int i = 0;
			Connection con = getConnection();

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				// String link_id = nodes[0];
				String link_id = nodes[0] + "" + nodes[3].substring(1) + ""
						+ nodes[4].substring(1);
				int funClass = Integer.parseInt(nodes[1]);
				i++;

				FileInputStream fstream1 = new FileInputStream(
						"H:\\jiayunge\\Highway_Sensor_Close.csv");
				DataInputStream in1 = new DataInputStream(fstream1);
				BufferedReader br1 = new BufferedReader(new InputStreamReader(
						in1));
				String strLine1;

				FileInputStream fstream2 = new FileInputStream(
						"H:\\jiayunge\\arterial_Sensor_Close.csv");
				DataInputStream in2 = new DataInputStream(fstream2);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in2));
				String strLine2;

				FileWriter fstream_out = new FileWriter(
						"H:\\jiayunge\\test\\road" + i + ".kml");
				BufferedWriter out = new BufferedWriter(fstream_out);

				out.write("<kml>\n <Document>\n");
				out.write("<Placemark><name>"
						+ String.valueOf(link_id)
						+ "</name><description>"
						+ String.valueOf(funClass)
						+ "</description><LineString><tessellate>1</tessellate><coordinates>"
						+ nodes[6] + "," + nodes[5] + ",0," + nodes[8] + ","
						+ nodes[7]
						+ ",0 </coordinates></LineString></Placemark>\n");
				/*
				 * if(funClass == 1 || funClass == 2){
				 * //System.out.println("im in"+funClass); while ((strLine1 =
				 * br1.readLine()) != null){ String[] nodes1 =
				 * strLine1.split(","); String link = nodes1[0]+""+nodes1[1];
				 * if(link_id.equals(link)){ //System.out.println("hit!");
				 * for(int k = 2;k<nodes1.length;k++){ int sensor_id =
				 * Integer.parseInt(nodes1[k]);
				 * 
				 * String sql =
				 * "select start_lat_long from highway_congestion_config where link_id = "
				 * +sensor_id; PreparedStatement f = con.prepareStatement(sql,
				 * ResultSet.TYPE_SCROLL_INSENSITIVE,
				 * ResultSet.CONCUR_READ_ONLY); ResultSet rs = f.executeQuery();
				 * rs.first();
				 * 
				 * STRUCT st = (STRUCT) rs.getObject(1); JGeometry geom =
				 * JGeometry.load(st); //pairs[count] = new
				 * PairInfo(geom.getPoint()[1], geom.getPoint()[0]); double lati
				 * = geom.getPoint()[1]; double lon = geom.getPoint()[0];
				 * out.write("<Placemark><name>"+String.valueOf(sensor_id)+
				 * "</name><Point><coordinates>"
				 * +String.valueOf(lon)+","+String.valueOf
				 * (lati)+",0</coordinates></Point></Placemark>\n");
				 * 
				 * rs.close(); f.close(); } break;
				 * 
				 * }
				 * 
				 * }
				 * 
				 * 
				 * }else{ //System.out.println("im in"+funClass); while
				 * ((strLine2 = br2.readLine()) != null){ String[] nodes2 =
				 * strLine2.split(","); String link = nodes2[0]+nodes2[1];
				 * if(link_id.equals(link)){ //System.out.println("hit!");
				 * for(int k = 2;k<nodes2.length;k++){ int sensor_id =
				 * Integer.parseInt(nodes2[k]);
				 * 
				 * String sql =
				 * "select start_lat_long from arterial_congestion_config where link_id = "
				 * +sensor_id; PreparedStatement f = con.prepareStatement(sql,
				 * ResultSet.TYPE_SCROLL_INSENSITIVE,
				 * ResultSet.CONCUR_READ_ONLY); ResultSet rs = f.executeQuery();
				 * rs.first();
				 * 
				 * STRUCT st = (STRUCT) rs.getObject(1); JGeometry geom =
				 * JGeometry.load(st); //pairs[count] = new
				 * PairInfo(geom.getPoint()[1], geom.getPoint()[0]); double lati
				 * = geom.getPoint()[1]; double lon = geom.getPoint()[0];
				 * out.write("<Placemark><name>"+String.valueOf(sensor_id)+
				 * "</name><Point><coordinates>"
				 * +String.valueOf(lon)+","+String.valueOf
				 * (lati)+",0</coordinates></Point></Placemark>\n");
				 * 
				 * rs.close(); f.close(); } break;
				 * 
				 * }
				 * 
				 * }
				 * 
				 * 
				 * }
				 */
				out.write("</Document></kml>");
				out.close();
				// br1.close();
				// br2.close();
				// in1.close();
				// in2.close();

				System.out.println("roads finished: " + i);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void correct_inputfile() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			FileWriter fstream2 = new FileWriter(
					"C:\\Users\\jiayunge\\Edges_corrected.csv");
			BufferedWriter out = new BufferedWriter(fstream2);

			String strLine;
			int i = 0;

			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				long link_id = Long.parseLong(nodes[0]);

				String sql = "SELECT dir_travel FROM STREETS_DCA1  WHERE link_id = "
						+ link_id;
				// System.out.println(sql);
				Connection con = getConnection();
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				ResultSet r = f.executeQuery();
				r.first();
				String dir_travel = r.getString(1);
				if (dir_travel.equals("B")) {
					out.write(strLine);
					out.write("\n");

					i++;
					System.out.println(i + " roads finished");
					continue;
				}

				// change the order of the start and ending nodes

				out.write(nodes[0] + "," + nodes[1] + "," + nodes[2] + ","
						+ nodes[4] + "," + nodes[3] + "," + nodes[7] + ","
						+ nodes[8] + "," + nodes[5] + "," + nodes[6]);
				out.write("\n");

				i++;
				System.out.println(i + " roads finished");

				r.close();
				f.close();
				con.close();

			}

			out.close();
			System.out.println("finished!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void calculate_travel_time_new2(int time_no) {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Edges_ppt.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			double time = 0.;
			int count = 0;
			int[][] pair = new int[4][2];

			// n20659,n41578
			// n41578,n20658
			// n20658,n20590
			// n20590,n20828

			pair[0][0] = 20659;
			pair[0][1] = 41578;
			pair[1][0] = 41578;
			pair[1][1] = 20658;
			pair[2][0] = 20658;
			pair[2][1] = 20590;
			pair[3][0] = 20590;
			pair[3][1] = 20828;
			for (int m = 0; m < 4; m++) {
				// String[] nodes = strLine.split(",");
				int st_id = pair[m][0];
				int end_id = pair[m][1];

				FileInputStream fstream2 = new FileInputStream(
						"C:\\Users\\jiayunge\\AdjList_Thursday.txt");
				DataInputStream in2 = new DataInputStream(fstream2);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in2));
				String strLine2 = "";

				for (int i = 0; i <= st_id; i++) {
					strLine2 = br2.readLine();
				}

				String[] nodes2 = strLine2.split(";");
				int flag = 0;
				for (int i = 0; i < nodes2.length && (flag == 0); i++) {
					String[] nodes3 = nodes2[i].split(":");
					if (nodes3[0].equals("n" + String.valueOf(end_id) + "(V)")
							|| nodes3[0].equals("n" + String.valueOf(end_id)
									+ "(F)")) {
						flag = 1;
						if (nodes3[0].endsWith("(F)")) {
							time += Double.parseDouble(nodes3[1]);
							System.out.println("n" + String.valueOf(st_id)
									+ "-n" + String.valueOf(end_id) + ": "
									+ Double.parseDouble(nodes3[1]));
						} else {
							String[] nodes4 = nodes3[1].split(",");
							int increase_no = (int) (time / 900000.);
							int time_slot = time_no + increase_no;
							time += Double.parseDouble(nodes4[time_slot]);
							System.out.println("n" + String.valueOf(st_id)
									+ "-n" + String.valueOf(end_id) + ": "
									+ Double.parseDouble(nodes4[time_slot]));
						}
					}
				}

				br2.close();
				in2.close();
				fstream2.close();

				count++;
				System.out.println(count + " roads processed");

				// time = time + Double.parseDouble(nodes[2]);
			}

			br.close();
			in.close();
			fstream.close();

			time = time / 1000.;
			time = time / 60.;
			System.out.println("Total travel time : " + time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void gains_avg() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\avg2.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			double[] temp = new double[62];
			String strLine;
			int i = 0;
			while ((strLine = br.readLine()) != null) {

				temp[i] = Double.parseDouble(strLine);
				i++;
			}

			double avg = 0.;
			for (int k = 0; k < 62; k++)
				avg += temp[k];

			System.out.println("avg = " + (avg / (double) 62));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void calculate_travel_time_new(int time_no) {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Edges_test15_7pm.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			double time = 0.;
			int count = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int st_id = Integer.parseInt(nodes[3].substring(1));
				int end_id = Integer.parseInt(nodes[4].substring(1));

				FileInputStream fstream2 = new FileInputStream(
						"C:\\Users\\jiayunge\\AdjList_Thursday.txt");
				DataInputStream in2 = new DataInputStream(fstream2);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in2));
				String strLine2 = "";

				for (int i = 0; i <= st_id; i++) {
					strLine2 = br2.readLine();
				}

				String[] nodes2 = strLine2.split(";");
				int flag = 0;
				for (int i = 0; i < nodes2.length && (flag == 0); i++) {
					String[] nodes3 = nodes2[i].split(":");
					if (nodes3[0].equals("n" + String.valueOf(end_id) + "(V)")
							|| nodes3[0].equals("n" + String.valueOf(end_id)
									+ "(F)")) {
						flag = 1;
						if (nodes3[0].endsWith("(F)"))
							time += Double.parseDouble(nodes3[1]);
						else {
							String[] nodes4 = nodes3[1].split(",");
							int increase_no = (int) (time / 900000.);
							int time_slot = time_no + increase_no;
							time += Double.parseDouble(nodes4[time_slot]);
						}
					}
				}

				br2.close();
				in2.close();
				fstream2.close();

				count++;
				System.out.println(count + " roads processed");

				// time = time + Double.parseDouble(nodes[2]);
			}

			br.close();
			in.close();
			fstream.close();

			time = time / 1000.;
			time = time / 60.;
			System.out.println("Total travel time : " + time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void test_hashmap() {
		int a0 = 1;
		int a1 = 2;
		int b0 = 6;
		int b1 = 7;

		testmap.put(a0, b0);
		testmap.put(a0, b1);
		System.out.println("size = " + testmap.size());
	}

	public static void calculate_travel_time() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Highway_travel_time_pattern_testroads1.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			double time = 0.;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");

				time = time + Double.parseDouble(nodes[2]);
			}

			br.close();
			in.close();
			fstream.close();

			FileInputStream fstream2 = new FileInputStream(
					"C:\\Users\\jiayunge\\Arterial_travel_time_pattern_testroads1.csv");
			DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			String strLine2;

			while ((strLine2 = br2.readLine()) != null) {
				String[] nodes = strLine2.split(",");

				time = time + Double.parseDouble(nodes[2]);
			}

			System.out.println("total time = " + time);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testroads() {
		try {
			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\test.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			FileWriter fstream_out = new FileWriter("H:\\Jiayunge\\test.csv");
			out = new BufferedWriter(fstream_out);

			strLine = br.readLine();

			String[] nodes = strLine.split(";");

			System.out.println("length = " + (nodes.length - 1));

			int count = 0;
			for (int i = 0; i < nodes.length - 2; i++) {
				String query = nodes[i] + "," + nodes[i + 1];

				FileInputStream fstream2 = new FileInputStream(
						"H:\\Jiayunge\\Edges_G_1234_final_withextranames.csv");
				DataInputStream in2 = new DataInputStream(fstream2);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in2));

				String strLine2;
				while ((strLine2 = br2.readLine()) != null) {
					String[] nodes2 = strLine2.split(",");
					String test = nodes2[5] + "," + nodes2[6] + "," + nodes2[7]
							+ "," + nodes2[8];

					if (query.equals(test)) {
						out.write(strLine2);
						out.write("\r\n");
						// System.out.println("find!");
						break;
					}

				}
				count++;
				System.out.println("processed No " + (i + 1) + "-" + (i + 2)
						+ " road");

				br2.close();
				in2.close();
				fstream2.close();

			}

			out.close();
			fstream_out.close();
			System.out.println("finished!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void create_graph_kml() {
		try {
			// FileInputStream fstream = new
			// FileInputStream("C:\\Users\\jiayunge\\test1.csv");
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\h2.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// FileWriter fstream_out = new
			// FileWriter("C:\\Users\\jiayunge\\test1.kml");
			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\h2.kml");
			out = new BufferedWriter(fstream_out);
			String strLine;
			Connection con = getConnection();
			out.write("<kml><Document>");
			int count = 0;
			while ((strLine = br.readLine()) != null) {
				count++;
				System.out.println("Processing no = " + count);
				String[] nodes = strLine.split(",");

				FileInputStream fstream2 = new FileInputStream(
						"C:\\Users\\jiayunge\\Edges.csv");
				DataInputStream in2 = new DataInputStream(fstream2);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in2));
				String strLine2;

				while ((strLine2 = br2.readLine()) != null) {

					String[] nodes2 = strLine2.split(",");
					if (nodes2[0].equals(nodes[0])) {
						out.write("<Placemark><name>"
								+ nodes[0]
								+ "</name><description>link</description><LineString><tessellate>1</tessellate><coordinates>"
								+ nodes2[6] + "," + nodes2[5] + ",0 "
								+ nodes2[8] + "," + nodes2[7] + ",0 "
								+ "</coordinates> </LineString></Placemark>");
						for (int i = 2; i < nodes.length; i++) {
							String sql = "select link_id,start_lat_long from highway_congestion_config where link_id ="
									+ nodes[i];
							PreparedStatement f = con.prepareStatement(sql,
									ResultSet.TYPE_SCROLL_INSENSITIVE,
									ResultSet.CONCUR_READ_ONLY);
							ResultSet rs = f.executeQuery();
							rs.first();
							STRUCT st = (STRUCT) rs.getObject(2);
							JGeometry geom = JGeometry.load(st);
							// pairs[count] = new PairInfo(geom.getPoint()[1],
							// geom.getPoint()[0]);
							double lati = geom.getPoint()[1];
							double lon = geom.getPoint()[0];

							out.write("<Placemark><name>"
									+ String.valueOf(rs.getInt(1))
									+ "</name><Point><coordinates>"
									+ String.valueOf(lon) + ","
									+ String.valueOf(lati)
									+ ",0 </coordinates></Point></Placemark>");

							rs.close();
							f.close();
						}
						break;

					}
				}

				// br2.close();
				// in2.close();
				// fstream2.close();
			}
			out.write("</Document></kml>");
			out.close();
			System.out.println("finished!");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void create_new_edge_close() {
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\jiayunge\\Arterial_Sensor_Close.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\Arterial_Sensor_Close2.csv");
			out = new BufferedWriter(fstream_out);
			String strLine;
			int count = 0;
			int flag = 0;
			while ((strLine = br.readLine()) != null) {
				flag = 0;
				String[] nodes = strLine.split(",");
				if (nodes.length < 3)
					continue;
				out.write(strLine + "\n");
				flag = 1;
				if (flag == 1)
					count++;
			}
			System.out.println("count = " + count);
			out.close();
		} catch (Exception r) {
			r.printStackTrace();
		}

	}

	public static void create_new_EdgeFile() {
		try {
			int count = 0;
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(
					"C:\\Users\\jiayunge\\EdgesNew3.txt");
			out = new BufferedWriter(fstream_out);
			String strLine;
			int flag = 0;
			int k = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				flag = 0;
				for (int i = 0; i < count; i++) {
					if (Integer.parseInt(nodes[0]) == check[i]) {
						flag = 1;
						break;
					}
				}

				if (flag == 0) {

					check[count] = Integer.parseInt(nodes[0]);
					count++;
					out.write(strLine + "\r\n");

				}
				k++;

				System.out.println("round = " + k);
			}
			out.close();
			System.out.println("aa");
		} catch (Exception r) {
			r.printStackTrace();
		}

	}

	public static void create_roads() {
		try {
			FileInputStream fstream = new FileInputStream(
					"H:\\Jiayunge\\Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			FileWriter fstream_out = new FileWriter(
					"H:\\Jiayunge\\Func1to4.kml");
			out = new BufferedWriter(fstream_out);
			String strLine;
			out.write("<kml><Document>");
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");

				// System.out.println(strLine);
				// System.out.println(nodes[0]);

				// int FuncClass = Integer.parseInt(nodes[1]);

				// if(nodes[2].endsWith("I-10")||nodes[2].endsWith("I-110")){
				String st_node = nodes[3];
				String end_node = nodes[4];
				// long LinkId =
				// Long.parseLong(nodes[0]+""+nodes[3].substring(1)+""+nodes[4].substring(1));
				// System.out.println(LinkId);
				// out.write(LinkId+"\r\n");
				out.write("<Placemark><name>"
						+ nodes[2]
						+ "</name><description>"
						+ "testcase1"
						+ "</description><LineString><tessellate>1</tessellate><coordinates>"
						+ nodes[6] + "," + nodes[5] + ",0 " + nodes[8] + ","
						+ nodes[7]
						+ ",0 </coordinates></LineString></Placemark>");
				// }

			}
			out.write("</Document></kml>");
			out.close();
			br.close();
			System.out.println("hehe");
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void create_sensors() {
		try {
			FileWriter fstream = new FileWriter(
					"C:\\Users\\jiayunge\\sensors_highway.txt");
			out = new BufferedWriter(fstream);
			Connection con = getConnection();
			System.out.println("Connected");
			String sql = "select link_id,start_lat_long from highway_congestion_config";
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = f.executeQuery();
			int count = 0;
			out.write("<kml><Document>");
			while (rs.next()) {
				count++;

				int LinkId = rs.getInt(1);
				STRUCT st = (STRUCT) rs.getObject(2);
				JGeometry geom = JGeometry.load(st);
				// pairs[count] = new PairInfo(geom.getPoint()[1],
				// geom.getPoint()[0]);
				double lati = geom.getPoint()[1];
				double lon = geom.getPoint()[0];
				out.write("<Placemark><name>" + String.valueOf(LinkId)
						+ "</name><Point><coordinates>" + String.valueOf(lon)
						+ "," + String.valueOf(lati)
						+ ",0</coordinates></Point></Placemark>");

				// sensorsGrid[index1][index2].add(rs.getInt(1));
			}
			out.write("</Document></kml>");
			out.close();
			f.close();
			con.close();
			System.out.println("Count = " + count);
		} catch (Exception r) {
			r.printStackTrace();
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

}
