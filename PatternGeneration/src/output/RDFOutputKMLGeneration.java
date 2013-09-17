package output;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFOutputKMLGeneration {

	/**
	 * @param file
	 */
	static String root				= "file";
	// for write link file
	static String linkFile			= "RDF_Link.txt";
	// for write node file
	static String nodeFile			= "RDF_Node.txt";
	// for write kml file
	static String kmlLinkFile		= "RDF_Link.kml";
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readNodeFile();
		readLinkFile();
		generateLinkKML();
	}
	
	private static void generateLinkKML() {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlLinkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo link 	= iterator.next();
				long linkId 		= link.getLinkId();
				String streetName 	= link.getStreetName();
				long refNodeId 		= link.getRefNodeId();
				long nonRefNodeId 	= link.getNonRefNodeId();
				int functionalClass = link.getFunctionalClass();
				String direction 	= link.getDirection();
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpool 	= link.isCarpool();
				int speedCategory 	= link.getSpeedCategory();
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Name:" 		+ streetName + "\r\n";
				kmlStr += "Class:" 		+ functionalClass + "\r\n";
				kmlStr += "Category:" 	+ speedCategory + "\r\n";
				kmlStr += "Dir:" 		+ direction + "\r\n";
				kmlStr += "Ramp:" 		+ ramp + "\r\n";
				kmlStr += "Tollway:" 	+ tollway + "\r\n";
				kmlStr += "Carpool:" 	+ carpool + "\r\n";
				kmlStr += "</description>";
				
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				LocationInfo loc1 = nodeMap.get(refNodeId).getLocation();
				kmlStr += loc1.getLongitude()+ "," + loc1.getLatitude()+ "," + loc1.getZLevel() + " ";
				LocationInfo loc2 = nodeMap.get(nonRefNodeId).getLocation();
				kmlStr += loc2.getLongitude()+ "," + loc2.getLatitude()+ "," + loc2.getZLevel() + " ";
				kmlStr += "</coordinates></LineString></Placemark>\n";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}
	
	private static void readNodeFile() {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + nodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split("\\|");
				
				long 			nodeId 	= Long.parseLong(nodes[0]);
				String[]		locStr 	= nodes[1].split(",");
				int 			zLevel	= Integer.parseInt(nodes[2]);
				LocationInfo 	location= new LocationInfo(Double.parseDouble(locStr[0]), Double.parseDouble(locStr[1]), zLevel);
				
				RDFNodeInfo RDFNode = new RDFNodeInfo(nodeId, location);
				
				nodeMap.put(nodeId, RDFNode);

				if (debug % 10000 == 0)
					System.out.println("record " + debug + " finish!");
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
	
	private static void readLinkFile() {
		System.out.println("read link file...");
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
				boolean carpool 		= nodes[9].equals("T") ? false : true;
				
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

}
