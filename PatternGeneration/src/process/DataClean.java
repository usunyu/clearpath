package process;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class DataClean {

	/**
	 * @param file
	 */
	static String root = "file";
	static String closeSensorKML = "Close_Sensor.kml";
	static String averageSpeedFile = "Average_Speed_List.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param arguments
	 */
	static double closeDistance = 4;
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	/**
	 * @param sensor
	 */
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	static HashMap<Integer, ArrayList<Integer>> closeSensorMap = new HashMap<Integer, ArrayList<Integer>>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchSensor();
		mapCloseSensor();
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
					if(closeSensorMap.containsKey(sensorA.getSensorId())) {
						ArrayList<Integer> tempList = closeSensorMap.get(sensorA.getSensorId());
						boolean added = false;
						for(int k = 0; k < tempList.size(); k++) {
							int tempId = tempList.get(k);
							SensorInfo tempSensor = sensorMap.get(tempId);
							double tempDis = DistanceCalculator.CalculationByDistance(sensorA.getNode(), tempSensor.getNode());
							if(dis < tempDis) {
								tempList.add(k, sensorB.getSensorId());
								added = true;
								break;
							}
						}
						if(!added) {
							tempList.add(tempList.size(), sensorB.getSensorId());
						}
					}
					else {
						ArrayList<Integer> newList = new ArrayList<Integer>();
						newList.add(sensorB.getSensorId());
						closeSensorMap.put(sensorA.getSensorId(), newList);
					}
					if(closeSensorMap.containsKey(sensorB.getSensorId())) {
						ArrayList<Integer> tempList = closeSensorMap.get(sensorB.getSensorId());
						boolean added = false;
						for(int k = 0; k < tempList.size(); k++) {
							int tempId = tempList.get(k);
							SensorInfo tempSensor = sensorMap.get(tempId);
							double tempDis = DistanceCalculator.CalculationByDistance(sensorB.getNode(), tempSensor.getNode());
							if(dis < tempDis) {
								tempList.add(k, sensorA.getSensorId());
								added = true;
								break;
							}
						}
						if(!added) {
							tempList.add(tempList.size(), sensorA.getSensorId());
						}
					}
					else {
						ArrayList<Integer> newList = new ArrayList<Integer>();
						newList.add(sensorA.getSensorId());
						closeSensorMap.put(sensorB.getSensorId(), newList);
					}
				}
			}
		}
		System.out.println("map closest sensor finish!");
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
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				int sensorId = res.getInt(1);
				// System.out.println("fetching sensor " + sensorId);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				if (!checkSensor.contains(sensorId)) {
					SensorInfo sensor = new SensorInfo(sensorId, onStreet, fromStreet, node, direction);
					checkSensor.add(sensorId);
					sensorList.add(sensor);
					sensorMap.put(sensorId, sensor);
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
