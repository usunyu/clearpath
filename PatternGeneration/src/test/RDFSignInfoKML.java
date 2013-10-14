package test;

import java.util.*;
import java.io.*;
import java.sql.*;

import objects.*;
import function.*;

public class RDFSignInfoKML {

	/**
	 * @param file
	 */
	static String root			= "file";
	static String linkSignKML		= "RDF_Link_Sign.kml";
	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
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
	 * @param sign
	 * <signId, signObject>
	 */
	static HashMap<Long, RDFSignInfo> signMap = new HashMap<Long, RDFSignInfo>();
	
	public static void main(String[] args) {
		RDFInput.readNodeFile(nodeMap);
		
		RDFInput.readLinkFile(linkMap, nodeMap);
		RDFInput.readLinkGeometry(linkMap);
		RDFInput.readLinkLane(linkMap);

		fetchSignOrigin();
		fetchSignDest();
		fetchSignElement();
		
		generateSignKML();
	}
	
	public static void generateSignKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkSignKML);
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
				boolean exitName	= link.isExitName();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				LinkedList<SensorInfo>	sensorList = link.getSensorList();
				
				LinkedList<RDFSignInfo>	signList = link.getSignList();
				
				if(signList == null) {
					continue;
				}
				
				String dirStr	= null;
				for(int dir : directionList) {
					if(dirStr == null)
						dirStr = String.valueOf(dir);
					else
						dirStr += SEPARATION + dir;
				}
				String signStr = "null";
				if(signList != null && signList.size() != 0) {
					int i = 0;
					for(RDFSignInfo sign : signList) {
						if(i++ == 0)
							signStr = String.valueOf(sign.getSignId());
						else
							signStr += SEPARATION + sign.getSignId();
					}
				}
				String kmlStr = "<Placemark><name>Sign:" + signStr + "</name>";
				kmlStr += "<description>";
				if(baseName.contains("&"))
					baseName = baseName.replaceAll("&", " and ");
				kmlStr += "Link:"			+ linkId + "\r\n";
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
				kmlStr += "Exit:" 			+ exitName + "\r\n";
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
				kmlStr += "</coordinates></LineString>";
				kmlStr += "<Style><LineStyle>";
				kmlStr += "<color>FFFF0000</color>";
				kmlStr += "<width>2</width>";
				kmlStr += "</LineStyle></Style></Placemark>\n";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}
	
	public static void fetchSignElement() {
		System.out.println("fetch sign element...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

			sql = "SELECT * FROM rdf_sign_element";
			
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;
				
				long signId				= res.getLong("sign_id");
				
				if(!signMap.containsKey(signId)) {
					continue;
				}

				int destNumber			= res.getInt("destination_number");
				int entryNumber			= res.getInt("entry_number");
				String entryType			= res.getString("entry_type");
				int textNumber			= res.getInt("text_number");
				String textType			= res.getString("text_type");
				String text				= res.getString("text");
				String directionCode		= res.getString("direction_code");
				
				RDFSignElemInfo signElem = new RDFSignElemInfo(destNumber, entryNumber, entryType, textNumber, textType, text, directionCode);
				
				RDFSignInfo sign = signMap.get(signId);
				sign.addSignElem(signElem);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchSignElement: debug code: " + debug);
		}
		System.out.println("fetch sign element...");
	}
	
	public static void fetchSignDest() {
		System.out.println("fetch sign dest...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

			sql = "SELECT * FROM rdf_sign_destination";
			
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;
				
				long signId = 			res.getLong("sign_id");
				long destLinkId = 		res.getLong("dest_link_id");
				
				if(!signMap.containsKey(signId) && !linkMap.containsKey(destLinkId)) {
					continue;
				}
				
				int destNumber =		res.getInt("destination_number");
				String exitNumber =		res.getString("exit_number");
				boolean straightOnSign =	res.getString("straight_on_sign").equals(YES) ? true : false;
				
				RDFSignInfo sign;
				if(signMap.containsKey(signId)) {
					sign = signMap.get(signId);
				}
				else {
					sign = new RDFSignInfo(signId);
					signMap.put(signId, sign);
				}
				
				RDFSignDestInfo signDest = new RDFSignDestInfo(destLinkId, destNumber, exitNumber, straightOnSign);
				
				sign.addSignDest(signDest);
				
				// add to link
				RDFLinkInfo link = linkMap.get(destLinkId);
				if(link != null)
					link.addSign(sign);
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchSignDest: debug code: " + debug);
		}
		System.out.println("fetch sign dest finish!");
	}
	
	public static void fetchSignOrigin() {
		System.out.println("fetch sign origin...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

			sql = "SELECT * FROM rdf_sign_origin";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				debug++;

				long signId = 		res.getLong("sign_id");
				long originLinkId =	res.getLong("originating_link_id");
				
				if(!linkMap.containsKey(originLinkId)) {
					continue;
				}
				
				RDFSignInfo sign = new RDFSignInfo(signId);
				sign.setOriginLinkId(originLinkId);
				
				signMap.put(signId, sign);
				
				// add to link
				RDFLinkInfo link = linkMap.get(originLinkId);
				link.addSign(sign);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("fetchSignOrigin: debug code: " + debug);
		}
		System.out.println("fetch sign origin finish!");
	}

}





