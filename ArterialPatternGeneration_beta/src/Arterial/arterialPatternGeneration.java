package Arterial;

import java.io.*;
import java.sql.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class arterialPatternGeneration {
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static ArrayList<SensorInfo> sensorList = new ArrayList<SensorInfo>();

	public static void main(String[] args) {
		fetchSensor();
	}

	public static void fetchSensor() {
		try {
			System.out.println("fetch Sensor...");
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			sql = "select link_id, onstreet, fromstreet, start_lat_long, direction, affected_numberof_lanes from arterial_congestion_config";
			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while(res.next()) {
				int sensorId = res.getInt(1);
				String onStreet = res.getString(2);
				String fromStreet = res.getString(3);
				STRUCT st = (STRUCT) res.getObject(4);
				JGeometry geom = JGeometry.load(st);
				PairInfo node = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
				int direction = res.getInt(5);
				int affected = res.getInt(6);
				SensorInfo sensorInfo = new SensorInfo(sensorId, onStreet, fromStreet, node, direction, affected);
				sensorList.add(sensorInfo);
			}
			System.out.println("fetch sensor finish!");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
