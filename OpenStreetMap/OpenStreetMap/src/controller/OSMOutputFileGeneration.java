package controller;

import model.*;
import global.*;
import object.*;

public class OSMOutputFileGeneration {
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMParam.paramConfig();
		
		OSMInput.readNodeFile();
		OSMInput.readWayFile();
		OSMInput.readWayInfo();
		OSMInput.readWktsFile();
		OSMInput.readNodeLocationGrid();
		
		// over write the cvs file
		OSMOutput.writeNodeFile();
		OSMOutput.writeWayFile();
		OSMOutput.writeWayInfo();
	}
}
