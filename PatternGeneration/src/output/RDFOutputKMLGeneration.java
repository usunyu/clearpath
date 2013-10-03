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
	static String linkFile			= "RDF_Link.csv";
	static String nodeFile			= "RDF_Node.csv";
	static String linkGeometryFile	= "RDF_Link_Geometry.csv";
	static String kmlLinkFile		= "RDF_Link.kml";
	static String kmlNodeFile		= "RDF_Node.kml";
	static String matchSensorFile	= "RDF_Sensor_Match.csv";
	static String matchSensorKML	= "RDF_Sensor_Match.kml";
	static String linkLaneFile		= "RDF_Link_Lane.csv";
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
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param sensor
	 */
	static HashMap<Integer, SensorInfo> matchSensorMap = new HashMap<Integer, SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	
	public static void main(String[] args) {
		
		readNodeFile();
		generateNodeKML();
		
		readLinkFile();
		readLinkGeometry();
		readLinkLane();
		
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
			
			for(int sensorId : matchSensorMap.keySet()) {
				SensorInfo sensor = matchSensorMap.get(sensorId);
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
				kmlStr +=  longi + SEPARATION + lati +  SEPARATION + zLevel;
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
				String[] nodes = strLine.split(SEPARATION);
				
				long 	linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo link 	= linkMap.get(linkId);
				
				for(int i = 1; i < nodes.length; i++) {
					int sensorId = Integer.parseInt(nodes[i]);
					SensorInfo sensor 	= sensorMap.get(sensorId);
					link.addSensor(sensor);
					
					if(!matchSensorMap.containsKey(sensorId))
						matchSensorMap.put(sensorId, sensor);
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
			
			for(long nodeId : nodeMap.keySet()) {
				RDFNodeInfo node = nodeMap.get(nodeId);
				LocationInfo location = node.getLocation();
				double lati = location.getLatitude();
				double longi = location.getLongitude();
				int zLevel = location.getZLevel();
				
				String kmlStr = "<Placemark><name>" + nodeId + "</name>";
				kmlStr += "<description>";
				kmlStr += "ZLevel: " + zLevel;
				kmlStr += "</description>";
				kmlStr += "<Point><coordinates>";
				kmlStr +=  longi + SEPARATION + lati +  SEPARATION + zLevel;
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

			for(long linkId : linkMap.keySet()) {
				RDFLinkInfo link 	= linkMap.get(linkId);
				String baseName 	= link.getBaseName();
				long refNodeId 		= link.getRefNodeId();
				long nonRefNodeId 	= link.getNonRefNodeId();
				int functionalClass = link.getFunctionalClass();
				String travelDirection 	= link.getTravelDirection();
				LinkedList<Integer> directionList = link.getDirectionList();
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpool 	= link.isCarpool();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				LinkedList<SensorInfo>	sensorList = link.getSensorList();
				
				String dirStr	= null;
				for(int dir : directionList) {
					if(dirStr == null)
						dirStr = String.valueOf(dir);
					else
						dirStr += SEPARATION + dir;
				}
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				if(baseName.contains("&"))
					baseName = baseName.replaceAll("&", " and ");				
				kmlStr += "Name:" 			+ baseName + "\r\n";
				kmlStr += "Ref:" 			+ refNodeId + "\r\n";
				kmlStr += "Nonref:" 		+ nonRefNodeId + "\r\n";
				kmlStr += "Class:" 			+ functionalClass + "\r\n";
				kmlStr += "Category:" 		+ speedCategory + "\r\n";
				kmlStr += "Dir:" 			+ dirStr + "\r\n";
				kmlStr += "TraDir:" 		+ travelDirection + "\r\n";
				kmlStr += "Ramp:" 			+ ramp + "\r\n";
				kmlStr += "Tollway:" 		+ tollway + "\r\n";
				kmlStr += "Carpool:" 		+ carpool + "\r\n";
				if(sensorList != null && sensorList.size() != 0) {
					String sensorStr = "null";
					ListIterator<SensorInfo> sensorIt = sensorList.listIterator();
					int i = 0;
					while(sensorIt.hasNext()) {
						SensorInfo sensor = sensorIt.next();
						if(i++ == 0)
							sensorStr = String.valueOf(sensor.getSensorId());
						else
							sensorStr += SEPARATION + sensor.getSensorId();
					}
					kmlStr += "Sensor:" + sensorStr + "\r\n";
				}
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				ListIterator<LocationInfo> pIterator = pointsList.listIterator();
				while(pIterator.hasNext()) {
					LocationInfo loc = pIterator.next();
					kmlStr += loc.getLongitude()+ SEPARATION + loc.getLatitude()+ SEPARATION + loc.getZLevel() + " ";
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
				String[] nodes = strLine.split(SEPARATION);
				
				long 			nodeId 	= Long.parseLong(nodes[0]);
				double			lat		= Double.parseDouble(nodes[1]);
				double			lon		= Double.parseDouble(nodes[2]);
				int 			zLevel	= Integer.parseInt(nodes[3]);
				
				LocationInfo 	location= new LocationInfo(lat, lon, zLevel);
				
				RDFNodeInfo node = new RDFNodeInfo(nodeId, location);
				
				nodeMap.put(nodeId, node);
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

	private static void readLinkLane() {
		System.out.println("read link lane...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkLaneFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				long linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo	link	= linkMap.get(linkId);
				for(int i = 1; i < nodes.length; i += 4) {
					long laneId		= Long.parseLong(nodes[i]);
					String travelDirection = nodes[i + 1];
					int laneType	= Integer.parseInt(nodes[i + 2]);
					int accessId 	= Integer.parseInt(nodes[i + 3]);
					RDFLaneInfo	lane = new RDFLaneInfo(laneId, travelDirection, laneType, accessId);

					link.addLane(lane);
				}
			}
			br.close();
			in.close();
			fstream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("readLinkLane: debug code: " + debug);
		}
		System.out.println("read link lane finish!");
	}

	private static void readLinkGeometry() {
		System.out.println("read link geometry...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkGeometryFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				long linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo	link	= linkMap.get(linkId);
				for(int i = 1; i < nodes.length; i+=3) {
					double lat		= Double.parseDouble(nodes[i]);
					double lon		= Double.parseDouble(nodes[i + 1]);
					int zLevel	= Integer.parseInt(nodes[i + 2]);
					LocationInfo loc = new LocationInfo(lat, lon, zLevel);
					link.addPoint(loc);
				}
			}
			br.close();
			in.close();
			fstream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("readLinkGeometry: debug code: " + debug);
		}
		System.out.println("read link geometry finish!");
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
				String[] nodes = strLine.split(SEPARATION);
				
				long 	linkId 			= Long.parseLong(nodes[0]);
				long 	refNodeId 		= Long.parseLong(nodes[1]);
				long 	nonRefNodeId 	= Long.parseLong(nodes[2]);
				String 	baseName 		= nodes[3];
				int		accessId		= Integer.parseInt(nodes[4]);
				int 	functionalClass = Integer.parseInt(nodes[5]);
				int 	speedCategory 	= Integer.parseInt(nodes[6]);
				String 	travelDirection = nodes[7];
				boolean ramp 			= nodes[8].equals(YES) ? true : false;
				boolean tollway 		= nodes[9].equals(YES) ? true : false;
				
				
				RDFLinkInfo link = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
				
				link.setBaseName(baseName);
				link.setAccessId(accessId);
				link.setFunctionalClass(functionalClass);
				link.setSpeedCategory(speedCategory);
				link.setTravelDirection(travelDirection);
				link.setRamp(ramp);
				link.setTollway(tollway);
				
				// add direction
				RDFNodeInfo refNode = nodeMap.get(refNodeId);
				RDFNodeInfo nonRefNode = nodeMap.get(nonRefNodeId);
				if(travelDirection.equals("T")) {
					int direction = Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation());
					link.addDirection(direction);
				}
				else if(travelDirection.equals("F")) {
					int direction = Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation());
					link.addDirection(direction);
				}
				else if(travelDirection.equals("B")) {
					int direction = Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation());
					link.addDirection(direction);
					direction = Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation());
					link.addDirection(direction);
				}
				
				linkMap.put(linkId, link);

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
