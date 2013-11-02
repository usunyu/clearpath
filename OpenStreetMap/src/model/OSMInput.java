package model;

import java.util.*;

import java.io.*;

import object.*;
import global.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import javax.xml.stream.*;
import javax.xml.stream.events.*;

public class OSMInput {
	
	/**
	 * check whether file exist
	 * @param file
	 */
	public static void checkFileExist(String file) {
		File f = new File(file);
		
		if(!f.exists()) {
			System.err.println("Can not find " + file + ", program exit!");
			System.exit(-1);
		}
	}

	/**
	 * build adjList and adjReverseList from adjlist.csv
	 * @param adjListHashMap
	 * @param adjReverseListHashMap
	 */
	public static void readAdjList(HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap, HashMap<Long, LinkedList<ToNodeInfo>> adjReverseListHashMap) {
		System.out.println("loading adjlist file: " + OSMParam.adjlistFile);

		int debug = 0;
		try {
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.adjlistFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				if (debug % 100000 == 0)
					System.out.println("completed " + debug + " lines.");

				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
				long startNode = Long.parseLong(nodes[0]);

				LinkedList<ToNodeInfo> toNodeList = new LinkedList<ToNodeInfo>();
				String[] adjlists = nodes[1].split(OSMParam.SEMICOLON);
				for (int i = 0; i < adjlists.length; i++) {
					String adjComponent = adjlists[i];
					long toNode = Long.parseLong(adjComponent.substring(0, adjComponent.indexOf('(')));
					String fixStr = adjComponent.substring(adjComponent.indexOf('(') + 1, adjComponent.indexOf(')'));
					if (fixStr.equals(OSMParam.FIX)) { // fixed
						int travelTime = Integer.parseInt(adjComponent.substring(adjComponent.indexOf(OSMParam.COLON) + 1));
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTime);
						toNodeList.add(toNodeInfo);
						// build the reverse adjlist
						LinkedList<ToNodeInfo> fromNodeList;
						if(adjReverseListHashMap.containsKey(toNode)) {
							fromNodeList = adjReverseListHashMap.get(toNode);
						}
						else {
							fromNodeList = new LinkedList<ToNodeInfo>();
							adjReverseListHashMap.put(toNode, fromNodeList);
						}
						ToNodeInfo fromNodeInfo = new ToNodeInfo(startNode, travelTime);
						fromNodeList.add(fromNodeInfo);
					} else { // variable
						String timeList = adjComponent.substring(adjComponent.indexOf(OSMParam.COLON) + 1);
						String[] timeValueList = timeList.split(OSMParam.COMMA);
						int[] travelTimeArray = new int[timeValueList.length];
						for (int j = 0; j < timeValueList.length; j++)
							travelTimeArray[j] = Integer.parseInt(timeValueList[j]);
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
						toNodeList.add(toNodeInfo);
						// build the reverse adjlist
						LinkedList<ToNodeInfo> fromNodeList;
						if(adjReverseListHashMap.containsKey(toNode)) {
							fromNodeList = adjReverseListHashMap.get(toNode);
						}
						else {
							fromNodeList = new LinkedList<ToNodeInfo>();
							adjReverseListHashMap.put(toNode, fromNodeList);
						}
						ToNodeInfo fromNodeInfo = new ToNodeInfo(startNode, travelTimeArray);
						fromNodeList.add(fromNodeInfo);
					}
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
	 * build adjList from adjlist.csv
	 * @param adjListHashMap
	 */
	public static void readAdjList(HashMap<Long, LinkedList<ToNodeInfo>> adjListHashMap) {
		System.out.println("loading adjlist file: " + OSMParam.adjlistFile);

		int debug = 0;
		try {
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.adjlistFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				if (debug % 100000 == 0)
					System.out.println("completed " + debug + " lines.");

				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
				long startNode = Long.parseLong(nodes[0]);

				LinkedList<ToNodeInfo> toNodeList = new LinkedList<ToNodeInfo>();
				String[] adjlists = nodes[1].split(OSMParam.SEMICOLON);
				for (int i = 0; i < adjlists.length; i++) {
					String adjComponent = adjlists[i];
					long toNode = Long.parseLong(adjComponent.substring(0, adjComponent.indexOf('(')));
					String fixStr = adjComponent.substring(adjComponent.indexOf('(') + 1, adjComponent.indexOf(')'));
					if (fixStr.equals(OSMParam.FIX)) { // fixed
						int travelTime = Integer.parseInt(adjComponent.substring(adjComponent.indexOf(OSMParam.COLON) + 1));
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTime);
						toNodeList.add(toNodeInfo);
					} else { // variable
						String timeList = adjComponent.substring(adjComponent.indexOf(OSMParam.COLON) + 1);
						String[] timeValueList = timeList.split(OSMParam.COMMA);
						int[] travelTimeArray = new int[timeValueList.length];
						for (int j = 0; j < timeValueList.length; j++)
							travelTimeArray[j] = Integer.parseInt(timeValueList[j]);
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
						toNodeList.add(toNodeInfo);
					}
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.edgeCVSFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				int edgeId = Integer.parseInt(nodes[1]);
				String name = nodes[2];
				String highway = nodes[3];
				boolean isOneway = nodes[4].equals(OSMParam.ONEDIRECT);
				String nodeListStr = nodes[5];
				int distance = Integer.parseInt(nodes[6]);
				
				String[] nodeIds = nodeListStr.split(OSMParam.COMMA);
				LinkedList<Long> nodeList = new LinkedList<Long>();
				for(String nodeStr : nodeIds) {
					nodeList.add(Long.parseLong(nodeStr));
				}
				EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highway, isOneway, nodeList, distance);
				edgeHashMap.put(edge.getId(), edge);
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.edgeCVSFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				int edgeId = Integer.parseInt(nodes[1]);
				String name = nodes[2];
				String highway = nodes[3];
				boolean isOneway = nodes[4].equals(OSMParam.ONEDIRECT);
				String nodeListStr = nodes[5];
				int distance = Integer.parseInt(nodes[6]);
				
				String[] nodeIds = nodeListStr.split(OSMParam.COMMA);
				LinkedList<Long> nodeList = new LinkedList<Long>();
				for(String nodeStr : nodeIds) {
					nodeList.add(Long.parseLong(nodeStr));
				}
				EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highway, isOneway, nodeList, distance);
				edgeHashMap.put(edge.getId(), edge);
				
				String nodesId = edge.getStartNode() + OSMParam.COMMA + edge.getEndNode();
				nodesToEdge.put(nodesId, edge);
				if(!isOneway) {
					nodesId = edge.getEndNode() + OSMParam.COMMA + edge.getStartNode();
					nodesToEdge.put(nodesId, edge);
				}
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.wktsFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.wayInfoFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION + OSMParam.ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				WayInfo wayInfo = wayHashMap.get(wayId);
				HashMap<String, String> infoHashMap = null;
				
				for(int i = 1; i < nodes.length; i++) {
					String[] keyValueSet = nodes[i].split(OSMParam.ESCAPE_SEPARATION);
					if(keyValueSet == null || keyValueSet.length <= 1)
						continue;
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.wayCSVFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
				long wayId = Long.parseLong(nodes[0]);
				boolean isOneway = nodes[1].equals(OSMParam.ONEDIRECT) ? true : false;
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.nodeCSVFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.ESCAPE_SEPARATION);
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.osmFile;
			checkFileExist(file);
			// Setup a new eventReader
			InputStream in = new FileInputStream(file);
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
					if (startElement.getName().getLocalPart().equals(OSMParam.NODE)) {	// read node
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(OSMParam.ID))
								nodeId = Long.parseLong(attribute.getValue());
							if (attribute.getName().toString().equals(OSMParam.LAT))
								latitude = Double.parseDouble(attribute.getValue());
							if (attribute.getName().toString().equals(OSMParam.LON))
								longitude = Double.parseDouble(attribute.getValue());
						}
						location = new LocationInfo(latitude, longitude);
					}
					
					// If we have a item element we create a new item
					if (startElement.getName().getLocalPart().equals(OSMParam.WAY)) {	// read way
						// set default
						isOneway = false;
						name = OSMParam.UNKNOWN_STREET;
						highway = OSMParam.UNKNOWN_HIGHWAY;
						infoHashMap = null;
						
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(OSMParam.ID)) {
								wayId = Long.parseLong(attribute.getValue());
								break;
							}
						}
					}
					// Read child tag element of way
					if (startElement.getName().getLocalPart().equals(OSMParam.TAG)) {	// read tag
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(OSMParam.K))
								kAttr = attribute.getValue();
							if (attribute.getName().toString().equals(OSMParam.V))
								vAttr = attribute.getValue();
						}
						if(kAttr.equals(OSMParam.NAME)) {
							name = vAttr;
						}
						else if(kAttr.equals(OSMParam.HIGHWAY)) {
							highway = vAttr;
						}
						else if(kAttr.equals(OSMParam.ONEWAY)) {
							if(vAttr.equals(OSMParam.YES))
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
					
					if (startElement.getName().getLocalPart().equals(OSMParam.RELATION)) {	// skip relation
						break;
					}
				}
				
				// If we reach the end of an item element we add it to the list
				if (event.isEndElement()) {
			       	EndElement endElement = event.asEndElement();
			       	if (endElement.getName().getLocalPart().equals(OSMParam.NODE)) {
			       		nodeInfo = new NodeInfo(nodeId, location);
			       		nodeArrayList.add(nodeInfo);
			       	}
			       	if (endElement.getName().getLocalPart().equals(OSMParam.WAY)) {
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
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.extraNodeFile;
			checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
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
			String fileName = OSMParam.root + OSMParam.SEGMENT + OSMParam.osmFile;
			checkFileExist(fileName);
			File file = new File(fileName);
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




