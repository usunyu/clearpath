package controller;

import model.*;
import global.*;

public class OSMGenerateKML {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMParam.paramConfig(args[0]);
		OSMOutput.generateWayKML(OSMData.wayHashMap, OSMData.nodeHashMap);
		OSMOutput.generateNodeKML(OSMData.nodeHashMap);
	}
}
