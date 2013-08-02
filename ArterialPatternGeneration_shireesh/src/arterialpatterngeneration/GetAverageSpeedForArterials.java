/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package arterialpatterngeneration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Objects.UtilClass;

public class GetAverageSpeedForArterials {
	// params
	static String root = "../GeneratedFile";
	// database
	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

	public static void main(String[] args) throws SQLException,
			NumberFormatException, IOException {

		createPatterns();

	}

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			Connection connHome = DriverManager.getConnection(url_home,
					userName, password);
			return connHome;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return connHome;

	}

	private static void createPatterns() {
		int i = 0;
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {

			System.out.println("Getting Averages now");
			fstream = new FileWriter(root + "/AverageSpeeds_Arterials.txt");
			out = new BufferedWriter(fstream);
			// MOD(TO_CHAR(t2.date_and_time, 'J'), 7) + 1 NOT IN (6, 7) and
			String sql = "SELECT avg(t2.SPEED) FROM arterial_Averages3_full3 T2 where month = 'May' GROUP BY  TIME ORDER BY TIME";
			System.out.println(sql);
			Connection con = getConnection();
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet r = f.executeQuery();
			while (r.next() && i < 60) {
				Double average = r.getDouble(1);
				if (average != 0.0) {
					System.out.println(average);
					out.write(UtilClass.getStartTime(i) + "-"
							+ UtilClass.getEndTime(i) + "," + average);
					out.write("\n");
					System.out.println(UtilClass.getStartTime(i) + "-"
							+ UtilClass.getEndTime(i) + "," + average);
				}
				i++;
			}
			r.close();
			f.close();
			con.close();
			out.close();
			fstream.close();
			System.out.println("File Writing Complete");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
