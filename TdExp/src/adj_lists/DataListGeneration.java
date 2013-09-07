package adj_lists;

import java.io.*;
import java.util.*;

import adj_lists.Pair;

//import Fibonacci library
import library.FibonacciHeap;  
import library.FibonacciHeapNode;


	/**
	 * @author  Ugur
	 *
	 *
	 *
	 * It randomly picks {@link #totalTrials} of pairs and calls tdsp for each time frame. it basically produces
	 * 3 types of files:
	 * 		-paths. is commented in main function, it was mostly useful for the small dataset. It contains .txt files for
	 * 				each source point. Exaustively, computes the paths to **ALL** (or some randomly selected) target nodes
	 * 				and does that for each and every time stamp.
	 * 		-pairs.	Created by the function printPaths (I know I could have chosen better names..). For the randomly sele-
	 * 				cted pair of nodes, it prints:
	 * 					- First, a list of all the different paths used throughout the day
	 * 					- Second, for each path, of the used ones it prints tuples of the form:
	 * 							[1,1412431]
	 * 							[42,14324]
	 * 					  The first number is the time stamp for which the path was used. The second number is the actual
	 * 					  travel time at that time frame using that path.
	 * 		-stats.	For each source node, it creates one file. the file contains for each trial target node the tottal number of
	 * 				different paths, along with the number of critical points. then there are some numbers following, representing
	 * 				the **length** (in nodes) of each of the distinct paths. This last feature, has not been used in the analysis yet
	 *
	 * IMPORTANT. 	In line 271 the if statement introduces the smoothing factor (is the second clause in miliseconds ) appropriate values
	 * 				are 60000=1min and 180000=3mins
	 *
	 * the coresponding directories: pairs2smoothed1 and pairs2smoothed3
	 *
	 * void tdsp() computes time-dependent shortest path
	 *
	 */


public class DataListGeneration {

	static int numNodes, Lsp = 0;
    static int start, end, time;
    static Queue<Integer> path;
    static Map<Pair<String,Integer>, List<Pair<Integer, Integer>>> allPaths = new HashMap<Pair<String,Integer>, List<Pair<Integer, Integer>>>();
    static int timeInterval=15;		// corresponds to time granularity
    static int times;				// here the third dimension of the graph will be stored
    static int totalTrials=3000;
   
    static int NumOfNodes = 1734530;
    static double nodes[][] = new double[NumOfNodes][2];

	public static class Node {					// Class Node. Cost and ArrTime are used interchangably, I kept them for compitability with Ugur's code
												// dist is of no use.
	    public Node(int id, int c, int t) {
	        nodeId = id;
	        cost = c;
	        arrTime = t;
	        dist = new Vector(10, 5);
	    }
	    public int getNodeId() {
	        return nodeId;
	    }
	    public int getNodeCost() {
	        return cost;
	    }
	    public int getArrTime() {
	        return arrTime;
	    }
	    public void setArrTime(int t) {
	        arrTime = t;
	    }
	    public void setCost(int t) {
	        cost = t;
	    }

	    private int nodeId, cost, arrTime;
	    private Vector dist; // denoted by 'c' in the algorithm

	}

	public static BuildList graph = new BuildList();			// Loads the graph into the memory
	//public static BuildList staticGraph = new BuildList("StaticData_Sunday.txt");

	private static void addToHash(String path, int size, int timeOfDay, int travelTime){		// every time a new path is discovered, we add it to the hash
																								// along with the timeframe that is being used and the traveltime

		List<Pair<Integer, Integer>> list;
		Pair<String,Integer> path_size= new Pair<String,Integer>(path,size);
		Pair<Integer, Integer> pair = new Pair<Integer,Integer>(timeOfDay,travelTime);

		if (!allPaths.containsKey(path_size)){
			list=new ArrayList<Pair<Integer, Integer>>();
			allPaths.put(path_size, list);
		}
		else {
			list=allPaths.get(path_size);
		}
		list.add(pair);

	}

	private static void printPaths (int source, int target) {		//the method prints the discrete paths along with the time instant
																	//they were used and what was the travel time then.
		try {
			PrintWriter out = new PrintWriter(new FileWriter("pairs2\\"+source+"-"+target+".txt"));

			for(Pair<String,Integer> path: allPaths.keySet()){
				   out.write(path.getLeft()  + " \n");
			}
			out.write("\n");
			for(Pair<String,Integer> path: allPaths.keySet()){
				   out.write( path.getLeft()  + " \n");

				   for (Pair<Integer,Integer> pair: allPaths.get(path)){
					   out.write("["+pair.getLeft() +","+pair.getRight()+"]\n");
				   }
				   out.write("\n");
			}
			out.close();


		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void printPaths (int source, int target, int queryGroup) {		//the method prints the discrete paths along with the time instant
		//they were used and what was the travel time then.
		try {
			PrintWriter out = new PrintWriter(new FileWriter("queryresult\\sunday\\onepercentsmoothed\\smoothed_q_" + queryGroup + "\\"+source+"-"+target+".txt"));

			for(Pair<String,Integer> path: allPaths.keySet()){
				out.write(path.getLeft()  + " \n");
			}
			out.write("\n");

			for(Pair<String,Integer> path: allPaths.keySet()){
				out.write( path.getLeft()  + " \n");

				for (Pair<Integer,Integer> pair: allPaths.get(path)){
					out.write("["+pair.getLeft() +","+pair.getRight()+"]\n");
				}
				out.write("\n");
			}
			out.close();


		} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
		}
}

	private static void clearHash() {
		allPaths.clear();

	}

	private static void printStats (int source, int target, int cp){

		String path="stats2smoothed3\\stats."+source+".txt";
		File f = new File(path);
		FileOutputStream out=null;

		if (!f.exists()) {
			try {
				out = new FileOutputStream(path, false);
//				System.out.println("---------------------"+source+"-------------------");

			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			try {
				out = new FileOutputStream(path, true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			String update = target+":"+allPaths.keySet().size()+","+cp;


			 for(Pair<String,Integer> path_size: allPaths.keySet()){
				   update=update+" "+path_size.getRight();
			 }

			update=update+"\n";
			out.write(update.getBytes());
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(target+":"+allPaths.keySet().size()+","+cp);
	}

	private static void printStats (int source, int target, int cp, int queryGroup){

		String path="stats2smoothedOne\\sunday\\stats_result\\stats_q_"+queryGroup+".txt";
		File f = new File(path);
		FileOutputStream out=null;

		if (!f.exists()) {
			try {
				out = new FileOutputStream(path, false);
//				System.out.println("---------------------"+source+"-------------------");

			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			try {
				out = new FileOutputStream(path, true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			String update = source + "\t" + target+":"+allPaths.keySet().size()+","+cp;


			 for(Pair<String,Integer> path_size: allPaths.keySet()){
				   update=update+" "+path_size.getRight();
			 }

			update=update+"\n";
			out.write(update.getBytes());
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(target+":"+allPaths.keySet().size()+","+cp);
	}


	public static LinkedList<Integer> stringToList (String s){		// Converts a string into a LinkedList of integers (aka path) for node manipulation

		LinkedList<Integer> pathList = new LinkedList<Integer>();
		StringTokenizer sT= new StringTokenizer(s," ");

		while (sT.hasMoreTokens()) {
			pathList.offer(Integer.parseInt(sT.nextToken()));
		}

		return pathList;

	}

	public static int compute (LinkedList<Integer> list, int time) {		//Given a path (as a list of nodes) and a departure time,
																			//compute the travel time.
		int path_time=0;
		int when=time;
		int i,j;
		int times=graph.getTimes();
		LinkedList<Integer> path= new LinkedList<Integer> ();
		path=(LinkedList<Integer>) list.clone();

		while (!path.isEmpty()){
			List<adj_lists.Pair<Integer, int[]>> neighbors;
			when=time+path_time/60/1000/timeInterval;

			if (when>times-1){
				when=times-1;
			}
			i=path.poll();
		    if ((path.peek()) !=null) {
		    	j=path.peek();
		    	neighbors=graph.getList().get(i);
		    	if (neighbors==null)
	        		continue;
	            for(Pair<Integer, int[]> pair: neighbors) {
	            	if (pair.getLeft()==j){
	            		path_time+=pair.getRight()[when];
	            		break;
	            	}
	            }
		    }

		}
		return path_time;
	}

	public static int computePath(String path, int startTime){
		LinkedList<Integer> Path=stringToList(path);
		return compute(Path, startTime);
	}

	
/*
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		path= new LinkedList<Integer>();


		int n=graph.getSize();
		times=graph.getTimes();

		System.out.println("graph size is " + n);

		String prevPath="";
		String currPath="";
		int criticalPoints=-1;
		int path_length;
		Random generator = new Random();


		String dir_path = "querypair\\monday\\monday_q_";
		int totalQueryPair = 3000;
		int totalQueryGroup = 9;
		for (int queryGroup = 0; queryGroup < totalQueryGroup; queryGroup++){
			try{
				String queryFile = dir_path + queryGroup + ".txt";
				System.out.println(queryFile);
				RandomAccessFile pairFile = new RandomAccessFile(queryFile, "rw");
				int queryNum =  Integer.parseInt(pairFile.readLine());
				System.out.println("Total " + queryNum + " pair");

				for (int pairNo = 0; pairNo < totalQueryPair; pairNo++){
					String temp = pairFile.readLine();
					int sou = Integer.parseInt(temp.substring(0, temp.indexOf("\t")));
					int tar = Integer.parseInt(temp.substring(temp.indexOf("\t") + 1));
					Pair<String,Integer> pair=computeStaticPath(sou, tar);
					int staticTime=pair.getRight();
					//System.out.println("Query Pair: " + sou + " " + tar);
					int currNode = -1;
					//int prevNode = 0;
					for (int k = 0; k < times; k++){
						tdsp_2(sou,tar,k);
						path_length=path.size()-2;	// from the size of the queue we remove the last element (time) and we count edges (nodes-1)
						while (!path.isEmpty()) {
							currNode = path.poll();
							if (path.peek()!=null) {
							    currPath=currPath.concat(currNode+" ");
							}
						}
						if (((!prevPath.equals(currPath)) && (computePath(prevPath,k)-currNode> 0.01 * staticTime))||(prevPath=="")) { //60000, 180000
							addToHash(currPath,path_length,k,currNode); 	// in the last iteration of while loop currNode will contain the travel time
							prevPath=currPath;
							criticalPoints++;

						}
						else
							addToHash(prevPath,stringToList(prevPath).size()-1,k,computePath(prevPath,k)); 	// in the last iteration of while loop currNode will contain the travel time

						currPath="";						// clear path
						//prevNode = currNode;
					}
					printPaths(sou,tar, queryGroup);
					printStats(sou,tar, criticalPoints, queryGroup);
					criticalPoints=-1;
					prevPath="";
					currPath="";
					clearHash();
					if (pairNo % 500 == 0){
						System.out.println("Calcualted: " + pairNo + " pairs");
					}
				}
				pairFile.close();
			}
			catch (IOException io){
				System.err.println(io.toString());
		        System.err.println(io.getLocalizedMessage());
		        System.exit(1);
			}
		}
	}*/
	
	
	public static void showpath(Queue<Integer> path){
		try{
			double distance = 0.;
			int count = 0;
			int[] pair = new int[2];
		
			while(!path.isEmpty()){
			   pair[count++] = path.poll();
			   System.out.print("n"+pair[count-1]+";");
		       if(count==2){
		    	   distance += distance(nodes[pair[0]][0], nodes[pair[0]][1],nodes[pair[1]][0], nodes[pair[1]][1]);
		         count = 0;
		       }
			}
			
			System.out.print("distance is "+distance+";");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//read in the nodes coordinates information 
	public static void readInNodesInfo(){
		try{
			FileInputStream is = new FileInputStream("CA_Node.txt");
    		DataInputStream in = new DataInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
		    String strLine = "";
		    int i = 0;
		    while((strLine = br.readLine())!=null){
		    	String[] column = strLine.split("\\|\\|");
		    	String[] info = column[2].split(",");
		    	nodes[i][0] = Double.parseDouble(info[0]);
		    	nodes[i][1] = Double.parseDouble(info[1]);
		        i++;
		    }
		    
		    System.out.println("nodes coordinats loaded!");
		    
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		try{
			//get the input parameters
			//1). departure node id
			//2). destination node id
			//3). departure time index
			/*int start = Integer.parseInt(args[0]);
			int end = Integer.parseInt(args[1]);
			int time = Integer.parseInt(args[2]);
			*/
			int start = 30;
			int end = 4248;
			int time = 10;
			//load in the nodes info---nodes coordinates 
			readInNodesInfo();
			
			path= new LinkedList<Integer>(); 
            int n=graph.getSize();
			times=graph.getTimes();

			System.out.println("graph size is " + n);
			
			//System.out.println("startid = "+start+", endid = "+end+", timeindex = "+time);
			//get the path
			//System.out.println("the nodes in the path before the query: "+path.size());
			long startTime = System.nanoTime();
			tdsp_2(start ,end ,time);
			long endTime = System.nanoTime();
			System.out.println("running time of Original tdsp_2: "+(endTime-startTime)+"ns");
			//System.out.println("how many nodes in the path? : "+path.size());
			
						
			//output the information
			showpath(path);
			
			//System.out.println(compute ((LinkedList<Integer>)path, time));
            System.out.println("travel time = "+ Lsp/1000./60.);  
            
            path.clear();            
            startTime = System.nanoTime();
			tdsp_3(start, end, time);
			endTime = System.nanoTime();
			System.out.println("running time of the one with FibonaaciHeap: "+(endTime-startTime)+"ns");
			
			//output the information
			showpath(path);
			
			//System.out.println(compute ((LinkedList<Integer>)path, time));
            System.out.println("travel time = "+ Lsp/1000./60.);  
            
            path.clear();            
            startTime = System.nanoTime();
			tdsp_4(start, end, time);
			endTime = System.nanoTime();
			System.out.println("running time of the one with FibonaaciHeap: "+(endTime-startTime)+"ns");
			
			//output the information
			showpath(path);
			
			//System.out.println(compute ((LinkedList<Integer>)path, time));
            System.out.println("travel time = "+ Lsp/1000./60.);  
            
            path.clear();            
            startTime = System.nanoTime();
			tdsp_5(start, end, time);
			endTime = System.nanoTime();
			System.out.println("running time of the one with FibonaaciHeap: "+(endTime-startTime)+"ns");
			
			//output the information
			showpath(path);
			
			//System.out.println(compute ((LinkedList<Integer>)path, time));
            System.out.println("travel time = "+ Lsp/1000./60.);  
            
            
            
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/*
	 * The difference between my function with tdsp is that I used an array visited[] to
	 * indicate whether a node has been visited before so that we do not need to scan the
	 * priority queue before updating it
	 * commented by Dingxiong
	 */
	private static void tdsp_2(int start, int end, int time) {
	       	   PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
	               new Comparator<Node>() {
	                   public int compare(Node n1, Node n2) {
	                       return n1.cost - n2.cost;
	                   }
	               }
	           );
		
		//System.out.println("M1");
		int startTime=time;								// stores the trip initialization time

		int nn= graph.getSize();
		int i, len = nn, arrTime, w, j, id;
		int[] c = new int[len];
		int[] parent = new int[len];
		boolean unreachable=false;
		/*
		 * add visited vector
		 * commented by Dingxiong
		 */
		boolean[] visited = new boolean[len];
		for(i=0; i<len; i++) {
			visited[i] = false;                      		//indicating infinity
		}

		for(i=0; i<len; i++)
			parent[i] = -1;

		Iterator<Node> it;
		boolean qFlag = false;

		for(i=0; i<len; i++) {
			if(i == start)
				c[i] = 0;                       					//starting node
			else
				c[i] = Integer.MAX_VALUE;                      		//indicating infinity
		}

		// try{
		//    FileWriter fstream = new FileWriter("pairs2\\qu2.txt");
		//    BufferedWriter out = new BufferedWriter(fstream);


		Node tempN, n, s = new Node(start, 0, startTime);       	//creating the starting node with nodeId = start and cost = 0 and arrival time = time
		priorityQ.offer(s);                     					//inserting s into the priority queue
		int count=0;
		// problem: algorithm did not end even find end node here!!!???
		while ((n = priorityQ.poll()) != null) { 					//while Q is not empty
			List<adj_lists.Pair<Integer, int[]>> neighbors;

			time= startTime+n.getArrTime()/60/1000/timeInterval; 			//define time interval at the beginning (granularity)
			// time=startTime;
			// count++;
			if (time>times-1){												// time [6am - 9 pm], we regard times after 8PM as constant edge weights
				time=times-1;
			}

			id = n.getNodeId();
			/*
			 * using visited vector
			 */
			if (visited[id] == true)
				continue;
			visited[id] = true;

			// System.out.println(count+"");
			neighbors=(List<Pair<Integer, int[]>>) graph.getList().get(id);
			arrTime = n.getArrTime();

			if (neighbors==null)
				continue;

			for(Pair<Integer, int[]> pair: neighbors) {

				int node=pair.getLeft();
				int travelTime=pair.getRight()[time];
				/*
				 * if the node is visited, we bypass it
				 * commented by Dingxiong
				 */
				if (visited[node] == true) continue;
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				//commented by Dingxiong
				if (arrTime + travelTime < c[node]) {
					c[node] = arrTime + travelTime;
					parent[node] = id;
					priorityQ.offer(new Node(node, c[node], c[node]));
				}
			}
		}
		//out.write("\n");
		//out.close();
		// }
		// catch (IOException io) {
		//      System.err.println(io.toString());
		//      System.exit(1);
		//  }

		//find the path
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

		if (start == end)
			System.out.println("Your starting node is the same as your ending node.");
		else {
			i = start;
			j = 1;
			// System.out.print(i);
			path.offer(i);
			while (nextNode[i] != end) {
				if (nextNode[i]==-1){
					unreachable=true;
					break;
				}
				path.offer(nextNode[i]);
				// System.out.print(" " + nextNode[i]);
				i = nextNode[i];
			}

			if (!unreachable){
				path.offer(end);
				//path.offer((c[end]-startTime));

				if(c[end] > Lsp) {
					Lsp = c[end];
				}
			}
			else {
				System.out.println("unreachable");
			}
		}
	}

	
	//tdsp_2 using FibonacciHeap as the PriorityQ
	private static void tdsp_3(int start, int end, int time) {
    	 /*  PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
            new Comparator<Node>() {
                public int compare(Node n1, Node n2) {
                    return n1.cost - n2.cost;
                }
            }
        );*/
    	   
        FibonacciHeap<Node> priorityQ = new FibonacciHeap<Node>();
   		//HashMap<Integer,Node> priorityQMap1 = new HashMap<Integer,Node>();
   		//HashMap<Node,FibonacciHeapNode<Node>> priorityQMap2 = new HashMap<Node,FibonacciHeapNode<Node>>();

        int startTime=time;								// stores the trip initialization time

        int nn= graph.getSize();
        int i, len = nn, arrTime, w, j, id;
        int[] c = new int[len];
        int[] parent = new int[len];
        boolean unreachable=false;
        /*
         * add visited vector
         * commented by Dingxiong
         */
        boolean[] visited = new boolean[len];
        for(i=0; i<len; i++) {
                visited[i] = false;                      		//indicating infinity
        }


        for(i=0; i<len; i++)
            parent[i] = -1;

        Iterator<Node> it;
        boolean qFlag = false;

        for(i=0; i<len; i++) {
            if(i == start)
                c[i] = 0;                       					//starting node
            else
                c[i] = Integer.MAX_VALUE;                      		//indicating infinity
        }

       // try{
	      //    FileWriter fstream = new FileWriter("pairs2\\qu2.txt");
	      //    BufferedWriter out = new BufferedWriter(fstream);


        Node tempN, n, s = new Node(start, 0, startTime);       	//creating the starting node with nodeId = start and cost = 0 and arrival time = time
        //priorityQ.offer(s);                     					//inserting s into the priority queue
        FibonacciHeapNode<Node> new_node = new FibonacciHeapNode<Node>(s);
		priorityQ.insert(new_node, s.getNodeCost());  // inserting s into the priority queue
		//priorityQMap1.put(s.getNodeId(), s);
		//priorityQMap2.put(s, new_node);
		  
        
        int count=0;
        while (!(priorityQ.isEmpty())) { 					//while Q is not empty
        	FibonacciHeapNode<Node> Fnode = priorityQ.min();
        	priorityQ.removeMin();
        	n = Fnode.getData();
        	
        	List<adj_lists.Pair<Integer, int[]>> neighbors;

        	time= startTime+n.getArrTime()/60/1000/timeInterval; 			//define time interval at the beginning (granularity)
//        	time=startTime;
//        	count++;
        	if (time>times-1){												// time [6am - 9 pm], we regard times after 8PM as constant edge weights
        		time=times-1;
        	}
            id = n.getNodeId();
            /*
             * using visited vector
             */
            if (visited[id] == true)
         	   continue;
            visited[id] = true;

//            System.out.println(count+"");
            neighbors=(List<Pair<Integer, int[]>>) graph.getList().get(id);
            arrTime = n.getArrTime();

            if (neighbors==null)
        		continue;
            for(Pair<Integer, int[]> pair: neighbors) {

            	int node=pair.getLeft();
            	int travelTime=pair.getRight()[time];
            	/*
            	 * if the node is visited, we bypass it
            	 * commented by Dingxiong
            	 */
            	if (visited[node] == true) continue;
            	// if we find a node with updated distance, just insert it to the priority queue
            	// even we pop out another node with same id later, we know that it was visited and will ignore it
            	//commented by Dingxiong
            	if (arrTime + travelTime < c[node]) {
            		c[node] = arrTime + travelTime;
            		parent[node] = id;
            		//priorityQ.offer(new Node(node, c[node], c[node]));
            		Node node2 = new Node(node, c[node], c[node]);
					FibonacciHeapNode<Node> new_node2 = new FibonacciHeapNode<Node>(node2);
					priorityQ.insert(new_node2,c[node]);  //arrival time = c[i]
					//priorityQMap1.put(node,node2);
					//priorityQMap2.put(node2, new_node2);
            	  }
             }

        }
        	//out.write("\n");
        //out.close();
       // }
       // catch (IOException io) {
	      //      System.err.println(io.toString());
	      //      System.exit(1);
	      //  }


       //find the path
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


        if (start == end)
            System.out.println("Your starting node is the same as your ending node.");
        else {
            i = start;
            j = 1;
//            System.out.print(i);
            path.offer(i);
            while (nextNode[i] != end) {
            	if (nextNode[i]==-1){
            		unreachable=true;
            		break;
            	}
            	path.offer(nextNode[i]);
//                System.out.print(" " + nextNode[i]);
                i = nextNode[i];
            }
            if (!unreachable){

	            //path.offer((c[end]-startTime));
            	path.offer(end);
//	            System.out.println(" "+c[end]);
	            if(c[end] > Lsp) {
	                Lsp = c[end];
	            }
            }
            else {
            	//path.offer(end);
            	//path.offer(-1);
            	System.out.println("Path unavailable");

            }
        }
}

	


	//tdsp_2 with A*
	private static void tdsp_4(int start, int end, int time) {
	       	   PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
	               new Comparator<Node>() {
	                   public int compare(Node n1, Node n2) {
	                       return n1.cost - n2.cost;
	                   }
	               }
	           );
	       	   
	       	double hdistance = distance(nodes[start][0], nodes[start][1],nodes[end][0], nodes[end][1]);
	 		int htime = (int)((hdistance*60.0*60.0*1000.0)/60);

	       	   //System.out.println("M1");
	       	   
	           int startTime=time;								// stores the trip initialization time

	           int nn= graph.getSize();
	           int i, len = nn, arrTime, w, j, id;
	           int[] c = new int[len];
	           int[] parent = new int[len];
	           boolean unreachable=false;
	           /*
	            * add visited vector
	            * commented by Dingxiong
	            */
	           boolean[] visited = new boolean[len];
	           for(i=0; i<len; i++) {
	                   visited[i] = false;                      		//indicating infinity
	           }


	           for(i=0; i<len; i++)
	               parent[i] = -1;

	           Iterator<Node> it;
	           boolean qFlag = false;

	           for(i=0; i<len; i++) {
	               if(i == start)
	                   c[i] = 0+htime;                       					//starting node
	               else
	                   c[i] = Integer.MAX_VALUE;                      		//indicating infinity
	           }

	          // try{
		      //    FileWriter fstream = new FileWriter("pairs2\\qu2.txt");
		      //    BufferedWriter out = new BufferedWriter(fstream);


	           Node tempN, n, s = new Node(start, c[start], startTime);       	//creating the starting node with nodeId = start and cost = 0 and arrival time = time
	           priorityQ.offer(s);                     					//inserting s into the priority queue
	           int count=0;
	           while ((n = priorityQ.poll()) != null) { 					//while Q is not empty
	           	List<adj_lists.Pair<Integer, int[]>> neighbors;

	           	time= startTime+n.getArrTime()/60/1000/timeInterval; 			//define time interval at the beginning (granularity)
//	           	time=startTime;
//	           	count++;
	           	if (time>times-1){												// time [6am - 9 pm], we regard times after 8PM as constant edge weights
	           		time=times-1;
	           	}
	               id = n.getNodeId();
	               /*
	                * using visited vector
	                */
	               if (visited[id] == true)
	            	   continue;
	               visited[id] = true;

//	               System.out.println(count+"");
	               neighbors=(List<Pair<Integer, int[]>>) graph.getList().get(id);
	               arrTime = n.getArrTime();

	               if (neighbors==null)
	           		continue;
	               for(Pair<Integer, int[]> pair: neighbors) {

	               	int node=pair.getLeft();
	               	int travelTime=pair.getRight()[time];
	             
	               	hdistance = distance(nodes[node][0],nodes[node][1],nodes[end][0],nodes[end][1]);
 				    htime = (int)((hdistance*60.0*60.0*1000.0)/60);
	               	
	               	/*
	               	 * if the node is visited, we bypass it
	               	 * commented by Dingxiong
	               	 */
	               	if (visited[node] == true) continue;
	               	// if we find a node with updated distance, just insert it to the priority queue
	               	// even we pop out another node with same id later, we know that it was visited and will ignore it
	               	//commented by Dingxiong
	               	if (arrTime + travelTime + htime < c[node]) {
	               		c[node] = arrTime + travelTime + htime;
	               		parent[node] = id;
	               		priorityQ.offer(new Node(node, c[node], c[node]-htime));
	               	  }
	                }

	           }
	           	//out.write("\n");
	           //out.close();
	          // }
	          // catch (IOException io) {
		      //      System.err.println(io.toString());
		      //      System.exit(1);
		      //  }


	          //find the path
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


	           if (start == end)
	               System.out.println("Your starting node is the same as your ending node.");
	           else {
	               i = start;
	               j = 1;
//	               System.out.print(i);
	               path.offer(i);
	               while (nextNode[i] != end) {
	               	if (nextNode[i]==-1){
	               		unreachable=true;
	               		break;
	               	}
	               	path.offer(nextNode[i]);
//	                   System.out.print(" " + nextNode[i]);
	                   i = nextNode[i];
	               }
	               if (!unreachable){
                    path.offer(end);
	   	            //path.offer((c[end]-startTime));

	   	            if(c[end] > Lsp) {
	   	                Lsp = c[end];
	   	            }
	               }
	               else {
	            	System.out.println("unreachable");

	               }
	           }
	}
	
	
	//tdsp_2 using FibonacciHeap as the PriorityQ with A*
		private static void tdsp_5(int start, int end, int time) {
	    	 /*  PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
	            new Comparator<Node>() {
	                public int compare(Node n1, Node n2) {
	                    return n1.cost - n2.cost;
	                }
	            }
	        );*/
	    	   
	        FibonacciHeap<Node> priorityQ = new FibonacciHeap<Node>();
	   		//HashMap<Integer,Node> priorityQMap1 = new HashMap<Integer,Node>();
	   		//HashMap<Node,FibonacciHeapNode<Node>> priorityQMap2 = new HashMap<Node,FibonacciHeapNode<Node>>();

	        int startTime=time;								// stores the trip initialization time

	        int nn= graph.getSize();
	        int i, len = nn, arrTime, w, j, id;
	        int[] c = new int[len];
	        int[] parent = new int[len];
	        boolean unreachable=false;
	        
	    	double hdistance = distance(nodes[start][0], nodes[start][1],nodes[end][0], nodes[end][1]);
	 		int htime = (int)((hdistance*60.0*60.0*1000.0)/60);
	        
	        /*
	         * add visited vector
	         * commented by Dingxiong
	         */
	        boolean[] visited = new boolean[len];
	        for(i=0; i<len; i++) {
	                visited[i] = false;                      		//indicating infinity
	        }


	        for(i=0; i<len; i++)
	            parent[i] = -1;

	        Iterator<Node> it;
	        boolean qFlag = false;

	        for(i=0; i<len; i++) {
	            if(i == start)
	                c[i] = 0+htime;                       					//starting node
	            else
	                c[i] = Integer.MAX_VALUE;                      		//indicating infinity
	        }

	       // try{
		      //    FileWriter fstream = new FileWriter("pairs2\\qu2.txt");
		      //    BufferedWriter out = new BufferedWriter(fstream);


	        Node tempN, n, s = new Node(start, start, startTime);       	//creating the starting node with nodeId = start and cost = 0 and arrival time = time
	        //priorityQ.offer(s);                     					//inserting s into the priority queue
	        FibonacciHeapNode<Node> new_node = new FibonacciHeapNode<Node>(s);
			priorityQ.insert(new_node, s.getNodeCost());  // inserting s into the priority queue
			//priorityQMap1.put(s.getNodeId(), s);
			//priorityQMap2.put(s, new_node);
			  
	        
	        int count=0;
	        while (!(priorityQ.isEmpty())) { 					//while Q is not empty
	        	FibonacciHeapNode<Node> Fnode = priorityQ.min();
	        	priorityQ.removeMin();
	        	n = Fnode.getData();
	        	
	        	List<adj_lists.Pair<Integer, int[]>> neighbors;

	        	time= startTime+n.getArrTime()/60/1000/timeInterval; 			//define time interval at the beginning (granularity)
//	        	time=startTime;
//	        	count++;
	        	if (time>times-1){												// time [6am - 9 pm], we regard times after 8PM as constant edge weights
	        		time=times-1;
	        	}
	            id = n.getNodeId();
	            /*
	             * using visited vector
	             */
	            if (visited[id] == true)
	         	   continue;
	            visited[id] = true;

//	            System.out.println(count+"");
	            neighbors=(List<Pair<Integer, int[]>>) graph.getList().get(id);
	            arrTime = n.getArrTime();

	            if (neighbors==null)
	        		continue;
	            for(Pair<Integer, int[]> pair: neighbors) {

	            	int node=pair.getLeft();
	            	int travelTime=pair.getRight()[time];
	            	
	            	hdistance = distance(nodes[node][0],nodes[node][1],nodes[end][0],nodes[end][1]);
 				    htime = (int)((hdistance*60.0*60.0*1000.0)/60);
 				    
	            	/*
	            	 * if the node is visited, we bypass it
	            	 * commented by Dingxiong
	            	 */
	            	if (visited[node] == true) continue;
	            	// if we find a node with updated distance, just insert it to the priority queue
	            	// even we pop out another node with same id later, we know that it was visited and will ignore it
	            	//commented by Dingxiong
	            	if (arrTime + travelTime + htime< c[node]) {
	            		c[node] = arrTime + travelTime + htime;
	            		parent[node] = id;
	            		//priorityQ.offer(new Node(node, c[node], c[node]));
	            		Node node2 = new Node(node, c[node], c[node]-htime);
						FibonacciHeapNode<Node> new_node2 = new FibonacciHeapNode<Node>(node2);
						priorityQ.insert(new_node2,c[node]);  //arrival time = c[i]
						//priorityQMap1.put(node,node2);
						//priorityQMap2.put(node2, new_node2);
	            	  }
	             }

	        }
	        	//out.write("\n");
	        //out.close();
	       // }
	       // catch (IOException io) {
		      //      System.err.println(io.toString());
		      //      System.exit(1);
		      //  }


	       //find the path
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


	        if (start == end)
	            System.out.println("Your starting node is the same as your ending node.");
	        else {
	            i = start;
	            j = 1;
//	            System.out.print(i);
	            path.offer(i);
	            while (nextNode[i] != end) {
	            	if (nextNode[i]==-1){
	            		unreachable=true;
	            		break;
	            	}
	            	path.offer(nextNode[i]);
//	                System.out.print(" " + nextNode[i]);
	                i = nextNode[i];
	            }
	            if (!unreachable){

		            //path.offer((c[end]-startTime));
	            	path.offer(end);
//		            System.out.println(" "+c[end]);
		            if(c[end] > Lsp) {
		                Lsp = c[end];
		            }
	            }
	            else {
	            	//path.offer(end);
	            	//path.offer(-1);
	            	System.out.println("Path unavailable");

	            }
	        }
	}

	
	private static void tdsp(int start, int end, int time) {		// Ugur algorithm for computing TDSP

        PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
            new Comparator<Node>() {
                public int compare(Node n1, Node n2) {
                    return n1.cost - n2.cost;
                }
            }
        );

        int startTime=time;								// stores the trip initialization time

        int nn= graph.getSize();
        int i, len = nn, arrTime, w, j, id;
        int[] c = new int[len];
        int[] parent = new int[len];
        boolean unreachable=false;


        for(i=0; i<len; i++)
            parent[i] = -1;

        Iterator<Node> it;
        boolean qFlag = false;

        for(i=0; i<len; i++) {
            if(i == start)
                c[i] = 0;                       					//starting node
            else
                c[i] = Integer.MAX_VALUE;                      		//indicating infinity
        }

        //try{
	    //FileWriter fstream = new FileWriter("pairs2\\each_qu1.txt", true);
	    //BufferedWriter out = new BufferedWriter(fstream);


        Node tempN, n, s = new Node(start, 0, startTime);       	//creating the starting node with nodeId = start and cost = 0 and arrival time = time
        priorityQ.offer(s);                     					//inserting s into the priority queue
        int count=0;
        while ((n = priorityQ.poll()) != null) { 					//while Q is not empty
        	List<adj_lists.Pair<Integer, int[]>> neighbors;

        	time= startTime+n.getArrTime()/60/1000/timeInterval; 			//define time interval at the beginning (granularity)
//        	time=startTime;
//        	count++;
        	if (time>times-1){												// time [6am - 9 pm], we regard times after 8PM as constant edge weights
        		time=times-1;
        	}
            id = n.getNodeId();
            //out.write(id + "\t" + c[id] );
            //out.write("\n");

            if (id==end){
            	break;
            }

            neighbors=(List<Pair<Integer, int[]>>) graph.getList().get(id);
            arrTime = n.getArrTime();

            if (neighbors==null)
        		continue;


            for(Pair<Integer, int[]> pair: neighbors) {

            	int node=pair.getLeft();
            	int travelTime=pair.getRight()[time];

            	 if (arrTime + travelTime < c[node]) {

            		 c[node] = arrTime + travelTime;
                     parent[node] = id;
                     it = priorityQ.iterator();

                     while (it.hasNext() == true) {
                         if ( (tempN = it.next()).getNodeId() == node) {
                             if(priorityQ.remove(tempN) == true) {
                            	 //We should also set Cost here, because cost is used to set the priority queue
                                 tempN.setCost(c[node]);
                            	 //commented by Diongxiong
                                 tempN.setArrTime(c[node]);
                            	 priorityQ.offer(tempN);
                                 qFlag = true;
                             }
                             break;

                         }
                     }

                     if(qFlag == false) {
                         priorityQ.offer(new Node(node, c[node], c[node])); //arrival time = c[i]
                    //     System.out.println("inserting " + i);
                     }
                     else
                         qFlag = false;

            	 }
            }



        }
       // out.close();
        //return;
        //out.write("\n");
       // }
        //catch (IOException io) {
        //    System.err.println(io.toString());
        //    System.exit(1);
       // }

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


        if (start == end)
            System.out.println("Your starting node is the same as your ending node.");
        else {
            i = start;
            j = 1;
//            System.out.print(i);
            path.offer(i);
            while (i != end) {
            	if (nextNode[i]==-1){
            		unreachable=true;
            		break;
            	}
            	path.offer(nextNode[i]);
//                System.out.print(" " + nextNode[i]);
                i = nextNode[i];
            }
            if (!unreachable){

	            path.offer((c[end]-startTime));
//	            System.out.println(" "+c[end]);
	            if(c[end] > Lsp) {
	                Lsp = c[end];
	            }
            }
            else {
            	path.offer(end);
            	path.offer(-1);

            }
        }
	}
	
	
	 public static double distance(double stlat, double stlon, double endlat, double endlon) {  
		  double  Radius = 6371* 0.621371192; 
	      //double lat1 = StartP.getLati();  
	      double lat1 = stlat;
		  //double lat2 = EndP.getLati();  
	      double lat2 = endlat;
	      //double lon1 = StartP.getLongi();  
	      double lon1 = stlon;
	      //double lon2 = EndP.getLongi();  
	      double lon2 = endlon;
	      
	      double dLat = Math.toRadians(lat2-lat1);  
	      double dLon = Math.toRadians(lon2-lon1);  
	      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  
	         Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *  
	         Math.sin(dLon/2) * Math.sin(dLon/2);  
	      double c = 2 * Math.asin(Math.sqrt(a));  
	      return Radius * c;  
	   }  


}