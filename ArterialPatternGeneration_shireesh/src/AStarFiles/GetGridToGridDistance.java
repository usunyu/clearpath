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
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import tdsp.TDSPQueryJava;

/**
 * 
 * @author clearp
 */
public class GetGridToGridDistance {
	private static int[] gridId = new int[2500];
	private static int gridCount = 0;
	private static HashMap<Integer, ArrayList<Integer>> gridBP = new HashMap<Integer, ArrayList<Integer>>();
	private static HashMap<Integer, Integer> nodeGrids = new HashMap<Integer, Integer>();
	private static HashMap<Integer, PairInfo> nodeCoors = new HashMap<Integer, PairInfo>();
	static TDSPQueryJava t = new TDSPQueryJava();

	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;

	public static void main(String args[]) {

		readGridFileIntoMemory();
		readNodesFromDB();
		getGridTime();
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
				if (!(grid_nodes.isEmpty())) {
					gridBP.put(GridId, grid_nodes);
					gridId[gridCount++] = GridId;
				}

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

	private static int tdsp(int nodeId, Integer bp) {
		String start = nodeCoors.get(nodeId).getLati() + ","
				+ nodeCoors.get(nodeId).getLongi();
		String end = nodeCoors.get(bp).getLati() + ","
				+ nodeCoors.get(bp).getLongi();
		return t.process("False", start, end);
	}

	private static void getGridTime() {
		int i, j;
		try {
			System.out.println(gridCount);

			for (/* i=0 */i = 134; i < gridCount; i++) {

				System.out.print(i + ":");
				boolean flag = false;
				for (j = 0; j < gridCount; j++) {
					if (i == 134 && !flag) {
						j = 170;
						flag = true;
					}
					if (i == j)
						continue;

					final int i1 = i;
					final int j1 = j;
					Thread t1 = new Thread() {

						@Override
						public void run() {
							FileWriter fstream = null;
							try {
								String name = String.valueOf(i1) + "."
										+ String.valueOf(j1);

								ArrayList<Integer> bpsi = gridBP
										.get(gridId[i1]);
								ArrayList<Integer> bpsj = gridBP
										.get(gridId[j1]);
								String tt = getShortestTT(bpsi, bpsj);
								fstream = new FileWriter(
										"H:\\Network1234\\GridToGrid20_20\\"
												+ name + ".csv");
								BufferedWriter out = new BufferedWriter(fstream);
								if (!(tt.equals("8000"))) {
									out.write(gridId[i1] + "," + gridId[j1]
											+ "," + tt + "\n");
								}
								out.close();
								System.out.print(j1 + ",");
							} catch (IOException ex) {
								Logger.getLogger(
										GetGridToGridDistance.class.getName())
										.log(Level.SEVERE, null, ex);
							} finally {
								try {
									fstream.close();
								} catch (IOException ex) {
									Logger.getLogger(
											GetGridToGridDistance.class
													.getName()).log(
											Level.SEVERE, null, ex);
								}
							}
						}
					};

					while (Thread.activeCount() > 1)
						Thread.yield();

					t1.start();
				}
				System.out.println();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getShortestTT(ArrayList<Integer> bpsi,
			ArrayList<Integer> bpsj) {
		int min = 8000000;

		for (int i = 0; i < bpsi.size(); i++) {
			for (int j = 0; j < bpsj.size(); j++) {
				int time = tdsp(bpsi.get(i), bpsj.get(j));

				if (time < min)
					min = time;
			}
		}
		return String.valueOf(min);
	}

}
