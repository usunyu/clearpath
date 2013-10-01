package tdsp.servlets;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import library.FibonacciHeap;
import library.FibonacciHeapNode;


import Objects.LinkInfo;
import Objects.Node;
import Objects.NodeConnectionValues;
import Objects.NodeValues;

import java.util.ArrayList;

/**
 * This class is TDSP processing Java Servlet Code counterpart of Java Code
 * for network 1234 using Fibonacci Heap Implementatation
 * @see FibonacciHeap
 * @see FibonacciHeapNode
 * @see TDSPQueryListJavaCodeFibo1234
 */
public class TDSPQueryCompare extends HttpServlet {
	
	public static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	public static String userName = "clearp";
	public static String password = "clearp";
	public static Connection connHome = null;
	
	public HashMap<String,ArrayList<Integer>> updatetable =new HashMap<String, ArrayList<Integer>>();
	
	//compare table
     public StringBuffer[] CompareTable = new StringBuffer[111532];
	
	//compare table ends
	
	private static final long serialVersionUID = 1L;
    private NodeConnectionValues[][] graphTDSP;

    double[][] nodes;
     Map<Integer, Integer> midPoints;
     double[][] midPointOrds;
	 LinkInfo links [] = new LinkInfo[100000];
	 int link_count = 0;
	 private int length1; 
	 private int length2; 
	 private String day;
	 private int dayIndex;
	 private double compareLat;
	 private double compareLon;
	 private int direction;
	 private int length3;
	 private static String [] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday","All"};
      PrintWriter out;
     
  
    @Override
    public void init(ServletConfig config){
    	try {
			super.init(config);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	readFile();
    	
    	for(int i=0;i<days.length;i++)
        	readListTDSP(i,days[i]);
    }
      
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        ResourceBundle rb =
            ResourceBundle.getBundle("LocalStrings",request.getLocale());
        response.setContentType("text/plain");
        out = response.getWriter();
        /*
        String[] tempNode = request.getParameter("location").split(",");
        compareLat = Double.parseDouble(tempNode[0]);
        compareLon= Double.parseDouble(tempNode[1]);
        direction = Integer.parseInt(request.getParameter("direction"));
        
        int stID = findNN(compareLat,compareLon);
        
        //comparecode begin
        int outendID=-1;
        int findmark  = 0;
      
			String temp = CompareTable[stID].toString();
			String[] tempnodes = temp.split(";");
			for(int k = 0; k<tempnodes.length; k++){
				String[] mynodes = tempnodes[k].split(",");
				outendID = Integer.parseInt(mynodes[0]);
				int dir = Integer.parseInt(mynodes[1]);
				if(dir == direction){
					findmark = 1;
					break;
				}
			}
		
        
        //comparecode ends
      if(findmark==1)
        out.print("found: startNodeID = "+stID+", endNodeID = "+outendID);
      else
    	out.print("no result!");  
      */
       
        out.print("Helo world!!!");
     // compare();
      
      
       
    }
    
    private void readListTDSP(int index, String day) {

    	try {
    			InputStream is = getServletContext().getResourceAsStream(getFileName(day));
  			//FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\AdjList.txt");
				 DataInputStream in = new DataInputStream(is);
	        	 BufferedReader file = new BufferedReader(new InputStreamReader(in));
	       	     String tmp , temp, temp2;
	            tmp = file.readLine();
	            
	              
	            int i = 0;
	            while (tmp != null) {
	            	
	            	if(!(tmp.equals("NA")))
	            		{
	            		graphTDSP[i][index] = new NodeConnectionValues();
	            		StringTokenizer sT = new StringTokenizer(tmp, ";");
	            		
		                int j = 0, k=0;
		                while (sT.hasMoreTokens()) {
		                	temp = sT.nextToken(); 
		                	
		                	j = Integer.parseInt(temp.substring(1, temp.indexOf("(")));
		                    String type = temp.substring(temp.indexOf("(")+1, temp.indexOf(")"));
		                    int values [] = new int[60];
		                    if(type.equals("V"))
		                    {
		                    	k = 0;
			                    
			                    StringTokenizer sT2 = new StringTokenizer(temp, ",");
			                    
			                    while(sT2.hasMoreTokens()) {
			                        temp2 = sT2.nextToken();
			                        if(temp2.indexOf(":")!= -1)
			                        {values[k++] = Integer.parseInt(temp2.substring(temp2.indexOf(":")+1));
			                        }
			                        else
			                        {values[k++] = Integer.parseInt(temp2);
			                        
			                        }
			                    }
		                    }
		                    else
		                    {
		                    	for(k=0;k<length3;k++)
		                    		{values[k] = Integer.parseInt(temp.substring(temp.indexOf(":")+1));
		                    		
		                    		}
		                    }
		                    graphTDSP[i][index].nodes.remove(j);
		                	graphTDSP[i][index].nodes.put(j, new NodeValues(j,values));
		                }		
	            	
	            }
	            	
	                i++;
	                tmp = file.readLine();
	            
	            }
	            file.close();
	            
	        }
	        catch (IOException io) {
	            io.printStackTrace();
	            System.exit(1);
	        } 
	        catch (RuntimeException re) {
	        	re.printStackTrace();
	            System.exit(1);
	        }
    } 
   
   public void compare(){
	   try{
		      Connection con = getConnection();
		      
		      String sql = "select link_id,speed,day,time from arterial_averages3 where link_id = 10019 and day = 'Monday' order by time";
		      
			  PreparedStatement f = con.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			  ResultSet rs = f.executeQuery();
             	
			  while(rs.next()){
			    day = rs.getString(3);
			    for(int i=0;i<days.length;i++)
		        {
		        	if(days[i].equals(day))
		        		dayIndex = i;
		        }
              
			    int hours = Integer.parseInt(rs.getString(4).split(":")[0]);
			    int minutes = Integer.parseInt(rs.getString(4).split(":")[1]);
			    int timeIndex = ((hours-6)*4)+(minutes/15);
			    
			    out.print(rs.getDouble(2)+","+dayIndex+","+timeIndex+"\n");
			  
			  
			  }  
		   
		      
	   }catch(Exception e){
		   e.printStackTrace();
	   }
   }



    private int findNN(double latitude, double longitude){
        int NNID = 1;
        double minDistance = (latitude-nodes[0][0])*(latitude-nodes[0][0]) + (longitude-nodes[0][1])*(longitude-nodes[0][1]);
        for(int i=1; i<nodes.length; i++){
            double dist = (latitude-nodes[i][0])*(latitude-nodes[i][0]) + (longitude-nodes[i][1])*(longitude-nodes[i][1]);
            if(dist < minDistance){
                NNID = i;
                minDistance = dist;
            }
        }
        return NNID;
    }

		
    private void readFile(){
        try{
        	  InputStream is = getServletContext().getResourceAsStream("\\WEB-INF\\classes\\TDSP Files1234\\TDSPData.obj");
  			 //FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\TDSPData.obj");
		     ObjectInputStream ois = new ObjectInputStream(is);
            
            length1 = ois.readInt();
            length2 = ois.readInt();
            length3 = ois.readInt();
            //graphTDSP = new NodeConnectionValues[length1][8];
            
            for(int i =0; i<length1; i++)
            	CompareTable[i] =new StringBuffer("");
            
          
            int nodeNum = ois.readInt();
            nodes = new double[nodeNum][2];
            for(int i=0; i<nodeNum; i++){
                nodes[i][0] = ois.readDouble();
                nodes[i][1] = ois.readDouble();
            }

            ois.close();
            
            InputStream input = getServletContext().getResourceAsStream("\\WEB-INF\\classes\\TDSP Files1234\\Edges_withDirction.csv");
  			DataInputStream in = new DataInputStream(input);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        String strLine;
	        
	        while((strLine = br.readLine())!=null){
	        	String[] mynodes = strLine.split(",");
	        	int stID = Integer.parseInt(mynodes[3].substring(1));
	        	int endID  = Integer.parseInt(mynodes[4].substring(1));
	        	int dir = Integer.parseInt(mynodes[11]);
	            CompareTable[stID].append(endID+","+dir+";");        	
	        }
            
           
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

   
	
    
    
    
    
    private int getTime(int arrTime) {
    	int minutesTime = (int)(arrTime/60000.0);
    	if(minutesTime>=0 && minutesTime<7)
    	return 0;
    	else if(minutesTime>=7 && minutesTime<22)
    		return 1;
    	else if(minutesTime>=22 && minutesTime<37)
    		return 2;
    	else if(minutesTime>=37 && minutesTime<52)
    		return 3;
    	else
    		return 4;
    	
    }
    
    private static String getFileName(String day){
		return("\\WEB-INF\\classes\\TDSP Files1234\\AdjList_"+day+".txt");
	}
    
    
   
   
    public static Connection getConnection() {
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

    
      
    
    
    
}



