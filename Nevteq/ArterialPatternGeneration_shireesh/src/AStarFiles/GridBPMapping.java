/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AStarFiles;

/**
 *
 * @author clearp
 */
import Objects.GridInfo;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.PairInfo;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class GridBPMapping {

    static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
        static GridInfo [] grids = new GridInfo[2500];
        static ArrayList<Integer> [] gridNodes = new ArrayList[2500];
        static ArrayList<Integer> [] gridBPs = new ArrayList[2500];
        static ArrayList<Long> [] gridLinks = new ArrayList[2500];
        static int gridCount = 0;

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

    public static void main(String args[]){
        getGridDetails();
        System.out.println("Grids Found="+gridCount);
        getGridNodes();
        getOverlapEdgesOnGrids();
        getGridNodesAndBPCount();
        putGridNodesToFile();
        putGridBPToDB();
    }

    private static void getGridDetails() {
          System.out.println("Getting Grid Details");
        try {
            Connection con = getConnection();
            String sql = "select  * from GridInfo20_20";
            PreparedStatement f = con.prepareStatement(sql);
            ResultSet rs = f.executeQuery();

            while(rs.next()){
                int gridId = rs.getInt(1);
                STRUCT st = (STRUCT) rs.getObject(2);
                JGeometry geom = JGeometry.load(st);
                double [] ords = geom.getOrdinatesArray();
                grids[gridCount++] = new GridInfo(gridId,new PairInfo(ords[1],ords[0]),new PairInfo(ords[3],ords[2]));
                //System.out.println(gridId+" "+ords[0]+","+ords[1]+" "+ords[2]+","+ords[3]);
            }
            rs.close();
            f.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getOverlapEdgesOnGrids() {
          System.out.println("Getting Grid BP's");
        Connection con = getConnection();
             try {
                 for(int i=0;i<gridCount;i++){
                 gridBPs[i] = new ArrayList<Integer>();
                 gridLinks[i] = new ArrayList<Long>();
                   if(i%5==0)
                   System.out.println(i);
                String gridGeom = "MDSYS.SDO_GEOMETRY(2003,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(" + grids[i].getStart().getLongi() + "," + grids[i].getStart().getLati() + "," + grids[i].getEnd().getLongi() + "," + grids[i].getEnd().getLati() + "))";
                String sql = "SELECT ST_NODE,END_NODE,LINK_ID FROM EDGEINFO1234 WHERE SDO_RELATE(GEOM,"+gridGeom + ",'mask=ANYINTERACT')='TRUE'";
                //System.out.println(sql);
                PreparedStatement f = con.prepareStatement(sql);
                ResultSet rs = f.executeQuery();
                while (rs.next()) {
                    gridLinks[i].add(rs.getLong(3));
                    int st_node = Integer.parseInt(rs.getString(1).substring(1));
                    int end_node = Integer.parseInt(rs.getString(2).substring(1));

                   if(gridNodes[i].contains(st_node) &&  !(gridNodes[i].contains(end_node)) && !(gridBPs[i].contains(st_node)))
                    gridBPs[i].add(st_node);

                    if(gridNodes[i].contains(end_node) &&  !(gridNodes[i].contains(st_node)) && !(gridBPs[i].contains(end_node)))
                    gridBPs[i].add(end_node);

                   /*
                   if(gridNodes[i].contains(st_node) && !(gridBPs[i].contains(st_node)))
                       gridBPs[i].add(st_node);

                    
                   if(gridNodes[i].contains(end_node)&& !(gridBPs[i].contains(end_node)))
                       gridBPs[i].add(end_node);
                    */
                       //System.out.println(st_node+" "+end_node);
                }
                rs.close();
                f.close();
                }
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        }

    private static void getGridNodes() {
        System.out.println("Getting Grid Nodes");
             Connection con = getConnection();
       
            try {
                 for(int i=0;i<gridCount;i++){
                     gridNodes[i] = new ArrayList<Integer>();
                String sql = "SELECT NODEID FROM NODEINFO20_20 WHERE GRIDID="+grids[i].getGridId();
                PreparedStatement f = con.prepareStatement(sql);
                ResultSet rs = f.executeQuery();
                while (rs.next()) {
                   gridNodes[i].add(rs.getInt(1));
                }
                rs.close();
                f.close();
                
            }
                 con.close();
                 }
                 catch (SQLException ex) {
                Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE, null, ex);
            
        }
             
    }

    private static void getGridNodesAndBPCount() {
   int nodeCount=0,BPCount=0,gridBPCount=0;
        for(int i=0;i<gridCount;i++){
            nodeCount+=gridNodes[i].size();
            
            if(!(gridBPs[i].isEmpty()))
                gridBPCount++;
            
            BPCount+=gridBPs[i].size();
            System.out.print(grids[i].getGridId()+"->");
            for(int j=0;j<gridLinks[i].size();j++)
                System.out.print(gridLinks[i].get(j)+" ");
            System.out.println();
        }
        System.out.println("Total Nodes found in all grids ="+nodeCount);
        System.out.println("Total Grids Found with atleast 1 BP="+gridBPCount);
        System.out.println("Total BP found in all grids ="+BPCount);
    }

    private static void putGridNodesToFile() {
	FileWriter 	fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter("H:\\Network1234\\GridBPs20_20.csv");
            out = new BufferedWriter(fstream);
             for(int i=0;i<gridCount;i++){
                 out.write(grids[i].getGridId()+",");
                    for(int j=0;j<gridBPs[i].size();j++){
                        out.write(gridBPs[i].get(j)+",");
                    }
                 out.write("\n");
             }
            
        } catch (IOException ex) {
            Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
                fstream.close();
            } catch (IOException ex) {
                Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void putGridBPToDB() {
        System.out.println("WRITING GRID BP TO DB");
        Connection con = getConnection();
        PreparedStatement f = null;
        try
        {

        for(int i=0;i<gridCount;i++){
            if(i%50==0)
            System.out.println(i);
              for(int j=0;j<gridBPs[i].size();j++){

                    String sql = "INSERT INTO GRID_BP20_20 VALUES(" + grids[i].getGridId() + "," + gridBPs[i].get(j) + ")";
                    f = con.prepareStatement(sql);
                    f.executeUpdate();
                    f.close();
                     }
        }
        con.close();
    }
       catch (SQLException ex) {
                    Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE, null, ex);
                }

        System.out.println("WRITING COMPLETED");
}
}
