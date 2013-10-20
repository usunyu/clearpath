package function;

import object.*;

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
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class OSMInput {
	/**
	 * @param file
	 */
	static String root = "file";
	static String osmFile;
	static String nodeTxtFile;
	static String wayTxtFile;
	static String extraNodeFile;
	
	/**
	 * @param xml
	 */
	static final String ID 			= "id";
	static final String NODE 		= "node";
	static final String WAY 		= "way";
	static final String LAT 		= "lat";
	static final String LON 		= "lon";
	static final String TAG			= "tag";
	static final String K			= "k";
	static final String V			= "v";
	static final String NAME		= "name";
	static final String HIGHWAY		= "highway";
	static final String ONEWAY		= "oneway";
	static final String YES			= "yes";
	static final String RELATION	= "relation";
	
	private static void paramConfig(String name) {
		osmFile 		= name + ".osm";
		nodeTxtFile 	= name + "_node.txt";
		wayTxtFile 		= name + "_way.txt";
		extraNodeFile	= name + "_way_extra.txt";
	}
	
	/**
	 * use sTaX API to read OSM(XML) file instead of using DOM
	 * refer to http://www.vogella.com/articles/JavaXML/article.html
	 * @param wayArrayList
	 * @param nodeArrayList
	 */
	public static void readOsmFileStax(ArrayList<WayInfo> wayArrayList, ArrayList<NodeInfo> nodeArrayList) {
		System.out.println("read osm file...");
		int debug = 0;
		try {
			// First create a new XMLInputFactory
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			// Setup a new eventReader
			InputStream in = new FileInputStream(root + "/" + osmFile);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		    // Read the XML document
			// NodeInfo
			NodeInfo nodeInfo = null;
			long nodeId = 0;
			LocationInfo location = null;
			double latitude = 0;
			double longitude = 0;
			// WayInfo
			WayInfo wayInfo = null;
			long wayId = 0;
			String kAttr = null;
			String vAttr = null;
			String name = null;
			String highway = null;
			boolean isOneway = false;
			while (eventReader.hasNext()) {
				debug++;
				XMLEvent event = eventReader.nextEvent();
				 
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					// If we have a item element we create a new item
					if (startElement.getName().getLocalPart().equals(NODE)) {	// read node
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(ID))
								nodeId = Long.parseLong(attribute.getValue());
							if (attribute.getName().toString().equals(LAT))
								latitude = Double.parseDouble(attribute.getValue());
							if (attribute.getName().toString().equals(LON))
								longitude = Double.parseDouble(attribute.getValue());
						}
						location = new LocationInfo(latitude, longitude);
					}
					// If we have a item element we create a new item
					if (startElement.getName().getLocalPart().equals(WAY)) {	// read way
						// set default
						isOneway = false;
						name = "null";
						highway = "null";
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(ID)) {
								wayId = Long.parseLong(attribute.getValue());
								break;
							}
						}
					}
					// Read child tag element of way
					if (startElement.getName().getLocalPart().equals(TAG)) {	// read tag
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(K))
								kAttr = attribute.getValue();
							if (attribute.getName().toString().equals(V))
								vAttr = attribute.getValue();
						}
						if(kAttr.equals(NAME)) {
							name = vAttr;
						}
						else if(kAttr.equals(HIGHWAY)) {
							highway = vAttr;
						}
						else if(kAttr.equals(ONEWAY)) {
							if(vAttr.equals(YES))
								isOneway = true;
						}
						else {
							// TODO: put other in info map
						}
					}
					
					if (startElement.getName().getLocalPart().equals(RELATION)) {	// skip relation
						break;
					}
				}
				
				// If we reach the end of an item element we add it to the list
		        if (event.isEndElement()) {
		        	EndElement endElement = event.asEndElement();
		        	if (endElement.getName().getLocalPart().equals(NODE)) {
		        		nodeInfo = new NodeInfo(nodeId, location);
		        		nodeArrayList.add(nodeInfo);
		        	}
		        	if (endElement.getName().getLocalPart().equals(WAY)) {
		        		wayInfo = new WayInfo(wayId, isOneway, name, highway, null, null);
		        		wayArrayList.add(wayInfo);
		        	}
		        }
			}
		}
		catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("readOsmFileStax: debug code: " + debug);
	    }
		System.out.println("read osm file finish!");
	}
}




