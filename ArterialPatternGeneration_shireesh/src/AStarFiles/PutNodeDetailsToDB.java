/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AStarFiles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author clearp
 */
public class PutNodeDetailsToDB {

    static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

    private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager.getConnection(url_home,
					userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;

	}

public static void main(String[] args) {
         writeDB();
    }

    private static void writeDB(){
        //readListTDSP();
        try{
            BufferedReader br = new BufferedReader(new FileReader("H:\\Network1234\\Nodes.csv"));
            Connection con = getConnection();
            int count = 0;
            String line;
            while( (line = br.readLine()) != null){
                
                if(count%100==0)
                System.out.println(count);
                count++;
                String[] coordinates = line.split(",");
                int gridId = getGridId(Double.parseDouble( coordinates[1]),Double.parseDouble( coordinates[2]));
                //System.out.println(gridId);
                if(gridId==-1)
                    System.out.println("panga");
                
                String geomQuery="MDSYS.SDO_GEOMETRY(2001,8307,SDO_POINT_TYPE("+Double.parseDouble( coordinates[2] )+","+Double.parseDouble( coordinates[1])+",NULL),NULL,NULL)";
				String sql = "insert into NodeInfo20_20 values("+Integer.parseInt( coordinates[0].substring(1))+","+geomQuery+","+gridId+")";
				//System.out.println(sql);
                                PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				f.executeUpdate();
        			f.close();
            }
            con.close();
        }
    catch (Exception ioe){
            ioe.printStackTrace();
            System.exit(-1);
        }
   }
    
    private static int getGridId( double lat, double longi) {
        try {
            String geomQuery = "MDSYS.SDO_GEOMETRY(2001,8307,SDO_POINT_TYPE(" + longi + "," + lat + ",NULL),NULL,NULL)";
            String sql = "select gridId from GridInfo20_20 where sdo_relate(geom," + geomQuery + ",'mask=contains')='TRUE'";
            Connection con = getConnection();
            PreparedStatement f = con.prepareStatement(sql);
            ResultSet rs = f.executeQuery();
            int id=0;
            if(rs.next())
            {
                id=rs.getInt(1);
           }
            rs.close();
            f.close();
            con.close();
            return id;
            
        } catch (SQLException ex) {
            
                ex.printStackTrace();
         }
        
        return -1;
    }
}
