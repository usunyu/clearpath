package process;

import data.*;
import function.*;

public class RDFOutputKMLGeneration {
	
	public static void main(String[] args) {
		
		//RDFInput.readNodeFile(RDFData.nodeMap);
		//RDFOutput.generateNodeKML(RDFData.nodeMap);
		
		//RDFInput.readLinkFile(RDFData.linkMap, RDFData.nodeMap);
		//RDFInput.readLinkGeometry(RDFData.linkMap);
		//RDFInput.readLinkLane(RDFData.linkMap);
		
		//RDFInput.fetchSensor(RDFData.sensorMatchMap);
		//RDFInput.readMatchSensor(RDFData.linkMap, RDFData.sensorMatchMap);
		
		RDFOutput.generateSensorKML(RDFData.sensorMatchMap);
		
		RDFOutput.generateLinkKML(RDFData.linkMap);
	}
}
