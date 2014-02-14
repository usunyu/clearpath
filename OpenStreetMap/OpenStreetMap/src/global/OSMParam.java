package global;

public class OSMParam {
	/**
	 * @param file
	 */
	public static String root;
	// source
	public static String osmFile;
	// wkts
	public static String wktsFile;
	// name
	public static String name;
	// csv
	public static String nodeCSVFile;
	public static String wayCSVFile;
	public static String wayInfoFile;
	public static String edgeCVSFile;
	public static String edgeBCVSFile;
	public static String adjlistFile;
	public static String patternCVSFile;
	// kml
	public static String wayKMLFile;
	public static String edgeKMLFile;
	public static String nodeKMLFile;
	public static String pathKMLFile;
	// test
	public static String entranceExitFile;
	public static String pathNodeKMLFile;
	public static String highwayKMLFile;
	public static String costReportFile;
	public static String transversalNodeKMLFile;
	public static String startEndNodeKMLFile;
	public static String locationsWithLatLongsFile;
	public static String analyzeReportFile;
	public static String edgeCVSFileOld;
	// deprecated
	public static String extraNodeFile;
	// days
	public static String[] days				= {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	/**
	 * @param osm
	 */
	public static String MOTORWAY			= "motorway";
	public static String MOTORWAY_LINK		= "motorway_link";
	public static String TRUNK				= "trunk";
	public static String TRUNK_LINK			= "trunk_link";
	public static String PRIMARY			= "primary";
	public static String PRIMARY_LINK		= "primary_link";
	public static String SECONDARY			= "secondary";
	public static String SECONDARY_LINK 	= "secondary_link";
	public static String TERTIARY			= "tertiary";
	public static String TERTIARY_LINK		= "tertiary_link";
	public static String RESIDENTIAL 		= "residential";
	public static String CYCLEWAY			= "cycleway";
	public static String TRACK				= "track";
	public static String ROAD				= "road";
	public static String PROPOSED			= "proposed";
	public static String CONSTRUCTION		= "construction";
	public static String ABANDONED			= "abandoned";
	public static String SCALE				= "scale";
	public static String TURNING_CIRCLE 	= "turning_circle";
	public static String LIVING_STREET 		= "living_street";
	public static String BRIDLEWAY  		= "bridleway";
	public static String UNCLASSIFIED		= "unclassified";
	/**
	 * @param xml
	 */
	public static final String ID 			= "id";
	public static final String NODE 		= "node";
	public static final String WAY 			= "way";
	public static final String LAT 			= "lat";
	public static final String LON 			= "lon";
	public static final String TAG			= "tag";
	public static final String K			= "k";
	public static final String V			= "v";
	public static final String NAME			= "name";
	public static final String HIGHWAY		= "highway";
	public static final String ONEWAY		= "oneway";
	public static final String YES			= "yes";
	public static final String RELATION		= "relation";
	/**
	 * @param csv
	 */
	public static String SEPARATION			= "|";
	public static String ESCAPE_SEPARATION	= "\\|";
	public static String SEGMENT			= "/";
	public static String COMMA				= ",";
	public static String SEMICOLON			= ";";
	public static String COLON				= ":";
	public static String ONEDIRECT			= "O";
	public static String BIDIRECT			= "B";
	public static String FIX				= "F";
	public static String VARIABLE			= "V";
	public static String LINEEND			= "\r\n";
	public static String UNKNOWN_STREET 	= "Unknown Street";
	public static String UNKNOWN_HIGHWAY 	= "Unknown Highway";
	/**
	 * @param const
	 */
	public static int FEET_PER_MILE			= 5280;
	public static int SECOND_PER_HOUR		= 3600;
	public static int MILLI_PER_SECOND		= 1000;
	public static int SECOND_PER_MINUTE		= 60;
	
	public static void paramConfig() {
		root 								= "file";
		/**
		 * @param
		 * put your *.osm file name here (not include suffix)
		 * the *.osm file should be in you file folder
		 */
		name								= "los_angeles";
		// source
		osmFile 							= name + ".osm";
		// wkts
		wktsFile							= name + ".osm.wkts";
		// csv
		nodeCSVFile 						= name + "_node.csv";
		wayCSVFile 							= name + "_way.csv";
		wayInfoFile							= name + "_info.csv";
		edgeCVSFile							= name + "_edge.csv";
		edgeBCVSFile						= name + "_edge_b.csv";
		adjlistFile							= name + "_adjlist.csv";
		patternCVSFile						= name + "_pattern.csv";
		// kml
		wayKMLFile							= name + "_way.kml";
		edgeKMLFile 						= name + "_edge.kml";
		nodeKMLFile							= name + "_node.kml";
		pathKMLFile							= name + "_path.kml";
		// test
		entranceExitFile					= name + "_entrance_exit.kml";
		pathNodeKMLFile						= name + "_path_node.kml";
		highwayKMLFile						= name + "_highway.kml";
		costReportFile						= name + "_cost_report.csv";
		transversalNodeKMLFile				= name + "_transversal_node.kml";
		startEndNodeKMLFile					= name + "_start_end_node.kml";
		locationsWithLatLongsFile			= name + "_locations_with_latlongs.csv";
		analyzeReportFile					= name + "_analyze_report.csv";
		edgeCVSFileOld						= name + "_edge_old.csv";
		// deprecated
		extraNodeFile						= name + "_way_extra.csv";
	}

	public static void initialHierarchy() {
		OSMData.hierarchyHashMap.put(MOTORWAY, 1);
		OSMData.hierarchyHashMap.put(MOTORWAY_LINK, 1);
		OSMData.hierarchyHashMap.put(TRUNK, 1);
		OSMData.hierarchyHashMap.put(TRUNK_LINK, 1);
		OSMData.hierarchyHashMap.put(PRIMARY, 2);
		// link can be use to connect the highway
		OSMData.hierarchyHashMap.put(PRIMARY_LINK, 1);
		OSMData.hierarchyHashMap.put(SECONDARY, 3);
		OSMData.hierarchyHashMap.put(SECONDARY_LINK, 1);
		OSMData.hierarchyHashMap.put(TERTIARY, 4);
		OSMData.hierarchyHashMap.put(TERTIARY_LINK, 1);
		OSMData.hierarchyHashMap.put(RESIDENTIAL, 5);
		OSMData.hierarchyHashMap.put(CYCLEWAY, 5);
		OSMData.hierarchyHashMap.put(TURNING_CIRCLE, 5);
		OSMData.hierarchyHashMap.put(BRIDLEWAY, 5);
		OSMData.hierarchyHashMap.put(LIVING_STREET, 6);
		OSMData.hierarchyHashMap.put(TRACK, 6);
		OSMData.hierarchyHashMap.put(CONSTRUCTION, 6);
		OSMData.hierarchyHashMap.put(PROPOSED, 6);
		OSMData.hierarchyHashMap.put(ROAD, 6);
		OSMData.hierarchyHashMap.put(ABANDONED, 6);
		OSMData.hierarchyHashMap.put(SCALE, 6);
		OSMData.hierarchyHashMap.put(UNCLASSIFIED, 6);
	}
}
