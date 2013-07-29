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
	static String averageSpeedCleanFile = "Average_Speed_List_Clean.txt";
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	// static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// param
	static double closeDistance = 4;
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	// sensor
	static HashSet<Integer> checkSensor = new HashSet<Integer>();
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();
	static HashMap<Integer, SensorInfo> sensorMap = new HashMap<Integer, SensorInfo>();
	static HashMap<Integer, ArrayList<Integer>> closeSensorMap = new HashMap<Integer, ArrayList<Integer>>();
	// pattern
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();
	static ArrayList<Integer> sensorPatternList = new ArrayList<Integer>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fetchSensor();
		mapCloseSensor();
		generateSensorKML();
//		writeAverageFile(0);
		readAverageFile(0);
		cleanTable();
		writeCleanFile(0);
	}
	
	private static void writeCleanFile(int dayIndex) {
		System.out.println("write clean file...");
		int errorNo = 0;
		try {
			FileWriter fstream = new FileWriter(root + "/" + days[dayIndex] + "_" + averageSpeedCleanFile);
			BufferedWriter out = new BufferedWriter(fstream);
			for(int i = 0; i < sensorPatternList.size(); i++) {
				int sensorId = sensorPatternList.get(i);
				double[] speedArray = sensorSpeedPattern.get(sensorId);
				for(int j = 0; j < speedArray.length; j++) {
					String strLine = sensorId + ";" + speedArray[j] + ";" + UtilClass.getStartTime(j) + "\r\n";
					out.write(strLine);
				}
			}
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + errorNo);
		}
		System.out.println("write clean file finish!");
	}
	
	private static void cleanTable() {
		System.out.println("clean table...");
		int error = 0;
		try {
			for(int i = 0; i < sensorPatternList.size(); i++) {
				error++;
				if(error == 912)
					System.err.println("Error Here");
				int sensorId = sensorPatternList.get(i);
				double[] speedArray = sensorSpeedPattern.get(sensorId);
				for(int j = 0; j < speedArray.length; j++) {
					if(speedArray[j] == 0) {
						// find close sensor
						ArrayList<Integer> closeSensorList = closeSensorMap.get(sensorId);
						if(closeSensorList == null) {
							System.err.println("close sensor list is null, it should not happen, error code: " + error + ", sensor id:" + sensorId);
							continue;
						}
						for(int k = 0; k < closeSensorList.size(); k++) {
							int closeSensorId = closeSensorList.get(k);
							double[] closeSpeedArray = sensorSpeedPattern.get(closeSensorId);
							if(closeSpeedArray[j] != 0) {
								speedArray[j] = closeSpeedArray[j];
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Error Code: " + error);
		}
		System.out.println("clean table finish!");
	}
	
	private static void readAverageFile(int dayIndex) {
		System.out.println("read average file...");
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + days[dayIndex] + "_" + averageSpeedFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(";");
				int sensorId = Integer.parseInt(nodes[0]);
				double speed = Double.parseDouble(nodes[1]);
				String time = nodes[2];
				
				if(!sensorPatternList.contains(sensorId))
					sensorPatternList.add(sensorId);

				if (sensorSpeedPattern.containsKey(sensorId)) {
					double[] tempArray = sensorSpeedPattern.get(sensorId);
					int index = UtilClass.getIndex(time);
					if (index >= 0 && index <= 59)
						tempArray[index] = speed;
				} else {
					double[] newArray = new double[60];
					int index = UtilClass.getIndex(time);
					if (index >= 0 && index <= 59)
						newArray[index] = speed;
					sensorSpeedPattern.put(sensorId, newArray);
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read average file finish!");
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
				ArrayList<Integer> closeList = closeSensorMap.get(sensor.getSensorId());
				if(closeList != null) {
					for(int j = 0; j < closeList.size(); j++) {
						int tempSensor = closeList.get(j);
						if(j == 0) {
							closeSensorStr = Integer.toString(tempSensor);
						}
						else {
							closeSensorStr += ";" + tempSensor;
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
					SensorInfo sensor = new SensorInfo(sensorId, onStreet,
							fromStreet, node, direction);
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
