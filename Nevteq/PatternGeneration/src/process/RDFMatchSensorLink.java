package process;

import java.util.*;

import data.*;
import objects.*;
import function.*;

public class RDFMatchSensorLink {

	/**
	 * @param arguments
	 */
	static double searchDistance = 0.15;
	static double corssSearchDistance = 0.15;
	static double noDirSearchDistance = 0.01;
	static int devide = 10;
	static int thirdRoundTime = 3;
	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
	
	public static void main(String[] args) {
		RDFInput.readNodeFile(RDFData.nodeMap);
		RDFInput.readLinkFile(RDFData.linkMap, RDFData.nodeMap);
		RDFInput.readLinkGeometry(RDFData.linkMap);
		
		RDFInput.buildNodeAdjList(RDFData.linkMap, RDFData.nodeAdjList);
		RDFInput.buildNodeToLinkMap(RDFData.linkMap, RDFData.nodeToLinkMap);
		
		RDFInput.fetchSensor(RDFData.sensorMatchMap);
		matchLinkSensor(RDFData.linkMap, RDFData.nodeMap, RDFData.sensorMatchMap, RDFData.nodeAdjList, RDFData.nodeToLinkMap);
		RDFOutput.writeSensorMatch(RDFData.linkMap);
	}
	
	private static void matchLinkSensor(HashMap<Long, RDFLinkInfo> linkMap, HashMap<Long, RDFNodeInfo> nodeMap, 
			HashMap<Integer, SensorInfo> allSensorMap, HashMap<Long, LinkedList<Long>> nodeAdjList,
			HashMap<String, RDFLinkInfo> nodeToLinkMap) {
		HashMap<Integer, SensorInfo> matchSensorMap = new HashMap<Integer, SensorInfo>();
		// 3 round match algorithm
		System.out.println("match sensors to links...");
		// 1) match same direction
		firstRoundMatch(matchSensorMap, linkMap, allSensorMap);
		// 2) match direction 0 to 3, 1 to 2
		secondRoundMatch(matchSensorMap, linkMap, allSensorMap);
		// 3) match nearest link
		for (int i = 0; i < thirdRoundTime; i++)
			thirdRoundMatch(matchSensorMap, linkMap, nodeMap, nodeAdjList, nodeToLinkMap);
		// override, store in data share
		RDFData.sensorMatchMap = matchSensorMap;
		System.out.println("match Sensors finish!");
	}
	
	private static void firstRoundMatch(HashMap<Integer, SensorInfo> matchSensorMap, HashMap<Long, RDFLinkInfo> linkMap,
			HashMap<Integer, SensorInfo> allSensorMap) {
		System.out.println("first round...");
		int debug = 0;
		try {
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				// just match highway
				if(link.getFunctionalClass() != 1 && link.getFunctionalClass() != 2)
					continue;
				LinkedList<LocationInfo> pointsList = link.getPointList();
				for (double step = searchDistance / devide; step < searchDistance; step += step) {
					ListIterator<LocationInfo> pointIterator = pointsList.listIterator();
					while(pointIterator.hasNext()) {
						LocationInfo nLoc = pointIterator.next();
						// for every sensor
						for(SensorInfo sensor : allSensorMap.values()) {
							LocationInfo sLoc = sensor.getLocation();
							// same direction
							LinkedList<Integer> directionList = link.getDirectionList();
							for(int dir : directionList) {
								if (sensor.getDirection() == dir) {
									double distance = Geometry.calculateDistance(nLoc, sLoc);
									// in the search area
									if (distance < step) {
										// match sensor
										if (!link.containsSensor(sensor))
											link.addSensor(sensor);
										if (!matchSensorMap.containsKey(sensor.getSensorId())) {
											matchSensorMap.put(sensor.getSensorId(), sensor);
										}
									}
								}
							}
						}
					}
				}
				if (debug % 1000 == 0 || debug == linkMap.size())
					System.out.println((float) debug / linkMap.size() * 100 + "%");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("firstRoundMatch: debug code " + debug);
		}
		System.out.println("first round finish!");
	}
	
	private static void secondRoundMatch(HashMap<Integer, SensorInfo> matchSensorMap, HashMap<Long, RDFLinkInfo> linkMap,
			HashMap<Integer, SensorInfo> allSensorMap) {
		System.out.println("second round...");
		int debug = 0;
		try {
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				// just match highway
				if(link.getFunctionalClass() != 1 && link.getFunctionalClass() != 2)
					continue;
				LinkedList<LocationInfo> pointsList = link.getPointList();
				for (double step = corssSearchDistance / devide; step < searchDistance; step += step) {
					ListIterator<LocationInfo> pointIterator = pointsList.listIterator();
					while(pointIterator.hasNext()) {
						LocationInfo nLoc = pointIterator.next();
						// for every sensor
						for(SensorInfo sensor : allSensorMap.values()) {
							LocationInfo sLoc = sensor.getLocation();
							// same direction
							LinkedList<Integer> directionList = link.getDirectionList();
							for(int dir : directionList) {
								// 0 : 3, 1 : 2
								if ((sensor.getDirection() == 0 && dir == 3)
										|| (sensor.getDirection() == 3 && dir == 0)
										|| (sensor.getDirection() == 1 && dir == 2)
										|| (sensor.getDirection() == 2 && dir == 1)) {
									double distance = Geometry.calculateDistance(nLoc, sLoc);
									// in the search area
									if (distance < step) {
										// match sensor
										if (!link.containsSensor(sensor))
											link.addSensor(sensor);
										if (!matchSensorMap.containsKey(sensor.getSensorId())) {
											matchSensorMap.put(sensor.getSensorId(), sensor);
										}
									}
								}
							}
						}
					}
				}
				if (debug % 1000 == 0 || debug == linkMap.size())
					System.out.println((float) debug / linkMap.size() * 100 + "%");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("secondRoundMatch: debug code " + debug);
		}
		System.out.println("second round finish!");
	}
	
	private static void thirdRoundMatch(HashMap<Integer, SensorInfo> matchSensorMap, HashMap<Long, RDFLinkInfo> linkMap,
			HashMap<Long, RDFNodeInfo> nodeMap, HashMap<Long, LinkedList<Long>> nodeAdjList, HashMap<String, RDFLinkInfo> nodeToLinkMap) {
		System.out.println("third round...");
		int debug = 0;
		try {
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				// just match highway
				if(link.getFunctionalClass() != 1 && link.getFunctionalClass() != 2)
					continue;
				LinkedList<SensorInfo> linkSensorList = link.getSensorList();
				if(linkSensorList == null || linkSensorList.size() == 0)
					continue;
				long nodeId1 = link.getRefNodeId();
				LinkedList<Long> adjList1 = nodeAdjList.get(nodeId1);
				ListIterator<Long> adjIterator1 = adjList1.listIterator();
				while(adjIterator1.hasNext()) {
					long nodeId2 = adjIterator1.next();
					String nodesStr = nodeId1 + SEPARATION + nodeId2;
					RDFNodeInfo nearNode = nodeMap.get(nodeId2);
					if (nodeToLinkMap.containsKey(nodesStr)) {
						RDFLinkInfo nearLink = nodeToLinkMap.get(nodesStr);
						if (nearLink.getSensorList() == null || nearLink.getSensorList().size() == 0) {
							// match
							ListIterator<SensorInfo> sensorIterator = linkSensorList.listIterator();
							SensorInfo nearestSensor = sensorIterator.next();
							double minDis = Geometry.calculateDistance(nearNode.getLocation(), nearestSensor.getLocation());
							while(sensorIterator.hasNext()) {
								SensorInfo otherSensor = sensorIterator.next();
								double dis = Geometry.calculateDistance(nearNode.getLocation(), otherSensor.getLocation());
								if (dis < minDis) {
									nearestSensor = otherSensor;
								}
							}
							nearLink.addSensor(nearestSensor);
						}
					}
				}
				
				long nodeId3 = link.getNonRefNodeId();
				LinkedList<Long> adjList2 = nodeAdjList.get(nodeId3);
				ListIterator<Long> adjIterator2 = adjList2.listIterator();
				while(adjIterator2.hasNext()) {
					long nodeId4 = adjIterator2.next();
					String nodesStr = nodeId3 + SEPARATION + nodeId4;
					RDFNodeInfo nearNode = nodeMap.get(nodeId4);
					if (nodeToLinkMap.containsKey(nodesStr)) {
						RDFLinkInfo nearLink = nodeToLinkMap.get(nodesStr);
						if (nearLink.getSensorList() == null || nearLink.getSensorList().size() == 0) {
							// match
							ListIterator<SensorInfo> sensorIterator = linkSensorList.listIterator();
							SensorInfo nearestSensor = sensorIterator.next();
							double minDis = Geometry.calculateDistance(nearNode.getLocation(), nearestSensor.getLocation());
							while(sensorIterator.hasNext()) {
								SensorInfo otherSensor = sensorIterator.next();
								double dis = Geometry.calculateDistance(nearNode.getLocation(), otherSensor.getLocation());
								if (dis < minDis) {
									nearestSensor = otherSensor;
								}
							}
							nearLink.addSensor(nearestSensor);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("thirdRoundMatch: debug code " + debug);
		}
		System.out.println("third round finish!");
	}
}
