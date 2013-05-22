/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package highwaypatterngeneration;

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

import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateListForHighways1to5 {

	static int numElem = 2032;
	static HashMap<String, LinkInfo> links = new HashMap<String, LinkInfo>();
        static HashMap<Integer,HashMap<Integer,Double []>> links_speed = new HashMap<Integer,HashMap<Integer,Double []>>();
        static int link_count = 0;
        static String url_home = "jdbc:oracle:thin:@gd.usc.edu:1521:adms";
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
	static LinkInfo[] links_with_sensors = new LinkInfo[1100000];
	static ArrayList<Integer> check = new ArrayList<Integer>();
	private static int links_with_sensor_count;
	//private static String FILE_LINK ="H:\\clearp\\links_all.csv";
	private static String FILE_LINK = "H:\\Jiayunge\\Edges_G_12345_final_extraStnames.csv";
		//"H:\\clearp\\links_all.csv";
        private static String [] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        
        
    static HashMap<Integer,String> sensors_stNames = new HashMap<Integer, String>();  
    static HashMap<String,Integer> LinkDirection = new HashMap<String,Integer>();
    
    @SuppressWarnings("unchecked")
	static ArrayList<Integer>[][] sensorsGrid = (ArrayList<Integer>[][]) new ArrayList[500][500];
	@SuppressWarnings("unchecked")
	static ArrayList<Integer>[][] sensorsGridDir = (ArrayList<Integer>[][]) new ArrayList[500][500];
        static int factor_count[] = new int[500];

	public static void main(String[] args) throws SQLException,
			NumberFormatException, IOException {

		readFileInMemory();
                
                readEdgeSensors();
           //add
                getLinkDirections();
                getGridDetailsFromFile();
                
           //add ends     
		GetLinkWithSensors();
                
                for(int i=0;i<days.length;i++){
                        final int index = i;
                        Thread t1 = new Thread() {
                        @Override
                        public void run() {
                           try {
                                 System.out.println("Getting Speeds for " + days[index]);
                                 getHighwaySensorAverages(index, days[index]);
                                 System.out.println("Creating patterns for " + days[index]);
                                 createPatterns_new(index, days[index]);
                              } catch (SQLException ex) {
                                   Logger.getLogger(CreateListForHighways.class.getName()).log(Level.SEVERE, null, ex);
                               }
                         }
                    };
                    t1.start();
                    
            }
		/*
		 int index = 0;
		 System.out.println("Getting Speeds for " + days[index]);
         getHighwaySensorAverages(index, days[index]);
         System.out.println("Creating patterns for " + days[index]);
        // createPatterns(index, days[index]);
         createPatterns_new(index, days[index]);
                //writeLinksToFile();
          */       
                 
         System.out.println("\n finished!!!!");
	}

	private static void createPatterns_new(int index, String day) {
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
				//System.out.println(i + " " + link.toString());
                                fstream = new FileWriter("H:\\Jiayunge\\Output_"+day+"\\"
						+ link.getLinkId() + ".txt");
				
				out = new BufferedWriter(fstream);

				double distance = getDistance(link);
				//System.out.println("Link Distance=" + distance);
				out.write(link.toString());
				out.write("\n");
				out.write("Link Distance=" + distance);
				out.write("\n");

				int count = link.sensors.size();
				for (int k = 0; k < 60; k++) {
                                double avg=0.0;
                                int count_valid = 0;
                                    for(j = 0;j<count;j++)
                                {
                                      if(links_speed.get(index).containsKey(link.sensors.get(j)))
                                        avg+=links_speed.get(index).get(link.sensors.get(j))[k];
                                      if(links_speed.get(index).get(link.sensors.get(j))[k]>0.)
                                    	  count_valid++;
                                }

                                if(avg!=0.0){
                                    avg/=count_valid;
                                    Double TravelTime = distance / avg * 60;
					                out.write(k + "," + avg + "," + TravelTime);
					                out.write("\n");
                                }
                                else {
                                     
                                	
                                	double temp_speed = approximation(link,index,k);
                                	if(temp_speed > 0.){
                                		out.write(k + "," + temp_speed + "," + distance / temp_speed * 60.);
                                	    out.write("\n");
                                	}
                                	else{                                	
                                        out.write("Average");
						                out.write("\n");
						                out.close();
						                fstream.close();
                                        break;
                                    }
                                }

                            }
                              	i++;
                              	System.out.println(i+"roads finished!!!");
				out.close();
				fstream.close();
				}
                                    



			System.out.println("File Writing Complete");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
        private static void readEdgeSensors() {

		try {
			System.out.println("Reading Edge Sensors---"+ links.size());

			FileInputStream fstream = new FileInputStream("H:\\Jiayunge\\Highway_Sensor_close.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if(strLine.equals(""))
					continue;

				String[] nodes = strLine.split(",");

				if(nodes.length==0 || nodes.length==1 || nodes.length==2)
					continue;
				String LinkId = nodes[0]+""+nodes[1];
                                System.out.println(LinkId);
				int i = 2;
				ArrayList<Integer> sensors = new ArrayList<Integer>();
				while (i < nodes.length) {
					sensors.add(Integer.parseInt(nodes[i]));
					i++;
				}
				 
				  LinkInfo link = links.get(LinkId);
				  link.sensors = sensors;
				  links.put(LinkId, link);
				 
			}
				in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

		}


	private static void writeLinksToFile() throws IOException {
		FileWriter fstream = new FileWriter("H:\\Jiayunge\\Highway_Sensor_Close.csv");
		out = new BufferedWriter(fstream);
		int i = 0, count = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
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

	private static void readFileInMemory() {

		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				String LinkId = nodes[0];
				int FuncClass = Integer.parseInt(nodes[1]);

              if(FuncClass == 1 || FuncClass == 2)
             {
				String st_name = nodes[2];
				String st_node = nodes[3];
				String end_node = nodes[4];
				String index = st_node.substring(1)+""+end_node.substring(1);
				String LinkIdIndex = LinkId+""+index;
                            	String index2 = LinkIdIndex;
			
				int i = 5, count = 0;
				PairInfo[] pairs = new PairInfo[10];
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
                    }
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}

		private static void GetLinkWithSensors() {
		int i = 0, count = 0;
		Set<String> keys = links.keySet();
		Iterator<String> iter = keys.iterator();
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

	private static void createPatterns(int index, String day) {
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
				//System.out.println(i + " " + link.toString());
                                fstream = new FileWriter("C:\\Users\\jiayunge\\Output_"+day+"\\"
						+ link.getLinkId() + ".txt");
				
				out = new BufferedWriter(fstream);

				double distance = getDistance(link);
				//System.out.println("Link Distance=" + distance);
				out.write(link.toString());
				out.write("\n");
				out.write("Link Distance=" + distance);
				out.write("\n");

				int count = link.sensors.size();
				for (int k = 0; k < 60; k++) {
                                double avg=0.0;
                                    for(j = 0;j<count;j++)
                                {
                                      if(links_speed.get(index).containsKey(link.sensors.get(j)))
                                        avg+=links_speed.get(index).get(link.sensors.get(j))[k];
                                }

                                if(avg!=0.0){
                                    avg/=count;
                                    Double TravelTime = distance / avg * 60;
					out.write(k + "," + avg + "," + TravelTime);
					out.write("\n");
                                }
                                else {
                                        out.write("Average");
						out.write("\n");
						out.close();
						fstream.close();
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

    private static void getHighwaySensorAverages(int index, String day) throws SQLException {

        String sql = "select link_id,onstreet, fromstreet from highway_congestion_config";
		Connection con = getConnection();
		PreparedStatement f = con.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = f.executeQuery();

		int count = 0;
		System.out.println("Getting Sensors from Config");
		while (rs.next()) {
			LinkIds[count] = rs.getInt(1);			
			count++;
			sensors_stNames.put(rs.getInt(1),rs.getString(2)+","+rs.getString(3));
		}
		rs.close();
		f.close();
		con.close();int c2 = 0,c3=0;
		System.out.println(count+" Sensors Successfully Imported from Config");
		con = getConnection();
		
		HashMap<Integer,Double []> speeds = new HashMap<Integer,Double []>();
		
                for(int i=0;i<count;i++){
                    if(i%100==0){
                        System.out.println(i);
                        con.close();
                        con = getConnection();
                    }
                    if(day.equals("All"))
                     sql = "select avg(speed) from highway_Averages3_april where link_id="+LinkIds[i]+"group by time order by time";
                    else
                    sql = "select speed from highway_Averages3_april where day='"+day+"' and month = 'April' and link_id= '"+LinkIds[i]+"' order by time";

                    f = con.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                     rs = f.executeQuery();
                     int k=0;
                     Double speed[] = new Double[60];
                     
                     if(!rs.next()){
                   	  for(int t=0;t<60;)
                   		  speed[t++] = 0.;
                     }
                     else{
                       rs.beforeFirst();	 
                       while(rs.next() && k<60){
                          speed[k++] = rs.getDouble(1);
                        }
                       for(;k<60;k++)
                       	speed[k] = 0.;
                     }
                     speeds.put(LinkIds[i], speed);
                     
                     rs.close();
                     f.close();
                }
                
                Double speed[] = new Double[60];
                for(int k=0;k<60;k++)
                	speed[k] = 0.;
                speeds.put(-1, speed);
                links_speed.put(index,speeds);
    }

   public static double approximation(LinkInfo link,int index, int k){
    	try{
    		    int find_mark = 0;
    		    ArrayList<Integer> available_sensors = new ArrayList<Integer>();
				int factor=1;
				
				while((find_mark==0) && factor<499){

				 int idx1 = getIndex1(link.getNodes()[0].getLati());
				 int idx2 = getIndex2(link.getNodes()[0].getLongi());
				 for(int k1=Math.max(0,idx1-factor);k1<Math.min(499, idx1+factor);k1++)
				 {
					 for(int j1=Math.max(0,idx2-factor);j1<Math.min(499, idx2+factor);j1++)
					 {

						 if(sensorsGrid[k1][j1]!=null)
							{
								int numElem2 = sensorsGrid[k1][j1].size();
								
								for(int i1=0;i1<numElem2;i1++)
								{   
									
									
									if(LinkDirection.get(link.getLinkId()) == sensorsGridDir[k1][j1].get(i1)&&(links_speed.get(index).get(sensorsGrid[k1][j1].get(i1))[k]>0.))
                                    {
										 String[] nodes = link.getSt_name().split(";");
										 
										 
										 
										 int hit = 0;
										 for(int s= 0 ;s < nodes.length;s++){
										    if(sensors_stNames.get(sensorsGrid[k1][j1].get(i1)).split(",")[0].equals(nodes[s]))
										    	hit = 1;
										    if(sensors_stNames.get(sensorsGrid[k1][j1].get(i1)).split(",")[1].equals(nodes[s]))
										    	hit = 1;
									     }
										 
						            
										 
										 if(hit==1){
											if(!available_sensors.contains(sensorsGrid[k1][j1].get(i1))) 
											  available_sensors.add(sensorsGrid[k1][j1].get(i1));
										    find_mark = 1;
										 }
                                         //link.sensors.add(sensorsGrid[k1][j1].get(i1));
                                         //factor_count[factor]++;
                                    }
									//rs.close();
									//f.close();

								}

							}
					}
				}

				idx1 = getIndex1(link.getNodes()[1].getLati());
				idx2 = getIndex2(link.getNodes()[1].getLongi());

				for(int k1=Math.max(0,idx1-factor);k1<Math.min(499, idx1+factor);k1++)
				{
					for(int j1=Math.max(0,idx2-factor);j1<Math.min(499, idx2+factor);j1++)
					{
						//END Lat,Long
							if(sensorsGrid[k1][j1]!=null)
								{
									//System.out.println("Sensors found for this link with sensor count="+ sensorsGrid[idx1][idx2].size());
									int numElem2 = sensorsGrid[k1][j1].size();
									for(int i1=0;i1<numElem2;i1++)
									{  
										
										if(LinkDirection.get(link.getLinkId()) == sensorsGridDir[k1][j1].get(i1)&&(links_speed.get(index).get(sensorsGrid[k1][j1].get(i1))[k]>0.))
                                        {
											 String[] nodes = link.getSt_name().split(";");
											 
											
											
											 
											 int hit = 0;
											 for(int s= 0 ;s < nodes.length;s++){
											    if(sensors_stNames.get(sensorsGrid[k1][j1].get(i1)).split(",")[0].equals(nodes[s]))
											    	hit = 1;
											    if(sensors_stNames.get(sensorsGrid[k1][j1].get(i1)).split(",")[1].equals(nodes[s]))
											    	hit = 1;
										     }
											 
											 
											 
											 
											 if(hit==1){
												if(!available_sensors.contains(sensorsGrid[k1][j1].get(i1))) 
												   available_sensors.add(sensorsGrid[k1][j1].get(i1));
											    find_mark = 1;
											 }
											
                                                          
                                        }
                                         
									}

								}
					}
				}

				
				factor++;
			}
			
		//calculate average speed
	    
			//System.out.println("find_mark = "+find_mark+" !!!!!!");	
				
			double speed = 0.;	
			if(find_mark==1)
			  for(int i = 0; i< available_sensors.size();i++){
				speed += links_speed.get(index).get(available_sensors.get(i))[k]; 
			  }
		  
		  if(find_mark==1)	
		   return  speed/(double)available_sensors.size();	
		  else
			return speed;  
		//calculate average speed ends		
    		
    	}catch(Exception e){
    		return  0.;	
    	}
    }

   
   private static int getIndex2(double longi) {

		//System.out.print(longi+" ");
		double l1 =-119.3;
		double l2=-117.6;
		if(longi<l1 || longi>l2)
			return 0;
		double step = (l2-l1)/500.0;
		int index=0;
		while(l1<longi)
		{
			index++;
			l1+=step;
		}
		//System.out.println(index);
		if(index>=500)
			return 499;
		return index;
	}

	private static int getIndex1(double lati) {
		//System.out.print(lati+" ");
		double l1 =34.2824;
		double l2=33.9072;
		if(lati>l1 || lati<l2)
			return 0;
		double step = (l1-l2)/500.0;
		int index=0;
		while(l1>lati)
		{
			index++;
			l1-=step;
		}
		//System.out.println(index);
		if(index>=500)
			return 499;
		return index;
	}
   
	
	 private static void getLinkDirections() throws SQLException {
	      try{
	    	FileInputStream fstream = new FileInputStream("H:\\Jiayunge\\highway_link_direction_G.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count=0;
			while ((strLine = br.readLine()) != null) {
			   String[] nodes = strLine.split(",");
			   String link_id = nodes[0];
			   int direction = Integer.parseInt(nodes[1]);
			   LinkDirection.put(link_id,direction);
			   count++;
			}
	    	
			in.close();
			System.out.println(count+" roads direction loaded!");
	      }catch(Exception e){
	    	  e.printStackTrace();
	      }
	    	
	        
	    }

   
	 private static void getGridDetailsFromFile() {
			try {
				FileInputStream fstream = new FileInputStream("H:\\Jiayunge\\grid_highway.txt");
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				int count=0;
				while ((strLine = br.readLine()) != null) {
					String[] nodes = strLine.split(",");
					int index1 = Integer.parseInt(nodes[0]);
					int index2 = Integer.parseInt(nodes[1]);
					sensorsGrid[index1][index2] = new ArrayList<Integer>();
					sensorsGridDir[index1][index2] = new ArrayList<Integer>();
					int i = 2;
					//System.out.println(nodes.length);
					boolean flag=false;
					while (i < nodes.length) {
						flag=true;
						//System.out.println(index1+" "+index2+" "+nodes[i]);
					sensorsGrid[index1][index2].add(Integer.parseInt(nodes[i]));
					sensorsGridDir[index1][index2].add(Integer.parseInt(nodes[i+1]));
					i=i+2;
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
	 
	 
}
