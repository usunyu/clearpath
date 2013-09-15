package input;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFInputFileGeneration {

	/**
	 * @param file
	 */
	static String root 		= "file";
	// for write link file
	static String linkFile 		= "RDF_Link.txt";
	// for write node file
	static String nodeFile 	= "RDF_Node.txt";
	/**
	 * @param database
	 */
	static String urlHome 	= "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName 	= "NAVTEQRDF";
	static String password 	= "NAVTEQRDF";
	static Connection connHome = null;
	/**
	 * @param node
	 */
	static LinkedList<RDFNodeInfo> nodeList = new LinkedList<RDFNodeInfo>();
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	
	public static void main(String[] args) {
		//fetchNode();
		//writeNodeFile();
		
		fetchLink();
		fetchGeometry();
		writeLinkFile();
	}
	
	private static void fetchGeometry() {
		System.out.println("fetch geometry...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			long listSize = linkList.size();
			
			while(iterator.hasNext()) {
				debug++;
				RDFLinkInfo RDFLink = iterator.next();
				long linkId = RDFLink.getLinkId();
				
				sql =	"SELECT lat, lon, zlevel " + 
						"FROM rdf_link_geometry " + 
						"WHERE link_id=" + linkId + " " +
						"ORDER BY seq_num";
				
				pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				res = pstatement.executeQuery();
				
				LinkedList<LocationInfo> pointsList = null;
				while (res.next()) {
					
					if(pointsList == null)
						pointsList = new LinkedList<LocationInfo>();
					
					double lat = 	res.getDouble("lat") / 100000;
					double lng = 	res.getDouble("lon") / 100000;
					int zLevel = 	res.getInt("zlevel");
					
					LocationInfo location = new LocationInfo(lat, lng, zLevel);
					pointsList.add(location);
				}
				RDFLink.setPointsList(pointsList);
				
				if (debug % 250 == 0) {
					// reconnect
					res.close();
					pstatement.close();
					con.close();
					con = getConnection();
					System.out.println((double)debug / listSize * 100 + "% finish!");
				}
			}
			if(!res.isClosed()) {
				res.close();
				pstatement.close();
				con.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchGeometry: debug code: " + debug);
		}
		System.out.println("fetch geometry finish!");
	}
	
	private static void writeLinkFile() {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo RDFLink = iterator.next();
				long linkId = RDFLink.getLinkId();
				long refNodeId = RDFLink.getRefNodeId();
				long nonRefNodeId = RDFLink.getNonRefNodeId();
				int functionalClass = RDFLink.getFunctionalClass();
				
				String strLine = linkId + "|" + refNodeId + "|" + nonRefNodeId +  "|" + functionalClass + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}
	
	private static void fetchLink() {
		System.out.println("fetch link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql =	"SELECT t1.link_id, t1.ref_node_id, t1.nonref_node_id, t2.functional_class " + 
					"FROM rdf_link t1 " + 
					"LEFT JOIN rdf_nav_link t2 " + 
					"ON t1.link_id=t2.link_id";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				long linkId = res.getLong("link_id");
				long refNodeId = res.getLong("ref_node_id");
				long nonRefNodeId = res.getLong("nonref_node_id");
				String checkFunClass = res.getString("functional_class");
				int functionalClass;
				if(checkFunClass == null)
					functionalClass = -1;
				else
					functionalClass = res.getInt("functional_class");

				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId, functionalClass);

				linkList.add(RDFLink);

				if (debug % 100000 == 0)
					System.out.println("record " + debug + " finish!");
				
			}

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchLink: debug code: " + debug);
		}
		System.out.println("fetch link finish!");
	}
	
	private static void writeNodeFile() {
		System.out.println("write node file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + nodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<RDFNodeInfo> iterator = nodeList.listIterator();
			while (iterator.hasNext()) {
				RDFNodeInfo RDFNode = iterator.next();
				long nodeId = RDFNode.getNodeId();
				LocationInfo location = RDFNode.getLocation();
				String locationStr = location.getLatitude() + "," + location.getLongitude();
				int zLevel = location.getZLevel();
				String strLine = nodeId + "|" + locationStr + "|" + zLevel + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write node file finish!");
	}

	private static void fetchNode() {
		System.out.println("fetch node...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT * FROM rdf_node";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				long nodeId = 	res.getLong("node_id");
				double lat = 	res.getDouble("lat") / 100000;
				double lng = 	res.getDouble("lon") / 100000;
				int zLevel = 	res.getInt("zlevel");
				LocationInfo location = new LocationInfo(lat, lng, zLevel);

				RDFNodeInfo RDFNode = new RDFNodeInfo(nodeId, location);
				
				nodeList.add(RDFNode);
				
				if (debug % 100000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchNode: debug code: " + debug);
		}
		System.out.println("fetch node finish!");
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




