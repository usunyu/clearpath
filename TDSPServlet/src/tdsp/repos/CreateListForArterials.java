package tdsp.repos;

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
import java.util.Iterator;
import java.util.Set;

import javax.swing.text.html.MinimalHTMLWriter;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.UtilClass;
import Objects_repos.AdjList;

public class CreateListForArterials {

	static int numElem = 4575;
	static HashMap<Long, LinkInfo> links = new HashMap<Long, LinkInfo>();
	static HashMap<Long, LinkInfo> links2 = new HashMap<Long, LinkInfo>();
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
	@SuppressWarnings("unchecked")
	static ArrayList<Integer>[][] sensorsGrid = (ArrayList<Integer>[][]) new ArrayList[100][100];
	static LinkInfo[] links_with_sensors = new LinkInfo[83586];
	static ArrayList<Integer> check = new ArrayList<Integer>();
	private static int links_with_sensor_count;
	//private static String FILE_LINK ="H:\\clearp_arterial\\links_all_2.csv";
	private static String FILE_LINK = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Correct Network\\Edges.csv";
		//"H:\\clearp\\links_all.csv";

	public static void main(String[] args) throws SQLException,
			NumberFormatException, IOException {

		readFileInMemory();
		fillAdjList();
		GetLinkWithSensors();
		//getGridSensors();
		//getGridDetailsFromFile();
		//addSensorsToEmptyLinks();
		//GetLinkWithSensors();
		//findNearbySensorsToFillRemainingLinks();
		//GetLinkWithSensors();
		writeLinksToFile();
		//createPatterns();

	}

	

	private static void writeLinksToFile() throws IOException {
		FileWriter fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\Arterial_Sensor_Close.csv");
		out = new BufferedWriter(fstream);
		int i = 0, count = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			if (!(link.sensors.isEmpty())) {
				links_with_sensors[count] = link;
				out.write(String.valueOf(link.getLinkId()).substring(0,8)+",");
				out.write(String.valueOf(link.getLinkId()).substring(8)+",");
				count++;
				for (int j = 0; j < link.sensors.size(); j++) {
					out.write(link.sensors.get(j)+",");
					if (!(check.contains(link.sensors.get(j)))) {
						check.add(link.sensors.get(j));
					}
				}
				
			}
			i++;
			out.write("\n");
		}
		out.close();
		System.out.println("File Written" );
		
	}



	private static void getGridDetailsFromFile() {
		try {
			FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\grid.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count=0;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int index1 = Integer.parseInt(nodes[0]);
				int index2 = Integer.parseInt(nodes[1]);
				sensorsGrid[index1][index2] = new ArrayList<Integer>();
				int i = 2;
				//System.out.println(nodes.length);
				boolean flag=false;
				while (i < nodes.length) {
					flag=true;
					//System.out.println(index1+" "+index2+" "+nodes[i]);
				sensorsGrid[index1][index2].add(Integer.parseInt(nodes[i]));
				i++;
				}
				if(flag)
					count++;
			}
			System.out.println(count+" sensor locations formed");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
		
	}

	private static void addSensorsToEmptyLinks() {
		int i = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
		System.out.println(keys.size());
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			
			if(link.getFunc_class()==1 || link.getFunc_class()==2 || link.getFunc_class()==5)
			{	i++;
				continue;
			}
			
			if (link.sensors.isEmpty()) {
				
				//Start Lat,Long
				
				int idx1 = getIndex1(link.getNodes()[0].getLati());
				int idx2 = getIndex2(link.getNodes()[0].getLongi());
					if(sensorsGrid[idx1][idx2]!=null)
					{
						//System.out.println("Sensors found for this link with sensor count="+ sensorsGrid[idx1][idx2].size());
						int numElem = sensorsGrid[idx1][idx2].size();
						
						//links.remove(link.getLinkId());
						for(int i1=0;i1<numElem;i1++)
						{
							
							link.sensors.add(sensorsGrid[idx1][idx2].get(i1));
							
						}
						
					}
					
					//END Lat,Long
					
					idx1 = getIndex1(link.getNodes()[1].getLati());
					idx2 = getIndex2(link.getNodes()[1].getLongi());
						if(sensorsGrid[idx1][idx2]!=null)
						{
							//System.out.println("Sensors found for this link with sensor count="+ sensorsGrid[idx1][idx2].size());
							int numElem = sensorsGrid[idx1][idx2].size();
							for(int i1=0;i1<numElem;i1++)
							{
								
								link.sensors.add(sensorsGrid[idx1][idx2].get(i1));
								
							}
							links.put(link.getLinkId(), link);
						}
					
					
			}
			i++;
		}
	}
		
	private static void findNearbySensorsToFillRemainingLinks() {
		int i = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
		System.out.println(keys.size());
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			
			if(link.getFunc_class()==1 || link.getFunc_class()==2 || link.getFunc_class()==5)
			{	i++;
				continue;
			}
			
			if (link.sensors.isEmpty()) {
				
				//Start Lat,Long
				int factor=1;
				while(link.sensors.isEmpty() && factor<99){
				
				int idx1 = getIndex1(link.getNodes()[0].getLati());
				int idx2 = getIndex2(link.getNodes()[0].getLongi());
				for(int k1=Math.max(0,idx1-factor);k1<Math.min(99, idx1+factor);k1++)
				{
					for(int j1=Math.max(0,idx2-factor);j1<Math.min(99, idx2+factor);j1++)
					{
												
						if(sensorsGrid[k1][j1]!=null)
							{
								//System.out.println("Sensors found for this link with sensor count="+ sensorsGrid[idx1][idx2].size());
								int numElem = sensorsGrid[k1][j1].size();
								
								//links.remove(link.getLinkId());
								for(int i1=0;i1<numElem;i1++)
								{
									
									link.sensors.add(sensorsGrid[k1][j1].get(i1));
									
								}
								
							}
					}
				}
				
				idx1 = getIndex1(link.getNodes()[1].getLati());
				idx2 = getIndex2(link.getNodes()[1].getLongi());
				
				for(int k1=Math.max(0,idx1-factor);k1<Math.min(99, idx1+factor);k1++)
				{
					for(int j1=Math.max(0,idx2-factor);j1<Math.min(99, idx2+factor);j1++)
					{
						//END Lat,Long
							if(sensorsGrid[k1][j1]!=null)
								{
									//System.out.println("Sensors found for this link with sensor count="+ sensorsGrid[idx1][idx2].size());
									int numElem = sensorsGrid[k1][j1].size();
									for(int i1=0;i1<numElem;i1++)
									{
										
										link.sensors.add(sensorsGrid[k1][j1].get(i1));
										
									}
									
								}
					}
				}
				
				links.put(link.getLinkId(), link);		
				factor++;
			}
			}
			i++;
		}
		
	}
	
	
	
	
	private static int getIndex2(double longi) {

		//System.out.print(longi+" ");
		double l1 =-119.3;
		double l2=-117.6;
		if(longi<l1 || longi>l2)
			return 0;
		double step = (l2-l1)/100.0;
		int index=0;
		while(l1<longi)
		{
			index++;
			l1+=step;
		}
		//System.out.println(index);
		if(index>=100)
			return 99;
		return index;
	}

	private static int getIndex1(double lati) {
		//System.out.print(lati+" ");
		double l1 =34.2824;
		double l2=33.9072;
		if(lati>l1 || lati<l2)
			return 0;
		double step = (l1-l2)/100.0;
		int index=0;
		while(l1>lati)
		{
			index++;
			l1-=step;
		}
		//System.out.println(index);
		if(index>=100)
			return 99;
		return index;
	}

	private static void getGridSensors() throws SQLException {

		try{
			FileWriter fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\grid.txt");
			out = new BufferedWriter(fstream);
			
			double lat1= 34.2824,lat2=33.9072,long1=-119.3,long2=-117.6;
			//double lat1= 34.336,lat2=33.8,long1=-119.3,long2=-117.6;
		double latstep=(lat1-lat2)/100.0;
		double longstep = (long2-long1)/100.0;
		System.out.println(latstep+" "+longstep);
		Connection con = getConnection();
		int index1=0,count=0;
		for(double i=lat1;i>lat2;index1++,i=i-latstep)
		{	System.out.println(index1);
			if(index1>=100)
				continue;
			int index2=0;
			for(double j=long1;j<long2;index2++,j=j+longstep)
			{
				System.out.print(" "+index2);
				if(index2>=100)
					continue;
				out.write(index1+","+index2);
				double lati1=i,lati2=i-latstep,longi1=j,longi2=j+longstep;
				String geomQuery="MDSYS.SDO_GEOMETRY(2003,8307,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY("+
					longi1+","+lati1+","+longi2+","+lati2+"))";
				String sql = "select link_id from arterial_congestion_config where "+
				"SDO_relate(start_lat_long,"+geomQuery+",'mask=inside')='TRUE'";
				//System.out.println(sql);
				
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = f.executeQuery();
				
				sensorsGrid[index1][index2]= new ArrayList<Integer>();
				while (rs.next()) {
					count++;
					out.write(","+rs.getInt(1));
					sensorsGrid[index1][index2].add(rs.getInt(1));
				}
				rs.close();
				f.close();
				out.write("\n");
			}
		}
		con.close();
		out.close();
		fstream.close();
		System.out.println("\n"+count);
		}
		catch(Exception r)
		{
			r.printStackTrace();
		}
	}

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			Connection connHome = DriverManager.getConnection(url_home,
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
				
				int FuncClass = Integer.parseInt(nodes[1]);
				String st_node = nodes[2];
				String end_node = nodes[3];
				long LinkId = Long.parseLong(nodes[0]+""+nodes[2].substring(1)+""+nodes[3].substring(1));
				//System.out.println(LinkId);
				int i = 4, count = 0;
				PairInfo[] pairs = new PairInfo[1000];
				while (i < nodes.length) {
					double lati = Double.parseDouble(nodes[i]);
					double longi = Double.parseDouble(nodes[i + 1]);
					pairs[count] = new PairInfo(lati, longi);
					count++;
					i = i + 2;
				}
				if(links.get(LinkId) != null)
					System.out.println("Duplicate LinkIds");
				
				links.put(LinkId, new LinkInfo(LinkId, FuncClass, st_node,
						end_node, pairs, count));

			}
			//links2 = links;
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void fillAdjList() throws SQLException,
			NumberFormatException, IOException {

		System.out.println("Fill Adj List called with total count ="+ links.size());

		String sql = "select link_id,start_lat_long from arterial_congestion_config";
		Connection con = getConnection();
		PreparedStatement f = con.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = f.executeQuery();

		int count = 0;
		System.out.println("Getting Sensors from Config");
		while (rs.next()) {
			LinkIds[count] = rs.getInt(1);
			STRUCT st = (STRUCT) rs.getObject(2);
			JGeometry geom = JGeometry.load(st);
			pairs[count] = new PairInfo(geom.getPoint()[1], geom.getPoint()[0]);
			//Direction[count] = rs.getInt(3);
			count++;
		}
		rs.close();
		f.close();
		con.close();int c2 = 0;
		System.out.println("Sensors Successfully Imported from Config");
		
		for (int i = 0; i < numElem; i++) {
			hasEdge[i] = false;

			if(i%200==0)
			{
				System.out.println(((double)i/numElem*100.0)+"% of Sensors Processing Completed");
			}
		String sql1 = "SELECT LINK_ID,func_class,SDO_NN_DISTANCE(1) dist  FROM edgeinfo T2 where SDO_NN(T2.GEOM,MDSYS.SDO_GEOMETRY(2001,8307,MDSYS.SDO_POINT_TYPE("
					+ pairs[i].getQuery()
					+ ",NULL),NULL,NULL),'sdo_num_res=15 distance=200 unit=METER',1)='TRUE' order by dist";

			//System.out.println(sql1);
        	Connection con2 = getConnection();
			PreparedStatement f1 = con2.prepareStatement(sql1,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet r = f1.executeQuery();
			int c = 0;
			
			while (r.next()) {
				

					long linkId = r.getLong(1);
					if(r.getInt(2)==3 || r.getInt(2)==4)
					{
						hasEdge[i] = true;
					//System.out.println(pairs[i].getQuery());
					LinkInfo linkFound = links.get(linkId);
					//System.out.println(linkFound);
					links.remove(linkId);
					linkFound.sensors.add(LinkIds[i]);
					links.put(linkId, linkFound);
					c++;
					//break;
					}

			} 
			if(!hasEdge[i]) {
				System.out.println(sql1);
				c2++;
					}
			
			r.close();
			f.close();
			con2.close();

		}
		System.out.println(c2+" sensors with no link found within 200 meters");
	}

	private static void GetLinkWithSensors() {
		int i = 0, count = 0;
		Set<Long> keys = links.keySet();
		Iterator<Long> iter = keys.iterator();
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			if (!(link.sensors.isEmpty())) {
				links_with_sensors[count] = link;
				count++;
				for (int j = 0; j < link.sensors.size(); j++) {
					if (!(check.contains(link.sensors.get(j)))) {
						check.add(link.sensors.get(j));
					}
				}
				
			}
			i++;
		}
		System.out.println("Total Sensors found =" + check.size()
				+ " and total links on which sensors found=" + count);
		links_with_sensor_count = count;
	}

	private static void createPatterns() {
		int i = 0, j = 0;
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {

			System.out.println("Creating Patterns now");
			while (i < links_with_sensor_count) {

				if(i%200==0)
				{
					System.out.println(((double)i/links_with_sensor_count*100.0)+"% of Links Completed");
				}
				
				LinkInfo link = links_with_sensors[i];
				System.out.println(i + " " + link.toString());
				/*"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\temp"*/
				fstream = new FileWriter(/*"H:\\clearp_arterial\\Output_Files_Arterial\\"*/"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\temp"
						+ link.getLinkId() + ".txt");
				out = new BufferedWriter(fstream);

				double distance = getDistance(link);
				System.out.println("Link Distance=" + distance);
				out.write(link.toString());
				out.write("\n");
				out.write("Link Distance=" + distance);
				out.write("\n");
				
				int count = link.sensors.size();
				j = 0;
				String query = "";
				
				while (j < count) {
					if (j != count - 1)
						query += "link_id =" + link.sensors.get(j) + " OR ";
					else
						query += "link_id =" + link.sensors.get(j);
					j++;
				}
				
				boolean flag = true;
				
				for (int k = 0; k < 60; k++) {
				
				if(flag)
				{
										
					String query2 ="and MOD(TO_CHAR(t2.date_and_time, 'J'), 7) + 1 NOT IN (6, 7) and t2.date_and_time>= to_date('"+UtilClass.getStartTime(k)+"','HH24:MI') and t2.date_and_time< to_date('"+UtilClass.getEndTime(k)+"','HH24:MI')";
				String sql = "SELECT avg(t2.SPEED) FROM ARTERIAL_CONGESTION_HISTORY T2 WHERE "
						+ query+" "+query2;
				System.out.println(sql);
				Connection con = getConnection();
				PreparedStatement f = con.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet r = f.executeQuery();
				if(r.first())
				{
					Double average = r.getDouble(1);
					if(average!=0.0)
					{
					System.out.println(average);
					Double TravelTime = distance / average * 60;
					out.write(k + "," + average + "," + TravelTime);
					out.write("\n");
					System.out.println(k + "," + average + "," + TravelTime);
					}
					else
					{
						System.out.println("Average");
						out.write("Average");
						out.write("\n");
						out.close();
						fstream.close();
						r.close();
						f.close();
						con.close();
						flag=false;
						continue;
					}
				}
				r.close();
				f.close();
				con.close();
				
				}
					else
					{
						break;
					}
				}
				
				i++;
				out.close();
				fstream.close();
				}
				
				
				
			
			System.out.println("File Writing Complete");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static double getDistance(LinkInfo link) {
		PairInfo[] pairs = link.getNodes();
		double distance = 0.0;
		for (int i = 0; i < link.getPairCount() - 1; i++) {
			distance += DistanceCalculator.CalculationByDistance(pairs[i],
					pairs[i + 1]);
		}
		return distance;
	}

	

}
