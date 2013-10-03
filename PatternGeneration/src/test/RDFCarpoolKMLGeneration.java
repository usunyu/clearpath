package test;
import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFCarpoolKMLGeneration {

	/**
	 * @param file
	 */
	static String root				= "file";
	static String linkFile			= "RDF_Link.csv";
	static String linkLaneFile		= "RDF_Link_Lane.csv";
	static String linkGeometryFile	= "RDF_Link_Geometry.csv";
	static String kmlLinkFile		= "RDF_Link_Carpool.kml";
	static String carpoolFile		= "RDF_Carpool.csv";
	/**
	 * @param database
	 */
	static String urlHome			= "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName			= "NAVTEQRDF";
	static String password			= "NAVTEQRDF";
	static Connection connHome		= null;
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
	 * @param carpool
	 */
	static LinkedList<Long> carpoolList = new LinkedList<Long>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readLinkFile();
		readLinkGeometry();
		readLinkLane();
		generateLinkKML();
		//fetchCarpool();
		//writeCarpool();
	}

	private static void writeCarpool() {
		System.out.println("write carpool file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + carpoolFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<Long> iterator = carpoolList.listIterator();
			while (iterator.hasNext()) {
				long linkId = iterator.next();
				String strLine = linkId + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write carpool file finish!");
	}
	
	private static void fetchCarpool() {
		System.out.println("fetch carpool...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT link_id FROM rdf_nav_link s1, rdf_access s2 WHERE s1.access_id = s2.access_id AND carpools = 'Y'";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				long linkId = res.getLong("link_id");
				
				carpoolList.add(linkId);
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchCarpool: debug code: " + debug);
		}
		System.out.println("fetch carpool finish!");
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
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpool 	= link.isCarpool();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				if(!carpool)
					continue;
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				if(baseName.contains("&"))
					baseName = baseName.replaceAll("&", " and ");				
				kmlStr += "Name:" 			+ baseName + "\r\n";
				kmlStr += "Ref:" 			+ refNodeId + "\r\n";
				kmlStr += "Nonref:" 		+ nonRefNodeId + "\r\n";
				kmlStr += "Class:" 			+ functionalClass + "\r\n";
				kmlStr += "Category:" 		+ speedCategory + "\r\n";
				kmlStr += "TraDir:" 		+ travelDirection + "\r\n";
				kmlStr += "Ramp:" 			+ ramp + "\r\n";
				kmlStr += "Tollway:" 		+ tollway + "\r\n";
				kmlStr += "Carpool:" 		+ carpool + "\r\n";
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
