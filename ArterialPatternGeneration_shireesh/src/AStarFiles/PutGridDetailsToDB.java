/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AStarFiles;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 
 * @author clearp
 */
public class PutGridDetailsToDB {

	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager
					.getConnection(url_home, userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;

	}

	private static void putGrid() {

		try {
			double lat1 = 34.336, lat2 = 33.8, long1 = -119.3, long2 = -117.6;
			double latstep = (lat1 - lat2) / 20.0;
			double longstep = (long2 - long1) / 20.0;
			System.out.println(latstep + " " + longstep);
			Connection con = getConnection();
			int index1 = 0, count = 0;
			for (double i = lat1; i > lat2; index1++, i = i - latstep) {
				System.out.println(index1);
				if (index1 >= 20)
					continue;
				int index2 = 0;
				for (double j = long1; j < long2; index2++, j = j + longstep) {
					System.out.print(" " + index2);
					if (index2 >= 20)
						continue;
					double lati1 = i - latstep, lati2 = i, longi1 = j, longi2 = j
							+ longstep;
					String geomQuery = "MDSYS.SDO_GEOMETRY(2003,8307,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY("
							+ longi1 + "," + lati1 + "," + longi2 + "," + lati2 + "))";
					String sql = "insert into GridInfo20_20 values( "
							+ ((index1 * 100) + index2) + "," + geomQuery + ")";
					PreparedStatement f = con.prepareStatement(sql,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					f.executeUpdate();
					f.close();
				}
			}
			con.close();
			System.out.println("\n" + count);
		} catch (Exception r) {
			r.printStackTrace();
		}
	}

	public static void main(String args[]) {
		putGrid();
	}
}
