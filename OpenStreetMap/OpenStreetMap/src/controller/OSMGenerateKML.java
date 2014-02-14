package controller;

import model.*;
import global.*;

public class OSMGenerateKML {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMParam.paramConfig();
		// OSMOutput.generateWayKML();
		OSMOutput.generateEdgeKML();
		OSMOutput.generateNodeKML();
	}
}
