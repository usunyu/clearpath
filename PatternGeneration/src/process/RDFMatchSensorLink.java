package process;

import java.util.*;

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
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param connect
	 */
	static HashMap<Long, LinkedList<Long>> adjNodeList = new HashMap<Long, LinkedList<Long>>();
	// two nodes decide one link
	static HashMap<String, RDFLinkInfo> nodeToLink = new HashMap<String, RDFLinkInfo>();
	/**
	 * @param sensor
	 */
	static LinkedList<SensorInfo> sensorList = new LinkedList<SensorInfo>();
	static LinkedList<SensorInfo> matchSensorList = new LinkedList<SensorInfo>();
	
	public static void main(String[] args) {
		RDFInput.readNodeFile(nodeMap);
		RDFInput.readLinkFile(linkMap, nodeMap, adjNodeList, nodeToLink);
		RDFInput.readLinkGeometry(linkMap);
		RDFInput.fetchSensor(sensorList);
		matchLinkSensor();
		RDFOutput.writeSensorMatch(linkMap);
	}
	
	private static void matchLinkSensor() {
		HashSet<Integer> matchSensorDuplicate = new HashSet<Integer>();
		// 3 round match algorithm
		System.out.println("match sensors to links...");
		// 1) match same direction
		firstRoundMatch(matchSensorDuplicate);
		// 2) match direction 0 to 3, 1 to 2
		secondRoundMatch(matchSensorDuplicate);
		// 3) match nearest link
		for (int i = 0; i < thirdRoundTime; i++)
			thirdRoundMatch(matchSensorDuplicate);
		System.out.println("match Sensors finish!");
	}
	
	private static void firstRoundMatch(HashSet<Integer> matchSensorDuplicate) {
		System.out.println("first round...");
		int debug = 0;
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
					ListIterator<SensorInfo> sensorIterator = sensorList.listIterator();
					while(sensorIterator.hasNext()) {
						SensorInfo sensor = sensorIterator.next();
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
									if (!matchSensorDuplicate.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										matchSensorDuplicate.add(sensor.getSensorId());
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
		System.out.println("first round finish!");
	}
	
	private static void secondRoundMatch(HashSet<Integer> matchSensorDuplicate) {
		System.out.println("second round...");
		int debug = 0;
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
					ListIterator<SensorInfo> sensorIterator = sensorList.listIterator();
					while(sensorIterator.hasNext()) {
						SensorInfo sensor = sensorIterator.next();
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
									if (!matchSensorDuplicate.contains(sensor.getSensorId())) {
										matchSensorList.add(sensor);
										matchSensorDuplicate.add(sensor.getSensorId());
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
		System.out.println("second round finish!");
	}
	
	private static void thirdRoundMatch(HashSet<Integer> matchSensorDuplicate) {
		System.out.println("third round...");
		int debug = 0;
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
			LinkedList<Long> adjList1 = adjNodeList.get(nodeId1);
			ListIterator<Long> adjIterator1 = adjList1.listIterator();
			while(adjIterator1.hasNext()) {
				long nodeId2 = adjIterator1.next();
				String nodesStr = nodeId1 + SEPARATION + nodeId2;
				RDFNodeInfo nearNode = nodeMap.get(nodeId2);
				if (nodeToLink.containsKey(nodesStr)) {
					RDFLinkInfo nearLink = nodeToLink.get(nodesStr);
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
			LinkedList<Long> adjList2 = adjNodeList.get(nodeId3);
			ListIterator<Long> adjIterator2 = adjList2.listIterator();
			while(adjIterator2.hasNext()) {
				long nodeId4 = adjIterator2.next();
				String nodesStr = nodeId3 + SEPARATION + nodeId4;
				RDFNodeInfo nearNode = nodeMap.get(nodeId4);
				if (nodeToLink.containsKey(nodesStr)) {
					RDFLinkInfo nearLink = nodeToLink.get(nodesStr);
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
		System.out.println("third round finish!");
	}
}
