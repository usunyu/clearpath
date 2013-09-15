package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import objects.LocationInfo;
import objects.RDFNodeInfo;

public class NullSQLResult {

	/**
	 * @param database
	 */
	static String urlHome 	= "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName 	= "NAVTEQRDF";
	static String password 	= "NAVTEQRDF";
	static Connection connHome = null;
	
	public static void main(String[] args) {
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;

			con = getConnection();

			sql = "SELECT zlevel, z_coord FROM rdf_link_geometry WHERE link_id=34026544";

			pstatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();

			while (res.next()) {
				String zLevel = res.getString("zlevel");
				String zCoord = res.getString("z_coord");
				System.out.println(zLevel + "," + zCoord);
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			// TODO: handle exception
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
