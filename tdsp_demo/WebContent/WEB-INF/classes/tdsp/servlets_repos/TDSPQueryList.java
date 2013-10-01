package tdsp.servlets_repos;


import java.io.BufferedReader;
import java.io.DataInputStream;
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
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import Objects.Node;

public class TDSPQueryList extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private int Lsp = 0;
    private int[][] tdsp_new;
    private int[][][] graphTDSP;

    double[][] nodes;
    Map<Integer, Integer> midPoints;
    double[][] midPointOrds;

	private int length1;

	private int length2;

	private int length3;

  
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        ResourceBundle rb =
            ResourceBundle.getBundle("LocalStrings",request.getLocale());
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        int startNodeID;
        int endNodeID;
        readFile();
        readListTDSP();

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

        tdsp(startNodeID, endNodeID, time, out);
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
        	
            InputStream is = getServletContext().getResourceAsStream("\\WEB-INF\\classes\\TDSP Files\\TDSPData2.obj");
            ObjectInputStream ois = new ObjectInputStream(is);
            
            length1 = ois.readInt();
            length2 = ois.readInt();
            length3 = ois.readInt();
            graphTDSP = new int[length1][length2][length3];
            tdsp_new = new int[length1][length2];
            

            int nodeNum = ois.readInt();
            nodes = new double[nodeNum][2];
            for(int i=0; i<nodeNum; i++){
                nodes[i][0] = ois.readDouble();
                nodes[i][1] = ois.readDouble();
            }

            int midPointSize = ois.readInt();
            midPoints = new HashMap<Integer, Integer>();
            midPointOrds = new double[midPointSize][2];
            for(int i=0; i<midPointSize; i++){
                int key = ois.readInt();
                //double[] ords = new double[2];
                midPointOrds[i][0] = ois.readDouble();
                midPointOrds[i][1] = ois.readDouble();
                midPoints.put(key, i);
            }

            ois.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    private void readListTDSP() {

        InputStream is = getServletContext().getResourceAsStream("\\WEB-INF\\classes\\TDSP Files\\Adj_List_Adv.txt");
             try {
            	 DataInputStream in = new DataInputStream(is);
	        	 BufferedReader file = new BufferedReader(new InputStreamReader(in));
	       	     String tmp , temp, temp2;
	            tmp = file.readLine();
	             for(int i1=0;i1<length1;i1++)
	            	for(int j1=0;j1<length2;j1++)
	            		for(int k1=0;k1<length3;k1++)
	            			graphTDSP[i1][j1][k1] = -2;
	              
	            int i = 0;
	            while (tmp != null) {
	            	
	            	if(!(tmp.equals("NA")))
	            		{
	            	
	            		StringTokenizer sT = new StringTokenizer(tmp, ";");
	            		
		                int j = 0, k=0;
		                while (sT.hasMoreTokens()) {
		                	temp = sT.nextToken(); 
		                	j = Integer.parseInt(temp.substring(1, temp.indexOf("(")));
		                    String type = temp.substring(temp.indexOf("(")+1, temp.indexOf(")"));
		                    if(type.equals("V"))
		                    {
		                    	k = 0;
			                    
			                    StringTokenizer sT2 = new StringTokenizer(temp, ",");
			                    while(sT2.hasMoreTokens()) {
			                        temp2 = sT2.nextToken();
			                        if(temp2.indexOf(":")!= -1)
			                        graphTDSP[i][j][k++] = Integer.parseInt(temp2.substring(temp2.indexOf(":")+1));
			                        else
			                        graphTDSP[i][j][k++] = Integer.parseInt(temp2);
			                    }
		                    }
		                    else
		                    {
		                    	for(k=0;k<length3;k++)
		                    		graphTDSP[i][j][k] = Integer.parseInt(temp.substring(temp.indexOf(":")+1));
		                    }
		                	
		                }		
	            	
	            }  
	                i++;
	                tmp = file.readLine();
	            
	            }
	            file.close();
	            
	        }
	        catch (IOException io) {
	            System.err.println(io.toString());
	            System.exit(1);
	        } 
	        catch (RuntimeException re) {
	            System.err.println(re.toString());
	            System.exit(1);
	        }
	        
	  
            
	    }
	
    
    
    private void tdsp( int start, int end, int time, PrintWriter out) {

        PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
            new Comparator<Node>() {
                public int compare(Node n1, Node n2) {
                    return n1.getNodeCost() - n2.getNodeCost();
                }
            }
        );


        int i, len = graphTDSP.length, arrTime, w, j, id;
        int[] c = new int[len];
        int[] parent = new int[len];
        for(i=0; i<len; i++)
            parent[i] = -1;

        Iterator<Node> it;
        boolean qFlag = false;

        for(i=0; i<len; i++) {
            if(i == start)
                c[i] = 0;                       //starting node
            else
                c[i] = 100000;                      //indicating infinity
        }

        Node tempN, n, s = new Node(start, 0, time);       //creating the starting node with nodeId = start and cost = 0 and arrival time = time
        priorityQ.offer(s);                     //inserting s into the priority queue

        while ((n = priorityQ.poll()) != null) { //while Q is not empty

        	 int updTime = time;
        	id = n.getNodeId();
      //      System.out.println("\n\nPOPPED " + n.getNodeId() + " with arrival time = " + n.getArrTime());
            
            if(n.getNodeId()!=start)
            {
            updTime = time + getTime(n.getArrTime());
            if(updTime>56)
           	 updTime = 56;
            }
            
            
            for (i = 0; i < len; i++) {                  // traversing the graph to find out neighbours

                if (graphTDSP[id][i][updTime] != -2 && id != i) {        // non neighbours have a value of 1000

                   arrTime = n.getArrTime();
                     w = W(graphTDSP, id, i, updTime);
                     if (arrTime + w < c[i]) {
                        c[i] = arrTime + w;
                        parent[i] = id;
                        it = priorityQ.iterator();
                        while (it.hasNext() == true) {
                            if ( (tempN = it.next()).getNodeId() == i) {
                                if(priorityQ.remove(tempN) == true) {
                                    tempN.setArrTime(c[i]);
                                    priorityQ.offer(tempN);
                                    qFlag = true;
                                }
                                break;
                            }
                        }

                        if(qFlag == false) {
                            priorityQ.offer(new Node(i, 0, c[i])); //arrival time = c[i]
                       //     System.out.println("inserting " + i);
                        }
                        else
                            qFlag = false;
                    }
                }
            }
        }


        int temp;
        int[] nextNode = new int[len];
        for(i=0; i<len; i++)
            nextNode[i] = -1;

        temp = end;
        while(temp != -1) {
            if(parent[temp] != -1)
                nextNode[parent[temp]] = temp;
            temp = parent[temp];
        }


        if (start == end){
            //System.out.println("Your starting node is the same as your ending node.");
            out.print(""+nodes[start][0]+","+nodes[start][1]+";0");
            return;
        }
        else {
            i = start;
            j = 1;
            out.print(""+nodes[i][0]+","+nodes[i][1]+";");
            while (i != end) {
                 Integer midPointIndex = midPoints.get((i+1)*100 + nextNode[i]+1);
                if(midPointIndex != null){
                    double[] midOrds = midPointOrds[midPointIndex];
                    out.print("" + midOrds[0] + "," + midOrds[1] +";");
                }
                out.print("" + nodes[ nextNode[i]][0]+","+nodes[ nextNode[i]][1] + ";");

                // the following line is the change that was made for the new method
//no formula
//                tdsp_new[i][nextNode[i]] += 1;//graphTDSP[i][nextNode[i]][time];
 //the following is with the formula
//                tdsp_new[i][nextNode[i]] += c[end];//graphTDSP[i][nextNode[i]][time];
 //the following is with the formula + edge lengths
                tdsp_new[i][nextNode[i]] += c[end] + graphTDSP[i][nextNode[i]][time];
                i = nextNode[i];
            }
            out.print(""+c[end]/60);
            if(c[end] > Lsp) {
                Lsp = c[end];
            }
        }

    }
    
    private static int getTime(int arrTime) {
    	int minutesTime = arrTime/60;
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


    private int W(int[][][] graphTDSP, int n1, int n2, int time) {

        int cost = graphTDSP[n1][n2][time];
        if(cost == 1000)
            cost = -1;

        return cost;
    }
}



