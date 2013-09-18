package process;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFMatchSensorLink {

	/**
	 * @param file
	 */
	static String root 				= "file";
	static String linkFile			= "RDF_Link.txt";
	static String nodeFile			= "RDF_Node.txt";
	/**
	 * @param link
	 */
	static LinkedList<RDFLinkInfo> linkList = new LinkedList<RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	
	public static void main(String[] args) {
		readNodeFile();
		readLinkFile();
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
				boolean carpool 		= nodes[9].equals("T") ? true : false;
				
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
