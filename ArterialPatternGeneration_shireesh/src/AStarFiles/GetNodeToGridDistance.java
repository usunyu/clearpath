/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AStarFiles;

import Objects.PairInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
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
public class GetNodeToGridDistance {
	private static HashMap<Integer, ArrayList<Integer>> gridBP = new HashMap<Integer, ArrayList<Integer>>();
	private static HashMap<Integer, Integer> nodeGrids = new HashMap<Integer, Integer>();
	private static HashMap<Integer, PairInfo> nodeCoors = new HashMap<Integer, PairInfo>();
	private static HashMap<Integer, Integer> nodeGridOutTime = new HashMap<Integer, Integer>();
	static TDSPQueryJava t = new TDSPQueryJava();

	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

	public static void main(String args[]) {

		readGridFileIntoMemory();
		readNodesFromDB();
		System.out.println(nodeGrids.size());
		System.out.println(nodeCoors.size());
		getNodeGridOutTime();
		System.out.println(nodeGridOutTime.size());
		writeNodeTimeToFile();
		writeNodeGridToFile();
	}

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

	private static void readGridFileIntoMemory() {
		try {
			FileInputStream fstream = new FileInputStream(
					"H:\\Network1234\\GridBPs20_20.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int GridId = Integer.parseInt(nodes[0]);
				int i = 1;
				ArrayList<Integer> grid_nodes = new ArrayList<Integer>();
				while (i < nodes.length) {
					grid_nodes.add(Integer.parseInt(nodes[i]));
					i++;
				}
				gridBP.put(GridId, grid_nodes);

			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static void readNodesFromDB() {
		try {
			Connection con = getConnection();
			String sql = "SELECT NODEID,GRIDID,GEOM FROM NODEINFO20_20";
			PreparedStatement f = con.prepareStatement(sql);
			ResultSet rs = f.executeQuery();
			while (rs.next()) {
				nodeGrids.put(rs.getInt(1), rs.getInt(2));
				STRUCT st = (STRUCT) rs.getObject(3);
				JGeometry geom = JGeometry.load(st);
				nodeCoors.put(rs.getInt(1), new PairInfo(geom.getPoint()[1],
						geom.getPoint()[0]));
			}
			rs.close();
			f.close();
			con.close();

		} catch (SQLException ex) {
			Logger.getLogger(GetNodeToGridDistance.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	private static void getNodeGridOutTime() {
		Set<Integer> keys = nodeGrids.keySet();
		int count = 0;
		Iterator<Integer> iter = keys.iterator();
		int i = 0;
		while (i < keys.size()) {

			if (i % 20 == 0) {
				System.out.println("Node count=" + i
						+ "  and nodes with no grid bp found =" + count);
			}

			int nodeId = iter.next();
			int gridId = nodeGrids.get(nodeId);

			if (!gridBP.containsKey(gridId)) {
				count++;
				nodeGridOutTime.put(nodeId, 0);
				i++;
				continue;
			}

			if (gridBP.get(gridId).contains(nodeId)) {
				nodeGridOutTime.put(nodeId, 0);
				i++;
				continue;
			}
			nodeGridOutTime.put(nodeId,
					getNodeToBPShortestTime(nodeId, gridBP.get(gridId)));
			i++;
		}

	}

	private static Integer getNodeToBPShortestTime(int nodeId,
			ArrayList<Integer> BPs) {

		int min = 8000000;

		for (int i = 0; i < BPs.size(); i++) {
			int time = tdsp(nodeId, BPs.get(i));
			if (time < min)
				min = time;
		}
		return min;
	}

	private static int tdsp(int nodeId, Integer bp) {
		String start = nodeCoors.get(nodeId).getLati() + ","
				+ nodeCoors.get(nodeId).getLongi();
		String end = nodeCoors.get(bp).getLati() + ","
				+ nodeCoors.get(bp).getLongi();
		return t.process("False", start, end);
	}

	private static void writeNodeTimeToFile() {
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("H:\\Network1234\\NodeBPTime.csv");
			out = new BufferedWriter(fstream);
			Set<Integer> keys = nodeGridOutTime.keySet();
			Iterator<Integer> iter = keys.iterator();
			int i = 0;
			while (i < keys.size()) {
				int nodeId = iter.next();
				int tt = nodeGridOutTime.get(nodeId);
				out.write(nodeId + "," + tt + "\n");
				i++;
			}
			out.close();
		} catch (Exception ex) {
			Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	private static void writeNodeGridToFile() {
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("H:\\Network1234\\NodeGridInfo.csv");
			out = new BufferedWriter(fstream);
			Set<Integer> keys = nodeGrids.keySet();
			Iterator<Integer> iter = keys.iterator();
			int i = 0;
			while (i < keys.size()) {
				int nodeId = iter.next();
				int gridId = nodeGrids.get(nodeId);
				out.write(nodeId + "," + gridId + "\n");
				i++;
			}
			out.close();
		} catch (Exception ex) {
			Logger.getLogger(GridBPMapping.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

}
