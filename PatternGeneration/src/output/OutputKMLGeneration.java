package output;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class OutputKMLGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String highwayLinkFile = "Highway_Link.txt";
	static String arterialLinkFile = "Arterial_Link.txt";
	// for write kml file
	static String highwayKmlFile = "Highway_Link.kml";
	static String arterialKmlFile = "Arterial_Link.kml";
	/**
	 * @param link
	 */
	static ArrayList<LinkInfo> highwayLinkList = new ArrayList<LinkInfo>();
	static ArrayList<LinkInfo> arterialLinkList = new ArrayList<LinkInfo>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// true: highway, false: arterial
		readLinkFile(false);
		generateKML(false);
	}

	private static void generateKML(boolean isHighway) {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" +  (isHighway ? highwayKmlFile : arterialKmlFile));
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			ArrayList<LinkInfo> linkList = isHighway ? highwayLinkList : arterialLinkList;
			for (int i = 0; i < linkList.size(); i++) {
				LinkInfo link = linkList.get(i);
				int linkId = link.getLinkId();
				int funcClass = link.getFuncClass();
				String streetName = link.getStreetName();
				if(streetName.contains("&"))
					streetName = streetName.replaceAll("&", " and ");
				ArrayList<PairInfo> nodeList = link.getNodeList();

				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				kmlStr += "Class:" + funcClass + "\r\n";
				kmlStr += "Name:" + streetName + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
				}
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

	private static void readLinkFile(boolean isHighway) {
		System.out.println("read link file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + (isHighway ? highwayLinkFile : arterialLinkFile));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(";");
				int linkId = Integer.parseInt(nodes[0]);
				String allDir = nodes[1];
				String streetName = nodes[2];
				int funcClass = Integer.parseInt(nodes[3]);
				ArrayList<PairInfo> nodeList = getPairListFromStr(nodes[4]);
				int speedCat = Integer.parseInt(nodes[5]);
				String dirTravel = nodes[6];
				int startNode = Integer.parseInt(nodes[7]);
				int endNode = Integer.parseInt(nodes[8]);

				LinkInfo linkInfo = new LinkInfo(linkId, funcClass, streetName, startNode, endNode, nodeList, dirTravel, speedCat, allDir);

				if(isHighway)
					highwayLinkList.add(linkInfo);
				else
					arterialLinkList.add(linkInfo);
				
				if (debug % 100000 == 0)
					System.out.println("record " + debug + " finish!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + debug);
		}
		System.out.println("read link file finish!");
	}

	private static ArrayList<PairInfo> getPairListFromStr(String str) {
		String[] nodes = str.split(":");
		ArrayList<PairInfo> pairList = new ArrayList<PairInfo>();
		for (int i = 0; i < nodes.length; i++) {
			PairInfo pair = getPairFromStr(nodes[i]);
			pairList.add(pair);
		}
		return pairList;
	}

	private static PairInfo getPairFromStr(String str) {
		String[] nodes = str.split(",");
		PairInfo pair = new PairInfo(Double.parseDouble(nodes[1]), Double.parseDouble(nodes[0]));
		return pair;
	}
}
