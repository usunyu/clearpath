package controller;

import java.util.*;
import object.*;
import model.*;
import global.*;

/*
 * 2nd step for OSM Project
 * 1) read the data from OSM file
 * 2) generate xxx_node.csv and xxx_way.csv from data
 * 
 * format:
 * xxx_node.csv
 * nodeId        |          lat           |         lon
 * (id of node)|(latitude of node)|(longitude of node)
 * 
 * xxx_way.csv
 * wayId        |          isOneway          |       name     |   highway
 * (id of way)|(O:oneway B:bidirectional way)|(name of street)|(type of way)
 * 
 * type of way, refer to http://wiki.openstreetmap.org/wiki/Key:highway
 */

public class OSMInputFileGeneration {
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMParam.paramConfig();
		
		OSMInput.readOsmFileStax();
		OSMOutput.writeCSVFile();
		// we do not need way list and node list any more, free memory
		OSMData.wayArrayList = new ArrayList<WayInfo>();
		OSMData.nodeArrayList = new ArrayList<NodeInfo>();
	}
}
