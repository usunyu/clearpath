package RoadPattern;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.lang.model.element.Element;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class RoadPatternGeneration {

	/**
	 * @param args
	 */
	// street
	static String street = "FIGUEROA";
	// parameter
	static double searchDistance = 0.15;
	static int devide = 1000;
	private static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	// database
	static String root = "/Users/Sun/Documents/workspace/CleanPath/GeneratedFile";
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<LinkInfo> edgeList = new ArrayList<LinkInfo>();
	static HashSet<String> checkEdge = new HashSet<String>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static HashMap<String, PatternPairInfo> patternMap = new HashMap<String, PatternPairInfo>();
	static ArrayList<LinkInfo> linkList = new ArrayList<LinkInfo>();
	static ArrayList<LinkInfo> searchList = new ArrayList<LinkInfo>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		fetchSensor();
		
//		generateSensorKML();
		
		fetchEdge();

//		matchEdgeSensor();
		
//		generateEdgeKML();
		
//		generatePattern();
		
//		generateAllSensorKML();
		
//		generateNullSensorKML();
		
//		testDirection();
		
		// printEdgeWithSensor();
		
		// readFileToMemory();
		// searchStreet();
		// createPattern();
	}
	
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
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int linkId = res.getInt(1);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT)res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for(int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = geomPoints[i * 2 + 1];
					double longitude = geomPoints[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}
				int speedCat = res.getInt(6);
				int refNode = res.getInt(7);
				int nrefNode = res.getInt(8);
				
				LinkInfo link = new LinkInfo("test", linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, 1);
				int num = nodeList.size();
				int dir = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>Sensor:NULL";
				kmlStr += ", Direction:" + dir;
				kmlStr += ", DirTravel:" + link.getDirTravel();
				kmlStr += ", StreetName:" + link.getSt_name();
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
					if(j == 0)
						System.out.println("ref_node: (" + node.getLongi() + "," + node.getLati() + ")");
					if(j == nodeList.size() - 1)
						System.out.println("nref_node: (" + node.getLongi() + "," + node.getLati() + ")");
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
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				int affected = res.getInt(6);
				out.write("<Placemark><name>" + sensorId + "</name><description>Onstreet:" + onStreet
						+ ", Fromstreet: " + fromStreet + ", Direction:" + direction + ", Affected:" + affected
						+ "</description><Point><coordinates>" + node.getLongi()
						+ "," + node.getLati()
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
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				int affected = res.getInt(6);
				out.write("<Placemark><name>" + sensorId + "</name><description>Onstreet:" + onStreet
						+ ", Fromstreet: " + fromStreet + ", Direction:" + direction + ", Affected:" + affected
						+ "</description><Point><coordinates>" + node.getLongi()
						+ "," + node.getLati()
						+ ",0</coordinates></Point></Placemark>");
			}
			con.close();
			res.close();
			pstatement.close();
			out.write("</Document></kml>");
			out.close();
			fstream.close();
		}
		catch (Exception e) {
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
			for(int i = 0; i < edgeList.size(); i++) {
				LinkInfo link = edgeList.get(i);
				int id = link.getIntLinkId();
				SensorInfo sensor = link.getSensor();
				String sensorStr = "NULL";
				ArrayList<PairInfo> nodeList = link.getNodeList();
				if(sensor != null) {
					sensorStr = String.valueOf(sensor.getSensorId());
				}
				String direction = null;
				int num = nodeList.size();
				if(link.getDirTravel().equals("B")) {
					int dir = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
					direction = String.valueOf(dir);
					dir = DistanceCalculator.getDirection(nodeList.get(num - 1), nodeList.get(0));
					direction = direction + "," +String.valueOf(dir);
				}
				else if(link.getDirTravel().equals("T")) {
					int dir = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
					direction = String.valueOf(dir);
				}
				else {
					int dir = DistanceCalculator.getDirection(nodeList.get(num - 1), nodeList.get(0));
					direction = String.valueOf(dir);
				}
				
				String kmlStr = "<Placemark><name>Link:" + id + "</name>";
				kmlStr += "<description>Sensor:" + sensorStr;
				kmlStr += ", Direction:" + direction;
				kmlStr += ", DirTravel:" + link.getDirTravel();
				kmlStr += ", StreetName:" + link.getSt_name();
				kmlStr += ", FuncClass:" + link.getFunc_class();
				kmlStr += ", SpeedCat:" + link.getSpeedCat() + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for(int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
				}
				kmlStr += "</coordinates></LineString></Placemark>\n";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		}
		catch (Exception e) {
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
			for(int i = 0; i < sensorList.size(); i++) {
				SensorInfo sensor = sensorList.get(i);
				int id = sensor.getSensorId();
				double lati = sensor.getNode().getLati();
				double longi = sensor.getNode().getLongi();
				out.write("<Placemark><name>Sensor:" + id
						+ " </name><Point><coordinates>" + String.valueOf(longi)
						+ "," + String.valueOf(lati)
						+ ",0</coordinates></Point></Placemark>");
			}
			out.write("</Document></kml>");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate sensor kml finish!");
	}
	
	private static void generatePattern() {
		System.out.println("generate pattern...");
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("Pattern_" + street + "_" + days[0] + ".txt");
			out = new BufferedWriter(fstream);
			
			for(int i = 0; i < edgeList.size(); i++) {
				LinkInfo link = edgeList.get(i);
//				System.out.print("processing link " + link.getIntLinkId());
//				System.out.print("\r");
				if(link.getSensor() != null) {
					Connection con = null;
					String sql = null;
					PreparedStatement pstatement = null;
					ResultSet res = null;
					con = getConnection();
					
					sql = "select * from arterial_averages3 where link_id = '" + link.getSensor().getSensorId()
							+ "'" + " and day = '" + days[0] + "'";
					pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					res = pstatement.executeQuery();
					out.write("Link ID:" + link.getIntLinkId() + " Sensor ID:" + link.getSensor().getSensorId() + "\n");
					while(res.next()) {
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("generate pattern finish!");
	}
	
	private static void matchEdgeSensor() {
		System.out.println("match sensors to edges...");
		// first round
		for(int i = 0; i < edgeList.size(); i++) {
			LinkInfo link = edgeList.get(i);
//			System.out.println("processing link id: " + link.getIntLinkId());
			ArrayList<PairInfo> nodeList = link.getNodeList();
			boolean findSensor = false;
			if(nodeList.size() > 2) {
				// examine the intermediate node first
//				System.out.println("has more than 2 nodes");
				for(double step = searchDistance / devide; step < searchDistance; step += step) {
					// skip first one and last one
					for(int j = 1; j < nodeList.size() - 1; j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for(int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator.CalculationByDistance(node1, node2);
							if(distance < step) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
//								System.out.println("find sensor " + sensor.getSensorId() + " for link " + link.getLinkId());
								break;
							}
						}
						if(findSensor)
							break;
					}
					if(findSensor)
						break;
				}
			}
			
			if(!findSensor) {
				// examine the vertex
//				System.out.println("has 2 nodes");
				for(double step = searchDistance / devide; step < searchDistance; step += step) {
					for(int j = 0; j < nodeList.size(); j++) {
						PairInfo node1 = nodeList.get(j);
						// examine all the sensor extracted
						for(int k = 0; k < sensorList.size(); k++) {
							SensorInfo sensor = sensorList.get(k);
							PairInfo node2 = sensor.getNode();
							double distance = DistanceCalculator.CalculationByDistance(node1, node2);
							if(distance < step) {
								// find the sensor
								link.setSensor(sensor);
								findSensor = true;
//								System.out.println("find sensor " + sensor.getSensorId() + " for link " + link.getLinkId());
								break;
							}
						}
						if(findSensor)
							break;
					}
					if(findSensor)
						break;
				}
			}
			
			if(!findSensor) {
//				System.out.println("cannot find sensor for link " + link.getIntLinkId());
			}
		}
		int count = 0;
		for(int i = 0; i < edgeList.size(); i ++) {
			LinkInfo link = edgeList.get(i);
			if(link.getSensor() != null) {
				count++;
			}
		}
		System.out.println("there are " + count + " link has sensor in first round");
		
		// second round
		int lastCount = count;
		while(count != edgeList.size()) {
			for(int i = 0; i < edgeList.size(); i++) {
				LinkInfo link = edgeList.get(i);
				if(link.getSensor() != null) {
					// left
					if(i - 1 >= 0) {
						LinkInfo linkL = edgeList.get(i - 1);
						if(linkL.getSensor() == null) {
							int a = Math.abs(link.getStartNode() - linkL.getStartNode());
							int b = Math.abs(link.getStartNode() - linkL.getEndNode());
							int c = Math.abs(link.getEndNode() - linkL.getStartNode());
							int d = Math.abs(link.getEndNode() - linkL.getEndNode());
							
							int min = a < b ? a : b;
							min = min < c ? min : c;
							min = min < d ? min : d;
							
							if(min <= 100) {
								linkL.setSensor(link.getSensor());
								count++;
							}
						}
					}
					// right
					if(i + 1 < edgeList.size()) {
						LinkInfo linkR = edgeList.get(i + 1);
						if(linkR.getSensor() == null) {
							int a = Math.abs(link.getStartNode() - linkR.getStartNode());
							int b = Math.abs(link.getStartNode() - linkR.getEndNode());
							int c = Math.abs(link.getEndNode() - linkR.getStartNode());
							int d = Math.abs(link.getEndNode() - linkR.getEndNode());
							
							int min = a < b ? a : b;
							min = min < c ? min : c;
							min = min < d ? min : d;
							
							if(min <= 100) {
								linkR.setSensor(link.getSensor());
								count++;
							}
						}
					}
				}
			}
			if(count == lastCount)
				break;
			lastCount = count;	
		}
		System.out.println("there are " + count + " link has sensor in second round, there has " + edgeList.size() +" link");
		System.out.println("match Sensors finish!");
	}
	
	private static void printEdgeWithSensor() {
		int count = 0;
		for(int i = 0; i < edgeList.size(); i ++) {
			LinkInfo link = edgeList.get(i);
			String sensor = "";
			if(link.getSensor() != null) {
				count++;
				sensor = Integer.toString(link.getSensor().getSensorId());
			}
			else {
				sensor = "NULL";
			}
//			System.out.println("LinkInfo [LinkId=" + link.getIntLinkId()
//					+ ", Func Class=" + link.getFunc_class()
//					+ ", Start Node=" + link.getStartNode()
//					+ ", End Node=" + link.getEndNode()
//					+ ", ST Name=" + link.getSt_name()
//					+ ", Sensor=" + sensor
//					+ ", Nodes=" + link.getNodeList().toString() + "]");
		}
		int countN = edgeList.size() - count;
		System.out.println("there are " + count + " link has sensor, " + countN + " link has no sensor.");
	}
	
	private static void addSortEdge(LinkInfo link) {
		String id = link.getId();
		int refNode = link.getStartNode();
		int nrefNode = link.getEndNode();
		if(!checkEdge.contains(id)) {
			checkEdge.add(id);
			// sort link in edgeList by in_id
			int inId = refNode < nrefNode ? refNode : nrefNode;
			if(edgeList.isEmpty()) {
				edgeList.add(link);
			}
			else {
				boolean isAdd = false;
				for(int i = 0; i < edgeList.size(); i++) {
					int cstartNode = edgeList.get(i).getStartNode();
					int cendNode = edgeList.get(i).getEndNode();
					int cinId = cstartNode < cendNode ? cstartNode : cendNode;
					if(inId < cinId) {
						edgeList.add(i, link);
						isAdd = true;
						break;
					}
				}
				if(!isAdd)
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
			
			sql = "select * from streets_dca1_new where upper(st_name) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int linkId = res.getInt(1);
//				System.out.println("fetching link " + linkId);
				String dirTravel = res.getString(2);
				String stName = res.getString(3);
				int func_class = res.getInt(4);
				// handle for geom point
				STRUCT st = (STRUCT)res.getObject(5);
				JGeometry geom = JGeometry.load(st);
				double[] geomPoints = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for(int i = 0; i < geom.getNumPoints(); i++) {
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
				if(dirTravel.equals("B")) {
					String _id = id + refNode + nrefNode;
					int dir = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
					LinkInfo link = new LinkInfo(_id, linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, dir);
					addSortEdge(link);
					
					_id = id + nrefNode + refNode;
					dir = DistanceCalculator.getDirection(nodeList.get(num - 1), nodeList.get(0));
					link = new LinkInfo(_id, linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, dir);
					addSortEdge(link);
				}
				else if(dirTravel.equals("T")) {
					String _id = id + nrefNode + refNode;
					int dir = DistanceCalculator.getDirection(nodeList.get(num - 1), nodeList.get(0));
					LinkInfo link = new LinkInfo(_id, linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, dir);
					addSortEdge(link);
				}
				else {
					String _id = id + refNode + nrefNode;
					int dir = DistanceCalculator.getDirection(nodeList.get(0), nodeList.get(num - 1));
					LinkInfo link = new LinkInfo(_id, linkId, func_class, stName, refNode, nrefNode, nodeList, dirTravel, speedCat, dir);
					addSortEdge(link);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		}
		catch(Exception e) {
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
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from arterial_congestion_config " + 
			"where upper(onstreet) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
//				System.out.println("fetching sensor " + sensorId);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				if(!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, node, direction);
					checkSensor.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}
			
			// this maybe sensing the specified street
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction from arterial_congestion_config " +
			"where upper(fromstreet) like '%" + street + "%'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				if(!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, node, direction);
					checkSensor.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		}
		catch (Exception e) {
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

	private static void printEdge() {
		for(int i = 0; i < edgeList.size(); i++) {
			System.out.println(edgeList.get(i).getIntLinkId() + ": " +
					edgeList.get(i).getStartNode() + ", " + edgeList.get(i).getEndNode());
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
//				System.out.println("Link_ID:" + link_id + " St_Node:" + st_node
//						+ " Ed_Node:" + ed_node);
				out.write("Link_ID:" + link_id + " St_Node:" + st_node
						+ " Ed_Node:" + ed_node + "\n");
				double distance = DistanceCalculator.CalculationByDistance(
						link.getNodes()[0], link.getNodes()[1]);
				int[] intervals = patternInfo.getIntervals();
				for (int j = 0; j < 60 && j < intervals.length; j++) {
					double speed = (double) Math.round(distance / intervals[j]
							* 60 * 60 * 1000 * 100) / 100;
//					System.out.print(UtilClass.getStartTime(j) + "-"
//							+ UtilClass.getEndTime(j) + ":" + speed + ", ");
					out.write(UtilClass.getStartTime(j) + "-"
							+ UtilClass.getEndTime(j) + ":" + speed + ", ");
				}
//				System.out.println();
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
