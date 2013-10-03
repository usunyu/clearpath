package output;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import objects.*;

public class RDFOutputKMLGeneration {

	/**
	 * @param file
	 */
	static String root				= "file";
	static String linkFile			= "RDF_Link.txt";
	static String nodeFile			= "RDF_Node.txt";
	static String kmlLinkFile		= "RDF_Link.kml";
	static String kmlNodeFile		= "RDF_Node.kml";
	static String matchSensorFile	= "RDF_Sensor_Match.txt";
	static String matchSensorKML	= "RDF_Sensor_Match.kml";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
	static String UNKNOWN 			= "Unknown Street";
	static String YES				= "Y";
	static String NO				= "N";
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static LinkedList<RDFNodeInfo> nodeList = new LinkedList<RDFNodeInfo>();
	/**
	 * @param sensor
	 */
	static LinkedList<SensorInfo> sensorList = new LinkedList<SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	
	public static void main(String[] args) {
		
		readNodeFile();
		generateNodeKML();
		
		readLinkFile();
		
		fetchSensor();
		readMatchSensor();
		generateSensorKML();
		
		generateLinkKML();
	}
	
	private static void generateSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + matchSensorKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			
			ListIterator<SensorInfo> iterator = sensorList.listIterator();
			while(iterator.hasNext()) {
				SensorInfo sensor = iterator.next();
				int sensorId = sensor.getSensorId();
				String onStreet = sensor.getOnStreet();
				String fromStreet = sensor.getFromStreet();
				int direction = sensor.getDirection();
				
				LocationInfo location = sensor.getLocation();
				double lati = location.getLatitude();
				double longi = location.getLongitude();
				int zLevel = location.getZLevel();
				
				String kmlStr = "<Placemark><name>" + sensorId + "</name>";
				kmlStr += "<description>";
				kmlStr += "On: " + onStreet + "\r\n";
				kmlStr += "From: " + fromStreet + "\r\n";
				kmlStr += "Dir: " + direction + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<Point><coordinates>";
				kmlStr +=  longi + "," + lati +  "," + zLevel;
				kmlStr += "</coordinates></Point></Placemark>";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate sensor kml finish!");
	}
	
	private static void readMatchSensor() {
		System.out.println("read match sensor...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + matchSensorFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|");
				
				long 	linkId 		= Long.parseLong(nodes[0]);
				String[] sensorStr	= nodes[1].split(",");
				
				RDFLinkInfo RDFLink = linkMap.get(linkId);
				for(int i = 0; i < sensorStr.length; i++) {
					int sensorId = Integer.parseInt(sensorStr[i]);
					SensorInfo sensor 	= sensorMap.get(sensorId);
					RDFLink.addSensor(sensor);
					
					if(!sensorList.contains(sensor))
						sensorList.add(sensor);
				}

				if (debug % 1000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readMatchSensor: debug code: " + debug);
		}
		System.out.println("read match sensor finish!");
	}
	
	private static void fetchSensor() {
		System.out.println("fetch sensor...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "SELECT link_id, onstreet, fromstreet, start_lat_long, direction FROM highway_congestion_config ";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				LocationInfo location = new LocationInfo(geom.getPoint()[1], geom.getPoint()[0], 0);
				int direction = res.getInt(5);
				
				if(!sensorMap.containsKey(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, location, direction);
					sensorMap.put(sensorId, sensorInfo);
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
	
	private static void generateNodeKML() {
		System.out.println("generate node kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlNodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			
			ListIterator<RDFNodeInfo> iterator = nodeList.listIterator();
			while(iterator.hasNext()) {
				RDFNodeInfo node = iterator.next();
				long nodeId = node.getNodeId();
				LocationInfo location = node.getLocation();
				double lati = location.getLatitude();
				double longi = location.getLongitude();
				int zLevel = location.getZLevel();
				
				String kmlStr = "<Placemark><name>" + nodeId + "</name>";
				kmlStr += "<description>";
				kmlStr += "ZLevel: " + zLevel;
				kmlStr += "</description>";
				kmlStr += "<Point><coordinates>";
				kmlStr +=  longi + "," + lati +  "," + zLevel;
				kmlStr += "</coordinates></Point></Placemark>";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate node kml finish!");
	}
	
	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlLinkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo link 	= iterator.next();
				long linkId 		= link.getLinkId();
				String baseName 	= link.getBaseName();
				long refNodeId 		= link.getRefNodeId();
				long nonRefNodeId 	= link.getNonRefNodeId();
				int functionalClass = link.getFunctionalClass();
				String travelDirection 	= link.getTravelDirection();
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpoolRoad 	= link.isCarpoolRoad();
				boolean carpools 	= link.isCarpools();
				boolean expressLane = link.isExpressLane();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				LinkedList<SensorInfo>	sensorList = link.getSensorList();
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				if(baseName.contains("&"))
					baseName = baseName.replaceAll("&", " and ");				
				kmlStr += "Name:" 		+ baseName + "\r\n";
				kmlStr += "Ref:" 			+ refNodeId + "\r\n";
				kmlStr += "Nonref:" 		+ nonRefNodeId + "\r\n";
				kmlStr += "Class:" 		+ functionalClass + "\r\n";
				kmlStr += "Category:" 	+ speedCategory + "\r\n";
				kmlStr += "TraDir:" 		+ travelDirection + "\r\n";
				kmlStr += "Ramp:" 		+ ramp + "\r\n";
				kmlStr += "Tollway:" 	+ tollway + "\r\n";
				kmlStr += "CarpoolRoad:" 	+ carpoolRoad + "\r\n";
				kmlStr += "Carpools:" 	+ carpools + "\r\n";
				kmlStr += "ExpressLane:" 	+ expressLane + "\r\n";
				if(sensorList != null && sensorList.size() != 0) {
					String sensorStr = "null";
					ListIterator<SensorInfo> sensorIt = sensorList.listIterator();
					int i = 0;
					while(sensorIt.hasNext()) {
						SensorInfo sensor = sensorIt.next();
						if(i++ == 0)
							sensorStr = String.valueOf(sensor.getSensorId());
						else
							sensorStr += "," + sensor.getSensorId();
					}
					kmlStr += "Sensor:" + sensorStr + "\r\n";
				}
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				ListIterator<LocationInfo> pIterator = pointsList.listIterator();
				while(pIterator.hasNext()) {
					LocationInfo loc = pIterator.next();
					kmlStr += loc.getLongitude()+ "," + loc.getLatitude()+ "," + loc.getZLevel() + " ";
				}
				kmlStr += "</coordinates></LineString></Placemark>";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}
	
	private static void readNodeFile() {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|");
				
				long 			nodeId 	= Long.parseLong(nodes[0]);
				String[]		locStr 	= nodes[1].split(",");
				int 			zLevel	= Integer.parseInt(nodes[2]);
				LocationInfo 	location= new LocationInfo(Double.parseDouble(locStr[0]), Double.parseDouble(locStr[1]), zLevel);
				
				RDFNodeInfo RDFNode = new RDFNodeInfo(nodeId, location);
				
				nodeList.add(RDFNode);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readNodeFile: debug code: " + debug);
		}
		System.out.println("read node file finish!");
	}
	
	private static void readLinkFile() {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|");
				
				long 	linkId 			= Long.parseLong(nodes[0]);
				String 	baseName 		= nodes[1];
				long 	refNodeId 		= Long.parseLong(nodes[2]);
				long 	nonRefNodeId 	= Long.parseLong(nodes[3]);
				int 	functionalClass = Integer.parseInt(nodes[4]);
				String 	direction 		= nodes[5];
				int 	speedCategory 	= Integer.parseInt(nodes[6]);
				boolean ramp 			= nodes[7].equals("Y") ? true : false;
				boolean tollway 		= nodes[8].equals("Y") ? true : false;
				boolean carpoolRoad 	= nodes[9].equals("Y") ? true : false;
				boolean carpools 		= nodes[10].equals("Y") ? true : false;
				boolean expressLane 	= nodes[11].equals("Y") ? true : false;
				
				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
				RDFLink.setBaseName(baseName);
				RDFLink.setFunctionalClass(functionalClass);
				/**
				 * need fix
				 */
				
				LinkedList<LocationInfo> pointsList = new LinkedList<LocationInfo>();
				String[] pointsListStr		= nodes[12].split(";");
				for(int i = 0; i < pointsListStr.length; i++) {
					String[] locStr = pointsListStr[i].split(",");
					double lat = Double.parseDouble(locStr[0]);
					double lon = Double.parseDouble(locStr[1]);
					int z = Integer.parseInt(locStr[2]);
					LocationInfo loc = new LocationInfo(lat, lon, z);
					RDFLink.addPoint(loc);
					pointsList.add(loc);
				}
				
				//RDFLink.setPointsList(pointsList);
				
				linkList.add(RDFLink);
				linkMap.put(linkId, RDFLink);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readLinkFile: debug code: " + debug);
		}
		System.out.println("read link file finish!");
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
