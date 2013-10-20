package function;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import object.LocationInfo;
import object.NodeInfo;
import object.WayInfo;

public class OSMOutput {
	/**
	 * @param file
	 */
	static String root = "file";
	static String osmFile;
	static String nodeCSVFile;
	static String wayCSVFile;
	static String extraNodeFile;
	/**
	 * @param csv
	 */
	static String SEPARATION	= ",";
	static String SEMICOLON	= ";";
	static String COLON		= ":";
	static char ONEWAY		= 'O';
	static char BIDIRECT		= 'B';
	static String LINEEND		= "\r\n";
	
	static String UNKNOWN_STREET 		= "Unknown Street";
	static String UNKNOWN_HIGHWAY 	= "Unknown Highway";
	
	public static void paramConfig(String name) {
		osmFile 		= name + ".osm";
		nodeCSVFile 	= name + "_node.csv";
		wayCSVFile 		= name + "_way.csv";
		extraNodeFile	= name + "_way_extra.csv";
	}

	/**
	 * write to CSV file from the data read from XML file 
	 */
	public static void writeCSVFile(ArrayList<WayInfo> wayArrayList, ArrayList<NodeInfo> nodeArrayList) {
		System.out.println("write csv file...");
		int debug = 0, loop = 0;
		try {
			// write node
			FileWriter fstream = new FileWriter(root + "/" + nodeCSVFile);
			BufferedWriter out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < nodeArrayList.size(); i++) {
				debug++;
				NodeInfo nodeInfo = nodeArrayList.get(i);
				long nodeId = nodeInfo.getNodeId();
				LocationInfo location = nodeInfo.getLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				String strLine = nodeId + SEPARATION + latitude + SEPARATION + longitude + LINEEND;
				
				out.write(strLine);
			}
			out.close();
			
			// write way
			fstream = new FileWriter(root + "/" + wayCSVFile);
			out = new BufferedWriter(fstream);
			loop++;
			for (int i = 0; i < wayArrayList.size(); i++) {
				debug++;
				WayInfo wayInfo = wayArrayList.get(i);
				long wayId = wayInfo.getWayId();
				char isOneway = wayInfo.isOneway() ? ONEWAY : BIDIRECT;
				String name = wayInfo.getName();
				String highway = wayInfo.getHighway();
				String strLine = wayId + SEPARATION + isOneway + SEPARATION + name + SEPARATION + highway;
				
				ArrayList<Long> localNodeArrayList = wayInfo.getNodeArrayList();
				if(localNodeArrayList != null) {
					String nodeListStr = "";
					for(int j = 0; j < localNodeArrayList.size(); j++) {
						nodeListStr += localNodeArrayList.get(j);
						if(j < localNodeArrayList.size() - 1)
							nodeListStr += SEMICOLON;
					}
					strLine += SEPARATION + nodeListStr;
				}
				
				HashMap<String, String> infoHashMap = wayInfo.getInfoHashMap();
				if(infoHashMap != null) {
					Iterator<String> iter = infoHashMap.keySet().iterator();
					while(iter.hasNext()) {
						String key = iter.next();
						String val = infoHashMap.get(key);
						strLine += SEPARATION + key + COLON + val;
					}
				}
				
				strLine += LINEEND;
				out.write(strLine);
			}
			out.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("writeCSVFile: debug code: " + debug + ", in the " + loop + " loop.");
		}
		System.out.println("write csv file finish!");
	}
}
