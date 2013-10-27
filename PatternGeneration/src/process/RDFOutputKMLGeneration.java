package process;

import java.util.*;

import objects.*;
import function.*;

public class RDFOutputKMLGeneration {
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param sensor
	 */
	static HashMap<Integer, SensorInfo> matchSensorMap = new HashMap<Integer, SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	/**
	 * @param carpool
	 */
	static HashSet<Long> carpoolSet = new HashSet<Long>();
	
	public static void main(String[] args) {
		
		RDFInput.readNodeFile(nodeMap);
		RDFOutput.generateNodeKML(nodeMap);
		
		RDFInput.readLinkFile(linkMap, nodeMap);
		RDFInput.readLinkGeometry(linkMap);
		RDFInput.readLinkLane(linkMap);
		
		RDFInput.fetchSensor(sensorMap);
		RDFInput.readMatchSensor(linkMap, sensorMap, matchSensorMap);
		RDFOutput.generateSensorKML(matchSensorMap);
		
		RDFOutput.generateLinkKML(linkMap);
	}
}
