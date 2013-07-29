package cleanTable;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class CleanTable {

	/**
	 * @param args
	 */
	// file
	static String root = "file";
	static String closeSensorKML = "Close_Sensor.kml";
	static String averageSpeedFile = "Average_Speed_List.txt";
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	// static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// param
	static double closeDistance = 1;
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	// sensor
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashMap<SensorInfo, ArrayList<SensorInfo>> closeSensorMap = new HashMap<SensorInfo, ArrayList<SensorInfo>>();
	// pattern
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchSensor();
		mapCloseSensor();
//		generateSensorKML();
		writeAverageFile(0);
		
	}
	
	private static void writeAverageFile(int dayIndex) {
		System.out.println("write average speed...");
		int errorNo = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + days[dayIndex] + "_" + averageSpeedFile);
			BufferedWriter out = new BufferedWriter(fstream);

			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			String day = days[dayIndex];
			sql = "SELECT * FROM highway_averages3_new4 WHERE day = '" + day + "'";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			int i = 0;
			while (res.next()) {
				errorNo++;
				int sensorId = res.getInt(1);
				double speed = res.getDouble(2);
				String time = res.getString(5);

				String strLine = res.getString(1) + ";" + res.getString(2) + ";" + time + "\r\n";
				out.write(strLine);
				
				if(i % 10000 == 0)
					System.out.println("No." + i + " finish");
				i++;
			}

			out.close();

			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + errorNo);
		}
		System.out.println("write average speed finish!");
	}

	private static void mapCloseSensor() {
		System.out.println("map closest sensor...");
		for (int i = 0; i < sensorList.size() - 1; i++) {
			SensorInfo sensorA = sensorList.get(i);
			for (int j = i + 1; j < sensorList.size(); j++) {
				SensorInfo sensorB = sensorList.get(j);
				double dis = DistanceCalculator.CalculationByDistance(sensorA.getNode(), sensorB.getNode());
				if(dis <= closeDistance && (sensorA.getDirection() == sensorB.getDirection())) {
					// in the area
					if(closeSensorMap.containsKey(sensorA)) {
						ArrayList<SensorInfo> tempList = closeSensorMap.get(sensorA);
						boolean added = false;
						for(int k = 0; k < tempList.size(); k++) {
							SensorInfo tempSensor = tempList.get(k);
							double tempDis = DistanceCalculator.CalculationByDistance(sensorA.getNode(), tempSensor.getNode());
							if(dis < tempDis) {
								tempList.add(k, sensorB);
								added = true;
								break;
							}
						}
						if(!added) {
							tempList.add(tempList.size(), sensorB);
						}
					}
					else {
						ArrayList<SensorInfo> newList = new ArrayList<SensorInfo>();
						newList.add(sensorB);
						closeSensorMap.put(sensorA, newList);
					}
					if(closeSensorMap.containsKey(sensorB)) {
						ArrayList<SensorInfo> tempList = closeSensorMap.get(sensorB);
						boolean added = false;
						for(int k = 0; k < tempList.size(); k++) {
							SensorInfo tempSensor = tempList.get(k);
							double tempDis = DistanceCalculator.CalculationByDistance(sensorB.getNode(), tempSensor.getNode());
							if(dis < tempDis) {
								tempList.add(k, sensorA);
								added = true;
								break;
							}
						}
						if(!added) {
							tempList.add(tempList.size(), sensorA);
						}
					}
					else {
						ArrayList<SensorInfo> newList = new ArrayList<SensorInfo>();
						newList.add(sensorA);
						closeSensorMap.put(sensorB, newList);
					}
				}
			}
		}
		System.out.println("map closest sensor finish!");
	}
	
	private static void generateSensorKML() {
		System.out.println("generate sensor kml...");
		try {
			FileWriter fstream = new FileWriter(root + "/" + closeSensorKML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < sensorList.size(); i++) {
				SensorInfo sensor = sensorList.get(i);
				int id = sensor.getSensorId();
				double lati = sensor.getNode().getLati();
				double longi = sensor.getNode().getLongi();
				String closeSensorStr = "";
				ArrayList<SensorInfo> closeList = closeSensorMap.get(sensor);
				if(closeList != null) {
					for(int j = 0; j < closeList.size(); j++) {
						SensorInfo tempSensor = closeList.get(j);
						if(j == 0) {
							closeSensorStr = Integer.toString(tempSensor.getSensorId());
						}
						else {
							closeSensorStr += ";" + tempSensor.getSensorId();
						}
					}
					closeSensorStr += "\r\n";
				}
				out.write("<Placemark><name>" + id + "</name><description>"
						+ "Close:" + closeSensorStr
//						+ ", On:" + sensor.getOnStreet()
//						+ ", From: " + sensor.getFromStreet()
						+ ", Dir:" + sensor.getDirection()
						+ "</description><Point><coordinates>" + longi + ","
						+ lati + ",0</coordinates></Point></Placemark>");
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate sensor kml finish!");
	}

	private static void fetchSensor() {
		System.out.println("fetch sensor...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "SELECT link_id, onstreet, fromstreet, start_lat_long, direction FROM highway_congestion_config ";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				// System.out.println("fetching sensor " + sensorId);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]);
				int direction = res.getInt(5);
				if (!checkSensor.contains(sensorId)) {
					SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet,
							fromStreet, node, direction);
					checkSensor.add(sensorId);
					sensorList.add(sensorInfo);
				}
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("fetch sensor finish!");
	}

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager.getConnection(urlHome, userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;
	}
}
