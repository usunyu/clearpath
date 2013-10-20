package input;

import object.*;
import function.*;

import java.util.*;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/*
 * 2nd step for OSM Project
 * 1) read the data from OSM file
 * 2) generate xxx_node.csv and xxx_way.csv from data
 * 
 * format:
 * xxx_node.csv
 * nodeId        ,          lat           ,         lon
 * (id of node),(latitude of node),(longitude of node)
 * 
 * xxx_way.csv
 * wayId        ,          isOneway                      ,       name         ,   highway
 * (id of way),(O:oneway B:bidirectional way),(name of street),(type of way)
 * 
 * type of way, refer to http://wiki.openstreetmap.org/wiki/Key:highway
 */

public class OSMInputFileGeneration {
	/**
	 * @param node
	 */
	static ArrayList<NodeInfo> nodeArrayList = new ArrayList<NodeInfo>();
	/**
	 * @param way
	 */
	static ArrayList<WayInfo> wayArrayList = new ArrayList<WayInfo>();
	
	public static void run(String[] args) {
		main(args);
	}
	
	public static void main(String[] args) {
		OSMInput.paramConfig(args[0]);
		OSMInput.readOsmFileStax(wayArrayList, nodeArrayList);
		OSMOutput.paramConfig(args[0]);
		OSMOutput.writeCSVFile(wayArrayList, nodeArrayList);
	}
}
