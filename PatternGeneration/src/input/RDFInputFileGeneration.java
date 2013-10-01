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
	 * @param args
	 */
	static double UNIT				= 100000;
	static double LosAngelesLat1 	= 33.68   * UNIT;
	static double LosAngelesLat2 	= 34.27   * UNIT;
//	static double LosAngelesLon1 	= -118.92 * UNIT;
//	static double LosAngelesLon2 	= -117.55 * UNIT;
	static double LosAngelesLon1 	= -118.42 * UNIT;
	static double LosAngelesLon2 	= -117.95 * UNIT;
	/**
	 * @param node
	 */
	static LinkedList<RDFNodeInfo> nodeList = new LinkedList<RDFNodeInfo>();
	static HashSet<Long> nodeSet = new HashSet<Long>();
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	static LinkedList<String> linkBuffer = new LinkedList<String>();
	/**
	 * @param post code
	 */
	static ArrayList<Integer> postCodeList = new ArrayList<Integer>();
	
	public static void main(String[] args) {
		//fetchNode();
		//writeNodeFile();
		
		/*fetchLink();					deprecated */
		/*fetchGeometry();				deprecated */
		/*writeLinkFile();			 	deprecated */
		
		//fetchWriteLink();
		/*readFetchWriteGeometry();	 	deprecated */
		
		//fetchWriteGeometry();
		
		/**
		 *  Step 1) add the post code needed (deprecated not accurate and continue) 
		 *  		fetch link from post code (deprecated)
		 *  		fetch link by area (lat, lon)
		 *  		write link info to RDF_Link.txt
		 */
		/*initialPostCode();		 	deprecated */
		/*fetchLinkByPostCode();	 	deprecated */
		fetchLinkByArea();
		writeLinkDetail();
		/**
		 *  Step 2) read the info from RDF_Link.txt
		 *  		fetch the node info according the read data
		 *  		write the node info to RDF_Node.txt
		 */
		readLinkFile();
		fetchNodeBySet();
		writeNodeFile();
		/**
		 *  Step 3) read the info from RDF_Link.txt
		 *			fetch geometry points
		 */
		readLinkFile();
		fetchGeometry();
		writeLinkWithGeometry();
	}
	
	private static void fetchLinkByArea() {
		System.out.println("fetch link by area...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = getConnection();
			
			sql = 	"SELECT t8.link_id, street_name, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
					"FROM " +
					"(SELECT t6.link_id, road_name_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
					"FROM " +
					"(SELECT t4.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
					"FROM " +
					"(SELECT t1.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category " +
					"FROM rdf_link t1, rdf_node t2, rdf_nav_link t3 " +
					"WHERE ref_node_id = node_id " +
					"AND lat >= " + LosAngelesLat1 + " AND lat <= " + LosAngelesLat2 + " " +
					"AND lon >= " + LosAngelesLon1 + " AND lon <= " + LosAngelesLon2 + " " +
					"AND t1.link_id = t3.link_id) t4 " +
					"LEFT JOIN rdf_nav_link_attribute t5 " +
					"ON t4.link_id = t5.link_id) t6 " +
					"LEFT JOIN rdf_road_link t7 " +
					"ON t6.link_id = t7.link_id) t8 " +
					"LEFT JOIN rdf_road_name t9 " +
					"on t8.road_name_id = t9.road_name_id ";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			
			while (res.next()) {
				debug++;
				long linkId = res.getLong("link_id");
				
				if(linkMap.containsKey(linkId)) {
					String secondName = res.getString("street_name");
					RDFLinkInfo existLink = linkMap.get(linkId);
					existLink.addStreetName(secondName);
					continue;
				}
				
				String streetName = res.getString("street_name");
				long refNodeId = res.getLong("ref_node_id");
				long nonRefNodeId = res.getLong("nonref_node_id");
				int functionalClass = res.getInt("functional_class");
				String direction = res.getString("travel_direction");
				boolean ramp = res.getString("ramp").equals("Y") ? true : false;
				boolean tollway = res.getString("tollway").equals("Y") ? true : false;
				int speedCategory = res.getInt("speed_category");
				boolean carpool = res.getString("carpool_road") == null ? false : true;

				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, streetName, refNodeId, nonRefNodeId, functionalClass, direction, ramp, tollway, carpool, speedCategory );

				linkList.add(RDFLink);
				linkMap.put(linkId, RDFLink);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
			
			}

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkByPostCode: debug code: " + debug);
		}
		System.out.println("fetch " + linkList.size() + " links by area finish!");
	}
	
	private static void writeLinkWithGeometry() {
		System.out.println("write link with geometry...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo RDFLink = iterator.next();
				long linkId = RDFLink.getLinkId();
				String streetName = RDFLink.getStreetName();
				long refNodeId = RDFLink.getRefNodeId();
				long nonRefNodeId = RDFLink.getNonRefNodeId();
				int functionalClass = RDFLink.getFunctionalClass();
				String travelDirection = RDFLink.getTravelDirection();
				boolean ramp = RDFLink.isRamp();
				boolean tollway = RDFLink.isTollway();
				int speedCategory = RDFLink.getSpeedCategory();
				boolean carpool = RDFLink.isCarpool();
				LinkedList<LocationInfo> pointsList = RDFLink.getPointsList();
				
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
				
				String strLine = linkId + "|" + streetName + "|" + refNodeId + "|" + nonRefNodeId + "|" + functionalClass + "|" + travelDirection +"|" +
						speedCategory + "|" + (ramp ? "T" : "F") + "|" + (tollway ? "T" : "F") + "|" + (carpool ? "T" : "F") + "|" + pointsStr + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("write link with geometry finish!");
	}
	
	private static void addNodeByQuery(String query) {
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();
			
			sql = "SELECT node_id, lat, lon, zlevel FROM rdf_node WHERE node_id IN (" + query + ")";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			
			while (res.next()) {
				long 	nodeId 	= res.getLong("node_id");
				double 	lat 	= res.getDouble("lat") / UNIT;
				double 	lon 	= res.getDouble("lon") / UNIT;
				int 	zLevel 	= res.getInt("zlevel");
				LocationInfo location = new LocationInfo(lat, lon, zLevel);

				RDFNodeInfo RDFNode = new RDFNodeInfo(nodeId, location);
				
				nodeList.add(RDFNode);
			}
			res.close();
			pstatement.close();
			con.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void fetchNodeBySet() {
		System.out.println("fetch node by set...");
		int debug = 0;
		try {
			// build sql query
			StringBuffer nodeStringBuffer = new StringBuffer();
			Iterator<Long> iterator = nodeSet.iterator();
			int i = 0;
			while(iterator.hasNext()) {
				debug++;
				long id = iterator.next();
				if(i++ == 0)
					nodeStringBuffer.append(id);
				else
					nodeStringBuffer.append(", " + id);
				
				if(i % 500 == 0) {	// query will throw exception if too long
					String nodeQuery = nodeStringBuffer.toString();
					addNodeByQuery(nodeQuery);
					i = 0;
					nodeStringBuffer = new StringBuffer();
				}
				
				if(debug % 1000 == 0)
					System.out.println((double)debug / nodeSet.size() * 100 + "% finish!");
			}
			if(!nodeStringBuffer.toString().equals("")) {	// process rest
				String nodeQuery = nodeStringBuffer.toString();
				addNodeByQuery(nodeQuery);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("fetchNodeBySet: debug code: " + debug);
		}
		System.out.println("fetch " + nodeSet.size() + " nodes by set finish!");
	}
	
	private static void readLinkFile() {
		System.out.println("read link file...");
		linkList = new LinkedList<RDFLinkInfo>();
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
				boolean ramp 			= nodes[7].equals("T") ? true : false;
				boolean tollway 		= nodes[8].equals("T") ? true : false;
				boolean carpool 		= nodes[9].equals("T") ? true : false;
				
				// gather node id
				if(!nodeSet.contains(refNodeId))
					nodeSet.add(refNodeId);
				if(!nodeSet.contains(nonRefNodeId))
					nodeSet.add(nonRefNodeId);
				
				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, streetName, refNodeId, nonRefNodeId, functionalClass, direction, ramp, tollway, carpool, speedCategory );
				
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
	
	private static void writeLinkDetail() {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo RDFLink = iterator.next();
				long linkId = RDFLink.getLinkId();
				String streetName = RDFLink.getStreetName();
				long refNodeId = RDFLink.getRefNodeId();
				long nonRefNodeId = RDFLink.getNonRefNodeId();
				int functionalClass = RDFLink.getFunctionalClass();
				String travelDirection = RDFLink.getTravelDirection();
				boolean ramp = RDFLink.isRamp();
				boolean tollway = RDFLink.isTollway();
				int speedCategory = RDFLink.getSpeedCategory();
				boolean carpool = RDFLink.isCarpool();
				
				String strLine = linkId + "|" + streetName + "|" + refNodeId + "|" + nonRefNodeId + "|" + functionalClass + "|" + travelDirection +"|" +
						speedCategory + "|" + (ramp ? "T" : "F") + "|" + (tollway ? "T" : "F") + "|" + (carpool ? "T" : "F") + "\r\n";
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
	 * @deprecated
	 */
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
				
				sql = 	"SELECT t8.link_id, street_name, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
						"FROM " +
						"(SELECT t6.link_id, road_name_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
						"FROM " +
						"(SELECT t4.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road " +
						"FROM " +
						"(SELECT t1.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category " +
						"FROM rdf_link t1, rdf_postal_area t2, rdf_nav_link t3 " +
						"WHERE t2.postal_code = '" + postCode + "' " +
						"AND (t2.postal_area_id = t1.left_postal_area_id OR t2.postal_area_id = t1.right_postal_area_id) " +
						"AND t1.link_id = t3.link_id) t4 " +
						"LEFT JOIN rdf_nav_link_attribute t5 " +
						"ON t4.link_id = t5.link_id) t6 " +
						"LEFT JOIN rdf_road_link t7 " +
						"ON t6.link_id = t7.link_id) t8 " +
						"LEFT JOIN rdf_road_name t9 " +
						"on t8.road_name_id = t9.road_name_id ";
				
				System.out.println("execute query... ");
				pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				res = pstatement.executeQuery();
				
				while (res.next()) {
					debug++;

					long linkId = res.getLong("link_id");
					
					if(linkMap.containsKey(linkId)) {
						String secondName = res.getString("street_name");
						RDFLinkInfo existLink = linkMap.get(linkId);
						existLink.addStreetName(secondName);
						continue;
					}
					
					String streetName = res.getString("street_name");
					long refNodeId = res.getLong("ref_node_id");
					long nonRefNodeId = res.getLong("nonref_node_id");
					int functionalClass = res.getInt("functional_class");
					String direction = res.getString("travel_direction");
					boolean ramp = res.getString("ramp").equals("Y") ? true : false;
					boolean tollway = res.getString("tollway").equals("Y") ? true : false;
					int speedCategory = res.getInt("speed_category");
					boolean carpool = res.getString("carpool_road") == null ? false : true;

					RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, streetName, refNodeId, nonRefNodeId, functionalClass, direction, ramp, tollway, carpool, speedCategory );

					linkList.add(RDFLink);
					linkMap.put(linkId, RDFLink);

					if (debug % 1000 == 0)
						System.out.println("record " + debug + " finish!");
					
				}

				res.close();
				pstatement.close();
				con.close();
				
				System.out.println("total " + (double)p / postCodeList.size() * 100 + "% finish!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkByPostCode: debug code: " + debug);
		}
		System.out.println("fetch " + linkList.size() + " links by post code finish!");
	}
	
	/**
	 * @deprecated
	 */
	private static void initialPostCode() {
		System.out.println("initial post code...");
		// add needed post code here, LA 90001 ~ 90084, 90086 ~ 90089, 90091, 90093 ~ 90097,
		//								 90099, 90101 ~ 90103, 90174, 90185, 90189, 91331, 91335
		//for(int p = 90001; p <= 90189; p++)
		//	postCodeList.add(p);
		//for(int p = 90086; p <= 90089; p++)
		//	postCodeList.add(p);
		//postCodeList.add(90091);
		//for(int p = 90093; p <= 90097; p++)
		//	postCodeList.add(p);
		//postCodeList.add(90099);
		//for(int p = 90101; p <= 90103; p++)
		//	postCodeList.add(p);
		//postCodeList.add(90174);
		//postCodeList.add(90185);
		//postCodeList.add(90189);
		//postCodeList.add(91331);
		//postCodeList.add(91335);
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
			LinkedList<LocationInfo> pointsList = null;

			while (res.next()) {

				long linkId = 	res.getLong("link_id");
				double lat = 	res.getDouble("lat") / UNIT;
				double lng = 	res.getDouble("lon") / UNIT;
				int zLevel = 	res.getInt("zlevel");
				LocationInfo location = new LocationInfo(lat, lng, zLevel);
				
				if(linkId != lastLinkId) {
					if(debug > 0)
						linkList.add(RDFLink);
					
					debug++;
					
					// data is too large to store all in memory
					if (debug % 10000 == 0) {
						// append write
						FileWriter fstream = new FileWriter(root + "/" + linkGeometryFile, true);
						BufferedWriter out = new BufferedWriter(fstream);
						
						ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
						while(iterator.hasNext()) {
							RDFLinkInfo writeRDFLink = iterator.next();
							
							long writeLinkId = writeRDFLink.getLinkId();
							pointsList = writeRDFLink.getPointsList();
							
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
					pointsList = new LinkedList<LocationInfo>();
					pointsList.add(location);
					RDFLink.setPointsList(pointsList);
					lastLinkId = linkId;
				}
				else {
					pointsList = RDFLink.getPointsList();
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
					pointsList = writeRDFLink.getPointsList();
					
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
	 * too slow :(
	 * @deprecated
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
					double lat = 	res.getDouble("lat") / UNIT;
					double lng = 	res.getDouble("lon") / UNIT;
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
					
					double lat = 	res.getDouble("lat") / UNIT;
					double lng = 	res.getDouble("lon") / UNIT;
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
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("fetchGeometry: debug code: " + debug);
		}
		System.out.println("fetch geometry finish!");
	}
	
	/**
	 * @deprecated
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
	 * @deprecated
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
				double lat = 	res.getDouble("lat") / UNIT;
				double lng = 	res.getDouble("lon") / UNIT;
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




