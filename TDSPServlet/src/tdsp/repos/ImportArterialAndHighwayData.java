package tdsp.repos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class ImportArterialAndHighwayData {

	static String url_geodb = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
    static String url_home = "jdbc:oracle:thin:@localhost:1521:xe";
    static String userName = "SYSTEM";
    static String password = "yashu";
    static String userNameNavtech = "clearpath";
    static String passwordNavtech = "adf11p";
    
    public static void main(String[] args) {
        try{
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            System.out.print("connecting home database.....");
            Connection connHome = DriverManager.getConnection(url_home, userName, password);
            System.out.print("connecting geodb database.....");
            Connection connNavtech = DriverManager.getConnection(url_geodb, userNameNavtech, passwordNavtech);

            System.out.println("fetching..");
            Date start = new Date();
           /* String sql = "select s.link_id, s.geom from ( select * from navtech.streets_dca1 s1 where (s1.FUNC_CLASS=1 or s1.FUNC_CLASS=2)) s where "  // larger area
                    + "sdo_filter(s.geom,SDO_GEOMETRY(2003, 8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3),"
                    + "SDO_ORDINATE_ARRAY(-118.67913,33.59831,-117.09847,34.33519))) ='TRUE' ";
         */
            String sql = "select * from arterial.arterial_congestion_history where config_id='59' and link_status <> 'failed' and speed <> 0 and date_and_time between to_date('01-SEP-2011') AND to_date('01-JAN-2012')";
            PreparedStatement fetch = connNavtech.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

            fetch.setFetchSize(100000);
            ResultSet result = fetch.executeQuery();
            Date end = new Date();
            System.out.println("finished,"+(double)(end.getTime()-start.getTime())/60+", gettint to last row ..");
            PreparedStatement populate = connHome.prepareStatement("insert into arterial_congestion_history values(?,?,?,?,?,?,?,?,?)");
            
            while(result.next()){
            	
            	populate.setInt(1,result.getInt(1));
            	populate.setString(2,result.getString(2));
            	populate.setDate(3, result.getDate(3));
            	populate.setInt(4,result.getInt(4));
            	populate.setInt(5,result.getInt(5));
            	populate.setInt(6,result.getInt(6));
            	populate.setInt(7,result.getInt(7));
            	populate.setInt(8,result.getInt(8));
            	populate.setString(9,result.getString(9));
            	
            	populate.executeUpdate();
            }
            end = new Date();
            System.out.println("populate finished "+(start.getTime()-end.getTime())+",  row modified");
            populate.clearBatch();
            connHome.close();
            connNavtech.close();

        }catch(Exception ee){
            ee.printStackTrace();
        }
    }
}
