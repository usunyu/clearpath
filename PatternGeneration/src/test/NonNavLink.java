package test;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class NonNavLink {

	/**
	 * @param file
	 */
	static String root				= "file";
	static String nunNavLinkKML		= "RDF_Non_Nav_Link.kml";
	/**
	 * @param database
	 */
	static String urlHome			= "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName			= "NAVTEQRDF";
	static String password			= "NAVTEQRDF";
	static Connection connHome		= null;
	/**
	 * @param args
	 */
	static double UNIT				= 100000;
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchNonNavLink();
		fetchNodeInfoById();
		generateLinkKML();
	}
	
	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + nunNavLinkKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for(long linkId : linkMap.keySet()) {
				RDFLinkInfo link 	= linkMap.get(linkId);


				long refNodeId 		= link.getRefNodeId();
				long nonRefNodeId 	= link.getNonRefNodeId();
				
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Ref:" 			+ refNodeId + "\r\n";
				kmlStr += "Nonref:" 		+ nonRefNodeId + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				
				RDFNodeInfo n1 = nodeMap.get(refNodeId);
				kmlStr += n1.getLocation().getLongitude()+ "," + n1.getLocation().getLatitude()+ "," + n1.getLocation().getZLevel() + " ";
				RDFNodeInfo n2 = nodeMap.get(nonRefNodeId);
				kmlStr += n2.getLocation().getLongitude()+ "," + n2.getLocation().getLatitude()+ "," + n2.getLocation().getZLevel() + " ";
				
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
	
	private static void fetchNodeInfoById() {
		System.out.println("fetch node info by id...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = getConnection();
			con.setAutoCommit(false);
			
			sql = "SELECT node_id, lat, lon, zlevel FROM rdf_node";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, 
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.FETCH_FORWARD);
			pstatement.setFetchSize(1000);
			res = pstatement.executeQuery();
			
			while (res.next()) {
				debug++;
				
				long 	nodeId 	= res.getLong("node_id");
				
				if(!nodeMap.containsKey(nodeId))
					continue;
				
				double 	lat 	= res.getDouble("lat") / UNIT;
				double 	lon 	= res.getDouble("lon") / UNIT;
				int 	zLevel 	= res.getInt("zlevel");
				LocationInfo location = new LocationInfo(lat, lon, zLevel);
				
				RDFNodeInfo node = nodeMap.get(nodeId);
				node.setLocation(location);
				
				if(debug % 10000 == 0)
					System.out.println("processed " + debug + " records.");
			}
			res.close();
			pstatement.close();
			con.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchNodeInfoById: debug code: " + debug);
		}
		System.out.println("fetch node info by id finish!");
	}

	private static void fetchNonNavLink() {
		System.out.println("fetch non nav link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = getConnection();
			con.setAutoCommit(false);
			
			sql = "SELECT link_id, ref_node_id, nonref_node_id FROM rdf_link WHERE link_id NOT IN (SELECT DISTINCT link_id FROM rdf_nav_link)";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, 
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.FETCH_FORWARD);
			pstatement.setFetchSize(1000);
			res = pstatement.executeQuery();
			
			while (res.next()) {
				debug++;
				
				long linkId 	= res.getLong("link_id");
				long refNodeId 	= res.getLong("ref_node_id");
				long nonRefNodeId 	= res.getLong("nonref_node_id");
				
				RDFLinkInfo link = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
				linkMap.put(linkId, link);
				
				if(!nodeMap.containsKey(refNodeId)) {
					RDFNodeInfo node = new RDFNodeInfo(refNodeId);
					nodeMap.put(refNodeId, node);
				}
				if(!nodeMap.containsKey(nonRefNodeId)) {
					RDFNodeInfo node = new RDFNodeInfo(nonRefNodeId);
					nodeMap.put(nonRefNodeId, node);
				}
				
				if(debug >= 1000) {
					break;
				}
			}
			
			res.close();
			pstatement.close();
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("fetchNonNavLink: debug code: " + debug);
		}
		System.out.println("fetch non nav link finish!");
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
