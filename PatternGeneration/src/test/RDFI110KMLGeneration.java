package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import objects.*;
import function.*;

public class RDFI110KMLGeneration {
	/**
	 * @param file
	 */
	static String root 				= "file";
	static String I110KMLFile 		= "RDF_I110.kml";
	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
	static String UNKNOWN 			= "Unknown Street";
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
	
	public static void main(String[] args) {
		// read node
		RDFInput.readNodeFile(nodeMap);
		// read link
		RDFInput.readLinkFile(linkMap, nodeMap);
		RDFInput.readLinkName(linkMap);
		RDFInput.readLinkGeometry(linkMap);
		RDFInput.readLinkLane(linkMap);
		
		generateLinkKML();
	}
	
	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + I110KMLFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for(long linkId : linkMap.keySet()) {
				debug++;
				
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
				
				if(!baseName.equals("I-110")) {
					continue;
				}
				
				String laneStr = null;
				LinkedList<RDFLaneInfo> laneList = link.getLaneList();
				if(laneList != null) {
					for(RDFLaneInfo lane : laneList) {
						if(laneStr == null) {
							laneStr = String.valueOf(lane.getLaneType());
						}
						else {
							laneStr += "," + lane.getLaneType();
						}
					}
				}
				
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
				if(laneStr != null) {
					kmlStr += "LaneType:"		+ laneStr + "\r\n";
				}
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
			System.out.println("generateLinkKML: debug code " + debug);
		}
		System.out.println("generate link kml finish!");
	}
}
