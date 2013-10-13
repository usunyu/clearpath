package test;

import java.util.*;
import java.sql.*;

import objects.*;
import function.*;

public class RDFSignInfoKML {

	/**
	 * @param file
	 */
	static String root			= "file";
	static String linkExitKML		= "RDF_Link_Sign.kml";
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
				
				long signId = 			res.getLong("sign_id");
				
				if(!signMap.containsKey(signId)) {
					continue;
				}
				
				String textType = 		res.getString("text_type");
				String text =			res.getString("text");
				String directionCode =	res.getString("direction_code");
				
				RDFSignInfo sign = signMap.get(signId);
				sign.setTextType(textType);
				sign.setText(text);
				sign.setDirectionCode(directionCode);
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
				
				if(!signMap.containsKey(signId)) {
					continue;
				}
				
				long destLinkId = 		res.getLong("dest_link_id");
				String exitNumber =		res.getString("exit_number");
				boolean straightOnSign =	res.getString("straight_on_sign").equals(YES) ? true : false;
				
				RDFSignInfo sign = signMap.get(signId);
				sign.setDestLinkid(destLinkId);
				sign.setExitNumber(exitNumber);
				sign.setStraightOnSign(straightOnSign);
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
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("fetchSignOrigin: debug code: " + debug);
		}
		System.out.println("fetch sign origin finish!");
	}

}





