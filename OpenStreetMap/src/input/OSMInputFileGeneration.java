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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/*
 * 2nd step for OSM Project
 * 1) read the data from OSM file
 * 2) generate xxx_node.txt and xxx_way.txt from data
 * 
 * format:
 * xxx_node.txt
 * nodeId      ||lat               ||lon
 * (id of node)||(latitude of node)||(longitude of node)
 * 
 * xxx_way.txt
 * wayId      ||isOneway                      ||name            ||highway
 * (id of way)||(O:oneway B:bidirectional way)||(name of street)||(type of way)
 * 
 * type of way, refer to http://wiki.openstreetmap.org/wiki/Key:highway
 */

public class OSMInputFileGeneration {

	/**
	 * @param file
	 */
	static String root 			= "file";
	//static String osmFile 		= "map.osm";
	//static String nodeTxtFile 	= "osm_node.txt";
	//static String wayTxtFile 	= "osm_way.txt";
	static String extraNodeFile = "extra.wkts";
	static String osmFile 		= "los_angeles.osm";
	static String nodeTxtFile 	= "los_angeles_node.txt";
	static String wayTxtFile 	= "los_angeles_way.txt";
	/**
	 * @param node
	 */
	static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	/**
	 * @param way
	 */
	static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	/**
	 * @param xml
	 */
	static final String ID 			= "id";
	static final String NODE 		= "node";
	static final String WAY 		= "way";
	static final String LAT 		= "lat";
	static final String LON 		= "lon";
	static final String TAG			= "tag";
	static final String K			= "k";
	static final String V			= "v";
	static final String NAME		= "name";
	static final String HIGHWAY		= "highway";
	static final String ONEWAY		= "oneway";
	static final String YES			= "yes";
	static final String RELATION	= "relation";
	
	public static void main(String[] args) {
		readOsmFileStax();
		// readOsmFile();
		// readExtraFile();
		writeTxtFile();
	}
	
	/*
	 * use sTaX API to read OSM(XML) file instead of using DOM
	 * refer to http://www.vogella.com/articles/JavaXML/article.html
	 */
	public static void readOsmFileStax() {
		System.out.println("read osm file...");
		int debug = 0;
		try {
			// First create a new XMLInputFactory
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			// Setup a new eventReader
			InputStream in = new FileInputStream(root + "/" + osmFile);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		    // Read the XML document
			// NodeInfo
			NodeInfo nodeInfo = null;
			long nodeId = 0;
			LocationInfo location = null;
			double latitude = 0;
			double longitude = 0;
			// WayInfo
			WayInfo wayInfo = null;
			long wayId = 0;
			String kAttr = null;
			String vAttr = null;
			String name = null;
			String highway = null;
			boolean isOneway = false;
			while (eventReader.hasNext()) {
				debug++;
				XMLEvent event = eventReader.nextEvent();
				 
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					// If we have a item element we create a new item
					if (startElement.getName().getLocalPart().equals(NODE)) {	// read node
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(ID))
								nodeId = Long.parseLong(attribute.getValue());
							if (attribute.getName().toString().equals(LAT))
								latitude = Double.parseDouble(attribute.getValue());
							if (attribute.getName().toString().equals(LON))
								longitude = Double.parseDouble(attribute.getValue());
						}
						location = new LocationInfo(latitude, longitude);
					}
					// If we have a item element we create a new item
					if (startElement.getName().getLocalPart().equals(WAY)) {	// read way
						// set default
						isOneway = false;
						name = "null";
						highway = "null";
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(ID)) {
								wayId = Long.parseLong(attribute.getValue());
								break;
							}
						}
					}
					// Read child tag element of way
					if (startElement.getName().getLocalPart().equals(TAG)) {	// read tag
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(K))
								kAttr = attribute.getValue();
							if (attribute.getName().toString().equals(V))
								vAttr = attribute.getValue();
						}
						if(kAttr.equals(NAME)) {
							name = vAttr;
						}
						else if(kAttr.equals(HIGHWAY)) {
							highway = vAttr;
						}
						else if(kAttr.equals(ONEWAY)) {
							if(vAttr.equals(YES))
								isOneway = true;
						}
						else {
							// TODO: put other in info map
						}
					}
					
					if (startElement.getName().getLocalPart().equals(RELATION)) {	// skip relation
						break;
					}
				}
				
				// If we reach the end of an item element we add it to the list
		        if (event.isEndElement()) {
		        	EndElement endElement = event.asEndElement();
		        	if (endElement.getName().getLocalPart().equals(NODE)) {
		        		nodeInfo = new NodeInfo(nodeId, location);
		        		nodeArrayList.add(nodeInfo);
		        	}
		        	if (endElement.getName().getLocalPart().equals(WAY)) {
		        		wayInfo = new WayInfo(wayId, isOneway, name, highway, null, null);
		        		wayArrayList.add(wayInfo);
		        	}
		        }
			}
		}
		catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("readOsmFileStax: debug code: " + debug);
	    }
		System.out.println("read osm file finish!");
	}
	
	/*
	 * deprecated
	 * used for extra node after fixCompleteness, but fixCompleteness has problem
	 */
	public static void readExtraFile() {
		System.out.println("read extra file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + extraNodeFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] splitted = strLine.split("\\|\\|");
				long nodeId = Long.parseLong(splitted[0]);
				String locationStr = splitted[1];
				String[] location = locationStr.split(",");
				double latitude = Double.parseDouble(location[0]);
				double longitude = Double.parseDouble(location[1]);
				LocationInfo locationInfo = new LocationInfo(latitude, longitude);
				NodeInfo nodeInfo = new NodeInfo(nodeId, locationInfo);
				nodeArrayList.add(nodeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			e.printStackTrace();
			System.err.println("readNodeFile: debug code: " + debug);
		}
		System.out.println("read extra file finish!");
	}
	
	/*
	 * write to TXT file from the data read from XML file 
	 */
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
				char isOneway = wayInfo.isOneway() ? 'O' : 'B';
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				String strLine = null;
				
				ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
				if(localNodeArrayList != null) {
					String nodeListStr = "";
					for(int j = 0; j < localNodeArrayList.size(); j++) {
						nodeListStr += localNodeArrayList.get(j);
						if(j < localNodeArrayList.size() - 1)
							nodeListStr += ",";
					}
					strLine = wayId + "||" + name + "||" + nodeListStr + "\r\n";
				}
				
				strLine = wayId + "||" + isOneway + "||" + name + "||" + highway;
				HashMap<String, String> infoHashMap = wayInfo.getInfoHashMap();
				
				if(infoHashMap != null) {
					Iterator<String> iter = infoHashMap.keySet().iterator();
					while(iter.hasNext()) {
						String key = iter.next();
						String val = infoHashMap.get(key);
						strLine += "||" + key + "|" + val;
					}
				}
				
				strLine += "\r\n";
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

	/*
	 * deprecated
	 * can not deal with large amount of XML file
	 */
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
				
				if(i % 1000 == 0)
					System.out.println("landmarks processed " + ((double) i / nodeList.getLength() * 100) + "%" );
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
					boolean isOneway = false;
					String name = "null";
					String highway = "null";
					//ArrayList<Long> localNodeArrayList = new ArrayList<Long>();
					HashMap<String, String> infoHashMap = new HashMap<String, String>();
					
					if(node.hasChildNodes()) {
						NodeList childList = node.getChildNodes();
						for(int j = 0; j < childList.getLength(); j++) {
							Node child = childList.item(j);
							if (child.getNodeType() == Node.ELEMENT_NODE) {
								if(child.getNodeName().equals("nd")) {
									continue;
									//Element eChildElement = (Element) child;
									
									//long nodeId = Long.parseLong(eChildElement.getAttribute("ref"));
									//localNodeArrayList.add(nodeId);
								}
								if(child.getNodeName().equals("tag")) {
									Element eChildElement = (Element) child;
									
									String kAttr = eChildElement.getAttribute("k");
									String vAttr = eChildElement.getAttribute("v"); 
									
									if(kAttr.equals("name")) {
										name = vAttr;
									}
									else if(kAttr.equals("highway")) {
										highway = vAttr;
									}
									else if(kAttr.equals("oneway")) {
										if(vAttr.equals("yes"))
											isOneway = true;
									}
									else { // put them in info map
										infoHashMap.put(kAttr, vAttr);
									}
								}
							}
						}
					}
					
					WayInfo wayInfo = new WayInfo(wayId, isOneway, name, highway, null, infoHashMap);
					
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
