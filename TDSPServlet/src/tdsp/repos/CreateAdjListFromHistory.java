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

import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.UtilClass;
import Objects_repos.AdjList;

public class CreateAdjListFromHistory {

	  static LinkInfo links [] = new LinkInfo[10000];
	  static int link_count = 0;
	  static String url_home = "jdbc:oracle:thin:@localhost:1522:xe2";
	  static String userName = "clearp";
	  static String password = "clearp";
	  static Connection connHome = null;
	  static BufferedWriter out;
	  static int noData =0;

	  
	public static void main(String[] args) throws SQLException, NumberFormatException, IOException {

		readFileInMemory();
		getConnection();
		//Init();
		fillAdjList();
		//WriteFile();
		System.out.println("Average to be set in ="+noData);
	}


	private static void Init() {
		 FileWriter fstream = null;
		try {
			fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\out.txt");
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		 

	}


	private static void getConnection() {
		  try{
	            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
	            System.out.println("connecting home database.....");
	            connHome = DriverManager.getConnection(url_home, userName, password);
		  }
		  catch(Exception e)
		  {
			  System.out.println(e.getMessage());
		  }
		
	}


	private static void readFileInMemory() {
		
		try{
			  FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\links_all.csv");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  while ((strLine = br.readLine()) != null)   {
			  String [] nodes = strLine.split(",");
			  int LinkId = Integer.parseInt(nodes[0]);
			  int FuncClass = Integer.parseInt(nodes[1]);
			  String st_node = nodes[2];
			  String end_node = nodes[3];
			  int i=4,count=0;
			  PairInfo [] pairs = new PairInfo[100];
			  while(i<nodes.length)
			  {
				  double lati = Double.parseDouble(nodes[i]);
				  double longi = Double.parseDouble(nodes[i+1]);
				  pairs[count] = new PairInfo(lati, longi);
				  count++;
				  i=i+2;
			  }
			  
			  links[link_count++] = new LinkInfo(LinkId,FuncClass,st_node,end_node,pairs, count);
			  }
		  in.close();
			    }catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
			  }

		
	}
	
	private static void fillAdjList() throws SQLException, NumberFormatException, IOException {
	
		System.out.println("Fill Adj List called with total index i ="+link_count);
		FileWriter fstream2 = null;
		fstream2 = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\out.txt");
		BufferedWriter out2= new BufferedWriter(fstream2);
		for(int i = 0;i<link_count;i++)
		{
			System.out.println(i);
			String [] linkIds = new String[2000];
			int linkCount=0;
			FileWriter fstream = null;
			fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Output_Files\\out"+i+".txt");
			out = new BufferedWriter(fstream);
		String query = getQuery(i);
		double distance = getDistance(i);
		System.out.println("distance ="+distance);
		String linkIdQuery="";
		
		if(links[i].getFunc_class()==2)
		{
		String sql1 = "select link_id from highway_congestion_config where "+query;
		//System.out.println(sql1);
          PreparedStatement f = connHome.prepareStatement(sql1, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
          ResultSet r = f.executeQuery();
          
          if(!r.first())
          {
        	  System.out.println("No data found....setting average");
           	  out.write("Average");
           	  out2.write("Average");
           	  out.close();
           	noData++;
           	  continue;
          }
        	 while(r.next())
        	 { 
        		     linkIds[linkCount++] = r.getString(1);
        	 }
          r.close();
     	 f.close();
        	 for(int u=0;u<linkCount;u++)
        	 {	 System.out.println(linkIds[u]);
        	     out2.write(linkIds[u]+",");
        	 }
        	 System.out.println("Getting LinkIds");
        	 linkIdQuery = getLinkIds(linkIds, linkCount);
        	 System.out.println(linkIdQuery);
		}
		else
		{
			String sql1 = "select link_id from arterial_congestion_config where "+query;
			//System.out.println(sql1);
	          PreparedStatement f = connHome.prepareStatement(sql1, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	          ResultSet r = f.executeQuery();
	         
	          if(!r.first())
	          {
	        	  System.out.println("No data found....setting average");
               	  out.write("Average");
               	  out2.write("Average");
               	  out.close();
               	noData++;
               	  continue;
	          }
	        	 while(r.next())
	        	 {
	        		 System.out.println("Hello");
	        		linkIds[linkCount++] = r.getString(1);
	        	 }
	        	 r.close();
	        	 f.close();
	        	 for(int u=0;u<linkCount;u++)
	        	 {	 System.out.println(linkIds[u]);
	        	     out2.write(linkIds[u]+",");
	        	 }
	        	 
	        	 System.out.println("Getting LinkIds");
	        	 linkIdQuery = getLinkIds(linkIds, linkCount);
	        	 System.out.println(linkIdQuery);
		}
        	 out2.write("\n");
		/*
		for( int j=0; j<60;j++)
		{
		
		System.out.println("i="+i+",j="+j);	
		   String st_time = UtilClass.getStartTime(j);
		   String end_time = UtilClass.getEndTime(j);
		
		if(links[i].getFunc_class()==2)
		{
			System.out.println("Highway");
			String sql = "select avg(t1.speed) from highway_congestion_history_2  t1 "+
			"where to_char(t1.date_and_time, 'HH24:MI')<= '"+end_time+
			"' and to_char(t1.date_and_time, 'HH24:MI') >= '"+st_time+
			"' and "+linkIdQuery;
	          PreparedStatement fetch = connHome.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	          //fetch.setFetchSize(100000);
	          System.out.println(sql);
	          ResultSet result = fetch.executeQuery();
	          if(result.next())
	          {
	        	  out.write(links[i].getStart_node()+","+ links[i].getEnd_node()+","+ j+"," +(distance*60.0/Double.parseDouble(result.getString(1)))+"\n");
	        	  System.out.println("Travel Time(mins)="+(distance*60.0/Double.parseDouble(result.getString(1))));
	          }
	    
		}
		else
		{
			System.out.println("Arterial");
			String sql = "select avg(t1.speed) from arterial_congestion_history  t1 "+
			"where to_char(t1.date_and_time, 'HH24:MI')<= '"+end_time+
			"' and to_char(t1.date_and_time, 'HH24:MI') >= '"+st_time+
			"' and "+linkIdQuery;
	          PreparedStatement fetch = connHome.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	          //fetch.setFetchSize(100000);
	          System.out.println(sql);
	          ResultSet result = fetch.executeQuery();
	          if(result.next())
	          { 	 
	           	  out.write(links[i].getStart_node()+","+ links[i].getEnd_node()+","+ j+"," +(distance*60.0/Double.parseDouble(result.getString(1)))+"\n");
	          	         System.out.println("Travel Time(mins)="+(distance*60.0/Double.parseDouble(result.getString(1))));
	          }
		}
		}  */
		out.close(); 
	}	
	out2.close();
	}


	private static String getLinkIds(String[] linkIds, int linkCount) {
		String res = "";
		int i = 0;
	/*	System.out.println(linkCount);
	*/	while(i<linkCount)
		{
			if(i==linkCount-1)
			res+= "t1.link_id ="+linkIds[i]+"";
			else
				res+= "t1.link_id ="+linkIds[i]+" OR";
			i++;
		}
		return res;
	}


	private static double getDistance(int link_count) {
		PairInfo[] pairs = links[link_count].getNodes();
		double distance=0.0;
		for(int i=0;i<links[link_count].getPairCount()-1;i++)
		{
			distance+= DistanceCalculator.CalculationByDistance(pairs[i], pairs[i+1]);
		}
		return distance;
	}


	private static String getQuery(int link_count2) {
		String ret =  "SDO_TOUCH(start_lat_long,SDO_GEOMETRY(2006,NULL,NULL,SDO_ELEM_INFO_ARRAY(";
		String query1 = "";
		String query2 = "";
		PairInfo[] pairs = links[link_count2].getNodes();
		for(int i=0;i<links[link_count2].getPairCount();i++)
		{
			if(i!=0 && (i%2)!=0)
			{
				
			if(i!=links[link_count2].getPairCount()-1 && i!=links[link_count2].getPairCount()-2)
			query1 += ""+String.valueOf((((i+1)/2-1)*4)+1)+",2,1,";
			else
			query1 += ""+String.valueOf((((i+1)/2-1)*4)+1)+",2,1";
			}
			if(query2.equals(""))
			query2 += pairs[i].getQuery();
			else
			query2 += ","+pairs[i].getQuery();	
		}
		ret += query1+"),SDO_ORDINATE_ARRAY(";
		ret += query2+")))='TRUE'";
		
		return ret;
	}
	
	
	private static void WriteFile() {

		try{
			  out.close();
			  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	}


}
