package tdsp.repos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class NavtechRead {

    static String url_geodb = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
    static String url_home = "jdbc:oracle:thin:@localhost:1522:xe2";
    static String userName = "SYSTEM";
    static String password = "yashu";
    static String userNameNavtech = "navtech";
    static String passwordNavtech = "navteq";

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
            String sql = "select s.link_id, s.st_name,s.func_class,s.geom from navtech.streets_dca1 s where (s.FUNC_CLASS=1 or s.FUNC_CLASS=2)";
            PreparedStatement fetch = connNavtech.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

            fetch.setFetchSize(100000);
            ResultSet result = fetch.executeQuery();
            Date end = new Date();
            System.out.println("finished,"+(double)(end.getTime()-start.getTime())/60+", gettint to last row ..");
            
            result.last();
            System.out.println("getting rownums ..");
            int rownum = result.getRow();
            start = new Date();
            System.out.println("finished,"+(double)(start.getTime()-end.getTime())/60+","+rownum+" records returned .... getting to first row...");
            result.beforeFirst();
            System.out.println("after resetting cursor ..");
            int[] linkids = new int[rownum];
            int[] funcs = new int[rownum];
            String[] stNames = new String[rownum];
            JGeometry[] geom = new JGeometry[rownum];
            
            int i = 0;
            for(;i<rownum;i++){
            	result.next();
                linkids[i] = result.getInt(1);
                stNames[i] = result.getString(2);
                funcs[i] = result.getInt(3);
                STRUCT st = (STRUCT) result.getObject(4);
                geom[i] = JGeometry.load(st);
                
            }
            end = new Date();
            System.out.println("stored in arrays "+(end.getTime()-start.getTime())+", populating...");

            
            PreparedStatement populate = connHome.prepareStatement("insert into los_angeles_large_2 values(?,?,?,?)");
            for(i=0; i<rownum; i++){
                STRUCT obj = JGeometry.store(geom[i], connHome);
                populate.setInt(1, linkids[i]);
                populate.setString(2, stNames[i]);
                populate.setInt(3, funcs[i]);
                populate.setObject(4, obj);
                populate.executeUpdate();
               
            }
            start = new Date();
            System.out.println("populate finished "+(start.getTime()-end.getTime())+",  row modified");
            //populate.clearBatch();
            connHome.close();
            connNavtech.close();

        }catch(Exception ee){
            ee.printStackTrace();
        }
    }

}
