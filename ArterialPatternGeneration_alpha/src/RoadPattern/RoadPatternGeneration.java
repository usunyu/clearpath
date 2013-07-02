package RoadPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class RoadPatternGeneration {

	/**
	 * @param args
	 */
	// file
	private static String fileRoot = "../GeneratedFile";
	private static String fileLink = "Edges.csv";
	// street
	static String street = "FIGUEROA";
	static int StartNode = 49250387;
	static int EndNode = 49260081;
	// parameter
	static double searchDistance = 0.15;
	static int devide = 1000;
	private static String[] days = { "Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday", "Sunday" };
	// database
	static String root = "/Users/Sun/Documents/workspace/CleanPath/GeneratedFile";
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<LinkInfo> edgeList = new ArrayList<LinkInfo>();
	static ArrayList<LinkInfo> pathList = new ArrayList<LinkInfo>();

	static HashMap<Integer, LinkInfo> linkMap = new HashMap<Integer, LinkInfo>();

	static HashSet<String> checkEdge = new HashSet<String>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static HashMap<String, PatternPairInfo> patternMap = new HashMap<String, PatternPairInfo>();
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static ArrayList<LinkInfo> searchList = new ArrayList<LinkInfo>();

	static HashMap<String, LinkInfo> edgeMap = new HashMap<String, LinkInfo>();

	static HashMap<Integer, ArrayList<Integer>> adjNodeMap = new HashMap<Integer, ArrayList<Integer>>();

	static HashMap<String, Integer> nodesToLink = new HashMap<String, Integer>();

	static HashMap<Integer, PairInfo> nodePosition = new HashMap<Integer, PairInfo>();

	static double[] speedArray = new double[60];

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* ------------------------- */
		fetchEdge2();
		generateAdjNodeList();
		// printAdjNodeList();
		generatePath();
		fetchSensor();
		// generateSensorKML();
		matchEdgeSensor2(0);
		generateEdgeKML2();
		// generatePattern2();
		calAvergSpeed();
		generateExcel();
		/* ------------------------- */
		// readFileInMemory();
		// generateEdgesKML();
		// matchSensorsToEdges();
		/* ------------------------- */
		// fetchSensor();
		// generateSensorKML();
		// fetchEdge();
		// matchEdgeSensor();
		// generateEdgeKML();
		// generatePattern();
		// generateAllSensorKML();
		// generateNullSensorKML();
		// testDirection();
		// printEdgeWithSensor();
		/* ------------------------- */
		// readFileToMemory();
		// searchStreet();
		// createPattern();
	}

	/*
	 * --------------------------------------------------------------------------
	 * ----------------------------------------------------
	 */
	private static void generateExcel() {
		
	}
	
	private static void calAvergSpeed() {
		System.out.println("calculate avg speed...");
		try {
			for (int i = 0; i < pathList.size(); i++) {
				LinkInfo link = pathList.get(i);
				// System.out.print("processing link " + link.getIntLinkId());
				// System.out.print("\r");
				if (link.getSensor() != null) {
					Connection con = null;
					String sql = null;
					PreparedStatement pstatement = null;
					ResultSet res = null;
					con = getConnection();

					sql = "select * from arterial_averages3 where link_id = '"
							+ link.getSensor().getSensorId() + "'"
							+ " and day = '" + days[0] + "'";
					pstatement = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					res = pstatement.executeQuery();

					while (res.next()) {
						double speed = res.getDouble(2);
						String time = res.getString(5);
						int index = UtilClass.getIndex(time);
						if (index == -1)
							continue;
						speedArray[index] += speed;
					}

					res.close();
					pstatement.close();
					con.close();
				}
			}

			for (int i = 0; i < speedArray.length; i++) {
				speedArray[i] /= pathList.size();
				// System.out.println(speedArray[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("calculate avg speed finish!");
	}

	private static void generatePattern2() {
		System.out.println("generate pattern...");
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("Pattern_" + street + "_" + days[0]
					+ ".txt");
			out = new BufferedWriter(fstream);

			for (int i = 0; i < pathList.size(); i++) {
				LinkInfo link = pathList.get(i);
				// System.out.print("processing link " + link.getIntLinkId());
				// System.out.print("\r");
				if (link.getSensor() != null) {
					Connection con = null;
					String sql = null;
					PreparedStatement pstatement = null;
					ResultSet res = null;
					con = getConnection();

					sql = "select * from arterial_averages3 where link_id = '"
							+ link.getSensor().getSensorId() + "'"
							+ " and day = '" + days[0] + "'";
					pstatement = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					res = pstatement.executeQuery();
					out.write("Link ID:" + link.getIntLinkId() + " Sensor ID:"
							+ link.getSensor().getSensorId() + "\n");
					while (res.next()) {
						double speed = res.getDouble(2);
						String time = res.getString(5);
						out.write(time + ": " + speed + ", ");
					}
					out.write("\n");

					res.close();
					pstatement.close();
					con.close();
				}
			}

			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("generate pattern finish!");
	}

	private static void matchEdgeSensor2(int dir) {
		System.out.println("match sensors to edges...");
		// first round
		for (int i = 0; i < pathList.size(); i++) {
			LinkInfo link = pathList.get(i);
			// System.out.println("processing link id: " + link.getIntLinkId());
			ArrayList<PairInfo> nodeList = link.getNodeList();
			boolean findSensor = false;
			if (nodeList.size() > 2) {
				// examine the intermediate node first
				// System.out.println("has more than 2 nodes");
				for (double step = searchDistance / devide; step < searchDistance; step += step) {
					// skip first one and last one
					for (int j = 1; j < nodeList.size() - 1; j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for (int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator
									.CalculationByDistance(node1, node2);
							if (distance < step && sensor.getDirection() == dir) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
								// System.out.println("find sensor " +
								// sensor.getSensorId() + " for link " +
								// link.getLinkId());
								break;
							}
						}
						if (findSensor)
							break;
					}
					if (findSensor)
						break;
				}
			}

			if (!findSensor) {
				// examine the vertex
				// System.out.println("has 2 nodes");
				for (double step = searchDistance / devide; step < searchDistance; step += step) {
					for (int j = 0; j < nodeList.size(); j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for (int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator
									.CalculationByDistance(node1, node2);
							if (distance < step && sensor.getDirection() == dir) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
								// System.out.println("find sensor " +
								// sensor.getSensorId() + " for link " +
								// link.getLinkId());
								break;
							}
						}
						if (findSensor)
							break;
					}
					if (findSensor)
						break;
				}
			}

			// if(!findSensor) {
			// System.out.println("cannot find sensor for link " +
			// link.getIntLinkId());
			// }
		}
		int count = 0;
		for (int i = 0; i < pathList.size(); i++) {
			LinkInfo link = pathList.get(i);
			if (link.getSensor() != null) {
				count++;
			}
		}
		System.out.println("there are " + count
				+ " link has sensor in first round");

		// second round
		int lastCount = count;
		while (count != pathList.size()) {
			for (int i = 0; i < pathList.size(); i++) {
				LinkInfo link = pathList.get(i);
				if (link.getSensor() != null) {
					// left
					if (i - 1 >= 0) {
						LinkInfo linkL = pathList.get(i - 1);
						if (linkL.getSensor() == null) {
							linkL.setSensor(link.getSensor());
							count++;
						}
					}
					// right
					if (i + 1 < pathList.size()) {
						LinkInfo linkR = pathList.get(i + 1);
						if (linkR.getSensor() == null) {
							linkR.setSensor(link.getSensor());
							count++;
						}
					}
				}
			}
			if (count == lastCount)
				break;
			lastCount = count;
		}
		System.out.println("there are " + count
				+ " link has sensor in second round, there has "
				+ pathList.size() + " link");
		System.out.println("match Sensors finish!");
	}

	private static void generatePath() {
		System.out.println("generate path...");
		int current = StartNode;
		PairInfo endPosition = nodePosition.get(EndNode);
		while (current != EndNode) {
			ArrayList<Integer> adjList = adjNodeMap.get(current);
			int minNode = -1;
			double minDis = 0;
			for (int i = 0; i < adjList.size(); i++) {
				if (adjList.get(i) == 49251564)
					continue;
				if (minNode == -1) {
					minNode = adjList.get(i);
					PairInfo nodePos = nodePosition.get(minNode);
					minDis = DistanceCalculator.CalculationByDistance(nodePos,
							endPosition);
				} else {
					int node = adjList.get(i);
					PairInfo nodePos = nodePosition.get(node);
					double dis = DistanceCalculator.CalculationByDistance(
							nodePos, endPosition);
					if (dis < minDis) {
						minNode = node;
						minDis = dis;
					}
				}
			}
			String nodeString = String.valueOf(current)
					+ String.valueOf(minNode);
			int linkId = nodesToLink.get(nodeString);
			LinkInfo link = linkMap.get(linkId);
			pathList.add(link);
			current = minNode;
		}
		System.out.println("generate path finish!");
	}

	private static void generateEdgeKML2() {
		System.out.println("generate edge kml...");
		try {
			FileWriter fstream = new FileWriter("Edges_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < pathList.size(); i++) {
				LinkInfo link = pathList.get(i);
				int intId = link.getIntLinkId();
				SensorInfo sensor = link.getSensor();
				String sensorStr = "NULL";
				ArrayList<PairInfo> nodeList = link.getNodeList();
				if (sensor != null) {
					sensorStr = String.valueOf(sensor.getSensorId());
				}

				String kmlStr = "<Placemark><name>Link:" + intId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Sensor:" + sensorStr;
				kmlStr += "Start:" + link.getStartNode();
				kmlStr += "End:" + link.getEndNode();
				kmlStr += ", Direction:" + link.getAllDir();
				// kmlStr += ", StreetName:" + link.getSt_name();
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
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
		System.out.println("generate edge kml finish!");
	}

	private static void fetchEdge2() {
		try {
			System.out.println("fetch edges...");
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();

			sql = "select * from streets_dca1_new where upper(st_name) like '%"
					+ street + "%'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int linkId = res.getInt(1);
				// System.out.println("fetching link " + linkId);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT) res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for (int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = geomPoints[i * 2 + 1];
					double longitude = geomPoints[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}

				int speedCat = res.getInt(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);

				int num = nodeList.size();

				if (!nodePosition.containsKey(refNode))
					nodePosition.put(refNode, nodeList.get(0));
				if (!nodePosition.containsKey(nrefNode))
					nodePosition.put(nrefNode, nodeList.get(num - 1));

				if (dirTravel.equals("B")) {
					int dir1 = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					int dir2 = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String dir = dir1 + "," + dir2;
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							dir);
					edgeList.add(link);
					linkMap.put(linkId, link);
				} else if (dirTravel.equals("T")) {
					int dir = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							sDir);
					edgeList.add(link);
					linkMap.put(linkId, link);
				} else {
					int dir = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					String sDir = String.valueOf(dir);
					LinkInfo link = new LinkInfo(linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							sDir);
					edgeList.add(link);
					linkMap.put(linkId, link);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch edge finish!");
	}

	private static void generateAdjNodeList() {
		System.out.println("generate adj node list...");
		for (int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			int id = link.getIntLinkId();
			int start = link.getStartNode();
			int end = link.getEndNode();
			String seString = String.valueOf(start) + String.valueOf(end);
			String esString = String.valueOf(end) + String.valueOf(start);
			nodesToLink.put(seString, id);
			nodesToLink.put(esString, id);

			if (adjNodeMap.containsKey(start)) {
				ArrayList<Integer> tempList = adjNodeMap.get(start);
				if (!tempList.contains(end))
					tempList.add(end);
			} else {
				ArrayList<Integer> tempList = new ArrayList<Integer>();
				tempList.add(end);
				adjNodeMap.put(start, tempList);
			}

			if (adjNodeMap.containsKey(end)) {
				ArrayList<Integer> tempList = adjNodeMap.get(end);
				if (!tempList.contains(start))
					tempList.add(start);
			} else {
				ArrayList<Integer> tempList = new ArrayList<Integer>();
				tempList.add(start);
				adjNodeMap.put(end, tempList);
			}
		}
		System.out.println("generate adj node list finish!");
	}

	private static void printAdjNodeList() {
		for (int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			int start = link.getStartNode();
			int end = link.getEndNode();
			ArrayList<Integer> tempStartList = adjNodeMap.get(start);
			ArrayList<Integer> tempEndList = adjNodeMap.get(end);
			System.out.print(start + ": ");
			for (int j = 0; j < tempStartList.size(); j++)
				System.out.print(tempStartList.get(j) + ",");
			System.out.println();

			System.out.print(end + ": ");
			for (int j = 0; j < tempEndList.size(); j++)
				System.out.print(tempEndList.get(j) + ",");
			System.out.println();
		}
	}

	/*
	 * --------------------------------------------------------------------------
	 * ----------------------------------------------------
	 */

	private static void matchSensorsToEdges() {
		System.out.println("matching sensors to edges...");
		Set<String> keys = edgeMap.keySet();
		Iterator<String> iter = keys.iterator();
		int num = keys.size(), i = 0, count = 0;
		DecimalFormat df = new DecimalFormat("#.00");
		try {
			while (iter.hasNext()) {
				if (i++ % 500 == 0 || i == num)
					System.out.println(df.format((double) i / num * 100) + "%");
				LinkInfo link = edgeMap.get(iter.next());
				String sql = "select link_id, direction, start_lat_long from arterial_congestion_config where sdo_within_distance"
						+ "(START_LAT_LONG,MDSYS.SDO_GEOMETRY(2002,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY("
						+ link.getNodes()[0].getQuery()
						+ ","
						+ link.getNodes()[1].getQuery()
						+ ")),'distance=150 unit=METER')='TRUE'";
				Connection con = getConnection();
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();
				double max = 200;
				int closestSensor = 0;
				while (rs.next()) {

					int sensorId = rs.getInt(1);
					int direction = rs.getInt(2);
					// handle for geom point
					STRUCT st = (STRUCT) rs.getObject(3);
					JGeometry geom = JGeometry.load(st);
					double lati = geom.getPoint()[1];
					double lon = geom.getPoint()[0];
					PairInfo node = new PairInfo(lati, lon);

					int dir = DistanceCalculator.getDirection(link);
					double d1 = DistanceCalculator.CalculationByDistance(node,
							link.getNodes()[0]);
					double d2 = DistanceCalculator.CalculationByDistance(node,
							link.getNodes()[1]);
					double d = d1 < d2 ? d1 : d2;
					// select the closest
					if (d < max && dir == direction) {
						closestSensor = sensorId;
						count++;
					}
				}
				edgeMap.get(link.getLinkId()).setClosestSensor(closestSensor);
				rs.close();
				f.close();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("matching sensors to edges finish!");
		System.out.println("total" + num + "links, " + count
				+ " links have sensors.");
	}

	private static void readFileInMemory() {

		try {
			System.out.println("reading edges file...");
			FileInputStream fstream = new FileInputStream(fileRoot + "/"
					+ fileLink);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String LinkId = nodes[0];
				int FuncClass = Integer.parseInt(nodes[1]);

				if (FuncClass == 3 || FuncClass == 4 || FuncClass == 5) {
					String st_name = nodes[2];
					String st_node = nodes[3];
					String end_node = nodes[4];
					String index = st_node.substring(1) + ""
							+ end_node.substring(1);
					String LinkIdIndex = LinkId + "" + index;

					int i = 5, count = 0;
					PairInfo[] pairs = new PairInfo[10];
					while (i < nodes.length) {
						double lati = Double.parseDouble(nodes[i]);
						double longi = Double.parseDouble(nodes[i + 1]);
						pairs[count] = new PairInfo(lati, longi);
						count++;
						i = i + 2;
					}
					if (edgeMap.get(LinkIdIndex) != null)
						System.out.println(edgeMap.get(LinkIdIndex)
								+ "Duplicate LinkIds");

					edgeMap.put(LinkIdIndex,
							new LinkInfo(LinkIdIndex, FuncClass, st_name,
									st_node, end_node, pairs, count));

				}
			}
			in.close();
			System.out.println("reading edges file finish!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void generateEdgesKML() {
		System.out.println("generate edges kml...");
		try {
			FileWriter fstream = new FileWriter("Edges_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			Set<String> keys = edgeMap.keySet();
			Iterator<String> iter = keys.iterator();
			out.write("<kml><Document>");
			while (iter.hasNext()) {
				LinkInfo link = edgeMap.get(iter.next());
				PairInfo[] nodes = link.getNodes();
				String kmlStr = "<Placemark><name>Link:"
						+ link.getLinkId().substring(0, 8) + "</name>";
				kmlStr += "<description>Id:" + link.getLinkId();
				kmlStr += ", Direction:" + link.getDirection();
				kmlStr += ", DirTravel:" + link.getDirTravel();
				kmlStr += ", StreetName:" + link.getSt_name().replace('&', '+');
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				kmlStr += nodes[0].getLongi() + "," + nodes[0].getLati()
						+ ",0 ";
				kmlStr += nodes[1].getLongi() + "," + nodes[1].getLati()
						+ ",0 ";
				kmlStr += "</coordinates></LineString></Placemark>\n";
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate edges kml finish!");
	}

	/*
	 * --------------------------------------------------------------------------
	 * ----------------------------------------------------
	 */

	private static void testDirection() {
		try {
			FileWriter fstream = new FileWriter("test.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "select * from streets_dca1_new where link_id = '24589498'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int linkId = res.getInt(1);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT) res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for (int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = geomPoints[i * 2 + 1];
					double longitude = geomPoints[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}
				int speedCat = res.getInt(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);

				LinkInfo link = new LinkInfo("test", linkId, func_class,
						stName, refNode, nrefNode, nodeList, dirTravel,
						speedCat, 1);
				int num = nodeList.size();
				int dir = DistanceCalculator.getDirection(nodeList.get(0),
						nodeList.get(num - 1));

				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>Sensor:NULL";
				kmlStr += ", Direction:" + dir;
				kmlStr += ", DirTravel:" + link.getDirTravel();
				kmlStr += ", StreetName:" + link.getSt_name();
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
					if (j == 0)
						System.out.println("ref_node: (" + node.getLongi()
								+ "," + node.getLati() + ")");
					if (j == nodeList.size() - 1)
						System.out.println("nref_node: (" + node.getLongi()
								+ "," + node.getLati() + ")");
				}
				kmlStr += "</coordinates></LineString></Placemark>\n";
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
			con.close();
			res.close();
			pstatement.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static void generateNullSensorKML() {
		System.out.println("generate null sensor KML...");
		try {
			FileWriter fstream = new FileWriter("Null_Sensor_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction, affected_numberof_lanes from arterial_congestion_config where onstreet is null";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				int affected = res.getInt(6);
				out.write("<Placemark><name>" + sensorId
						+ "</name><description>Onstreet:" + onStreet
						+ ", Fromstreet: " + fromStreet + ", Direction:"
						+ direction + ", Affected:" + affected
						+ "</description><Point><coordinates>"
						+ node.getLongi() + "," + node.getLati()
						+ ",0</coordinates></Point></Placemark>");
			}
			con.close();
			res.close();
			pstatement.close();
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("generate null sensor KML finish");
	}

	private static void generateAllSensorKML() {
		System.out.println("generate all sensor KML...");
		try {
			FileWriter fstream = new FileWriter("All_Sensor_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction, affected_numberof_lanes from arterial_congestion_config";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				int affected = res.getInt(6);
				out.write("<Placemark><name>" + sensorId
						+ "</name><description>Onstreet:" + onStreet
						+ ", Fromstreet: " + fromStreet + ", Direction:"
						+ direction + ", Affected:" + affected
						+ "</description><Point><coordinates>"
						+ node.getLongi() + "," + node.getLati()
						+ ",0</coordinates></Point></Placemark>");
			}
			con.close();
			res.close();
			pstatement.close();
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate all sensor KML finish!");
	}

	private static void generateEdgeKML() {
		System.out.println("generate edge kml...");
		try {
			FileWriter fstream = new FileWriter("Edges_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < edgeList.size(); i++) {
				LinkInfo link = edgeList.get(i);
				String id = link.getId();
				int intId = link.getIntLinkId();
				SensorInfo sensor = link.getSensor();
				String sensorStr = "NULL";
				ArrayList<PairInfo> nodeList = link.getNodeList();
				if (sensor != null) {
					sensorStr = String.valueOf(sensor.getSensorId());
				}

				String kmlStr = "<Placemark><name>Link:" + intId + "</name>";
				kmlStr += "<description>Id:" + id;
				kmlStr += ", Sensor:" + sensorStr;
				kmlStr += ", Direction:" + link.getDirection();
				kmlStr += ", DirTravel:" + link.getDirTravel();
				kmlStr += ", StreetName:" + link.getSt_name();
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
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
		System.out.println("generate edge kml finish!");
	}

	private static void generateSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter("Sensors_List.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < sensorList.size(); i++) {
				SensorInfo sensor = sensorList.get(i);
				int id = sensor.getSensorId();
				double lati = sensor.getNode().getLati();
				double longi = sensor.getNode().getLongi();
				out.write("<Placemark><name>" + id
						+ "</name><description>Onstreet:"
						+ sensor.getOnStreet() + ", Fromstreet: "
						+ sensor.getFromStreet() + ", Direction:"
						+ sensor.getDirection()
						+ "</description><Point><coordinates>" + longi + ","
						+ lati + ",0</coordinates></Point></Placemark>");
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate sensor kml finish!");
	}

	private static void generatePattern() {
		System.out.println("generate pattern...");
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("Pattern_" + street + "_" + days[0]
					+ ".txt");
			out = new BufferedWriter(fstream);

			for (int i = 0; i < edgeList.size(); i++) {
				LinkInfo link = edgeList.get(i);
				// System.out.print("processing link " + link.getIntLinkId());
				// System.out.print("\r");
				if (link.getSensor() != null) {
					Connection con = null;
					String sql = null;
					PreparedStatement pstatement = null;
					ResultSet res = null;
					con = getConnection();

					sql = "select * from arterial_averages3 where link_id = '"
							+ link.getSensor().getSensorId() + "'"
							+ " and day = '" + days[0] + "'";
					pstatement = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					res = pstatement.executeQuery();
					out.write("Link ID:" + link.getIntLinkId() + " Sensor ID:"
							+ link.getSensor().getSensorId() + "\n");
					while (res.next()) {
						double speed = res.getDouble(2);
						String time = res.getString(5);
						out.write(time + ": " + speed + ", ");
					}
					out.write("\n");

					res.close();
					pstatement.close();
					con.close();
				}
			}

			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("generate pattern finish!");
	}

	private static void matchEdgeSensor() {
		System.out.println("match sensors to edges...");
		// first round
		for (int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			// System.out.println("processing link id: " + link.getIntLinkId());
			ArrayList<PairInfo> nodeList = link.getNodeList();
			boolean findSensor = false;
			if (nodeList.size() > 2) {
				// examine the intermediate node first
				// System.out.println("has more than 2 nodes");
				for (double step = searchDistance / devide; step < searchDistance; step += step) {
					// skip first one and last one
					for (int j = 1; j < nodeList.size() - 1; j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for (int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator
									.CalculationByDistance(node1, node2);
							if (distance < step
									&& sensor.getDirection() == link
											.getDirection()) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
								// System.out.println("find sensor " +
								// sensor.getSensorId() + " for link " +
								// link.getLinkId());
								break;
							}
						}
						if (findSensor)
							break;
					}
					if (findSensor)
						break;
				}
			}

			if (!findSensor) {
				// examine the vertex
				// System.out.println("has 2 nodes");
				for (double step = searchDistance / devide; step < searchDistance; step += step) {
					for (int j = 0; j < nodeList.size(); j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for (int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator
									.CalculationByDistance(node1, node2);
							if (distance < step
									&& sensor.getDirection() == link
											.getDirection()) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
								// System.out.println("find sensor " +
								// sensor.getSensorId() + " for link " +
								// link.getLinkId());
								break;
							}
						}
						if (findSensor)
							break;
					}
					if (findSensor)
						break;
				}
			}

			// if(!findSensor) {
			// System.out.println("cannot find sensor for link " +
			// link.getIntLinkId());
			// }
		}
		int count = 0;
		for (int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			if (link.getSensor() != null) {
				count++;
			}
		}
		System.out.println("there are " + count
				+ " link has sensor in first round");

		// second round
		int lastCount = count;
		while (count != edgeList.size()) {
			for (int i = 0; i < edgeList.size(); i++) {
				LinkInfo link = edgeList.get(i);
				if (link.getSensor() != null) {
					// left
					int n;
					for (n = 1; n < 3; n++) {
						if (i - n >= 0) {
							LinkInfo linkL = edgeList.get(i - n);
							if (linkL.getSensor() == null) {
								int a = Math.abs(link.getStartNode()
										- linkL.getStartNode());
								int b = Math.abs(link.getStartNode()
										- linkL.getEndNode());
								int c = Math.abs(link.getEndNode()
										- linkL.getStartNode());
								int d = Math.abs(link.getEndNode()
										- linkL.getEndNode());

								int min = a < b ? a : b;
								min = min < c ? min : c;
								min = min < d ? min : d;

								if (min <= 100
										&& link.getDirection() == linkL
												.getDirection()) {
									linkL.setSensor(link.getSensor());
									count++;
								}
							}
						}
					}
					// right
					for (n = 1; n < 3; n++) {
						if (i + n < edgeList.size()) {
							LinkInfo linkR = edgeList.get(i + n);
							if (linkR.getSensor() == null) {
								int a = Math.abs(link.getStartNode()
										- linkR.getStartNode());
								int b = Math.abs(link.getStartNode()
										- linkR.getEndNode());
								int c = Math.abs(link.getEndNode()
										- linkR.getStartNode());
								int d = Math.abs(link.getEndNode()
										- linkR.getEndNode());

								int min = a < b ? a : b;
								min = min < c ? min : c;
								min = min < d ? min : d;

								if (min <= 100
										&& link.getDirection() == linkR
												.getDirection()) {
									linkR.setSensor(link.getSensor());
									count++;
								}
							}
						}
					}
				}
			}
			if (count == lastCount)
				break;
			lastCount = count;
		}
		System.out.println("there are " + count
				+ " link has sensor in second round, there has "
				+ edgeList.size() + " link");
		System.out.println("match Sensors finish!");
	}

	private static void printEdgeWithSensor() {
		int count = 0;
		for (int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
			String sensor = "";
			if (link.getSensor() != null) {
				count++;
				sensor = Integer.toString(link.getSensor().getSensorId());
			} else {
				sensor = "NULL";
			}
			// System.out.println("LinkInfo [LinkId=" + link.getIntLinkId()
			// + ", Func Class=" + link.getFunc_class()
			// + ", Start Node=" + link.getStartNode()
			// + ", End Node=" + link.getEndNode()
			// + ", ST Name=" + link.getSt_name()
			// + ", Sensor=" + sensor
			// + ", Nodes=" + link.getNodeList().toString() + "]");
		}
		int countN = edgeList.size() - count;
		System.out.println("there are " + count + " link has sensor, " + countN
				+ " link has no sensor.");
	}

	private static void addSortEdge(LinkInfo link) {
		String id = link.getId();
		int refNode = link.getStartNode();
		int nrefNode = link.getEndNode();
		if (!checkEdge.contains(id)) {
			checkEdge.add(id);
			// sort link in edgeList by in_id
			int inId = refNode < nrefNode ? refNode : nrefNode;
			if (edgeList.isEmpty()) {
				edgeList.add(link);
			} else {
				boolean isAdd = false;
				for (int i = 0; i < edgeList.size(); i++) {
					int cstartNode = edgeList.get(i).getStartNode();
					int cendNode = edgeList.get(i).getEndNode();
					int cinId = cstartNode < cendNode ? cstartNode : cendNode;
					if (inId < cinId) {
						edgeList.add(i, link);
						isAdd = true;
						break;
					}
				}
				if (!isAdd)
					edgeList.add(link);
			}
		}
	}

	private static void fetchEdge() {
		try {
			System.out.println("fetch edges...");
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();

			sql = "select * from streets_dca1_new where upper(st_name) like '%"
					+ street + "%'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int linkId = res.getInt(1);
				// System.out.println("fetching link " + linkId);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT) res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for (int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = geomPoints[i * 2 + 1];
					double longitude = geomPoints[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}

				int speedCat = res.getInt(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);

				int num = nodeList.size();
				String id = String.valueOf(linkId);
				if (dirTravel.equals("B")) {
					String _id = id + refNode + nrefNode;
					int dir = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					LinkInfo link = new LinkInfo(_id, linkId, func_class,
							stName, refNode, nrefNode, nodeList, dirTravel,
							speedCat, dir);
					addSortEdge(link);

					_id = id + nrefNode + refNode;
					dir = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					link = new LinkInfo(_id, linkId, func_class, stName,
							refNode, nrefNode, nodeList, dirTravel, speedCat,
							dir);
					addSortEdge(link);
				} else if (dirTravel.equals("T")) {
					String _id = id + nrefNode + refNode;
					int dir = DistanceCalculator.getDirection(
							nodeList.get(num - 1), nodeList.get(0));
					LinkInfo link = new LinkInfo(_id, linkId, func_class,
							stName, refNode, nrefNode, nodeList, dirTravel,
							speedCat, dir);
					addSortEdge(link);
				} else {
					String _id = id + refNode + nrefNode;
					int dir = DistanceCalculator.getDirection(nodeList.get(0),
							nodeList.get(num - 1));
					LinkInfo link = new LinkInfo(_id, linkId, func_class,
							stName, refNode, nrefNode, nodeList, dirTravel,
							speedCat, dir);
					addSortEdge(link);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch edge finish!");
	}

	private static void fetchSensor() {
		try {
			System.out.println("fetch Sensor...");

			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			// assume this is sensing the specified street
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from arterial_congestion_config "
					+ "where upper(onstreet) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				// System.out.println("fetching sensor " + sensorId);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				if (!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet,
							fromStreet, node, direction);
					checkSensor.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}

			// this maybe sensing the specified street
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from arterial_congestion_config "
					+ "where upper(fromstreet) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				if (!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet,
							fromStreet, node, direction);
					checkSensor.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch sensor finish!");
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

	/*
	 * --------------------------------------------------------------------------
	 * ----------------------------------------------------
	 */

	private static void printEdge() {
		for (int i = 0; i < edgeList.size(); i++) {
			System.out.println(edgeList.get(i).getIntLinkId() + ": "
					+ edgeList.get(i).getStartNode() + ", "
					+ edgeList.get(i).getEndNode());
		}
	}

	private static void createPattern() {
		FileWriter fstream = null;
		BufferedWriter out = null;
		System.out.println("create pattern...");
		try {
			fstream = new FileWriter("Pattern_" + street + ".txt");
			out = new BufferedWriter(fstream);
			int sum = searchList.size();
			for (int i = 0; i < sum; i++) {
				LinkInfo link = searchList.get(i);
				String patternId = link.getStart_node() + link.getEnd_node();
				PatternPairInfo patternInfo = patternMap.get(patternId);
				String link_id = link.getStrLinkId();
				String st_node = link.getStart_node();
				String ed_node = link.getEnd_node();
				// System.out.println("Link_ID:" + link_id + " St_Node:" +
				// st_node
				// + " Ed_Node:" + ed_node);
				out.write("Link_ID:" + link_id + " St_Node:" + st_node
						+ " Ed_Node:" + ed_node + "\n");
				double distance = DistanceCalculator.CalculationByDistance(
						link.getNodes()[0], link.getNodes()[1]);
				int[] intervals = patternInfo.getIntervals();
				for (int j = 0; j < 60 && j < intervals.length; j++) {
					double speed = (double) Math.round(distance / intervals[j]
							* 60 * 60 * 1000 * 100) / 100;
					// System.out.print(UtilClass.getStartTime(j) + "-"
					// + UtilClass.getEndTime(j) + ":" + speed + ", ");
					out.write(UtilClass.getStartTime(j) + "-"
							+ UtilClass.getEndTime(j) + ":" + speed + ", ");
				}
				// System.out.println();
				out.write("\n");
			}

			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("create pattern finish!");
	}

	private static void searchStreet() {
		System.out.println("search " + street + " start");
		int sum = linkList.size();
		for (int i = 0; i < sum; i++) {
			LinkInfo link = linkList.get(i);
			if (link.getSt_name().contains(street)) {
				// if(link.getLinkId().contains("24009477"))
				// System.out.println(link.toString());
				searchList.add(link);
			}
			if (i % 10000 == 0)
				System.out.println((double) i / sum * 100 + "%");
		}
		System.out.println("search " + street + " finished! find "
				+ searchList.size() + " links");
	}

	private static void readFileToMemory() {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;

		try {
			System.out.println("read from AdjList_Monday.txt start");
			fstream = new FileInputStream(root + "/AdjList_Monday.txt");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			int lineNum = 0;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (strLine.equals("NA")) {
					lineNum++;
					continue;
				}
				String[] nodes = strLine.split(";");
				for (int i = 0; i < nodes.length; i++) {
					// info[0]: n1(V) info[1]: 7931,7931,7931,7931......
					String[] info = nodes[i].split(":");
					// info2[0]: n1 info2[1]: V)
					String[] info2 = info[0].split("\\(");
					int node1 = lineNum;
					int node2 = Integer.parseInt(info2[0].substring(1));
					int[] intervals = new int[60];
					// String flag = info2[1].substring(0, 1);
					// if(flag == "V") { } else { }
					String[] intervalS = info[1].split(",");
					int[] interval = new int[60];
					int intervalNum = 0;
					while (intervalNum < 60 && intervalNum < intervalS.length) {
						interval[intervalNum] = Integer
								.parseInt(intervalS[intervalNum]);
						intervalNum++;
					}
					PatternPairInfo pattern = new PatternPairInfo(node1, node2,
							interval);
					String patternId = "n" + node1 + "n" + node2;
					patternMap.put(patternId, pattern);
				}
				lineNum++;
				if (lineNum % 10000 == 0)
					System.out.print("read line " + lineNum + "\r");
			}
			br.close();
			in.close();
			fstream.close();
			System.out.println("read " + lineNum
					+ " lines from AdjList_Monday.txt finished!");

			System.out.println("read from Edges.csv start");
			fstream = new FileInputStream(root + "/Edges.csv");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			lineNum = 0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String link_id = nodes[0];
				int func_class = Integer.parseInt(nodes[1]);
				String st_name = nodes[2];
				String st_node = nodes[3];
				String ed_node = nodes[4];
				String linkIndexId = link_id + st_node.substring(1)
						+ ed_node.substring(1);
				PairInfo[] pairs = new PairInfo[2];
				pairs[0] = new PairInfo(Double.parseDouble(nodes[5]),
						Double.parseDouble(nodes[6]));
				pairs[1] = new PairInfo(Double.parseDouble(nodes[7]),
						Double.parseDouble(nodes[8]));
				LinkInfo link = new LinkInfo(linkIndexId, link_id, func_class,
						st_name, st_node, ed_node, pairs, 2);
				linkList.add(link);

				lineNum++;
				if (lineNum % 10000 == 0)
					System.out.print("read line " + lineNum + "\r");
			}
			br.close();
			in.close();
			fstream.close();
			System.out.println("read " + lineNum
					+ " lines from Edges.csv finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
