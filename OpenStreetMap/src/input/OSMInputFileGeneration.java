package input;

import object.*;

import java.util.*;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OSMInputFileGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	static String osmFile = "map.osm";
	static String nodeTxtFile = "osm_node.txt";
	static String wayTxtFile = "osm_way.txt";
	/**
	 * @param node
	 */
	static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	/**
	 * @param way
	 */
	static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readOsmFile();
		writeTxtFile();
	}
	
	public static void writeTxtFile() {
		System.out.println("write txt file...");
		int debug = 0, loop = 0;
		try {
			// write node
			FileWriter fstream = new FileWriter(root + "/" + nodeTxtFile);
			BufferedWriter out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < nodeArrayList.size(); i++) {
				debug++;
				NodeInfo nodeInfo = nodeArrayList.get(i);
				long nodeId = nodeInfo.getNodeId();
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				String strLine = nodeId + "||" + latitude + "," + longitude + "\r\n";
				
				out.write(strLine);
			}
			out.close();
			
			// write way
			fstream = new FileWriter(root + "/" + wayTxtFile);
			out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < wayArrayList.size(); i++) {
				debug++;
				WayInfo wayInfo = wayArrayList.get(i);
				long wayId = wayInfo.getWayId();
				int version = wayInfo.getVersion();
				String name = wayInfo.getName();
				ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
				String nodeListStr = "";
				for(int j = 0; j < localNodeArrayList.size(); j++) {
					nodeListStr += localNodeArrayList.get(j);
					if(j < localNodeArrayList.size() - 1)
						nodeListStr += ",";
				}
				
				String strLine = wayId + "||" + version + "||" + name + "||" + nodeListStr + "\r\n";
				
				out.write(strLine);
			}
			out.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeTxtFile: debug code: " + debug + ", in the " + loop + " loop.");
		}
		System.out.println("write txt file finish!");
	}

	public static void readOsmFile() {
		System.out.println("read osm file...");
		int debug = 0, loop = 0;
		try {
			File file = new File(root + "/" + osmFile);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			// read node
			loop++;
			NodeList nodeList = doc.getElementsByTagName("node");
			for (int i = 0; i < nodeList.getLength(); i++) {
				debug++;
				
				Node node = nodeList.item(i);
		 
				if (node.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element element = (Element) node;
					
					long nodeId = Long.parseLong(element.getAttribute("id"));
					double latitude = Double.parseDouble(element.getAttribute("lat"));
					double longitude = Double.parseDouble(element.getAttribute("lon"));
					LocationInfo location = new LocationInfo(latitude, longitude);
					NodeInfo nodeInfo = new NodeInfo(nodeId, location);
					
					nodeArrayList.add(nodeInfo);
				}
			}
			// read way
			loop++;
			nodeList = doc.getElementsByTagName("way");
			for (int i = 0; i < nodeList.getLength(); i++) {
				debug++;
				
				Node node = nodeList.item(i);
		 
				if (node.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element element = (Element) node;
					
					long wayId = Long.parseLong(element.getAttribute("id"));
					int version = Integer.parseInt(element.getAttribute("version"));
					String name = "null";
					ArrayList<Long> localNodeArrayList = new ArrayList<Long>();
					
					if(node.hasChildNodes()) {
						NodeList childList = node.getChildNodes();
						for(int j = 0; j < childList.getLength(); j++) {
							Node child = childList.item(j);
							if (child.getNodeType() == Node.ELEMENT_NODE) {
								if(child.getNodeName().equals("nd")) {
									
									Element eChildElement = (Element) child;
									
									long nodeId = Long.parseLong(eChildElement.getAttribute("ref"));
									localNodeArrayList.add(nodeId);
								}
								if(child.getNodeName().equals("tag")) {
									Element eChildElement = (Element) child;
									
									String kAttr = eChildElement.getAttribute("k");
									
									if(kAttr.equals("name")) {
										name = eChildElement.getAttribute("v");
									}
								}
							}
						}
					}
					
					WayInfo wayInfo = new WayInfo(wayId, version, name, localNodeArrayList);
					
					wayArrayList.add(wayInfo);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readOsmFile: debug code: " + debug + ", in the " + loop + " loop.");
		}
		System.out.println("read osm file finish!");
	}
}
