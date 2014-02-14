package function;

import java.io.*;
import java.sql.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import data.*;
import objects.*;

public class RDFInput {
	/**
	 * @param file
	 */
	static String root 				= "file";
	// for read link file
	static String linkFile			= "RDF_Link.csv";
	static String linkGeometryFile	= "RDF_Link_Geometry.csv";
	static String linkLaneFile		= "RDF_Link_Lane.csv";
	static String linkNameFile		= "RDF_Link_Name.csv";
	// for read node file
	static String nodeFile			= "RDF_Node.csv";
	// for read sensor file
	static String sensorMatchFile	= "RDF_Sensor_Match.csv";
	/**
	 * @param args
	 */
	static double UNIT				= 100000;
	static double LosAngelesLat1 	= 33.68   * UNIT;
	static double LosAngelesLat2 	= 34.27   * UNIT;
	//static double LosAngelesLon1 	= -118.92 * UNIT;
	//static double LosAngelesLon2 	= -117.55 * UNIT;
	static double LosAngelesLon1 	= -118.42 * UNIT;
	static double LosAngelesLon2 	= -117.95 * UNIT;
	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
	static String UNKNOWN 		= "Unknown Street";
	static String YES				= "Y";
	static String NO				= "N";
	static String TO_REF			= "T";
	static String FROM_REF		= "F";
	static String BIDIREC			= "B";
	
	/**
	 * build graph connection : node to link list
	 * @param linkMap
	 * @param nodeToLinkMap
	 */
	public static void buildNodeToLinkMap(HashMap<Long, RDFLinkInfo> linkMap, HashMap<String, RDFLinkInfo> nodeToLinkMap) {
		System.out.println("build node to link map...");
		for(RDFLinkInfo link : linkMap.values()) {
			long refNode = link.getRefNodeId();
			long nonRefNode = link.getNonRefNodeId();
			String travelDir = link.getTravelDirection();
			
			// add nodeToLink
			if(travelDir.equals(TO_REF)) {
				nodeToLinkMap.put(nonRefNode + SEPARATION + refNode, link);
			}
			else if(travelDir.equals(FROM_REF)) {
				nodeToLinkMap.put(refNode + SEPARATION + nonRefNode, link);
			}
			else if(travelDir.equals(BIDIREC)) {
				nodeToLinkMap.put(nonRefNode + SEPARATION + refNode, link);
				nodeToLinkMap.put(refNode + SEPARATION + nonRefNode, link);
			}
			else {
				System.err.println("undefine travel direction!");
			}
		}
		System.out.println("build node to link map finish!");
	}
	
	/**
	 * build graph connection : node adjcent list
	 * @param linkMap
	 * @param nodeAdjList
	 */
	public static void buildNodeAdjList(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, LinkedList<Long>> nodeAdjList) {
		System.out.println("build node adjcent list...");
		for(RDFLinkInfo link : linkMap.values()) {
			long refNode = link.getRefNodeId();
			long nonRefNode = link.getNonRefNodeId();
			String travelDir = link.getTravelDirection();
			if(travelDir.equals(TO_REF)) {	//from nonref to ref
				if(!nodeAdjList.containsKey(nonRefNode)) {
					LinkedList<Long> toList = new LinkedList<Long>();
					toList.add(refNode);
					nodeAdjList.put(nonRefNode, toList);
				}
				else {
					LinkedList<Long> toList = nodeAdjList.get(nonRefNode);
					toList.add(refNode);
				}
			}
			else if(travelDir.equals(FROM_REF)) { //from ref to nonref
				if(!nodeAdjList.containsKey(refNode)) {
					LinkedList<Long> toList = new LinkedList<Long>();
					toList.add(nonRefNode);
					nodeAdjList.put(refNode, toList);
				}
				else {
					LinkedList<Long> toList = nodeAdjList.get(refNode);
					toList.add(nonRefNode);
				}
			}
			else if(travelDir.equals(BIDIREC)) { // bi-direction
				if(!nodeAdjList.containsKey(nonRefNode)) {
					LinkedList<Long> toList = new LinkedList<Long>();
					toList.add(refNode);
					nodeAdjList.put(nonRefNode, toList);
				}
				else {
					LinkedList<Long> toList = nodeAdjList.get(nonRefNode);
					toList.add(refNode);
				}
				if(!nodeAdjList.containsKey(refNode)) {
					LinkedList<Long> toList = new LinkedList<Long>();
					toList.add(nonRefNode);
					nodeAdjList.put(refNode, toList);
				}
				else {
					LinkedList<Long> toList = nodeAdjList.get(refNode);
					toList.add(nonRefNode);
				}
			}
			else {
				System.err.println("undefine travel direction!");
			}
			
		}
		System.out.println("build node adjcent list finish!");
	}
	
	/**
	 * manual write carpool which not contained in database
	 */
	public static void addManualCarpool() {
		RDFData.carpoolManualSet.add(859176689l);
		RDFData.carpoolManualSet.add(859176688l);
		RDFData.carpoolManualSet.add(858795235l);
		RDFData.carpoolManualSet.add(858795234l);
		RDFData.carpoolManualSet.add(110161947l);
		RDFData.carpoolManualSet.add(939442621l);
		RDFData.carpoolManualSet.add(939442620l);
		RDFData.carpoolManualSet.add(783652297l);
		RDFData.carpoolManualSet.add(783652296l);
		RDFData.carpoolManualSet.add(28432663l);
		RDFData.carpoolManualSet.add(733916910l);
		RDFData.carpoolManualSet.add(782774872l);
		RDFData.carpoolManualSet.add(782774871l);
		RDFData.carpoolManualSet.add(776739442l);
		RDFData.carpoolManualSet.add(932209462l);
		RDFData.carpoolManualSet.add(932209461l);
		RDFData.carpoolManualSet.add(37825166l);
		RDFData.carpoolManualSet.add(859175347l);
		RDFData.carpoolManualSet.add(859175346l);
		RDFData.carpoolManualSet.add(28432674l);
		RDFData.carpoolManualSet.add(24612549l);
		RDFData.carpoolManualSet.add(857627783l);
		RDFData.carpoolManualSet.add(857627782l);
		RDFData.carpoolManualSet.add(110160325l);
		RDFData.carpoolManualSet.add(121237589l);
		RDFData.carpoolManualSet.add(121234936l);
		RDFData.carpoolManualSet.add(24612504l);
		RDFData.carpoolManualSet.add(28432725l);
		RDFData.carpoolManualSet.add(766693715l);
		RDFData.carpoolManualSet.add(766693714l);
		RDFData.carpoolManualSet.add(28432747l);
		RDFData.carpoolManualSet.add(28432746l);
		RDFData.carpoolManualSet.add(121238550l);
		RDFData.carpoolManualSet.add(932222662l);
		RDFData.carpoolManualSet.add(932222661l);
		RDFData.carpoolManualSet.add(928343450l);
		RDFData.carpoolManualSet.add(23928234l);
		RDFData.carpoolManualSet.add(121238464l);
		RDFData.carpoolManualSet.add(121238462l);
		RDFData.carpoolManualSet.add(121234999l);
		RDFData.carpoolManualSet.add(121238440l);
		RDFData.carpoolManualSet.add(110162068l);
		RDFData.carpoolManualSet.add(121240472l);
		RDFData.carpoolManualSet.add(859174498l);
		RDFData.carpoolManualSet.add(859174497l);
		RDFData.carpoolManualSet.add(121241061l);
		RDFData.carpoolManualSet.add(932209460l);
		RDFData.carpoolManualSet.add(932209459l);
		RDFData.carpoolManualSet.add(121235021l);
		RDFData.carpoolManualSet.add(121235045l);
		RDFData.carpoolManualSet.add(121235048l);
		RDFData.carpoolManualSet.add(121235036l);
		RDFData.carpoolManualSet.add(121235024l);
		RDFData.carpoolManualSet.add(121233715l);
		RDFData.carpoolManualSet.add(121241020l);
		RDFData.carpoolManualSet.add(121241019l);
		RDFData.carpoolManualSet.add(810665747l);
		RDFData.carpoolManualSet.add(810665746l);
		RDFData.carpoolManualSet.add(783167929l);
		RDFData.carpoolManualSet.add(783167928l);
		RDFData.carpoolManualSet.add(783167927l);
		RDFData.carpoolManualSet.add(24559194l);
		RDFData.carpoolManualSet.add(121240993l);
		RDFData.carpoolManualSet.add(932219289l);
		RDFData.carpoolManualSet.add(932219288l);
		RDFData.carpoolManualSet.add(810665759l);
		RDFData.carpoolManualSet.add(810665758l);
		RDFData.carpoolManualSet.add(810862652l);
		RDFData.carpoolManualSet.add(810862651l);
		RDFData.carpoolManualSet.add(812029169l);
		RDFData.carpoolManualSet.add(833175656l);
		RDFData.carpoolManualSet.add(833175655l);
		RDFData.carpoolManualSet.add(28433679l);
		RDFData.carpoolManualSet.add(37825151l);
		RDFData.carpoolManualSet.add(812029177l);
		RDFData.carpoolManualSet.add(833250425l);
		RDFData.carpoolManualSet.add(833250424l);
		RDFData.carpoolManualSet.add(857627771l);
		RDFData.carpoolManualSet.add(857627770l);
		RDFData.carpoolManualSet.add(782793132l);
		RDFData.carpoolManualSet.add(28433683l);
		RDFData.carpoolManualSet.add(857627769l);
		RDFData.carpoolManualSet.add(857627768l);
		RDFData.carpoolManualSet.add(721218884l);
		RDFData.carpoolManualSet.add(721218883l);
		RDFData.carpoolManualSet.add(28434272l);
		RDFData.carpoolManualSet.add(28434273l);
		RDFData.carpoolManualSet.add(857765113l);
		RDFData.carpoolManualSet.add(936892455l);
		RDFData.carpoolManualSet.add(936892454l);
		RDFData.carpoolManualSet.add(121240556l);
		RDFData.carpoolManualSet.add(24159548l);
		RDFData.carpoolManualSet.add(28433700l);
		RDFData.carpoolManualSet.add(28433701l);
		RDFData.carpoolManualSet.add(932219309l);
		RDFData.carpoolManualSet.add(932219308l);
		RDFData.carpoolManualSet.add(782793146l);
		RDFData.carpoolManualSet.add(23927685l);
		RDFData.carpoolManualSet.add(121233667l);
		RDFData.carpoolManualSet.add(121233668l);
		RDFData.carpoolManualSet.add(121233635l);
		RDFData.carpoolManualSet.add(121234214l);
		RDFData.carpoolManualSet.add(121234215l);
		RDFData.carpoolManualSet.add(28433706l);
		RDFData.carpoolManualSet.add(931101801l);
		RDFData.carpoolManualSet.add(931101800l);
		RDFData.carpoolManualSet.add(28433708l);
		RDFData.carpoolManualSet.add(126510246l);
		RDFData.carpoolManualSet.add(707425893l);
		RDFData.carpoolManualSet.add(871479750l);
		RDFData.carpoolManualSet.add(871479749l);
		RDFData.carpoolManualSet.add(121234212l);
		RDFData.carpoolManualSet.add(121234211l);
		RDFData.carpoolManualSet.add(126510430l);
		RDFData.carpoolManualSet.add(121234227l);
		RDFData.carpoolManualSet.add(128810192l);
		RDFData.carpoolManualSet.add(128810191l);
		RDFData.carpoolManualSet.add(706846087l);
		RDFData.carpoolManualSet.add(943507091l);
		RDFData.carpoolManualSet.add(943507090l);
		RDFData.carpoolManualSet.add(782730636l);
		RDFData.carpoolManualSet.add(128810198l);
		RDFData.carpoolManualSet.add(128810200l);
		RDFData.carpoolManualSet.add(128810199l);
		RDFData.carpoolManualSet.add(126510842l);
		RDFData.carpoolManualSet.add(943510086l);
		RDFData.carpoolManualSet.add(943510085l);
		
		RDFData.carpoolManualSet.add(128785472l);
		RDFData.carpoolManualSet.add(24612577l);
		RDFData.carpoolManualSet.add(23927675l);
		RDFData.carpoolManualSet.add(943506427l);
		RDFData.carpoolManualSet.add(943506428l);
		RDFData.carpoolManualSet.add(943506429l);
		RDFData.carpoolManualSet.add(943506430l);
		RDFData.carpoolManualSet.add(121234203l);
		RDFData.carpoolManualSet.add(921307669l);
		RDFData.carpoolManualSet.add(921307670l);
		RDFData.carpoolManualSet.add(947343920l);
		RDFData.carpoolManualSet.add(947343921l);
		RDFData.carpoolManualSet.add(924939367l);
		RDFData.carpoolManualSet.add(871479747l);
		RDFData.carpoolManualSet.add(121234208l);
		RDFData.carpoolManualSet.add(128798610l);
		RDFData.carpoolManualSet.add(717341681l);
		RDFData.carpoolManualSet.add(717341682l);
		RDFData.carpoolManualSet.add(875087857l);
		RDFData.carpoolManualSet.add(875087858l);
		RDFData.carpoolManualSet.add(121234220l);
		RDFData.carpoolManualSet.add(28433705l);
		RDFData.carpoolManualSet.add(121234217l);
		RDFData.carpoolManualSet.add(121234216l);
		RDFData.carpoolManualSet.add(721106790l);
		RDFData.carpoolManualSet.add(721106791l);
		RDFData.carpoolManualSet.add(121233848l);
		RDFData.carpoolManualSet.add(121233847l);
		RDFData.carpoolManualSet.add(782793140l);
		RDFData.carpoolManualSet.add(782793141l);
		RDFData.carpoolManualSet.add(24154620l);
		RDFData.carpoolManualSet.add(28433696l);
		RDFData.carpoolManualSet.add(28433697l);
		RDFData.carpoolManualSet.add(121240539l);
		RDFData.carpoolManualSet.add(755956799l);
		RDFData.carpoolManualSet.add(755956800l);
		RDFData.carpoolManualSet.add(755956798l);
		RDFData.carpoolManualSet.add(28433694l);
		RDFData.carpoolManualSet.add(981741513l);
		RDFData.carpoolManualSet.add(981741514l);
		RDFData.carpoolManualSet.add(857627761l);
		RDFData.carpoolManualSet.add(28434279l);
		RDFData.carpoolManualSet.add(28434278l);
		RDFData.carpoolManualSet.add(28484167l);
		RDFData.carpoolManualSet.add(128789071l);
		RDFData.carpoolManualSet.add(24159779l);
		RDFData.carpoolManualSet.add(782793126l);
		RDFData.carpoolManualSet.add(857627776l);
		RDFData.carpoolManualSet.add(857627777l);
		RDFData.carpoolManualSet.add(859172142l);
		RDFData.carpoolManualSet.add(954895444l);
		RDFData.carpoolManualSet.add(954895445l);
		RDFData.carpoolManualSet.add(28433677l);
		RDFData.carpoolManualSet.add(859172381l);
		RDFData.carpoolManualSet.add(859174485l);
		RDFData.carpoolManualSet.add(943679822l);
		RDFData.carpoolManualSet.add(943679823l);
		RDFData.carpoolManualSet.add(811173858l);
		RDFData.carpoolManualSet.add(810864760l);
		RDFData.carpoolManualSet.add(810665753l);
		RDFData.carpoolManualSet.add(756632328l);
		RDFData.carpoolManualSet.add(756632329l);
		RDFData.carpoolManualSet.add(121240990l);
		RDFData.carpoolManualSet.add(110162092l);
		RDFData.carpoolManualSet.add(857627780l);
		RDFData.carpoolManualSet.add(857627781l);
		RDFData.carpoolManualSet.add(859174491l);
		RDFData.carpoolManualSet.add(859174492l);
		RDFData.carpoolManualSet.add(810665744l);
		RDFData.carpoolManualSet.add(810665745l);
		RDFData.carpoolManualSet.add(121241006l);
		RDFData.carpoolManualSet.add(121241005l);
		RDFData.carpoolManualSet.add(967817625l);
		RDFData.carpoolManualSet.add(967817626l);
		RDFData.carpoolManualSet.add(121235037l);
		RDFData.carpoolManualSet.add(121235038l);
		RDFData.carpoolManualSet.add(121235022l);
		RDFData.carpoolManualSet.add(37825153l);
		RDFData.carpoolManualSet.add(954816377l);
		RDFData.carpoolManualSet.add(954816378l);
		RDFData.carpoolManualSet.add(857627786l);
		RDFData.carpoolManualSet.add(857627787l);
		RDFData.carpoolManualSet.add(857710924l);
		RDFData.carpoolManualSet.add(857710925l);
		RDFData.carpoolManualSet.add(128798417l);
		RDFData.carpoolManualSet.add(859186072l);
		RDFData.carpoolManualSet.add(859186073l);
		RDFData.carpoolManualSet.add(110162060l);
		RDFData.carpoolManualSet.add(121235000l);
		RDFData.carpoolManualSet.add(859186077l);
		RDFData.carpoolManualSet.add(859186078l);
		RDFData.carpoolManualSet.add(859186076l);
		RDFData.carpoolManualSet.add(859186062l);
		RDFData.carpoolManualSet.add(859186063l);
		RDFData.carpoolManualSet.add(23928235l);
		RDFData.carpoolManualSet.add(859186060l);
		RDFData.carpoolManualSet.add(859186061l);
		RDFData.carpoolManualSet.add(859186057l);
		RDFData.carpoolManualSet.add(859186054l);
		RDFData.carpoolManualSet.add(121238552l);
		RDFData.carpoolManualSet.add(857627774l);
		RDFData.carpoolManualSet.add(857627775l);
		RDFData.carpoolManualSet.add(121238631l);
		RDFData.carpoolManualSet.add(121238632l);
		RDFData.carpoolManualSet.add(28432694l);
		RDFData.carpoolManualSet.add(780472583l);
		RDFData.carpoolManualSet.add(780472584l);
		RDFData.carpoolManualSet.add(110160271l);
		RDFData.carpoolManualSet.add(110160270l);
		RDFData.carpoolManualSet.add(121238590l);
		RDFData.carpoolManualSet.add(28432676l);
		RDFData.carpoolManualSet.add(28432677l);
		RDFData.carpoolManualSet.add(121238595l);
		RDFData.carpoolManualSet.add(121237576l);
		RDFData.carpoolManualSet.add(37825165l);
		RDFData.carpoolManualSet.add(37825160l);
		RDFData.carpoolManualSet.add(783086689l);
		RDFData.carpoolManualSet.add(783086690l);
		RDFData.carpoolManualSet.add(782774874l);
		RDFData.carpoolManualSet.add(733916908l);
		RDFData.carpoolManualSet.add(781854669l);
		RDFData.carpoolManualSet.add(788181078l);
		RDFData.carpoolManualSet.add(788181079l);
		RDFData.carpoolManualSet.add(23842747l);
		RDFData.carpoolManualSet.add(110161949l);
		RDFData.carpoolManualSet.add(940006088l);
		RDFData.carpoolManualSet.add(940006089l);
	}
	
	/**
	 * mark carpool for link
	 * @param linkMap
	 */
	public static void markManualCarpool(HashMap<Long, RDFLinkInfo> linkMap) {
		for(RDFLinkInfo link : linkMap.values()) {
			long linkId = link.getLinkId();
			if(RDFData.carpoolManualSet.contains(linkId)) {
				link.setManualCarpool();
			}
		}
	}
	
	/**
	 * fetch sign element info
	 * @param signMap
	 */
	public static void fetchSignElement(HashMap<Long, RDFSignInfo> signMap) {
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
	
	/**
	 * fetch sign dest info
	 * @param linkMap
	 * @param signMap
	 */
	public static void fetchSignDest(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFSignInfo> signMap) {
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
	
	/**
	 * fetch sign origin info
	 * @param linkMap
	 * @param signMap
	 */
	public static void fetchSignOrigin(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFSignInfo> signMap) {
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
	
	/**
	 * read match sensor, override RDFData.sensorMap
	 * @param linkMap
	 * @param sensorMap
	 */
	public static void readMatchSensor(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Integer, SensorInfo> sensorMap) {
		System.out.println("read match sensor...");
		int debug = 0;
		HashMap<Integer, SensorInfo> matchSensorMap = new HashMap<Integer, SensorInfo>();
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + sensorMatchFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				
				long 	linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo link 	= linkMap.get(linkId);
				
				for(int i = 1; i < nodes.length; i++) {
					int sensorId = Integer.parseInt(nodes[i]);
					SensorInfo sensor 	= sensorMap.get(sensorId);
					link.addSensor(sensor);
					
					if(!matchSensorMap.containsKey(sensorId))
						matchSensorMap.put(sensorId, sensor);
				}

				if (debug % 1000 == 0)
					System.out.println("record " + debug + " finish!");
			}
			// override
			RDFData.sensorMatchMap = matchSensorMap;
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readMatchSensor: debug code: " + debug);
		}
		System.out.println("read match sensor finish!");
	}
	
	/**
	 * fetch sensor
	 * @param sensorMap
	 */
	public static void fetchSensor(HashMap<Integer, SensorInfo> sensorMap) {
		System.out.println("fetch sensor...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = Database.getConnection();
			sql = "SELECT link_id, onstreet, fromstreet, start_lat_long, direction FROM highway_congestion_config ";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt("link_id");
				String onStreet = res.getString("onstreet");
				String fromStreet = res.getString("fromstreet");
				STRUCT st = (STRUCT) res.getObject("start_lat_long");
				JGeometry geom = JGeometry.load(st);
				LocationInfo location = new LocationInfo(geom.getPoint()[1], geom.getPoint()[0], 0);
				int direction = res.getInt(5);
				
				if(!sensorMap.containsKey(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, location, direction);
					sensorMap.put(sensorId, sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch sensor finish!");
	}
	
	/**
	 * fetch sensor
	 * @param sensorList
	 */
	public static void fetchSensor(LinkedList<SensorInfo> sensorList) {
		System.out.println("fetch sensor...");
		try {
			HashSet<Integer> sensorDuplicate = new HashSet<Integer>();
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = Database.getConnection();
			sql = "SELECT link_id, onstreet, fromstreet, start_lat_long, direction FROM highway_congestion_config ";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt("link_id");
				String onStreet = res.getString("onstreet");
				String fromStreet = res.getString("fromstreet");
				STRUCT st = (STRUCT) res.getObject("start_lat_long");
				JGeometry geom = JGeometry.load(st);
				LocationInfo location = new LocationInfo(geom.getPoint()[1], geom.getPoint()[0], 0);
				int direction = res.getInt("direction");
				if (!sensorDuplicate.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, location, direction);
					sensorDuplicate.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch sensor finish!");
	}
	
	/**
	 * read link street name
	 * @param linkMap
	 */
	public static void readLinkName(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("read link name...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkNameFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				long linkId 		= Long.parseLong(nodes[0]);
				RDFLinkInfo	link	= linkMap.get(linkId);
				for(int i = 1; i < nodes.length; i ++) {
					String streetName = nodes[i];
					link.addStreetName(streetName, true);
				}
			}
			br.close();
			in.close();
			fstream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("readLinkName: debug code: " + debug);
		}
		System.out.println("read link name finish!");
	}
	
	/**
	 * read link lane info
	 * @param linkMap
	 */
	public static void readLinkLane(HashMap<Long, RDFLinkInfo> linkMap) {
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
	
	/**
	 * read link geometry points
	 * @param linkMap
	 */
	public static void readLinkGeometry(HashMap<Long, RDFLinkInfo> linkMap) {
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
	
	private static RDFLinkInfo getLinkFromLine(String strLine, HashMap<Long, RDFNodeInfo> nodeMap) {
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
		boolean exitName		= nodes[10].equals(YES) ? true : false;
		
		RDFLinkInfo link = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
		
		link.setBaseName(baseName);
		link.setAccessId(accessId);
		link.setFunctionalClass(functionalClass);
		link.setSpeedCategory(speedCategory);
		link.setTravelDirection(travelDirection);
		link.setRamp(ramp);
		link.setTollway(tollway);
		link.setExitName(exitName);
		
		// add direction
		RDFNodeInfo refNode = nodeMap.get(refNodeId);
		RDFNodeInfo nonRefNode = nodeMap.get(nonRefNodeId);
		if(travelDirection.equals("T")) {
			int direction = Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation());
			link.addDirection(direction);
		}
		else if(travelDirection.equals("F")) {
			int direction = Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation());
			link.addDirection(direction);
		}
		else if(travelDirection.equals("B")) {
			int direction = Geometry.getDirection(nonRefNode.getLocation(), refNode.getLocation());
			link.addDirection(direction);
			direction = Geometry.getDirection(refNode.getLocation(), nonRefNode.getLocation());
			link.addDirection(direction);
		}
		
		return link;
	}
	
	/**
	 * read link file
	 * @param linkMap
	 * @param nodeMap
	 */
	public static void readLinkFile(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				
				RDFLinkInfo link 		= getLinkFromLine(strLine, nodeMap);
				long	linkId			= link.getLinkId();
				
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
	
	/**
	 * read link file
	 * @param linkMap
	 * @param nodeMap
	 * @param nodeToLink
	 */
	public static void readLinkFile(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFNodeInfo> nodeMap, HashMap<String, RDFLinkInfo> nodeToLink) {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				
				RDFLinkInfo link 		= getLinkFromLine(strLine, nodeMap);
				
				long	linkId			= link.getLinkId();
				long 	refNodeId 		= link.getRefNodeId();
				long 	nonRefNodeId 	= link.getNonRefNodeId();
				String	travelDirection = link.getTravelDirection();
				
				// add nodeToLink
				if(travelDirection.equals("T")) {
					nodeToLink.put(nonRefNodeId + SEPARATION + refNodeId, link);
				}
				else if(travelDirection.equals("F")) {
					nodeToLink.put(refNodeId + SEPARATION + nonRefNodeId, link);
				}
				else if(travelDirection.equals("B")) {
					nodeToLink.put(nonRefNodeId + SEPARATION + refNodeId, link);
					nodeToLink.put(refNodeId + SEPARATION + nonRefNodeId, link);
				}
				
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
	
	/**
	 * read link file
	 * @param linkMap
	 * @param nodeMap
	 * @param adjNodeList from one node, find the near nodes
	 * @param nodeToLink from two connected node, find the link
	 */
	public static void readLinkFile(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFNodeInfo> nodeMap, HashMap<Long, LinkedList<Long>> adjNodeList, HashMap<String, RDFLinkInfo> nodeToLink) {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				RDFLinkInfo link 		= getLinkFromLine(strLine, nodeMap);
				long	linkId			= link.getLinkId();
				long 	refNodeId 		= link.getRefNodeId();
				long 	nonRefNodeId 	= link.getNonRefNodeId();
				String	travelDirection = link.getTravelDirection();
				
				linkMap.put(linkId, link);
				
				// add nodeToLink
				if(travelDirection.equals("T")) {
					nodeToLink.put(nonRefNodeId + SEPARATION + refNodeId, link);
				}
				else if(travelDirection.equals("F")) {
					nodeToLink.put(refNodeId + SEPARATION + nonRefNodeId, link);
				}
				else if(travelDirection.equals("B")) {
					nodeToLink.put(nonRefNodeId + SEPARATION + refNodeId, link);
					nodeToLink.put(refNodeId + SEPARATION + nonRefNodeId, link);
				}
				
				// add adjNodeList
				if (adjNodeList.containsKey(refNodeId)) {
					LinkedList<Long> tempList = adjNodeList.get(refNodeId);
					if (!tempList.contains(nonRefNodeId))
						tempList.add(nonRefNodeId);
				} else {
					LinkedList<Long> newList = new LinkedList<Long>();
					newList.add(nonRefNodeId);
					adjNodeList.put(refNodeId, newList);
				}

				if (adjNodeList.containsKey(nonRefNodeId)) {
					LinkedList<Long> tempList = adjNodeList.get(nonRefNodeId);
					if (!tempList.contains(refNodeId))
						tempList.add(refNodeId);
				} else {
					LinkedList<Long> newList = new LinkedList<Long>();
					newList.add(refNodeId);
					adjNodeList.put(nonRefNodeId, newList);
				}
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
	
	/**
	 * read node file
	 * @param nodeMap
	 */
	public static void readNodeFile(HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(SEPARATION);
				
				long 			nodeId 	= Long.parseLong(nodes[0]);
				double			lat		= Double.parseDouble(nodes[1]);
				double			lon		= Double.parseDouble(nodes[2]);
				int 			zLevel	= Integer.parseInt(nodes[3]);
				
				LocationInfo 	location= new LocationInfo(lat, lon, zLevel);
				
				RDFNodeInfo node = new RDFNodeInfo(nodeId, location);
				
				nodeMap.put(nodeId, node);
			}
			br.close();
			in.close();
			fstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("readNodeFile: debug code: " + debug);
		}
		System.out.println("read node file finish!");
	}
	
	/**
	 * fetch linkId and nodeId by area(lat, lon)
	 * @param linkMap
	 * @param nodeMap
	 */
	public static void fetchLinkNodeIdByArea(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("fetch link by area...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
			con.setAutoCommit(false);
			
			sql = 	"SELECT link_id, ref_node_id, nonref_node_id " +
					"FROM rdf_link, rdf_node " +
					"WHERE ref_node_id = node_id " +
					"AND lat >= " + LosAngelesLat1 + " AND lat <= " + LosAngelesLat2 + " " +
					"AND lon >= " + LosAngelesLon1 + " AND lon <= " + LosAngelesLon2 + " ";
			
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
				if(!linkMap.containsKey(linkId)) {
					RDFLinkInfo link = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);
					linkMap.put(linkId, link);
				}
				if(!nodeMap.containsKey(refNodeId)) {
					RDFNodeInfo node = new RDFNodeInfo(refNodeId);
					nodeMap.put(refNodeId, node);
				}
				if(!nodeMap.containsKey(nonRefNodeId)) {
					RDFNodeInfo node = new RDFNodeInfo(nonRefNodeId);
					nodeMap.put(nonRefNodeId, node);
				}
				if(debug % 10000 == 0) {
					System.out.println("processed " + debug + " records.");
				}
			}
			
			res.close();
			pstatement.close();
			con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkByArea: debug code: " + debug);
		}
		System.out.println("fetched " + linkMap.size() + " links and " + nodeMap.size() + " nodes!");
	}
	
	/**
	 * fetch node lat, lon and zlevel
	 * @param nodeMap
	 */
	public static void fetchNodeInfoById(HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("fetch node info by id...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
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
	
	/**
	 * fetch link info
	 * @param linkMap
	 */
	public static void fetchLinkInfoById(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("fetch link info by id...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
			con.setAutoCommit(false);
			
			sql = "SELECT link_id, access_id, functional_class, travel_direction, ramp, tollway, speed_category " +
					"FROM rdf_nav_link";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, 
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.FETCH_FORWARD);
			pstatement.setFetchSize(1000);
			res = pstatement.executeQuery();
			
			while (res.next()) {
				long linkId 			= res.getLong("link_id");
				
				if(!linkMap.containsKey(linkId))
					continue;
				
				debug++;
				
				RDFLinkInfo link = linkMap.get(linkId);
				
				int accessId			= res.getInt("access_id");
				int functionalClass		= res.getInt("functional_class");
				String travelDirection	= res.getString("travel_direction");
				boolean ramp			= res.getString("ramp").equals(YES) ? true : false;
				boolean tollway			= res.getString("tollway").equals(YES) ? true : false;
				int speedCategory		= res.getInt("speed_category");
				
				link.setAccessId(accessId);
				link.setFunctionalClass(functionalClass);
				link.setTravelDirection(travelDirection);
				link.setRamp(ramp);
				link.setTollway(tollway);
				link.setSpeedCategory(speedCategory);
				
				if(debug % 10000 == 0)
					System.out.println("processed " + (double) debug / linkMap.size() * 100 + "%.");
			}
			
			res.close();
			pstatement.close();
			con.close();
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkInfoById: debug code: " + debug);
		}
		System.out.println("fetch link info by id finish!");
	}
	
	/**
	 * eliminate non-nav link
	 * @param linkMap
	 */
	public static void eliminateNonNavLink(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("eliminate non nav link...");
		int debug = 0;
		try {
			LinkedList<Long> removeList = new LinkedList<Long>();
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				if(link.getAccessId() == 0) {
					//linkMap.remove(linkId);
					removeList.add(linkId);
				}
			}
			ListIterator<Long> iterator = removeList.listIterator();
			while(iterator.hasNext()) {
				long removeId = iterator.next();
				linkMap.remove(removeId);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("eliminateNonNavLink: debug code: " + debug);
		}
		System.out.println("eliminate non nav link finish!");
	}
	
	
	public static void eliminateUnrelatedNode(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("eliminate unrelated node...");
		int debug = 0;
		try {
			HashSet<Long> neededNodeId = new HashSet<Long>();
			for(long linkId : linkMap.keySet()) {
				RDFLinkInfo link = linkMap.get(linkId);
				long refNodeId = link.getRefNodeId();
				long nonRefNodeId = link.getNonRefNodeId();
				if(!neededNodeId.contains(refNodeId))
					neededNodeId.add(refNodeId);
				if(!neededNodeId.contains(nonRefNodeId))
					neededNodeId.add(nonRefNodeId);
			}
			LinkedList<Long> removeList = new LinkedList<Long>();
			for(long nodeId : nodeMap.keySet()) {
				if(!neededNodeId.contains(nodeId))
					removeList.add(nodeId);
			}
			for(long nodeId : removeList) {
				nodeMap.remove(nodeId);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("eliminateUnrelatedNode: debug code: " + debug);
		}
		System.out.println("eliminate unrelated node finish!");
	}
	
	/**
	 * fetch link road name
	 * @param linkMap
	 */
	public static void fetchLinkRoadById(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("fetch link name by id...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
			con.setAutoCommit(false);
			
			sql = "SELECT link_id, base_name, street_name, is_name_on_roadsign, is_exit_name " +
					"FROM rdf_road_link t1, rdf_road_name t2 WHERE t1.road_name_id = t2.road_name_id";

			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, 
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.FETCH_FORWARD);
			pstatement.setFetchSize(1000);
			
			res = pstatement.executeQuery();
			while (res.next()) {
				long linkId 				= res.getLong("link_id");
				
				if(!linkMap.containsKey(linkId))
					continue;
				
				debug++;
				
				RDFLinkInfo link = linkMap.get(linkId);
				
				String baseName				= res.getString("base_name");
				String streetName			= res.getString("street_name");
				boolean isNameOnRoadsign	= res.getString("is_name_on_roadsign").equals(YES) ? true : false;
				
				boolean isExitName			= res.getString("is_exit_name").equals(YES) ? true : false;
				
				if(isNameOnRoadsign) {
					link.setBaseName(baseName);
				}
				else {
					if(link.getBaseName() == null) {
						link.setBaseName(baseName);
					}
				}
				link.addStreetName(streetName, isNameOnRoadsign);
				link.setExitName(isExitName);
				
				if(debug % 10000 == 0)
					System.out.println("processed " + (double) debug / linkMap.size() * 100 + "%.");
			}
			
			res.close();
			pstatement.close();
			con.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkNameById: debug code: " + debug);
		}
		System.out.println("fetch link name by id finish!");
	}
	
	/**
	 * fetch link geometry points
	 * @param linkMap
	 */
	public static void fetchLinkGeometryById(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("fetch link geometry by id...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
			con.setAutoCommit(false);
			
			sql = "SELECT link_id, lat, lon, zlevel FROM rdf_link_geometry ORDER BY link_id, seq_num";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, 
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.FETCH_FORWARD);
			pstatement.setFetchSize(1000);
			
			res = pstatement.executeQuery();
			while (res.next()) {
				long linkId 	= res.getLong("link_id");
				
				if(!linkMap.containsKey(linkId))
					continue;
				
				debug++;
				
				RDFLinkInfo link = linkMap.get(linkId);
				
				double 	lat 	= res.getDouble("lat") / UNIT;
				double 	lon 	= res.getDouble("lon") / UNIT;
				int 	zLevel 	= res.getInt("zlevel");
				
				LocationInfo location = new LocationInfo(lat, lon, zLevel);
				link.addPoint(location);
				
				if(debug % 10000 == 0)
					System.out.println("processed " + debug + " records.");
			}
			res.close();
			pstatement.close();
			con.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("fetchLinkGeometryById: debug code: " + debug);
		}
		System.out.println("fetch link geometry by id finish!");
	}
	
	/**
	 * fetch lane information
	 * @param linkMap
	 */
	public static void fetchLaneInfoByLink(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("fetch lane info by link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
			con.setAutoCommit(false);
			
			sql = "SELECT lane_id, link_id, lane_travel_direction, lane_type, access_id FROM rdf_lane ORDER BY lane_number";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, 
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.FETCH_FORWARD);
			pstatement.setFetchSize(1000);
			
			res = pstatement.executeQuery();
			while (res.next()) {
				long linkId 			= res.getLong("link_id");
				
				if(!linkMap.containsKey(linkId))
					continue;
				
				debug++;
				
				long laneId				= res.getLong("lane_id");
				int accessId			= res.getInt("access_id");
				String travelDirection	= res.getString("lane_travel_direction");
				int laneType			= res.getInt("lane_type");
				
				RDFLaneInfo lane = new RDFLaneInfo(laneId, travelDirection, laneType, accessId);
				
				RDFLinkInfo link = linkMap.get(linkId);
				
				link.addLane(lane);
				
				if(debug % 10000 == 0)
					System.out.println("processed " +  debug + " records.");
			}
			res.close();
			pstatement.close();
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("fetchLaneInfoByLink: debug code: " + debug);
		}
		System.out.println("fetch lane info by link finish!");
	}
	
	/**
	 * @deprecated
	 * @param nodeList
	 */
	public static void fetchNode(LinkedList<RDFNodeInfo> nodeList) {
		System.out.println("fetch node...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

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
	
	/**
	 * @deprecated
	 * @param linkList
	 */
	public static void fetchLink(LinkedList<RDFLinkInfo> linkList) {
		System.out.println("fetch link...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

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
	
	/**
	 * @deprecated
	 * @param linkList
	 */
	public static void fetchGeometry(LinkedList<RDFLinkInfo> linkList) {
		System.out.println("fetch geometry...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = RDFDatabase.getConnection();
			
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
					RDFLink.addPoint(location);
					pointsList.add(location);
				}
				//RDFLink.setPointsList(pointsList);
				
				if (debug % 250 == 0) {
					// reconnect
					res.close();
					pstatement.close();
					con.close();
					con = RDFDatabase.getConnection();
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
	 * @param linkList
	 */
	public static void fetchWriteLink(LinkedList<RDFLinkInfo> linkList) {
		System.out.println("fetch and write link...");
		int debug = 0;
		try {
			// delete file if it exists
			//File oldFile = new File(root + "/" + linkFile);
			//if(oldFile.exists()) oldFile.delete();
			
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

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
					//FileWriter fstream = new FileWriter(root + "/" + linkFile, true);
					//BufferedWriter out = new BufferedWriter(fstream);
					
					ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
					while(iterator.hasNext()) {
						RDFLinkInfo writeRDFLink = iterator.next();
						long writeLinkId = writeRDFLink.getLinkId();
						long writeRefNodeId = writeRDFLink.getRefNodeId();
						long writeNonRefNodeId = writeRDFLink.getNonRefNodeId();
						int writeFunctionalClass = writeRDFLink.getFunctionalClass();
						
						String strLine = writeLinkId + "|" + writeRefNodeId + "|" + writeNonRefNodeId + "|" + writeFunctionalClass + "\r\n";
					//	out.write(strLine);
					}
					//out.close();
					
					System.out.println("record " + debug + " finish!");
					// free the old memory
					linkList = new LinkedList<RDFLinkInfo>();
				}
				
			}
			
			// read the rest
			if(!linkList.isEmpty()) {
				// append write
				//FileWriter fstream = new FileWriter(root + "/" + linkFile, true);
				//BufferedWriter out = new BufferedWriter(fstream);
				
				ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
				while(iterator.hasNext()) {
					RDFLinkInfo writeRDFLink = iterator.next();
					long writeLinkId = writeRDFLink.getLinkId();
					long writeRefNodeId = writeRDFLink.getRefNodeId();
					long writeNonRefNodeId = writeRDFLink.getNonRefNodeId();
					int writeFunctionalClass = writeRDFLink.getFunctionalClass();
					
					String strLine = writeLinkId + "|" + writeRefNodeId + "|" + writeNonRefNodeId + "|" + writeFunctionalClass + "\r\n";
					//out.write(strLine);
				}
				//out.close();
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
	 * read from RDF_Link.txt, get link_id
	 * fetch from DB, get geometry
	 * append write to RDF_Link_Temp.txt
	 * change RDF_Link_Temp.txt to RDF_Link.txt
	 * @deprecated too slow :(
	 * @param linkBuffer
	 */
	public static void readFetchWriteGeometry(LinkedList<String> linkBuffer) {
		System.out.println("read fetch and write geometry...");
		int debug = 0;
		try {
			// delete temp file if it exists
			//File oldFile = new File(root + "/" + linkTempFile);
			//if(oldFile.exists()) oldFile.delete();
			
			//FileInputStream fstream = new FileInputStream(root + "/" + linkFile);
			//DataInputStream in = new DataInputStream(fstream);
			//BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = RDFDatabase.getConnection();
			
			//while ((strLine = br.readLine()) != null) {
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
						pointsListStr = lat + SEPARATION + lng + SEPARATION + zLevel;
					else
						pointsListStr += ";" + lat + SEPARATION + lng + SEPARATION + zLevel;
				}
				linkBuffer.add(strLine + "|" + pointsListStr + "\r\n");
				
				if (debug % 1000 == 0) {
					// append write to temp
					//FileWriter tempFstream = new FileWriter(root + "/" + linkTempFile, true);
					//BufferedWriter out = new BufferedWriter(tempFstream);
					
					ListIterator<String> iterator = linkBuffer.listIterator();
					while(iterator.hasNext()) {
						String buffer = iterator.next();
						//out.write(buffer);
					}
					//out.close();
					// free memory
					linkBuffer = new LinkedList<String>();
					System.out.println("line " + debug + " finish!");
				}
				res.close();
				pstatement.close();
			//}
			// read the rest
			if(!linkBuffer.isEmpty()) {
				// append write to temp
				//FileWriter tempFstream = new FileWriter(root + "/" + linkTempFile, true);
				//BufferedWriter out = new BufferedWriter(tempFstream);
				
				ListIterator<String> iterator = linkBuffer.listIterator();
				while(iterator.hasNext()) {
					String buffer = iterator.next();
					//out.write(buffer);
				}
				//out.close();
				// free memory
				linkBuffer = new LinkedList<String>();
			}
			
			res.close();
			pstatement.close();
			con.close();
			
			//br.close();
			//in.close();
			//fstream.close();
			
			// delete original link file
			//oldFile = new File(root + "/" + linkFile);
			//if(oldFile.exists()) oldFile.delete();
			
			//oldFile =new File(root + "/" + linkTempFile);
			//File newFile =new File(root + "/" + linkFile);
	 
			//if(oldFile.renameTo(newFile))
			//	System.out.println("rename to " + root + "/" + linkTempFile + " succesful");
			//else
			//	System.out.println("rename failed");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readFetchWriteGeometry: debug code: " + debug);
		}
		System.out.println("read fetch and write geometry finish!");
	}
	
	/**
	 * @deprecated
	 * @param linkList
	 */
	public static void fetchWriteGeometry(LinkedList<RDFLinkInfo> linkList) {
		System.out.println("fetch write geometry...");
		int debug = 0;
		try {
			// delete file if it exists
			//File oldFile = new File(root + "/" + linkGeometryFile);
			//if(oldFile.exists()) oldFile.delete();
			
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();

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
						//FileWriter fstream = new FileWriter(root + "/" + linkGeometryFile, true);
						//BufferedWriter out = new BufferedWriter(fstream);
						
						ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
						while(iterator.hasNext()) {
							RDFLinkInfo writeRDFLink = iterator.next();
							
							long writeLinkId = writeRDFLink.getLinkId();
							pointsList = writeRDFLink.getPointList();
							
							String pointsStr = "null";
							ListIterator<LocationInfo> pIterator = pointsList.listIterator();
							int i = 0;
							while(pIterator.hasNext()) {
								LocationInfo loc = pIterator.next();
								if(i++ == 0)
									pointsStr = loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
								else
									pointsStr += ";" + loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
								
							}
							
							String strLine = writeLinkId + "|" + pointsStr + "\r\n";
							//out.write(strLine);
						}
						//out.close();
						
						System.out.println("record " + debug + " finish!");
						// free the old memory
						linkList = new LinkedList<RDFLinkInfo>();
					}
					
					//RDFLink = new RDFLinkInfo(linkId);
					pointsList = new LinkedList<LocationInfo>();
					pointsList.add(location);
					//RDFLink.setPointsList(pointsList);
					lastLinkId = linkId;
				}
				else {
					pointsList = RDFLink.getPointList();
					pointsList.add(location);
				}				
			}
			
			// read last RDFLink
			linkList.add(RDFLink);
			// read the rest
			if(!linkList.isEmpty()) {
				// append write
				//FileWriter fstream = new FileWriter(root + "/" + linkGeometryFile, true);
				//BufferedWriter out = new BufferedWriter(fstream);
				
				ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
				while(iterator.hasNext()) {
					RDFLinkInfo writeRDFLink = iterator.next();
					
					long writeLinkId = writeRDFLink.getLinkId();
					pointsList = writeRDFLink.getPointList();
					
					String pointsStr = "null";
					ListIterator<LocationInfo> pIterator = pointsList.listIterator();
					int i = 0;
					while(pIterator.hasNext()) {
						LocationInfo loc = pIterator.next();
						if(i == 0)
							pointsStr = loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
						else
							pointsStr += ";" + loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
						
					}
					
					String strLine = writeLinkId + "|" + pointsStr + "\r\n";
					//out.write(strLine);
					// free the old memory
					linkList = new LinkedList<RDFLinkInfo>();
				}
				//out.close();
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
	 * @deprecated
	 * @param postCodeList
	 */
	public static void initialPostCode(ArrayList<Integer> postCodeList) {
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
	
	/**
	 * @deprecated
	 * @param query
	 * @param nodeList
	 */
	private static void addNodeByQuery(String query, LinkedList<RDFNodeInfo> nodeList) {
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = RDFDatabase.getConnection();
			
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
	
	/**
	 * @deprecated
	 * @param nodeIdSet
	 * @param nodeList
	 */
	public static void fetchNodeBySet(HashSet<Long> nodeIdSet, LinkedList<RDFNodeInfo> nodeList) {
		System.out.println("fetch node by set...");
		int debug = 0;
		try {
			// build sql query
			StringBuffer nodeStringBuffer = new StringBuffer();
			Iterator<Long> iterator = nodeIdSet.iterator();
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
					addNodeByQuery(nodeQuery, nodeList);
					i = 0;
					nodeStringBuffer = new StringBuffer();
				}
				
				if(debug % 1000 == 0)
					System.out.println((double)debug / nodeIdSet.size() * 100 + "% finish!");
			}
			if(!nodeStringBuffer.toString().equals("")) {	// process rest
				String nodeQuery = nodeStringBuffer.toString();
				addNodeByQuery(nodeQuery, nodeList);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("fetchNodeBySet: debug code: " + debug);
		}
		System.out.println("fetch " + nodeIdSet.size() + " nodes by set finish!");
	}
	
	/**
	 * @deprecated
	 * @param postCodeList
	 * @param linkMap
	 * @param linkList
	 */
	public static void fetchLinkByPostCode(ArrayList<Integer> postCodeList, HashMap<Long, RDFLinkInfo> linkMap, LinkedList<RDFLinkInfo> linkList) {
		System.out.println("fetch link by post code...");
		int debug = 0;
		try {
			for(int p = 0; p < postCodeList.size(); p++) {
				int postCode = postCodeList.get(p);
				
				Connection con = null;
				String sql = null;
				PreparedStatement pstatement = null;
				ResultSet res = null;
				
				con = RDFDatabase.getConnection();
				
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
						//existLink.addStreetName(secondName);
						continue;
					}
					
					String streetName = res.getString("street_name");
					long refNodeId = res.getLong("ref_node_id");
					long nonRefNodeId = res.getLong("nonref_node_id");
					int functionalClass = res.getInt("functional_class");
					String direction = res.getString("travel_direction");
					boolean ramp = res.getString("ramp").equals(YES) ? true : false;
					boolean tollway = res.getString("tollway").equals(YES) ? true : false;
					int speedCategory = res.getInt("speed_category");
					boolean carpoolRoad = res.getString("carpool_road") == null ? false : true;
					//boolean carpools = res.getString("carpools").equals(YES) ? true : false;
					//boolean expressLane = res.getString("express_lane").equals(YES) ? true : false;

					//RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, streetName, refNodeId, nonRefNodeId, 
					//		functionalClass, direction, ramp, tollway, carpoolRoad, speedCategory, 
					//		carpools, expressLane);

					//linkList.add(RDFLink);
					//linkMap.put(linkId, RDFLink);

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
	 * @deprecated all things in one query, not clear
	 * @param linkList
	 * @param linkMap
	 */
	public static void fetchLinkByAreaAll(LinkedList<RDFLinkInfo> linkList, HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("fetch link by area...");
		int debug = 0;
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			
			con = RDFDatabase.getConnection();
			
			
			sql = 	"SELECT link_id, street_name, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road, express_lane, carpools " +
					"FROM " +
					"(SELECT t8.link_id, street_name, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road, express_lane, access_id " +
					"FROM " +
					"(SELECT t6.link_id, road_name_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road, express_lane, access_id " +
					"FROM " +
					"(SELECT t4.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, carpool_road, express_lane, access_id " +
					"FROM " +
					"(SELECT t1.link_id, ref_node_id, nonref_node_id, functional_class, travel_direction, ramp, tollway, speed_category, access_id " +
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
					"ON t8.road_name_id = t9.road_name_id) t10 " +
					"LEFT JOIN rdf_access t11 " +
					"ON t10.access_id = t11.access_id";
			
			System.out.println("execute query... ");
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			
			while (res.next()) {
				debug++;
				long linkId = res.getLong("link_id");
				
				if(linkMap.containsKey(linkId)) {
					String secondName = res.getString("street_name");
					RDFLinkInfo existLink = linkMap.get(linkId);
					//existLink.addStreetName(secondName);
					continue;
				}
				
				String streetName = res.getString("street_name");
				long refNodeId = res.getLong("ref_node_id");
				long nonRefNodeId = res.getLong("nonref_node_id");
				int functionalClass = res.getInt("functional_class");
				String direction = res.getString("travel_direction");
				boolean ramp = res.getString("ramp").equals(YES) ? true : false;
				boolean tollway = res.getString("tollway").equals(YES) ? true : false;
				int speedCategory = res.getInt("speed_category");
				boolean carpoolRoad = res.getString("carpool_road") == null ? false : true;
				boolean carpools = res.getString("carpools").equals(YES) ? true : false;
				boolean expressLane = res.getString("express_lane") == null ? false : true;

				RDFLinkInfo RDFLink = new RDFLinkInfo(linkId, refNodeId, nonRefNodeId);

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
			System.err.println("fetchLinkByArea: debug code: " + debug);
		}
		System.out.println("fetch " + linkList.size() + " links by area finish!");
	}
}
