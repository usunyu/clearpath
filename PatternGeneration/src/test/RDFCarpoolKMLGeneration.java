package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.ListIterator;

import objects.LocationInfo;
import objects.RDFLinkInfo;
import objects.SensorInfo;

public class RDFCarpoolKMLGeneration {

	/**
	 * @param file
	 */
	static String root				= "file";
	static String linkFile			= "RDF_Link.csv";
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
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	/**
	 * @param carpool
	 */
	static LinkedList<Long> carpoolList = new LinkedList<Long>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readLinkFile();
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
				//boolean carpoolRoad = link.isCarpoolRoad();
				//boolean carpools	= link.isCarpools();
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
				//kmlStr += "CarpoolRoad:" 	+ carpoolRoad + "\r\n";
				//kmlStr += "Carpools:" 	+ carpools + "\r\n";
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
				String 	streetName 		= nodes[1];
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
				
				if(expressLane)
					continue;
				if(functionalClass!=1 && functionalClass!=2)
					continue;
				
				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
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
