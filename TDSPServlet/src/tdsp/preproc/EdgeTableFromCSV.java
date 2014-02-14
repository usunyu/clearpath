package tdsp.preproc;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;

/**
 * This class reads the Edge file and writes it to the EdgeInfo table.
 * It gets the direction based on the highway name and then writes it to the database.
 * 
 *
 */
public class EdgeTableFromCSV {
	
	
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
	private static String FILE_LINK = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234New\\Edges.csv";
		//"H:\\clearp\\links_all.csv";
	static HashMap<String,String> map = new HashMap<String,String>();
	
	public static void main(String[] args) throws SQLException,
	NumberFormatException, IOException {

			readFileInMemory();
			getHwyDirections();
			writeDB();
			assignLinkDirections();

}
	
	private static void assignLinkDirections() throws IOException {
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
		System.out.println(keys.size());
		Connection con = getConnection();
		int i=0,count=0;
		FileWriter fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234New\\Edges2.csv");
		out = new BufferedWriter(fstream);
		
		while (i < keys.size()) {
			if(i%100==0)
				System.out.println(i);
			
			LinkInfo link = links.get(iter.next());
			if(link.getFunc_class()==3 || link.getFunc_class()==4 || link.getFunc_class()==5){
				i++;
				continue;
			}
			int direction = -1;
			double lati1 = link.getNodes()[0].getLati();
			double longi1 = link.getNodes()[0].getLongi();
			double lati2 = link.getNodes()[1].getLati();
			double longi2 = link.getNodes()[1].getLongi();
		
			try{
				
				String dir = map.get(link.getSt_name());
				if(dir.equals("0,1,2,3"))
				{
					direction = DistanceCalculator.getDirection(link);
				}
				else if(dir.equals("0,1"))
				{
					if(lati2>lati1){		// Going N
						direction = 0;
					}
					else if(lati2<lati1){		// Going S
						direction = 1;
					}
					else
					{
						direction = DistanceCalculator.getDirection(link);
					}
						
				}
				else if(dir.equals("2,3"))
				{
					if(longi2>longi1){		// Going E
						direction = 2;
					}
					else if(longi2<longi1){		// Going W
						direction = 3;
					}
					else
					{
						direction = DistanceCalculator.getDirection(link);
					}
				}
				
				if(link.getSt_name().contains("101"))
				{	
					if(lati1>lati2 && longi1>longi2 && direction==1)
						direction=0;
					if(lati1<lati2 && longi1<longi2 && direction==0)
						direction=1;
					
				}
			}
			catch(Exception e){
				
				direction = DistanceCalculator.getDirection(link);
			}
			if(direction==-1)
				System.out.println("Alert");
			
			updateDB(con,link.getLinkId(),direction);
			writeLinkToFile(out,link,direction);
			i++;
		}
		out.close();
		System.out.println(count);
		
	}
	private static void writeLinkToFile(BufferedWriter out2, LinkInfo link, int direction) throws IOException {
		out.write(link.getLinkId()+","+link.getFunc_class()+","+link.getSt_name()+","+direction+","+link.getStart_node()+","+link.getEnd_node()+","+link.getNodes()[0].getLati()+","+link.getNodes()[0].getLongi()+link.getNodes()[1].getLati()+","+link.getNodes()[1].getLongi()+"\n");
		
	}

	private static void updateDB(Connection con, long linkId, int direction) {
		
		String sql = "UPDATE EDGEINFO1234 SET DIRECTION="+direction +" where link_id="+linkId;
		//System.out.println(sql);
		PreparedStatement f = null;
		try {
			f = con.prepareStatement(sql);
			f.executeUpdate();
			f.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
	}
	private static void getHwyDirections() {
		
		map.put("114B", "0,1,2,3");
		map.put("3A", "0,1,2,3");
		map.put("3B", "0,1,2,3");
		map.put("ARTESIA FWY", "2,3");
		
		map.put("CA-110", "0,1");
		map.put("CA-118", "2,3");
		map.put("CA-126", "2,3");
		map.put("CA-134", "2,3");
		map.put("CA-170", "0,1");
		map.put("CA-2", "0,1");
		map.put("CA-210", "2,3");
		map.put("CA-23", "0,1");
		map.put("CA-57", "0,1");
		map.put("CA-60", "2,3");
		
		map.put("CA-91", "2,3");
		map.put("CA-91 HOV LN", "2,3");
		map.put("CENTURY FWY", "2,3");
		map.put("COLUMBIA ST", "0,1,2,3");
		map.put("FOOTHILL FWY", "2,3");
		map.put("GARDENA FWY", "2,3");
		map.put("GLENDALE BLVD", "0,1");
		map.put("GLENDALE FWY", "0,1");
		map.put("GLENN ANDERSON FWY", "2,3");
		map.put("GOLDEN STATE FWY", "0,1");
		map.put("HARBOR FWY", "0,1");
		
		map.put("HOLLYWOOD FWY", "0,1");
		map.put("I-10", "2,3");
		map.put("I-105", "2,3");
		map.put("I-110", "0,1");
		map.put("I-110-HOV-LN", "0,1");
		map.put("I-210", "2,3");
		map.put("I-405", "0,1");
		map.put("I-5", "0,1");
		map.put("I-5-HOV-LN", "0,1");
		map.put("I-5-TRUCK-BYP", "0,1");

		map.put("I-605", "0,1");
		map.put("I-710", "0,1");
		map.put("LONG BEACH FWY", "0,1");
		map.put("N ALVARADO ST", "0,1");
		map.put("ORANGE FWY", "0,1");
		map.put("ORANGE GROVE AVE", "2,3");
		map.put("PASADENA FWY", "0,1");
		map.put("POMONA FWY", "2,3");
		map.put("RIVERSIDE DR", "0,1");
		map.put("RIVERSIDE FWY", "2,3");

		map.put("RONALD REAGAN FWY", "2,3");
		map.put("ROSA PARKS FWY", "2,3");
		map.put("S PASADENA AVE", "0,1");
		map.put("S ST JOHN AVE", "0,1");
		map.put("SAN BERNARDINO FWY", "2,3");
		map.put("VENTURA FWY", "0,1,2,3");
		map.put("US-101", "0,1");
		map.put("WESTERN AVE", "0,1,2,3");
		map.put("VINELAND AVE", "0,1,2,3");
		map.put("VICTORY BLVD", "0,1,2,3");
		
		map.put("TUJUNGA AVE", "0,1,2,3");
		map.put("US HISTORIC ROUTE 66", "2,3");
		map.put("THOUSAND OAKS FWY", "0,1");
		map.put("SIMI VALLEY-SAN FERNANDO VALLEY FWY", "2,3");
		map.put("SIMI VALLEY FWY", "2,3");
		map.put("SANTA PAULA FWY", "2,3");
		map.put("SANTA MONICA FWY", "2,3");
		map.put("SANTA ANA FWY", "0,1");
		map.put("SAN GABRIEL RIVER FWY", "0,1");
		map.put("SAN DIEGO FWY", "0,1");
		
		
	}
	
	
	private static void writeDB() {
		int i = 0, count = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
		System.out.println(keys.size());
		Connection con = getConnection();
		while (i < keys.size()) {
			
			if(i%100==0)
				System.out.println(i);
			LinkInfo link = links.get(iter.next());
		String query = getQuery(link);
		String sql = "INSERT INTO EDGEINFO1234(LINK_ID,FUNC_CLASS,ST_NODE,END_NODE,GEOM) VALUES("+link.getLinkId()+","+link.getFunc_class()+",'"+link.getStart_node()+"','"+link.getEnd_node()+"',"+
		query+")";
		//System.out.println(sql);
		
		PreparedStatement f = null;
		try {
			f = con.prepareStatement(sql);
			f.executeUpdate();
			i++;
			f.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		}
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static String getQuery(LinkInfo link) {
		String query="MDSYS.SDO_GEOMETRY(2002,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(";
		for(int i=0;i<link.getNumPairs();i++)
		{
			PairInfo pair = link.getNodes()[i];
			if(i!=0)
			query+=","+pair.getLongi()+","+pair.getLati();
			else
				query+=pair.getLongi()+","+pair.getLati();	
		}
		query+="))";
		return query;
	}
	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager.getConnection(url_home,
					userName, password);
			return connHome;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return connHome;

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
				String st_name = nodes[2];
				String st_node = nodes[3];
				String end_node = nodes[4];
				String index = st_node.substring(1)+""+end_node.substring(1);
				String LinkIdIndex = String.valueOf(LinkId)+""+index;
				long index2 = Long.valueOf(LinkIdIndex);
				//System.out.println(nodes[0]+","+nodes[2].substring(1)+","+nodes[3].substring(1)+","+index2);
				
				int i = 5, count = 0;
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
				
				links.put(index2, new LinkInfo(index2, FuncClass, st_name, st_node,
						end_node, pairs, count));

			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}
}
