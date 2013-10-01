package tdsp.repos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCheck {

	static String url_home = "jdbc:oracle:thin:@localhost:1521:xe";
    static String userName = "SYSTEM";
    static String password = "yashu";
    
    public static void main(String args[]) throws SQLException{
    	
    	 DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
         //System.out.print("connecting home database.....");
         //Connection connHome = DriverManager.getConnection(url_home, userName, password);
         System.out.print("connecting geodb database.....");
         Connection conn = DriverManager.getConnection(url_home, userName, password);
         System.out.println("creating statement..");
         Statement stmt = conn.createStatement();
         System.out.println("inserting statement..");
         stmt.executeUpdate("create table los_angeles_large(LINK_ID number,geom SDO_GEOMETRY)");
         System.out.println("after inserting..");
         conn.close();

    }
}
