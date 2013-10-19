package output;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import objects.*;

public class OutputDatabaseGeneration {
	
	/**
	 * @param file
	 */
	static String root = "file";
	static String averageSpeedCleanFile = "Average_Speed_Clean.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	/**
	 * @param table
	 */
	static String tableName = "HIGHWAY_AVERAGES_CLEAN";
	/**
	 * @param arguments
	 */
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	/**
	 * @param pattern
	 */
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();
	static ArrayList<Integer> sensorPatternList = new ArrayList<Integer>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int month = 9; // October
		tableName = "HIGHWAY_AVERAGES_" + months[month].substring(0, 3) + "_CLEAN";
		createTable(tableName);
		//dropTable(tableName);
		for(int i = 0; i < days.length; i++) {
			System.out.println("output the data for " + months[month] + ", " + days[i] + "...");
			readAverageClean(month, i);
			insertTable(tableName, month, i);
			System.out.println("output the data for " + months[month] + ", " + days[i] + " finish!");
		}
		/* test */
		//readAverageClean(7, 0);
		//insertTable(tableName, 7, 0);
	}
	
	private static void insertTable(String table, int month, int day) {
		System.out.println("insert table...");
		try {
			Connection con = null;
			String sql = null;
			Statement stmt = null;
			con = getConnection();
			stmt = con.createStatement();
			for(int i = 0; i < sensorPatternList.size(); i++) {
				int sensorId = sensorPatternList.get(i);
				double[] speedArray = sensorSpeedPattern.get(sensorId);
				for(int j = 0; j < speedArray.length; j++) {
					sql = "INSERT INTO " + table +
							" VALUES ('" + sensorId + "', " + speedArray[j] + 
							", '" + months[month] +"', '" + days[day] + 
							"', '" + UtilClass.getStartTime(j) + "')";
					stmt.executeUpdate(sql);
				}
				
				if(i % 100 == 0)
					System.out.println((double)i / sensorPatternList.size() * 100 + "% finish!");
				
				System.out.println("record " + i + " finish!");
			}
			stmt.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("insert table finish!");
	}
	
	private static void readAverageClean(int month, int day) {
		System.out.println("read average file...");
		// initial again
		sensorPatternList = new ArrayList<Integer>();
		sensorSpeedPattern = new HashMap<Integer, double[]>();
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + months[month] + "_" + days[day] + "_" + averageSpeedCleanFile);
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
	
	private static void dropTable(String table) {
		System.out.println("drop table...");
		try {
			Connection con = null;
			String sql = null;
			Statement stmt = null;
			con = getConnection();
			stmt = con.createStatement();
			sql = "DROP TABLE " + table;
			stmt.executeUpdate(sql);
			stmt.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("drop table finish!");
	}
	
	private static void createTable(String table) {
		System.out.println("create table...");
		try {
			Connection con = null;
			String sql = null;
			Statement stmt = null;
			con = getConnection();
			stmt = con.createStatement();
			sql = "CREATE TABLE " + table +
	                   " (LINK_ID VARCHAR2(30 BYTE), " +
	                   " SPEED NUMBER, " + 
	                   " MONTH VARCHAR2(30 BYTE), " + 
	                   " DAY VARCHAR2(30 BYTE), " + 
	                   " TIME VARCHAR2(30 BYTE))";
			stmt.executeUpdate(sql);
			stmt.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("create table finish!");
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
