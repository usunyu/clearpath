package function;

import object.*;

import java.util.*;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import data.OSMData;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class OSMInput {
	/**
	 * @param file
	 */
	static String root = "file";
	static String osmFile;
	static String nodeCSVFile;
	static String wayCSVFile;
	static String wayInfoFile;
	static String wktsFile;
	static String edgeCVSFile;
	static String adjlistFile;
	// temp
	static String extraNodeFile;
	
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
	
	/**
	 * @param csv
	 */
	static String SEPARATION		= "|";
	static String ESCAPE_SEPARATION	= "\\|";
	static String SEGMENT			= "/";
	static String COMMA				= ",";
	static String SEMICOLON			= ";";
	static String COLON				= ":";
	static String ONEDIRECT			= "O";
	static String BIDIRECT			= "B";
	static String FIX				= "F";
	static String VARIABLE			= "V";
	static String LINEEND			= "\r\n";
	static String UNKNOWN_STREET 	= "Unknown Street";
	static String UNKNOWN_HIGHWAY 	= "Unknown Highway";
	
	public static void paramConfig(String name) {
		osmFile 		= name + ".osm";
		nodeCSVFile 	= name + "_node.csv";
		wayCSVFile 		= name + "_way.csv";
		wayInfoFile		= name + "_info.csv";
		wktsFile		= name + ".osm.wkts";
		edgeCVSFile		= name + "_edge.csv";
		edgeCVSFile		= name + "_edge.csv";
		adjlistFile		= name + "_adjlist.csv";
		// temp
		extraNodeFile	= name + "_way_extra.csv";
	}
	
	/**
	 * build adjlist
	 * @param adjListHashMap
	 */
	public static void buildAdjList(HashMap<Long, ArrayList<ToNodeInfo>> adjListHashMap) {
		System.out.println("loading adjlist file: " + adjlistFile);

		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + adjlistFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				if (debug % 100000 == 0)
					System.out.println("completed " + debug + " lines.");

				String[] nodes = strLine.split(ESCAPE_SEPARATION);
				long startNode = Long.parseLong(nodes[0]);

				ArrayList<ToNodeInfo> toNodeList = new ArrayList<ToNodeInfo>();
				String[] adjlists = nodes[1].split(SEMICOLON);
				for (int i = 0; i < adjlists.length; i++) {
					String adjComponent = adjlists[i];
					long toNode = Long.parseLong(adjComponent.substring(0, adjComponent.indexOf('(')));
					String fixStr = adjComponent.substring(adjComponent.indexOf('(') + 1, adjComponent.indexOf(')'));
					ToNodeInfo toNodeInfo;
					if (fixStr.equals(FIX)) { // fixed
						int travelTime = Integer.parseInt(adjComponent.substring(adjComponent.indexOf(COLON) + 1));
						toNodeInfo = new ToNodeInfo(toNode, travelTime);
					} else { // variable
						String timeList = adjComponent.substring(adjComponent.indexOf(COLON) + 1);
						String[] timeValueList = timeList.split(COMMA);
						int[] travelTimeArray = new int[timeValueList.length];
						for (int j = 0; j < timeValueList.length; j++)
							travelTimeArray[j] = Integer.parseInt(timeValueList[j]);
						toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
					}
					toNodeList.add(toNodeInfo);
				}
				adjListHashMap.put(startNode, toNodeList);
			}

			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("buildList: debug code: " + debug);
		}
		System.out.println("building list finish!");
	}
	
	/**
	 * read edge file
	 * @param edgeHashMap
	 */
	public static void readEdgeFile(HashMap<Long, EdgeInfo> edgeHashMap) {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + edgeCVSFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(ESCAPE_SEPARATION);
				long id = Long.parseLong(nodes[0]);
				long wayId = Long.parseLong(nodes[0].substring(0, nodes[0].length() - 4));
				int edgeId = Integer.parseInt(nodes[0].substring(nodes[0].length() - 4));
				String name = nodes[1];
				String highway = nodes[2];
				long startNode = Long.parseLong(nodes[3]);
				long endNode = Long.parseLong(nodes[4]);
				int distance = Integer.parseInt(nodes[5]);

				EdgeInfo edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, startNode, endNode, distance);
				edgeHashMap.put(id, edgeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readEdgeFile: debug code: " + debug);
		}
		System.out.println("read edge file finish!");
	}

	/**
	 * read edge file
	 * @param edgeHashMap
	 */
	public static void readEdgeFile(HashMap<Long, EdgeInfo> edgeHashMap, HashMap<String, EdgeInfo> nodesToEdge) {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + edgeCVSFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(ESCAPE_SEPARATION);
				long id = Long.parseLong(nodes[0]);
				long wayId = Long.parseLong(nodes[0].substring(0, nodes[0].length() - 4));
				int edgeId = Integer.parseInt(nodes[0].substring(nodes[0].length() - 4));
				String name = nodes[1];
				String highway = nodes[2];
				long startNode = Long.parseLong(nodes[3]);
				long endNode = Long.parseLong(nodes[4]);
				int distance = Integer.parseInt(nodes[5]);

				EdgeInfo edgeInfo = new EdgeInfo(wayId, edgeId, name, highway, startNode, endNode, distance);
				edgeHashMap.put(id, edgeInfo);
				
				String nodeId = startNode + COMMA + endNode;
				nodesToEdge.put(nodeId, edgeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readEdgeFile: debug code: " + debug);
		}
		System.out.println("read edge file finish!");
	}
	
	/**
	 * read wkts file, update way map and node map
	 * @param wayHashMap
	 * @param nodeHashMap
	 */
	public static void readWktsFile(HashMap<Long, WayInfo> wayHashMap, HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("read wkts file...");
		int debug = 0;
		try {
			HashMap<Long, WayInfo> wayNeedHashMap = new HashMap<Long, WayInfo>();
			HashMap<Long, NodeInfo> nodeNeedHashMap = new HashMap<Long, NodeInfo>();
			FileInputStream fstream = new FileInputStream(root + SEGMENT + wktsFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				WayInfo wayInfo = wayHashMap.get(wayId);
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>();
				for(int i = 1; i < nodes.length; i++) {
					long nodeId = Long.parseLong(nodes[i]);
					NodeInfo nodeInfo = nodeHashMap.get(nodeId);
					localNodeArrayList.add(nodeId);
					if(!nodeNeedHashMap.containsKey(nodeId)) {
						nodeNeedHashMap.put(nodeId, nodeInfo);
					}
				}
				wayInfo.setNodeArrayList(localNodeArrayList);
				if(!wayNeedHashMap.containsKey(wayId)) {
					wayNeedHashMap.put(wayId, wayInfo);
				}
				if(debug % 10000 == 0)
					System.out.println("processed line " + debug);
			}
			// update way map
			OSMData.wayHashMap = wayNeedHashMap;
			// update node map
			OSMData.nodeHashMap = nodeNeedHashMap;
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readWktsFile: debug code: " + debug);
		}
		System.out.println("read wkts file finish!");
	}
	
	/**
	 * read way info file
	 * @param wayHashMap
	 */
	public static void readWayInfo(HashMap<Long, WayInfo> wayHashMap) {
		System.out.println("read way info...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + SEGMENT + wayInfoFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(ESCAPE_SEPARATION + ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				WayInfo wayInfo = wayHashMap.get(wayId);
				HashMap<String, String> infoHashMap = null;
				
				for(int i = 1; i < nodes.length; i++) {
					String[] keyValueSet = nodes[i].split(ESCAPE_SEPARATION);
					String key = keyValueSet[0];
					String value = keyValueSet[1];
					if(infoHashMap == null) {
						infoHashMap = new HashMap<String, String>();
					}
					infoHashMap.put(key, value);
				}
				wayInfo.setInfoHashMap(infoHashMap);
			}
			br.close();
			in.close();
			fstream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("readWayInfo: debug code: " + debug);
		}
		System.out.println("read way info finish!");
	}
	
	/**
	 * read way csv file
	 * @param wayHashMap
	 */
	public static void readWayFile(HashMap<Long, WayInfo> wayHashMap) {
		System.out.println("read way file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + SEGMENT + wayCSVFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				boolean isOneway = nodes[1].equals(ONEDIRECT) ? true : false;
				String name = nodes[2];
				String highway = nodes[3];
				
				WayInfo wayInfo = new WayInfo(wayId, isOneway, name, highway);
				wayHashMap.put(wayId, wayInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readWayFile: debug code: " + debug);
		}
		System.out.println("read way file finish!");
	}
	
	/**
	 * read the node file
	 * @param nodeHashMap
	 */
	public static void readNodeFile(HashMap<Long, NodeInfo> nodeHashMap) {
		System.out.println("read node file...");
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + SEGMENT + nodeCSVFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(ESCAPE_SEPARATION);
				long nodeId = Long.parseLong(nodes[0]);
				double latitude = Double.parseDouble(nodes[1]);
				double longitude = Double.parseDouble(nodes[2]);
				LocationInfo locationInfo = new LocationInfo(latitude, longitude);
				NodeInfo nodeInfo = new NodeInfo(nodeId, locationInfo);
				nodeHashMap.put(nodeId, nodeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readNodeFile: debug code: " + debug);
		}
		System.out.println("read node file finish!");
	}
	
	/**
	 * use sTaX API to read OSM(XML) file instead of using DOM
	 * refer to http://www.vogella.com/articles/JavaXML/article.html
	 * @param wayArrayList
	 * @param nodeArrayList
	 */
	public static void readOsmFileStax(ArrayList<WayInfo> wayArrayList, ArrayList<NodeInfo> nodeArrayList) {
		System.out.println("read osm file...");
		int debug = 0;
		try {
			// First create a new XMLInputFactory
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			// Setup a new eventReader
			InputStream in = new FileInputStream(root + SEGMENT + osmFile);
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
			HashMap<String, String> infoHashMap = null;
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
						name = UNKNOWN_STREET;
						highway = UNKNOWN_HIGHWAY;
						infoHashMap = null;
						
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
							if(infoHashMap == null) {
								infoHashMap = new HashMap<String, String>();
							}
							infoHashMap.put(kAttr, vAttr);
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
		        		// skip nodeArrayList, we add it from wkts file
		        		wayInfo = new WayInfo(wayId, isOneway, name, highway);
		        		wayInfo.setInfoHashMap(infoHashMap);
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
	
	/**
	 * @deprecated used for extra node after fixCompleteness, but fixCompleteness has problem
	 * @param nodeArrayList
	 */
	public static void readExtraFile(ArrayList<NodeInfo> nodeArrayList) {
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
	
	/**
	 * @deprecated can not deal with large amount of XML file
	 * @param wayArrayList
	 * @param nodeArrayList
	 */
	public static void readOsmFile(ArrayList<WayInfo> wayArrayList, ArrayList<NodeInfo> nodeArrayList) {
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
					
					WayInfo wayInfo = new WayInfo(wayId, isOneway, name, highway);
					
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




