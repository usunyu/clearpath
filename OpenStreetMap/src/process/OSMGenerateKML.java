package process;

import data.*;
import function.*;

public class OSMGenerateKML {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMOutput.paramConfig(args[0]);
		OSMOutput.generateWayKML(OSMData.wayHashMap, OSMData.nodeHashMap);
		OSMOutput.generateNodeKML(OSMData.nodeHashMap);
	}
}
