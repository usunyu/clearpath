package input;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFInputFileGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFile = "RDF_Link.txt";
	// for write node file
	static String nodeFile = "RDF_Node.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName = "NAVTEQRDF";
	static String password = "NAVTEQRDF";
	static Connection connHome = null;
	/**
	 * @param node
	 */
	static ArrayList<RDFNodeInfo> nodeList = new ArrayList<RDFNodeInfo>();
	
	public static void main(String[] args) {
		fetchNode();
		writeNodeFile();
	}
	
	private static void writeNodeFile() {
		System.out.println("write node file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + nodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < nodeList.size(); i++) {
				RDFNodeInfo RDFNode = nodeList.get(i);
				
				long nodeId = RDFNode.getNodeId();
				PairInfo location = RDFNode.getLocation();
				String locationStr = location.getLati() + "," + location.getLongi();
				
				String strLine = nodeId + "|" + locationStr + "\r\n";
				
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

				long nodeId = res.getLong("node_id");
				double lat = res.getDouble("lat") / 100000;
				double lng = res.getDouble("lon") / 100000;
				PairInfo location = new PairInfo(lat, lng);

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
			System.err.println("Error Code: " + debug);
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




