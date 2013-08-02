/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AStarFiles.LandmarkFiles;

import Objects.PairInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import tdsp.TDSPQueryJava;

/**
 *
 * @author clearp
 */
public class NodeToLandMarkDistance {

    private static HashMap<Integer,PairInfo> nodeCoors = new HashMap<Integer,PairInfo>();
    private static HashMap<Integer,PairInfo> landMarkCoors = new HashMap<Integer,PairInfo>();
    private static HashMap<Integer,Integer> nodeGridOutTime = new HashMap<Integer,Integer>();
    static TDSPQueryJava t = new TDSPQueryJava();

    static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

public static void main(String args[]){

    readNodesFromDB();
    System.out.println(nodeCoors.size());
    getLandMarkInfo();
    //getNodeToLandMarkTime();
    getLandMarkToNodeTime();
}

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

 private static void readNodesFromDB() {
        try {
            Connection con = getConnection();
            String sql = "SELECT NODEID,GEOM FROM NODEINFO";
            PreparedStatement f = con.prepareStatement(sql);
            ResultSet rs = f.executeQuery();
            while (rs.next()) {
                STRUCT st = (STRUCT) rs.getObject(2);
                JGeometry geom = JGeometry.load(st);
                nodeCoors.put(rs.getInt(1), new PairInfo(geom.getPoint()[1], geom.getPoint()[0]));
            }
            rs.close();
            f.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(NodeToLandMarkDistance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

  private static void getLandMarkInfo() {
        try {
            Connection con = getConnection();
            String sql = "SELECT LM_ID,GEOM FROM LANDMARKINFO";
            PreparedStatement f = con.prepareStatement(sql);
            ResultSet rs = f.executeQuery();
            while (rs.next()) {
                STRUCT st = (STRUCT) rs.getObject(2);
                JGeometry geom = JGeometry.load(st);
                landMarkCoors.put(rs.getInt(1), new PairInfo(geom.getPoint()[1], geom.getPoint()[0]));
            }
            rs.close();
            f.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(NodeToLandMarkDistance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getNodeToLandMarkTime() {
        Set<Integer> keys1 = nodeCoors.keySet();
	Iterator<Integer> iter1 = keys1.iterator();
        for(int i=0;i<keys1.size();i++)
        {
            if(i%50==0)
            System.out.println(i);
            
            Set<Integer> keys2 = landMarkCoors.keySet();
            Iterator<Integer> iter2 = keys2.iterator();
            final int nodeId = iter1.next();

            for(int j=0;j<keys2.size();j++){
                   final int landMarkId = iter2.next();
                    Thread t1 = new Thread() {

                        @Override
                        public void run() {
                            try {
                                FileWriter fstream = null;
                                BufferedWriter out = null;
                                String name = nodeId + "." + landMarkId;
                                if (!(new File("H:\\Network123\\NodeToLandMark\\" + name + ".txt").exists())) {
                                    int time = tdsp(nodeCoors.get(nodeId), landMarkCoors.get(landMarkId));
                                    fstream = new FileWriter("H:\\Network123\\NodeToLandMark\\" + name + ".txt");
                                    out = new BufferedWriter(fstream);
                                    out.write(String.valueOf(time));
                                    out.close();
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(NodeToLandMarkDistance.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        private int tdsp(PairInfo node, PairInfo landMark) {
                            String start = node.getLati() + "," + node.getLongi();
                            String end = landMark.getLati() + "," + landMark.getLongi();
                            return t.process("False", start, end);
                        }
                    };

                    while (Thread.activeCount() > 20) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NodeToLandMarkDistance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                    t1.start();
                 
            }
        }
    }

        private static void getLandMarkToNodeTime() {
        Set<Integer> keys1 = nodeCoors.keySet();
	Iterator<Integer> iter1 = keys1.iterator();
        for(int i=0;i<keys1.size();i++)
        {
            if(i%50==0)
            System.out.println(i);

            Set<Integer> keys2 = landMarkCoors.keySet();
            Iterator<Integer> iter2 = keys2.iterator();
            final int nodeId = iter1.next();

            for(int j=0;j<keys2.size();j++){
                   final int landMarkId = iter2.next();
                    Thread t1 = new Thread() {

                        @Override
                        public void run() {
                            try {
                                FileWriter fstream = null;
                                BufferedWriter out = null;
                                String name = landMarkId + "." + nodeId;
                                if (!(new File("H:\\Network123\\LandMarkToNode\\" + name + ".txt").exists())) {
                                    int time = tdsp(landMarkCoors.get(landMarkId),nodeCoors.get(nodeId));
                                    fstream = new FileWriter("H:\\Network123\\LandMarkToNode\\" + name + ".txt");
                                    out = new BufferedWriter(fstream);
                                    out.write(String.valueOf(time));
                                    out.close();
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(NodeToLandMarkDistance.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        private int tdsp(PairInfo node, PairInfo landMark) {
                            String start = node.getLati() + "," + node.getLongi();
                            String end = landMark.getLati() + "," + landMark.getLongi();
                            return t.process("False", start, end);
                        }
                    };

                    while (Thread.activeCount() > 20) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NodeToLandMarkDistance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                    t1.start();

            }
        }
    }


}
