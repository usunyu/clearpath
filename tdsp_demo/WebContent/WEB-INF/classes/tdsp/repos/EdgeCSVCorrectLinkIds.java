package tdsp.repos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Objects.LinkInfo;
import Objects.PairInfo;
import Objects_repos.AdjList;

public class EdgeCSVCorrectLinkIds {

	static int numElem = 2032;
	static HashMap<Long, LinkInfo> links = new HashMap<Long, LinkInfo>();
	static int link_count = 0;
	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	static BufferedWriter out;
	static int noData = 0;
	static int LinkIds[] = new int[numElem];
	static PairInfo pairs[] = new PairInfo[numElem];
	static boolean hasEdge[] = new boolean[numElem];
	static int Direction[] = new int[numElem];
	@SuppressWarnings("unchecked")
	static ArrayList<Double>[] speeds = (ArrayList<Double>[]) new ArrayList[60];
	static LinkInfo[] links_with_sensors = new LinkInfo[9208];
	static ArrayList<Integer> check = new ArrayList<Integer>();
	private static int links_with_sensor_count;
	//private static String FILE_LINK ="H:\\clearp\\links_all.csv";
	private static String FILE_LINK = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Correct Network\\Edges.csv";
		//"H:\\clearp\\links_all.csv";
	
	public static void main(String[] args) throws SQLException,
	NumberFormatException, IOException {

readFileInMemory();
writeFile();
//createPatterns();

}

	private static void writeFile() throws IOException {
		int i = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
        BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Users\\Shireesh\\Desktop\\Edges_2.csv",true));
        while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			out.write(link.getLinkId()+","+link.getStart_node()+","+link.getEnd_node());
			for(int i1=0;i1<link.getNumPairs();i1++)
			{
				PairInfo pair = link.getNodes()[i1];
				out.write(","+pair.getLati()+","+pair.getLongi());
				}
			out.write("\n");
			i++;
		}
		
	}

	private static void readFileInMemory() {
		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int LinkId = Integer.parseInt(nodes[0]);
				int FuncClass = Integer.parseInt(nodes[1]);
				String st_node = nodes[2];
				String end_node = nodes[3];
				String index = st_node.substring(1)+""+end_node.substring(1);
				String LinkIdIndex = String.valueOf(LinkId)+""+index;
				long index2 = Long.valueOf(LinkIdIndex);
				System.out.println(index2);
				int i = 4, count = 0;
				PairInfo[] pairs = new PairInfo[1000];
				while (i < nodes.length) {
					double lati = Double.parseDouble(nodes[i]);
					double longi = Double.parseDouble(nodes[i + 1]);
					pairs[count] = new PairInfo(lati, longi);
					count++;
					i = i + 2;
				}
				if(links.get(index2) != null)
					System.out.println(links.get(index2)+"Duplicate LinkIds");
				
				links.put(index2, new LinkInfo(index2, FuncClass, st_node,
						end_node, pairs, count));

			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

		
	}
	
}
