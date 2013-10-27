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
	/**
	 * carpool which not contained in database
	 */
	public static void manualCarpool() {
		carpoolSet.add(859176689l);
		carpoolSet.add(859176688l);
		carpoolSet.add(858795235l);
		carpoolSet.add(858795234l);
		carpoolSet.add(110161947l);
		carpoolSet.add(939442621l);
		carpoolSet.add(939442620l);
		carpoolSet.add(783652297l);
		carpoolSet.add(783652296l);
		carpoolSet.add(28432663l);
		carpoolSet.add(733916910l);
		carpoolSet.add(782774872l);
		carpoolSet.add(782774871l);
		carpoolSet.add(776739442l);
		carpoolSet.add(932209462l);
		carpoolSet.add(932209461l);
		carpoolSet.add(37825166l);
		carpoolSet.add(859175347l);
		carpoolSet.add(859175346l);
		carpoolSet.add(28432674l);
		carpoolSet.add(24612549l);
		carpoolSet.add(857627783l);
		carpoolSet.add(857627782l);
		carpoolSet.add(110160325l);
		carpoolSet.add(121237589l);
		carpoolSet.add(121234936l);
		carpoolSet.add(24612504l);
		carpoolSet.add(28432725l);
		carpoolSet.add(766693715l);
		carpoolSet.add(766693714l);
		carpoolSet.add(28432747l);
		carpoolSet.add(28432746l);
		carpoolSet.add(121238550l);
		carpoolSet.add(932222662l);
		carpoolSet.add(932222661l);
		carpoolSet.add(928343450l);
		carpoolSet.add(23928234l);
		carpoolSet.add(121238464l);
		carpoolSet.add(121238462l);
		carpoolSet.add(121234999l);
		carpoolSet.add(121238440l);
		carpoolSet.add(110162068l);
		carpoolSet.add(121240472l);
		carpoolSet.add(859174498l);
		carpoolSet.add(859174497l);
		carpoolSet.add(121241061l);
		carpoolSet.add(932209460l);
		carpoolSet.add(932209459l);
		carpoolSet.add(121235021l);
		carpoolSet.add(121235045l);
		carpoolSet.add(121235048l);
		carpoolSet.add(121235036l);
		carpoolSet.add(121235024l);
		carpoolSet.add(121233715l);
		carpoolSet.add(121241020l);
		carpoolSet.add(121241019l);
		carpoolSet.add(810665747l);
		carpoolSet.add(810665746l);
		carpoolSet.add(783167929l);
		carpoolSet.add(783167928l);
		carpoolSet.add(783167927l);
		carpoolSet.add(24559194l);
		carpoolSet.add(121240993l);
		carpoolSet.add(932219289l);
		carpoolSet.add(932219288l);
		carpoolSet.add(810665759l);
		carpoolSet.add(810665758l);
		carpoolSet.add(810862652l);
		carpoolSet.add(810862651l);
		carpoolSet.add(812029169l);
		carpoolSet.add(833175656l);
		carpoolSet.add(833175655l);
		carpoolSet.add(28433679l);
		carpoolSet.add(37825151l);
		carpoolSet.add(812029177l);
	}
	
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
