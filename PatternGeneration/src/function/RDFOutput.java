package function;

import java.io.*;
import java.util.*;

import objects.*;

public class RDFOutput {
	/**
	 * @param file
	 */
	static String root				= "file";
	// for write link file
	static String linkFile			= "RDF_Link.csv";
	static String linkGeometryFile	= "RDF_Link_Geometry.csv";
	static String linkNameFile		= "RDF_Link_Name.csv";
	static String linkLaneFile		= "RDF_Link_Lane.csv";
	// for write node file
	static String nodeFile			= "RDF_Node.csv";
	// for write sensor file
	static String sensorMatchFile	= "RDF_Sensor_Match.csv";
	// for write kml file
	static String kmlLinkFile		= "RDF_Link.kml";
	static String kmlNodeFile		= "RDF_Node.kml";
	static String sensorMatchKML	= "RDF_Sensor_Match.kml";
	/**
	 * @param const
	 */
	static String SEPARATION		= ",";
	static String UNKNOWN 			= "Unknown Street";
	static String YES				= "Y";
	static String NO				= "N";
	
	/**
	 * generate link kml
	 * @param linkMap
	 */
	public static void generateLinkKML(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("generate link kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlLinkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");

			for(long linkId : linkMap.keySet()) {
				RDFLinkInfo link 	= linkMap.get(linkId);
				String baseName 	= link.getBaseName();
				long refNodeId 		= link.getRefNodeId();
				long nonRefNodeId 	= link.getNonRefNodeId();
				int functionalClass = link.getFunctionalClass();
				String travelDirection 	= link.getTravelDirection();
				LinkedList<Integer> directionList = link.getDirectionList();
				boolean ramp		= link.isRamp();
				boolean tollway		= link.isTollway();
				boolean carpool 	= link.isCarpool();
				boolean exitName	= link.isExitName();
				int speedCategory 	= link.getSpeedCategory();
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				LinkedList<SensorInfo>	sensorList = link.getSensorList();
				
				String dirStr	= null;
				for(int dir : directionList) {
					if(dirStr == null)
						dirStr = String.valueOf(dir);
					else
						dirStr += SEPARATION + dir;
				}
				
				String kmlStr = "<Placemark><name>Link:" + linkId + "</name>";
				kmlStr += "<description>";
				if(baseName.contains("&"))
					baseName = baseName.replaceAll("&", " and ");				
				kmlStr += "Name:" 			+ baseName + "\r\n";
				kmlStr += "Ref:" 			+ refNodeId + "\r\n";
				kmlStr += "Nonref:" 		+ nonRefNodeId + "\r\n";
				kmlStr += "Class:" 			+ functionalClass + "\r\n";
				kmlStr += "Category:" 		+ speedCategory + "\r\n";
				kmlStr += "Dir:" 			+ dirStr + "\r\n";
				kmlStr += "TraDir:" 		+ travelDirection + "\r\n";
				kmlStr += "Ramp:" 			+ ramp + "\r\n";
				kmlStr += "Tollway:" 		+ tollway + "\r\n";
				kmlStr += "Carpool:" 		+ carpool + "\r\n";
				kmlStr += "Exit:" 			+ exitName + "\r\n";
				if(sensorList != null && sensorList.size() != 0) {
					String sensorStr = "null";
					ListIterator<SensorInfo> sensorIt = sensorList.listIterator();
					int i = 0;
					while(sensorIt.hasNext()) {
						SensorInfo sensor = sensorIt.next();
						if(i++ == 0)
							sensorStr = String.valueOf(sensor.getSensorId());
						else
							sensorStr += SEPARATION + sensor.getSensorId();
					}
					kmlStr += "Sensor:" + sensorStr + "\r\n";
				}
				kmlStr += "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				ListIterator<LocationInfo> pIterator = pointsList.listIterator();
				while(pIterator.hasNext()) {
					LocationInfo loc = pIterator.next();
					kmlStr += loc.getLongitude()+ SEPARATION + loc.getLatitude()+ SEPARATION + loc.getZLevel() + " ";
				}
				kmlStr += "</coordinates></LineString></Placemark>";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate link kml finish!");
	}
	
	/**
	 * generate match sensor kml
	 * @param matchSensorMap
	 */
	public static void generateSensorKML(HashMap<Integer, SensorInfo> matchSensorMap) {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + sensorMatchKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			
			for(int sensorId : matchSensorMap.keySet()) {
				SensorInfo sensor = matchSensorMap.get(sensorId);
				String onStreet = sensor.getOnStreet();
				String fromStreet = sensor.getFromStreet();
				int direction = sensor.getDirection();
				
				LocationInfo location = sensor.getLocation();
				double lati = location.getLatitude();
				double longi = location.getLongitude();
				int zLevel = location.getZLevel();
				
				String kmlStr = "<Placemark><name>" + sensorId + "</name>";
				kmlStr += "<description>";
				kmlStr += "On: " + onStreet + "\r\n";
				kmlStr += "From: " + fromStreet + "\r\n";
				kmlStr += "Dir: " + direction + "\r\n";
				kmlStr += "</description>";
				kmlStr += "<Point><coordinates>";
				kmlStr +=  longi + SEPARATION + lati +  SEPARATION + zLevel;
				kmlStr += "</coordinates></Point></Placemark>";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate sensor kml finish!");
	}
	
	/**
	 * generate node kml
	 * @param nodeMap
	 */
	public static void generateNodeKML(HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("generate node kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + kmlNodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			
			for(long nodeId : nodeMap.keySet()) {
				RDFNodeInfo node = nodeMap.get(nodeId);
				LocationInfo location = node.getLocation();
				double lati = location.getLatitude();
				double longi = location.getLongitude();
				int zLevel = location.getZLevel();
				
				String kmlStr = "<Placemark><name>" + nodeId + "</name>";
				kmlStr += "<description>";
				kmlStr += "ZLevel: " + zLevel;
				kmlStr += "</description>";
				kmlStr += "<Point><coordinates>";
				kmlStr +=  longi + SEPARATION + lati +  SEPARATION + zLevel;
				kmlStr += "</coordinates></Point></Placemark>";
				
				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate node kml finish!");
	}
	
	/**
	 * write matched sensor
	 * @param linkMap
	 */
	public static void writeSensorMatch(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("write sensor match...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + sensorMatchFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(long linkId : linkMap.keySet()) {
				RDFLinkInfo link = linkMap.get(linkId);
				LinkedList<SensorInfo> sensorList = link.getSensorList();
				if(sensorList == null || sensorList.size() == 0)
					continue;
				ListIterator<SensorInfo> sIt = sensorList.listIterator();
				String sensorStr = "null";
				int i = 0;
				while(sIt.hasNext()) {
					SensorInfo sensor = sIt.next();
					if(i++ == 0)
						sensorStr = String.valueOf(sensor.getSensorId());
					else
						sensorStr += SEPARATION + sensor.getSensorId();
				}
				String strLine = linkId + SEPARATION + sensorStr + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write sensor match finish!");
	}
	
	/**
	 * write node to file
	 * @param nodeMap
	 */
	public static void writeNodeFile(HashMap<Long, RDFNodeInfo> nodeMap) {
		System.out.println("write node file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + nodeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(long nodeId : nodeMap.keySet()) {
				RDFNodeInfo node = nodeMap.get(nodeId);
				LocationInfo location = node.getLocation();
				String locationStr = location.getLatitude() + SEPARATION + location.getLongitude();
				int zLevel = location.getZLevel();
				String strLine = nodeId + SEPARATION + locationStr + SEPARATION + zLevel + "\r\n";
				out.write(strLine);
			}
			
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write node file finish!");
	}
	
	/**
	 * write link to file
	 * @param linkMap
	 */
	public static void writeLinkFile(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(long linkId : linkMap.keySet()) {
				RDFLinkInfo link = linkMap.get(linkId);
				
				long refNodeId = link.getRefNodeId();
				long nonRefNodeId = link.getNonRefNodeId();
				String baseName = link.getBaseName();
				if(baseName == null)
					baseName = UNKNOWN;
				int accessId = link.getAccessId();
				int functionalClass = link.getFunctionalClass();
				String travelDirection = link.getTravelDirection();
				boolean ramp = link.isRamp();
				boolean tollway = link.isTollway();
				boolean exitName = link.isExitName();
				int speedCategory = link.getSpeedCategory();
				
				String strLine = linkId + SEPARATION + refNodeId + SEPARATION + nonRefNodeId + SEPARATION + baseName + SEPARATION + 
								accessId + SEPARATION + functionalClass + SEPARATION + speedCategory + SEPARATION + travelDirection + SEPARATION + 
								(ramp ? YES : NO) + SEPARATION + (tollway ? YES : NO) + SEPARATION + (exitName ? YES : NO) + "\r\n";
				out.write(strLine);
			}
			
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}
	
	/**
	 * write street name
	 * @param linkMap
	 */
	public static void writeStreetName(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("write street name...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkNameFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				
				LinkedList<String> streetNameList = link.getStreetNameList();
				
				String streetNameStr = null;
				
				if(streetNameList == null)
					streetNameStr = UNKNOWN;
				else {
					for(String street : streetNameList) {
						if(streetNameStr == null)
							streetNameStr = street;
						else
							streetNameStr += SEPARATION + street;
					}
				}
				
				String strLine = linkId + SEPARATION + streetNameStr + "\r\n";
				out.write(strLine);
			}
			out.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("writeStreetName: debug: " + debug);
		}
		System.out.println("write street name finish!");
	}
	
	/**
	 * write link geometry points
	 * @param linkMap
	 */
	public static void writeLinkGeometry(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("write link geometry...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkGeometryFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				
				LinkedList<LocationInfo> pointsList = link.getPointList();
				
				String pointsStr = null;
				
				for(LocationInfo loc : pointsList) {
					if(pointsStr == null)
						pointsStr = loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
					else
						pointsStr += SEPARATION + loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
				}
				
				String strLine = linkId + SEPARATION + pointsStr + "\r\n";
				out.write(strLine);
			}
			
			out.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("writeLinkGeometry: debug code:" + debug);
		}
		System.out.println("write link geometry finish!");
	}
	
	/**
	 * write lane information
	 * @param linkMap
	 */
	public static void writeLinkLaneFile(HashMap<Long, RDFLinkInfo> linkMap) {
		System.out.println("write link lane file...");
		int debug = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkLaneFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(long linkId : linkMap.keySet()) {
				debug++;
				RDFLinkInfo link = linkMap.get(linkId);
				
				LinkedList<RDFLaneInfo> laneList = link.getLaneList();
				
				if(laneList == null)
					continue;
				
				String laneStr = null;
				
				for(RDFLaneInfo lane : laneList) {
					if(laneStr == null)
						laneStr = lane.getLaneId() + SEPARATION + lane.getTravelDirection() + SEPARATION + lane.getLaneType() + SEPARATION + lane.getAccessId();
					else
						laneStr += SEPARATION + lane.getLaneId() + SEPARATION + lane.getTravelDirection() + SEPARATION + lane.getLaneType() + SEPARATION + lane.getAccessId();
				}
				
				String strLine = linkId + SEPARATION + laneStr + "\r\n";
				out.write(strLine);
			}
			
			out.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("writeLinkLaneFile: debug code: " + debug);
		}
		System.out.println("write link lane file finish!");
	}
	
	/**
	 * @deprecated
	 * @param linkList
	 */
	public static void writeLinkDetail(LinkedList<RDFLinkInfo> linkList) {
		System.out.println("write link file...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo RDFLink = iterator.next();
				long linkId = RDFLink.getLinkId();
				String baseName = RDFLink.getBaseName();			
				long refNodeId = RDFLink.getRefNodeId();
				long nonRefNodeId = RDFLink.getNonRefNodeId();
				int functionalClass = RDFLink.getFunctionalClass();
				String travelDirection = RDFLink.getTravelDirection();
				boolean ramp = RDFLink.isRamp();
				boolean tollway = RDFLink.isTollway();
				int speedCategory = RDFLink.getSpeedCategory();
				//boolean carpoolRoad = RDFLink.isCarpoolRoad();
				boolean carpoolRoad = false;
				//boolean carpools = RDFLink.isCarpools();
				boolean carpools = false;
				//boolean expressLane = RDFLink.isExpressLane();
				boolean expressLane = false;
				
				String strLine = linkId + "|" + baseName + "|" + refNodeId + "|" + 
						nonRefNodeId + "|" + functionalClass + "|" + travelDirection +"|" +
						speedCategory + "|" + (ramp ? YES : NO) + "|" + (tollway ? YES : NO) + "|" +
						(carpoolRoad ? YES : NO) + "|" + (carpools ? YES : NO) + "|" + 
						(expressLane ? YES : NO) + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("write link file finish!");
	}
	
	/**
	 * @deprecated
	 * @param linkList
	 */
	public static void writeLinkWithGeometry(LinkedList<RDFLinkInfo> linkList) {
		System.out.println("write link with geometry...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + linkFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			ListIterator<RDFLinkInfo> iterator = linkList.listIterator();
			while(iterator.hasNext()) {
				RDFLinkInfo RDFLink = iterator.next();
				long linkId = RDFLink.getLinkId();
				String baseName = RDFLink.getBaseName();
				long refNodeId = RDFLink.getRefNodeId();
				long nonRefNodeId = RDFLink.getNonRefNodeId();
				int functionalClass = RDFLink.getFunctionalClass();
				String travelDirection = RDFLink.getTravelDirection();
				boolean ramp = RDFLink.isRamp();
				boolean tollway = RDFLink.isTollway();
				int speedCategory = RDFLink.getSpeedCategory();
				//boolean carpoolRoad = RDFLink.isCarpoolRoad();
				boolean carpoolRoad = false;
				//boolean carpools = RDFLink.isCarpools();
				boolean carpools = false;
				//boolean expressLane = RDFLink.isExpressLane();
				boolean expressLane = false;
				LinkedList<LocationInfo> pointsList = RDFLink.getPointList();
				
				String pointsStr = "null";
				ListIterator<LocationInfo> pIterator = pointsList.listIterator();
				int i = 0;
				while(pIterator.hasNext()) {
					LocationInfo loc = pIterator.next();
					if(i++ == 0)
						pointsStr = loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
					else
						pointsStr += ";" + loc.getLatitude() + SEPARATION + loc.getLongitude() + SEPARATION + loc.getZLevel();
				}
				
				String strLine = linkId + "|" + baseName + "|" + refNodeId + "|" + nonRefNodeId + "|" + 
						functionalClass + "|" + travelDirection +"|" + speedCategory + "|" + (ramp ? YES : NO) + "|" + 
						(tollway ? YES : NO) + "|" + (carpoolRoad ? YES : NO) + "|" + (carpools ? YES : NO) + "|" +
						(expressLane ? YES : NO) + "|" + pointsStr + "\r\n";
				out.write(strLine);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("write link with geometry finish!");
	}
}
