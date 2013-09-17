package input;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFInputFileGeneration {

	/**
	 * @param file
	 */
	static String root				= "file";
	// for write link file
	static String linkFile			= "RDF_Link.txt";
	static String linkGeometryFile	= "RDF_Link_Geometry.txt";
	static String linkTempFile		= "RDF_Link_Temp.txt";
	// for write node file
	static String nodeFile			= "RDF_Node.txt";
	/**
	 * @param database
	 */
	static String urlHome			= "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName			= "NAVTEQRDF";
	static String password			= "NAVTEQRDF";
	static Connection connHome		= null;
	/**
	 * @param node
	 */
	static LinkedList<RDFNodeInfo> nodeList = new LinkedList<RDFNodeInfo>();
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	static LinkedList<String> linkBuffer = new LinkedList<String>();
	/**
	 * @param post code
	 */
	static ArrayList<Integer> postCodeList = new ArrayList<Integer>();
	
	public static void main(String[] args) {
		//fetchNode();
		//writeNodeFile();
		
		//fetchLink();				//deprecated
		//fetchGeometry();			//deprecated
		//writeLinkFile();			//deprecated
		
		//fetchWriteLink();
		//readFetchWriteGeometry();	//deprecated
		
		//fetchWriteGeometry();
		
		// fetch link from post code
		initialPostCode();
		fetchLinkByPostCode();
		writeLinkByPostCode();
	}
	
	private static void writeLinkByPostCode() {
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
				String direction = RDFLink.getDirection();
				boolean ramp = RDFLink.isRamp();
				boolean tollway = RDFLink.isTollway();
				int speedCategory = RDFLink.getSpeedCategory();
				boolean carpool = RDFLink.isCarpool();
				
				String strLine = linkId + "|" + refNodeId + "|" + nonRefNodeId + "|" + functionalClass + "|" + direction +"|" +
						ramp + "|" + tollway + "|" + speedCategory + "|" + carpool + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}
	
	private static void fetchLinkByPostCode() {
		System.out.println("fetch link by post code...");
		int debug = 0;
		try {
			for(int p = 0; p < postCodeList.size(); p++) {
				int postCode = postCodeList.get(p);
				
				Connection con = null;
				String sql = null;
				PreparedStatement pstatement = null;
				ResultSet res = null;
				
				con = getConnection();
				
				sql = 	"SELECT t4.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
						"FROM " +
						"(SELECT t1.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category " +
						"FROM rdf_link t1, rdf_postal_area t2, rdf_nav_link t3 " +
						"WHERE t2.postal_code = '" + postCode + "' " +
						"AND (t2.postal_area_id = t1.left_postal_area_id OR t2.postal_area_id = t1.right_postal_area_id)" +
						"AND t1.link_id = t3.link_id) t4 " +
						"LEFT JOIN rdf_nav_link_attribute t5 " +
						"ON t4.link_id = t5.link_id";
				
				System.out.println("execute query... ");
				pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				res = pstatement.executeQuery();
				
				while (res.next()) {
					debug++;

					long linkId = res.getLong("link_id");
					long refNodeId = res.getLong("ref_node_id");
					long nonRefNodeId = res.getLong("nonref_node_id");
					int functionalClass = res.getInt("functional_class");
					String direction = res.getString("travel_direction");
					boolean ramp = res.getString("ramp").equals("Y") ? true : false;
					boolean tollway = res.getString("tollway").equals("Y") ? true : false;
					int speedCategory = res.getInt("speed_category");
					boolean carpool = res.getString("carpool_road") == null ? false : true;

					RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId, functionalClass, direction, ramp, tollway, carpool, speedCategory );

					linkList.add(RDFLink);

					if (debug % 1000 == 0)
						System.out.println("record " + debug + " finish!");
					
				}

				res.close();
				pstatement.close();
				con.close();
				
				if(p % 10 == 0)
					System.out.println((double)p / postCodeList.size() * 100 + "%");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkByPostCode: debug code: " + debug);
		}
		System.out.println("fetch link by post code finish!");
	}
	
	private static void initialPostCode() {
		System.out.println("initial post code...");
		// add needed post code here
		postCodeList.add(90007);
	}
	
	private static void fetchWriteGeometry() {
		System.out.println("fetch write geometry...");
		int debug = 0;
		try {
			// delete file if it exists
			File oldFile = new File(root + "/" + linkGeometryFile);
			if(oldFile.exists()) oldFile.delete();
			
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT link_id, lat, lon, zlevel FROM rdf_link_geometry ORDER BY link_id, seq_num";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			System.out.println("execute query...");
			res = pstatement.executeQuery();
			System.out.println("execute query finish!");
			
			long lastLinkId = -1;
			RDFLinkInfo RDFLink = null;

			while (res.next()) {

				long linkId = 	res.getLong("link_id");
				double lat = 	res.getDouble("lat") / 100000;
				double lng = 	res.getDouble("lon") / 100000;
				int zLevel = 	res.getInt("zlevel");
				LocationInfo location = new LocationInfo(lat, lng, zLevel);
				
				if(linkId != lastLinkId) {
					debug++;
					
					// data is too large to store all in memory
					if (debug % 100000 == 0) {
						// append write
						FileWriter fstream = new FileWriter(root + "/" + linkGeometryFile, true);
						BufferedWriter out = new BufferedWriter(fstream);
						
						ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
						while(iterator.hasNext()) {
							RDFLinkInfo writeRDFLink = iterator.next();
							
							long writeLinkId = writeRDFLink.getLinkId();
							LinkedList<LocationInfo> pointsList = writeRDFLink.getPointsList();
							
							String pointsStr = "null";
							ListIterator<LocationInfo> pIterator = pointsList.listIterator();
							int i = 0;
							while(pIterator.hasNext()) {
								LocationInfo loc = pIterator.next();
								if(i++ == 0)
									pointsStr = loc.getLatitude() + "," + loc.getLongitude() + "," + loc.getZLevel();
								else
									pointsStr += ";" + loc.getLatitude() + "," + loc.getLongitude() + "," + loc.getZLevel();
								
							}
							
							String strLine = writeLinkId + "|" + pointsStr + "\r\n";
							out.write(strLine);
						}
						out.close();
						
						System.out.println("record " + debug + " finish!");
						// free the old memory
						linkList = new LinkedList<RDFLinkInfo>();
					}
					
					
					RDFLink = new RDFLinkInfo(linkId);
					LinkedList<LocationInfo> pointsList = new LinkedList<LocationInfo>();
					pointsList.add(location);
					RDFLink.setPointsList(pointsList);
					lastLinkId = linkId;
					
					linkList.add(RDFLink);
				}
				else {
					LinkedList<LocationInfo> pointsList = RDFLink.getPointsList();
					pointsList.add(location);
				}				
			}
			
			// read last RDFLink
			linkList.add(RDFLink);
			// read the rest
			if(!linkList.isEmpty()) {
				// append write
				FileWriter fstream = new FileWriter(root + "/" + linkGeometryFile, true);
				BufferedWriter out = new BufferedWriter(fstream);
				
				ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
				while(iterator.hasNext()) {
					RDFLinkInfo writeRDFLink = iterator.next();
					
					long writeLinkId = writeRDFLink.getLinkId();
					LinkedList<LocationInfo> pointsList = writeRDFLink.getPointsList();
					
					String pointsStr = "null";
					ListIterator<LocationInfo> pIterator = pointsList.listIterator();
					int i = 0;
					while(pIterator.hasNext()) {
						LocationInfo loc = pIterator.next();
						if(i == 0)
							pointsStr = loc.getLatitude() + "," + loc.getLongitude() + "," + loc.getZLevel();
						else
							pointsStr += ";" + loc.getLatitude() + "," + loc.getLongitude() + "," + loc.getZLevel();
						
					}
					
					String strLine = writeLinkId + "|" + pointsStr + "\r\n";
					out.write(strLine);
					// free the old memory
					linkList = new LinkedList<RDFLinkInfo>();
				}
				out.close();
			}

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchWriteGeometry: debug code: " + debug);
		}
		System.out.println("fetch write geometry finish!");
	}
	
	/**
	 * read from RDF_Link.txt, get link_id
	 * fetch from DB, get geometry
	 * append write to RDF_Link_Temp.txt
	 * change RDF_Link_Temp.txt to RDF_Link.txt
	 * deprecated: too slow :(
	 */
	private static void readFetchWriteGeometry() {
		System.out.println("read fetch and write geometry...");
		int debug = 0;
		try {
			// delete temp file if it exists
			File oldFile = new File(root + "/" + linkTempFile);
			if(oldFile.exists()) oldFile.delete();
			
			FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|");
				String linkId = nodes[0];
				
				sql =	"SELECT lat, lon, zlevel " + 
						"FROM rdf_link_geometry " + 
						"WHERE link_id=" + linkId + " " +
						"ORDER BY seq_num";
				
				pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				res = pstatement.executeQuery();
				
				String pointsListStr = "null";
				int i = 0;
				while (res.next()) {
					double lat = 	res.getDouble("lat") / 100000;
					double lng = 	res.getDouble("lon") / 100000;
					int zLevel = 	res.getInt("zlevel");
					
					if(i++ == 0)
						pointsListStr = lat + "," + lng + "," + zLevel;
					else
						pointsListStr += ";" + lat + "," + lng + "," + zLevel;
				}
				linkBuffer.add(strLine + "|" + pointsListStr + "\r\n");
				
				if (debug % 1000 == 0) {
					// append write to temp
					FileWriter tempFstream = new FileWriter(root + "/" + linkTempFile, true);
					BufferedWriter out = new BufferedWriter(tempFstream);
					
					ListIterator<String> iterator = linkBuffer.listIterator();
					while(iterator.hasNext()) {
						String buffer = iterator.next();
						out.write(buffer);
					}
					out.close();
					// free memory
					linkBuffer = new LinkedList<String>();
					System.out.println("line " + debug + " finish!");
				}
				res.close();
				pstatement.close();
			}
			// read the rest
			if(!linkBuffer.isEmpty()) {
				// append write to temp
				FileWriter tempFstream = new FileWriter(root + "/" + linkTempFile, true);
				BufferedWriter out = new BufferedWriter(tempFstream);
				
				ListIterator<String> iterator = linkBuffer.listIterator();
				while(iterator.hasNext()) {
					String buffer = iterator.next();
					out.write(buffer);
				}
				out.close();
				// free memory
				linkBuffer = new LinkedList<String>();
			}
			
			res.close();
			pstatement.close();
			con.close();
			
			br.close();
			in.close();
			fstream.close();
			
			// delete original link file
			oldFile = new File(root + "/" + linkFile);
			if(oldFile.exists()) oldFile.delete();
			
			oldFile =new File(root + "/" + linkTempFile);
			File newFile =new File(root + "/" + linkFile);
	 
			if(oldFile.renameTo(newFile))
				System.out.println("rename to " + root + "/" + linkTempFile + " succesful");
			else
				System.out.println("rename failed");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readFetchWriteGeometry: debug code: " + debug);
		}
		System.out.println("read fetch and write geometry finish!");
	}
	
	private static void fetchWriteLink() {
		System.out.println("fetch and write link...");
		int debug = 0;
		try {
			// delete file if it exists
			File oldFile = new File(root + "/" + linkFile);
			if(oldFile.exists()) oldFile.delete();
			
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
				int functionalClass = res.getInt("functional_class");

				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
				RDFLink.setFunctionalClass(functionalClass);

				linkList.add(RDFLink);

				// data is too large to store all in memory
				if (debug % 100000 == 0) {
					// append write
					FileWriter fstream = new FileWriter(root + "/" + linkFile, true);
					BufferedWriter out = new BufferedWriter(fstream);
					
					ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
					while(iterator.hasNext()) {
						RDFLinkInfo writeRDFLink = iterator.next();
						long writeLinkId = writeRDFLink.getLinkId();
						long writeRefNodeId = writeRDFLink.getRefNodeId();
						long writeNonRefNodeId = writeRDFLink.getNonRefNodeId();
						int writeFunctionalClass = writeRDFLink.getFunctionalClass();
						
						String strLine = writeLinkId + "|" + writeRefNodeId + "|" + writeNonRefNodeId + "|" + writeFunctionalClass + "\r\n";
						out.write(strLine);
					}
					out.close();
					
					System.out.println("record " + debug + " finish!");
					// free the old memory
					linkList = new LinkedList<RDFLinkInfo>();
				}
				
			}
			
			// read the rest
			if(!linkList.isEmpty()) {
				// append write
				FileWriter fstream = new FileWriter(root + "/" + linkFile, true);
				BufferedWriter out = new BufferedWriter(fstream);
				
				ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
				while(iterator.hasNext()) {
					RDFLinkInfo writeRDFLink = iterator.next();
					long writeLinkId = writeRDFLink.getLinkId();
					long writeRefNodeId = writeRDFLink.getRefNodeId();
					long writeNonRefNodeId = writeRDFLink.getNonRefNodeId();
					int writeFunctionalClass = writeRDFLink.getFunctionalClass();
					
					String strLine = writeLinkId + "|" + writeRefNodeId + "|" + writeNonRefNodeId + "|" + writeFunctionalClass + "\r\n";
					out.write(strLine);
				}
				out.close();
				// free the old memory
				linkList = new LinkedList<RDFLinkInfo>();
			}

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchWriteLink: debug code: " + debug);
		}
		System.out.println("fetch and write link finish!");
	}
	
	/**
	 * deprecated
	 */
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
	
	/**
	 * deprecated
	 */
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
				//int functionalClass = RDFLink.getFunctionalClass();
				
				String strLine = linkId + "|" + refNodeId + "|" + nonRefNodeId + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}
	
	/**
	 * deprecated
	 */
	private static void fetchLink() {
		System.out.println("fetch link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			//sql =	"SELECT t1.link_id, t1.ref_node_id, t1.nonref_node_id, t2.functional_class " + 
			//		"FROM rdf_link t1 " + 
			//		"LEFT JOIN rdf_nav_link t2 " + 
			//		"ON t1.link_id=t2.link_id";
			
			sql = "SELECT link_id, ref_node_id, nonref_node_id FROM rdf_link";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				long linkId = res.getLong("link_id");
				long refNodeId = res.getLong("ref_node_id");
				long nonRefNodeId = res.getLong("nonref_node_id");
				//String checkFunClass = res.getString("functional_class");
				int functionalClass;
				//if(checkFunClass == null)
				//	functionalClass = -1;
				//else
				//	functionalClass = res.getInt("functional_class");

				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);

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
		System.out.println("connect to database... ");
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




