package main;

import java.io.*;
import java.util.*;

import model.*;
import global.*;
import object.*;

public class GenerateAdjList {
	/**
	 * read edge's pattern
	 */
	public static void readPatternFile() {
		System.out.println("read pattern file...");
		int debug = 0;
		try {
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.patternCVSFile;
			OSMInput.checkFileExist(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				debug++;
				String[] nodes = strLine.split(OSMParam.COMMA);
				long id = Long.parseLong(nodes[0]);
				ArrayList<Double> pattern = new ArrayList<Double>(420);
				for(int i = 1; i < nodes.length; i++) {
					pattern.add(Double.parseDouble(nodes[i]));
				}
				OSMData.edgePatternHashMap.put(id, pattern);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readPatternFile: debug code: " + debug);
		}
		System.out.println("read pattern file finish!");
	}
	
	/**
	 * read edge's pattern from resource
	 */
	public static void readPatternFileRes() {
		System.out.println("read pattern file...");
		int debug = 0;
		try {
			String file = OSMParam.patternCVSFile;
			//OSMInput.checkFileExist(file);
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
				String[] nodes = strLine.split(OSMParam.COMMA);
				long id = Long.parseLong(nodes[0]);
				ArrayList<Double> pattern = new ArrayList<Double>(420);
				for(int i = 1; i < nodes.length; i++) {
					pattern.add(Double.parseDouble(nodes[i]));
				}
				OSMData.edgePatternHashMap.put(id, pattern);
			}
			br.close();
			in.close();
			istream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("readPatternFile: debug code: " + debug);
		}
		System.out.println("read pattern file finish!");
	}
	
	/**
	 * read edge from old format file
	 */
	public static void readEdgeFileOld() {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			// from highway type to identity the index
			HashMap<String, Integer> highwayTypeCache = new HashMap<String, Integer>();
			String file = OSMParam.root + OSMParam.SEGMENT + OSMParam.edgeCVSFileOld;
			OSMInput.checkFileExist(file);
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
				String nodeListStr = nodes[4];
				int distance = Integer.parseInt(nodes[5]);
				
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
				EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highwayId, false, nodeList, distance);
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
	 * read old format edge file from resource
	 */
	public static void readEdgeFileOldRes() {
		System.out.println("read edge file...");
		int debug = 0;
		try {
			// from highway type to identity the index
			HashMap<String, Integer> highwayTypeCache = new HashMap<String, Integer>();
			String file = OSMParam.edgeCVSFileOld;
			//OSMInput.checkFileExist(file);
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
				String nodeListStr = nodes[4];
				int distance = Integer.parseInt(nodes[5]);
				
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
				EdgeInfo edge = new EdgeInfo(wayId, edgeId, name, highwayId, false, nodeList, distance);
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
	 * build adjlist for 7 days
	 */
	public static void buildAdjList() {
		System.out.println("build adjlist...");
		for(long id : OSMData.edgePatternHashMap.keySet()) {
			ArrayList<Double> pattern = OSMData.edgePatternHashMap.get(id);
			EdgeInfo edge = OSMData.edgeHashMap.get(id);
			long startNode = edge.getStartNode();
			long endNode = edge.getEndNode();
			ArrayList<ArrayList<Integer>> travelTimeArray = new ArrayList<ArrayList<Integer>>(7);
			for(int day = 0; day < 7; day++) {
				ArrayList<Integer> array = new ArrayList<Integer>(60);
				for(int i = day * 60; i < (day + 1) * 60; i++) {
					array.add((int)(pattern.get(i) * 1000));
				}
				travelTimeArray.add(array);
			}
			ToNodeInfo toNode = new ToNodeInfo(endNode, travelTimeArray);
			LinkedList<ToNodeInfo> toNodeList;
			if(OSMData.adjListHashMap.containsKey(startNode)) {
				toNodeList = OSMData.adjListHashMap.get(startNode);
			}
			else {
				toNodeList = new LinkedList<ToNodeInfo>();
				OSMData.adjListHashMap.put(startNode, toNodeList);
			}
			toNodeList.add(toNode);
		}
		System.out.println("build adjlist finish!");
	}
	
	/**
	 * write 7 days adjlist
	 */
	public static void writeAdjList() {
		System.out.println("write adjlist file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(OSMParam.adjlistFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(long nodeId : OSMData.adjListHashMap.keySet()) {
				debug++;
				String strLine;
				strLine = nodeId + OSMParam.SEPARATION;
				out.write(strLine);
				LinkedList<ToNodeInfo> localNodeList = OSMData.adjListHashMap.get(nodeId);

				// this node cannot go to any other node
				if (localNodeList == null)
					continue;

				for(ToNodeInfo toNode : localNodeList) {
					boolean isFix = toNode.isFix();
					if (!isFix) {
						// assign travel time
						ArrayList<ArrayList<Integer>> timeArrayList = toNode.getTravelTimeArray();
						strLine = "";
						strLine += toNode.getNodeId() + "(" + OSMParam.VARIABLE + ")" + OSMParam.COLON;
						out.write(strLine);
						// for 7 days
						for(int j = 0; j < timeArrayList.size(); j++) {
							ArrayList<Integer> timeList = timeArrayList.get(j);
							strLine = "";
							// for 60 times
							for (int k = 0; k < timeList.size(); k++) {
								strLine += timeList.get(k);
								if(j == timeArrayList.size() - 1 && k == timeList.size() - 1) {
									strLine += OSMParam.SEMICOLON;
								}
								else {
									strLine += OSMParam.COMMA;
								}
							}
							out.write(strLine);
						}
					} else {
						int travelTime = toNode.getTravelTime();
						strLine += toNode.getNodeId() + "(" + OSMParam.FIX + ")" + OSMParam.COLON;
						strLine += travelTime + OSMParam.SEMICOLON;
						out.write(strLine);
					}
				}
				out.write(OSMParam.LINEEND);
				out.flush();
				if(debug % 1000 == 0)
					System.out.println((double)debug / OSMData.adjListHashMap.keySet().size() * 100 + "%");
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeAdjList: debug code: " + debug);
		}
		System.out.println("write adjlist file finish!");
	}
	
	public static void main(String[] args) {
		OSMParam.paramConfig();
		readEdgeFileOldRes();
		readPatternFileRes();
		// for 7 days
		buildAdjList();
		writeAdjList();
	}

}
