package tdsp.repos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Objects.UtilClass;

public class GetAverageSpeedsForArterials {

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
		fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\AverageSpeeds_Arterials.txt");
		out = new BufferedWriter(fstream);
		while (i < 60) {

			//MOD(TO_CHAR(t2.date_and_time, 'J'), 7) + 1 NOT IN (6, 7) and  
			String query2 ="t2.date_and_time>= to_date('06-FEB-2012 "+UtilClass.getStartTime(i)+"', 'DD-MON-YYYY HH24:MI') and t2.date_and_time< to_date('10-FEB-2012 "+UtilClass.getEndTime(i)+"', 'DD-MON-YYYY HH24:MI')";
			String sql = "SELECT avg(t2.SPEED) FROM ARTERIAL_CONGESTION_HISTORY T2 WHERE "
					+" "+query2;
			System.out.println(sql);
			Connection con = getConnection();
			PreparedStatement f = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet r = f.executeQuery();
			if(r.first())
			{
				Double average = r.getDouble(1);
				if(average!=0.0)
				{
				System.out.println(average);
				out.write(UtilClass.getStartTime(i)+"-"+UtilClass.getEndTime(i) + "," + average);
				out.write("\n");
				System.out.println(UtilClass.getStartTime(i)+"-"+UtilClass.getEndTime(i) + "," + average);
				}
			}
			r.close();
			f.close();
			con.close();
			i++;
			
			}
			out.close();
			fstream.close();
			System.out.println("File Writing Complete");
	} catch (Exception e) {
		e.printStackTrace();
	}
}


}
