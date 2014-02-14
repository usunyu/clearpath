package model;

import java.text.*;
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
	 * cache for from lot and lon to node
	 */
	public static void readNodeLocationGrid() {
		System.out.println("preparing for nodes lat and lon...");
		for(NodeInfo node : OSMData.nodeHashMap.values()) {
			LocationInfo location = node.getLocation();
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			DecimalFormat df=new DecimalFormat("0.0");
			String latLonId = df.format(lat) + OSMParam.COMMA + df.format(lon);
			//System.out.println(latLonId);
			LinkedList<NodeInfo> nodeList;
			if(OSMData.nodeLocationGridMap.containsKey(latLonId)) {
				nodeList = OSMData.nodeLocationGridMap.get(latLonId);
			}
			else {
				nodeList = new LinkedList<NodeInfo>();
				OSMData.nodeLocationGridMap.put(latLonId, nodeList);
			}
			nodeList.add(node);
		}
		System.out.println("preparing for nodes lat and lon finish!");
	}
	
	/**
	 * we can find edge by node
	 */
	public static void addOnEdgeToNode() {
		System.out.println("preparing nodes on edge...");
		for(EdgeInfo edge : OSMData.edgeHashMap.values()) {
			LinkedList<Long> nodeList = edge.getNodeList();
			for(long nodeId : nodeList) {
				NodeInfo node = OSMData.nodeHashMap.get(nodeId);
				node.addOnEdge(edge);
			}
		}
		System.out.println("preparing nodes on edge finish!");
	}
	
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
	 * build adjList and adjReverseList from adjlist.csv for 7 days
	 */
	public static void readAdjList() {
		System.out.println("loading adjlist file: " + OSMParam.adjlistFile + "...");

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
				//if (debug % 100000 == 0)
				//	System.out.println("completed " + debug + " lines.");

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
						toNodeInfo.setMinTravelTime(travelTime);
						toNodeList.add(toNodeInfo);
						// build the reverse adjlist
						LinkedList<ToNodeInfo> fromNodeList;
						if(OSMData.adjReverseListHashMap.containsKey(toNode)) {
							fromNodeList = OSMData.adjReverseListHashMap.get(toNode);
						}
						else {
							fromNodeList = new LinkedList<ToNodeInfo>();
							OSMData.adjReverseListHashMap.put(toNode, fromNodeList);
						}
						ToNodeInfo fromNodeInfo = new ToNodeInfo(startNode, travelTime);
						fromNodeInfo.setMinTravelTime(travelTime);
						fromNodeList.add(fromNodeInfo);
					} else { // variable
						String timeList = adjComponent.substring(adjComponent.indexOf(OSMParam.COLON) + 1);
						String[] timeValueList = timeList.split(OSMParam.COMMA);
						int minTravelTime = Integer.MAX_VALUE;
						ArrayList<ArrayList<Integer>> travelTimeArray = new ArrayList<ArrayList<Integer>>();
						for(int day = 0; day < 7; day++) {
							ArrayList<Integer> array = new ArrayList<Integer>();
							for(int j = day * 60; j < (day + 1) * 60 && j < timeValueList.length; j++) {
								int time = Integer.parseInt(timeValueList[j]);
								array.add(time);
								if(time < minTravelTime)
									minTravelTime = time;
							}
							travelTimeArray.add(array);
						}
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
						toNodeInfo.setMinTravelTime(minTravelTime);
						toNodeList.add(toNodeInfo);
						// build the reverse adjlist
						LinkedList<ToNodeInfo> fromNodeList;
						if(OSMData.adjReverseListHashMap.containsKey(toNode)) {
							fromNodeList = OSMData.adjReverseListHashMap.get(toNode);
						}
						else {
							fromNodeList = new LinkedList<ToNodeInfo>();
							OSMData.adjReverseListHashMap.put(toNode, fromNodeList);
						}
						ToNodeInfo fromNodeInfo = new ToNodeInfo(startNode, travelTimeArray);
						fromNodeInfo.setMinTravelTime(minTravelTime);
						fromNodeList.add(fromNodeInfo);
					}
				}
				OSMData.adjListHashMap.put(startNode, toNodeList);
			}

			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("buildList: debug code: " + debug);
		}
		System.out.println("building graph finish!");
	}
	
	/**
	 * load adjlist from jar
	 */
	public static void readAdjListRes() {
		System.out.println("loading adjlist file: " + OSMParam.adjlistFile);

		int debug = 0;
		try {
			String file = OSMParam.adjlistFile;
			//checkFileExist(file);
			InputStream istream = OSMResLoader.load(file);
			if(istream == null) {
				System.err.println("cannot find " + file + ", program exit!");
				System.exit(-1);
			}
			DataInputStream in = new DataInputStream(istream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph...");

			while ((strLine = br.readLine()) != null) {
				//if (debug % 100000 == 0)
				//	System.out.println("completed " + debug + " lines.");

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
						if(OSMData.adjReverseListHashMap.containsKey(toNode)) {
							fromNodeList = OSMData.adjReverseListHashMap.get(toNode);
						}
						else {
							fromNodeList = new LinkedList<ToNodeInfo>();
							OSMData.adjReverseListHashMap.put(toNode, fromNodeList);
						}
						ToNodeInfo fromNodeInfo = new ToNodeInfo(startNode, travelTime);
						fromNodeList.add(fromNodeInfo);
					} else { // variable
						String timeList = adjComponent.substring(adjComponent.indexOf(OSMParam.COLON) + 1);
						String[] timeValueList = timeList.split(OSMParam.COMMA);
						int minTravelTime = Integer.MAX_VALUE;
						ArrayList<ArrayList<Integer>> travelTimeArray = new ArrayList<ArrayList<Integer>>();
						for(int day = 0; day < 7; day++) {
							ArrayList<Integer> array = new ArrayList<Integer>();
							for(int j = day * 60; j < (day + 1) * 60; j++) {
								debug++;
								int time = Integer.parseInt(timeValueList[j]);
								array.add(time);
								if(time < minTravelTime)
									minTravelTime = time;
							}
							travelTimeArray.add(array);
						}
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
						toNodeList.add(toNodeInfo);
						// build the reverse adjlist
						LinkedList<ToNodeInfo> fromNodeList;
						if(OSMData.adjReverseListHashMap.containsKey(toNode)) {
							fromNodeList = OSMData.adjReverseListHashMap.get(toNode);
						}
						else {
							fromNodeList = new LinkedList<ToNodeInfo>();
							OSMData.adjReverseListHashMap.put(toNode, fromNodeList);
						}
						ToNodeInfo fromNodeInfo = new ToNodeInfo(startNode, travelTimeArray);
						fromNodeList.add(fromNodeInfo);
					}
				}
				OSMData.adjListHashMap.put(startNode, toNodeList);
			}

			br.close();
			in.close();
			istream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("buildList: debug code: " + debug);
		}
		System.out.println("building graph finish!");
	}
	
	/**
	 * build adjList from adjlist.csv for 7 days, no reverse adjlist
	 */
	public static void readAdjListNoReverse() {
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
						int minTravelTime = Integer.MAX_VALUE;
						ArrayList<ArrayList<Integer>> travelTimeArray = new ArrayList<ArrayList<Integer>>();
						for(int day = 0; day < 7; day++) {
							ArrayList<Integer> array = new ArrayList<Integer>();
							for(int j = day * 60; j < (day + 1) * 60; j++) {
								int time = Integer.parseInt(timeValueList[j]);
								array.add(time);
								if(time < minTravelTime)
									minTravelTime = time;
							}
							travelTimeArray.add(array);
						}
						ToNodeInfo toNodeInfo = new ToNodeInfo(toNode, travelTimeArray);
						toNodeList.add(toNodeInfo);
					}
				}
				OSMData.adjListHashMap.put(startNode, toNodeList);
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
	 */
	public static void readEdgeFile() {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			// from highway type to identity the index
			HashMap<String, Integer> highwayTypeCache = new HashMap<String, Integer>();
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
				int highwayId;
				if(highwayTypeCache.containsKey(highway)) {
					highwayId = highwayTypeCache.get(highway);
				}
				else {
					highwayId = OSMData.edgeHighwayTypeList.size();
					highwayTypeCache.put(highway, highwayId);
					// add to highway list
					OSMData.edgeHighwayTypeList.add(highway);
				}
				EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highwayId, isOneway, nodeList, distance);
				OSMData.edgeHashMap.put(edge.getId(), edge);
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
	 * load edge from jar
	 */
	public static void readEdgeRes() {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			// from highway type to identity the index
			HashMap<String, Integer> highwayTypeCache = new HashMap<String, Integer>();
			String file = OSMParam.edgeCVSFile;
			//checkFileExist(file);
			InputStream istream = OSMResLoader.load(file);
			if(istream == null) {
				System.err.println("cannot find " + file + ", program exit!");
				System.exit(-1);
			}
			DataInputStream in = new DataInputStream(istream);
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
				int highwayId;
				if(highwayTypeCache.containsKey(highway)) {
					highwayId = highwayTypeCache.get(highway);
				}
				else {
					highwayId = OSMData.edgeHighwayTypeList.size();
					highwayTypeCache.put(highway, highwayId);
					// add to highway list
					OSMData.edgeHighwayTypeList.add(highway);
				}
				EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highwayId, isOneway, nodeList, distance);
				OSMData.edgeHashMap.put(edge.getId(), edge);
			}
			br.close();
			in.close();
			istream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readEdgeFile: debug code: " + debug);
		}
		System.out.println("read edge file finish!");
	}

	/**
	 * read wkts file, update way map and node map
	 */
	public static void readWktsFile() {
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
				WayInfo wayInfo = OSMData.wayHashMap.get(wayId);
				ArrayList<Long> localNodeArrayList = new ArrayList<Long>();
				for(int i = 1; i < nodes.length; i++) {
					long nodeId = Long.parseLong(nodes[i]);
					NodeInfo nodeInfo = OSMData.nodeHashMap.get(nodeId);
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
	 */
	public static void readWayInfo() {
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
				WayInfo wayInfo = OSMData.wayHashMap.get(wayId);
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
	 */
	public static void readWayFile() {
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
				OSMData.wayHashMap.put(wayId, wayInfo);
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
	 */
	public static void readNodeFile() {
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
				OSMData.nodeHashMap.put(nodeId, nodeInfo);
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
	 * load file from jar
	 */
	public static void readNodeRes() {
		System.out.println("read node file...");
		int debug = 0;
		try {
			String file = OSMParam.nodeCSVFile;
			InputStream istream = OSMResLoader.load(file);
			if(istream == null) {
				System.err.println("cannot find " + file + ", program exit!");
				System.exit(-1);
			}
			DataInputStream in = new DataInputStream(istream);
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
				OSMData.nodeHashMap.put(nodeId, nodeInfo);
			}
			br.close();
			in.close();
			istream.close();
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
	 */
	public static void readOsmFileStax() {
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
			       		OSMData.nodeArrayList.add(nodeInfo);
			       	}
			       	if (endElement.getName().getLocalPart().equals(OSMParam.WAY)) {
		        		// skip nodeArrayList, we add it from wkts file
		        		wayInfo = new WayInfo(wayId, isOneway, name, highway);
		        		wayInfo.setInfoHashMap(infoHashMap);
		        		OSMData.wayArrayList.add(wayInfo);
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
	 */
	public static void readExtraFile() {
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
				OSMData.nodeArrayList.add(nodeInfo);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			e.printStackTrace();
			System.err.println("readExtraFile: debug code: " + debug);
		}
		System.out.println("read extra file finish!");
	}
	
	/**
	 * @deprecated can not deal with large amount of XML file
	 */
	public static void readOsmFile() {
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
					
					OSMData.nodeArrayList.add(nodeInfo);
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
					
					OSMData.wayArrayList.add(wayInfo);
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




