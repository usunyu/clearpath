package test;
import java.io.*;
import java.sql.*;
import java.util.*;

import data.*;
import function.*;
import objects.*;

public class RDFCarpoolKMLGeneration {

	/**
	 * @param file
	 */
	static String root				= "file";
	static String kmlCarpoolFile		= "RDF_Link_Carpool.kml";
	static String carpoolFile			= "RDF_Carpool.csv";

	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
	static String UNKNOWN 		= "Unknown Street";
	static String YES				= "Y";
	static String NO				= "N";
	/**
	 * @param carpool
	 */
	static LinkedList<Long> carpoolList = new LinkedList<Long>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RDFInput.readNodeFile(RDFData.nodeMap);
		RDFInput.readLinkFile(RDFData.linkMap, RDFData.nodeMap);
		RDFInput.readLinkGeometry(RDFData.linkMap);
		RDFInput.readLinkLane(RDFData.linkMap);
		
		RDFInput.addManualCarpool();
		RDFInput.markManualCarpool(RDFData.linkMap);
		
		generateCarpoolKML(RDFData.linkMap);
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

			con = RDFDatabase.getConnection();

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
	
	private static void generateCarpoolKML(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("generate carpool kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlCarpoolFile);
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
		System.out.println("generate carpool kml finish!");
	}
}
