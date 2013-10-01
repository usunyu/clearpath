package tdsp.servlets;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
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
import Objects.PairInfo;

/**
 * This class is TDSP processing Java Servlet Code counterpart of Java Code
 * for network 123 using Fibonacci Heap Implementatation
 * @see FibonacciHeap
 * @see FibonacciHeapNode
 * @see TDSPQueryListJavaCodeFibo123
 */
public class TDSPQueryListNewFibo extends HttpServlet {

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
        link_count=0;
        int startNodeID;
        int endNodeID;
        day = request.getParameter("day");
        for(int i=0;i<days.length;i++)
        {
        	if(days[i].equals(day))
        		dayIndex = i;
        }

        if( request.getParameter("update").equals("False") ){
            String[] startOrds = request.getParameter("start").split(",");
            String[] endOrds = request.getParameter("end").split(",");
            startNodeID = findNN(Double.parseDouble(startOrds[0]), Double.parseDouble(startOrds[1]));
            endNodeID = findNN(Double.parseDouble(endOrds[0]), Double.parseDouble(endOrds[1]));
        }else{
            startNodeID = Integer.parseInt(request.getParameter("start"));
            endNodeID = Integer.parseInt(request.getParameter("end"));
        }

        int time = Integer.parseInt(request.getParameter("time"));

        tdsp(startNodeID, endNodeID, time);
        out.print("-"+startNodeID+"-"+endNodeID+"-"+time);
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
        	  InputStream is = getServletContext().getResourceAsStream("\\WEB-INF\\classes\\TDSP Files\\TDSPData.obj");
  			 //FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\TDSPData.obj");
		     ObjectInputStream ois = new ObjectInputStream(is);
            
            length1 = ois.readInt();
            length2 = ois.readInt();
            length3 = ois.readInt();
            graphTDSP = new NodeConnectionValues[length1][8];
            
            int nodeNum = ois.readInt();
            nodes = new double[nodeNum][2];
            for(int i=0; i<nodeNum; i++){
                nodes[i][0] = ois.readDouble();
                nodes[i][1] = ois.readDouble();
            }

            ois.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(-1);
        }
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
	
    
    
    private void tdsp( int start, int end, int time) {

    	 //System.out.println("TDSP CALLED WITH PARAMETERS:"+start+","+end+","+time+","+maxTime);

		FibonacciHeap<Node> priorityQ = new FibonacciHeap<Node>();
		HashMap<Integer,Node> priorityQMap1 = new HashMap<Integer,Node>();
		HashMap<Node,FibonacciHeapNode<Node>> priorityQMap2 = new HashMap<Node,FibonacciHeapNode<Node>>();


		int i, len = graphTDSP.length, j, id;
		// System.out.println("len="+len);
		int w, arrTime;
		int[] c = new int[len];
		int[] parent = new int[len];
		double shortestTime = 8000000;

		int[] nextNode = new int[len];
	
		for (i = 0; i < len; i++) {
			nextNode[i] = -1;
			parent[i] = -1;
			if (i == start)
				c[i] = 0; // starting node
			else
				c[i] = 8000000; // indicating infinity
		}

		Node n, s = new Node(start, 0, time); 			// creating the starting
														// node with nodeId =
														// start and cost = 0
														// and arrival time =
														// time
		  FibonacciHeapNode<Node> new_node = new FibonacciHeapNode<Node>(s);
		  priorityQ.insert(new_node, s.getNodeCost());  // inserting s into the priority queue
		  priorityQMap1.put(s.getNodeId(), s);
		  priorityQMap2.put(s, new_node);
		  
		  while (!(priorityQ.isEmpty())) { //while Q is not empty

	        	//System.out.println("Size of queue="+priorityQ.size());
	        	FibonacciHeapNode<Node> node = priorityQ.min();
	        	priorityQ.removeMin();
	        	n = node.getData();
	        	
	        	
			priorityQMap1.remove(n.getNodeId());	
			priorityQMap2.remove(n);
			
			int updTime = time;
			id = n.getNodeId();
			if (graphTDSP[id][dayIndex] == null)
				continue;
			

			if (id == end) {
				break;
			}

			if (id != start) {
				updTime = time + getTime(n.getArrTime());

				if (updTime > 59)
					updTime = 59;
			}

			if (graphTDSP[id][dayIndex].nodes != null /*&& n.getArrTime() < shortestTime*/) {
				HashMap<Integer, NodeValues> hmap = graphTDSP[id][dayIndex].nodes;

				Set<Integer> keys = hmap.keySet();
				Iterator<Integer> iter = keys.iterator();
				 //System.out.println("size->"+keys.size());
				int i2 = 0;
				while (i2 < keys.size()) {
					int key = iter.next();
					NodeValues val = hmap.get(key);
					arrTime = n.getArrTime();
					w = val.getValues()[updTime];
					
					//System.out.println("cost="+w);
					 if (arrTime + w < c[key]) {
                  	 c[key] = arrTime + w;
						parent[key] = id;
						//it = priorityQ.iterator();
						 						
						//System.out.println("Size of queue="+priorityQ.size());
						
						if(priorityQMap1.containsKey(key))
						{
						Node node_ = priorityQMap1.get(key);
						node_.setArrTime(c[key]);
						priorityQ.decreaseKey(priorityQMap2.get(node_),node_.getNodeCost());
						}
						else
						{
							Node node2 = new Node(key, c[key], c[key]);
							FibonacciHeapNode<Node> new_node2 = new FibonacciHeapNode<Node>(node2);
							priorityQ.insert(new_node2,c[key]);  //arrival time = c[i]
							priorityQMap1.put(key,node2);
							priorityQMap2.put(node2, new_node2);
						}
						
                   }
					 
					i2++;

				}
			}
		}


        int temp;
        temp = end;
        while(temp != -1) {
            if(parent[temp] != -1)
                nextNode[parent[temp]] = temp;
            temp = parent[temp];
        }


        if (start == end){
            //out.println("Your starting node is the same as your ending node.");
            out.println(""+nodes[start][0]+","+nodes[start][1]+";0");
            return;
        }
        else {
            i = start;
            j = 1;
            out.print(""+nodes[i][0]+","+nodes[i][1]+";");
            while (i != end && nextNode[i]!=-1) {
                 
            	out.print(""+nodes[ nextNode[i]][0]+","+nodes[ nextNode[i]][1] + ";");
            	i = nextNode[i];
            }
            out.print(""+(double)c[end]/60000.0);
            
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
		return("\\WEB-INF\\classes\\TDSP Files\\AdjList_"+day+".txt");
	}
    
}



