package adj_lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import adj_lists.DataListGeneration.Node;

/*
 * @author Dingxiong
 * Run static Dijkstra algorithm from 100 random nodes, and select the maximal distance as the longest one.
 * run dijkstra search from 15000 random nodes, for each node, generate 10 group source-target pairs with 
 * their distance increasing 
 */

public class GenerateQueryPairs {
	
	public static BuildList staticGraph = new BuildList("StaticData_Monday.txt");
	
	public static int computeMaximalDistance(int start){

        PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
            new Comparator<Node>() {
                public int compare(Node n1, Node n2) {
                    return n1.getNodeCost() - n2.getNodeCost();
                }
            }
        );
        
        
        int nn= staticGraph.getStaticSize();
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
        
        boolean[] visited = new boolean[len];
        for(i=0; i<len; i++) {
                visited[i] = false;                      		//indicating infinity
        }

        Node tempN, n, s = new Node(start, 0, 0);       			//creating the starting node with nodeId = start and cost = 0 and arrival time = time
        priorityQ.offer(s);                     					//inserting s into the priority queue
        int count=0;
        int maximal = 0;;
        while ((n = priorityQ.poll()) != null) { 					//while Q is not empty
        	List<adj_lists.Pair<Integer, Integer>> neighbors;
        	
        	id = n.getNodeId();
        	if (visited[id] == true)
        		continue;
        	visited[id] = true;
        	maximal = n.getArrTime();
//            System.out.println(count+"");
            neighbors=(List<Pair<Integer, Integer>>) staticGraph.getStaticList().get(id);
            arrTime = n.getArrTime();
            
            if (neighbors==null)
        		continue;
            for(Pair<Integer, Integer> pair: neighbors) {
          
            	int node=pair.getLeft();
            	int travelTime=pair.getRight();
            	if (visited[node] == true)
            		continue;
        	 if (arrTime + travelTime < c[node]) {
        		 c[node] = arrTime + travelTime;
                 parent[node] = id;               
                 priorityQ.offer(new Node(node, c[node], c[node])); //arrival time = c[i]
        		 
        	 }
           }
        }
        return maximal;
	}
	
	//generate query pairs
	public static boolean generateQueryPairs(int start, int groupNum, int maxDistance, ArrayList<Pair<Integer, Integer>> []queryPair){

        PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
            new Comparator<Node>() {
                public int compare(Node n1, Node n2) {
                    return n1.getNodeCost() - n2.getNodeCost();
                }
            }
        );
        
        
        int nn= staticGraph.getStaticSize();
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
        
        boolean[] visited = new boolean[len];
        for(i=0; i<len; i++) {
                visited[i] = false;                      		//indicating infinity
        }

        Node tempN, n, s = new Node(start, 0, 0);       			//creating the starting node with nodeId = start and cost = 0 and arrival time = time
        priorityQ.offer(s);                     					//inserting s into the priority queue
        int count=0;
        //int maximal = 0;
        int curDist, lastDist;
        //int scale = 1024;
        int scale = 128;
        int processNum = 0;
        int isAdded[] = new int[groupNum];
        int finalNode[] = new int[groupNum];
        int temp[] = new int[groupNum];
        Random generator = new Random();
        for (i = 0; i < groupNum; i++){
        	isAdded[i] = 0;
        	finalNode[i] = -1;
        	temp[i] = 0;
        }      
        int interval;
        curDist = maxDistance / scale;
        interval = (maxDistance - curDist) / 10;
        lastDist = curDist;
        curDist = 0;
        
        //System.out.println("Distance: " + curDist + " " + lastDist);
        while ((n = priorityQ.poll()) != null) { 					//while Q is not empty
        	List<adj_lists.Pair<Integer, Integer>> neighbors;
        	
        	id = n.getNodeId();
        	if (visited[id] == true)
        		continue;
        	visited[id] = true;
        	arrTime = n.getArrTime();
        	
        	//System.out.println("poll id: " + id );
        	//curDist = maxDistance / scale;
        	//lastDist= maxDistance * 2 / scale;
        	
        	//if we bypass some level
        	if (arrTime > lastDist){
        		while (arrTime > lastDist){
  
        			processNum++;
        			//scale /= 2;
        			//curDist = maxDistance / scale;
        			//lastDist = maxDistance * 2 / scale;
        			if (processNum == 1){
        				curDist = lastDist;
        				lastDist = curDist + interval;
        			}
        			
        			curDist += interval;
        			lastDist += interval;
        			if (processNum == 8){
        				lastDist = maxDistance;
        			}
        			
        		}
        		//System.out.println("Distance: " + curDist + " " + lastDist + " " + arrTime + " " + processNum);
        		if (processNum >= groupNum)
        			break;
        	}
        	
        	if (arrTime > curDist && arrTime < lastDist){
        		int k = Math.abs(generator.nextInt()) + 1;
        		//System.out.println("metrics : " + start + " " + id + " " + arrTime + " " + k);
        		if (k > isAdded[processNum]){
        			finalNode[processNum] = id;
        			isAdded[processNum] = k;
        			temp[processNum] = arrTime;
        			//System.out.println("metrics : " + processNum+ " " + start + " " + id + " " + arrTime + " " + k);
        		}
        		//queryPair[processNum].add(new Pair(start, id));
        		//processNum++;
        		//scale /= 2;
        		if (processNum >= groupNum)
        			break;
        	}
        	
//            System.out.println(count+"");
            neighbors=(List<Pair<Integer, Integer>>) staticGraph.getStaticList().get(id);
                        
            if (neighbors==null)
        		continue;
            for(Pair<Integer, Integer> pair: neighbors) {
          
            	int node=pair.getLeft();
            	int travelTime=pair.getRight();
            	//System.out.println("neighbor id: "+node);
            	if (visited[node] == true)
            		continue;
        	 if (arrTime + travelTime < c[node]) {
        		 c[node] = arrTime + travelTime;
                 parent[node] = id;       
                 //System.out.println("offer id: "+node);
                 priorityQ.offer(new Node(node, c[node], c[node])); //arrival time = c[i]      		 
        	 }
           }
        }
        //System.out.println("output array pairs");
        for (i = 0 ; i < groupNum ; ++i ){
			//if ( finalNode[i] == -1 ) System.out.println(" ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRROR!!!!!!!!!!!!!!!!");
			if(finalNode[i] != -1){
				queryPair[i].add(new Pair(start, finalNode[i]));
				//System.out.println("metrics : " + start + " " + finalNode[i] + " " + temp[i]);
			}
		}
        return true;
       
	}
	
	
	
	public static void main(String args[])
	{
		//Select 100 random nodes and chose the node with the maximal distance
		Random generator = new Random();
		int size = staticGraph.getStaticSize();
		int randomRunTimes = 100;
		int dis, maximal = 0;
		int node, bNode = 0;
		
		/*for (int i = 0; i < randomRunTimes; i++){
			node = generator.nextInt(size);
			dis = computeMaximalDistance(node);
			if (dis > maximal){
				maximal = dis;
				bNode = node;
			}
		}
		System.out.println("Maximal Distance: " + maximal + " Node ID: " + bNode);*/
		
		
		// In this experiment, we choose node id = 36279, and maximal distance = 9275594
		maximal = 10236866;
		
		//the minimal edge weight = 0, maximal edge weight = 2532755
		Map<Integer, List<Pair<Integer,Integer>>> graphList = staticGraph.getStaticList();
		int count = 0;
		int minDis = 1000000, maxDis = 0, curDis = 0;
		for (Integer nodeId: graphList.keySet()){
			count ++;
			List <Pair<Integer, Integer>> edgeList = graphList.get(nodeId);
			if (edgeList != null){
				Iterator <Pair<Integer, Integer>> lit = edgeList.iterator();
				while(lit.hasNext()){
					Pair <Integer, Integer> p = lit.next();
					//curDis = lit.next().getRight();
					curDis = p.getRight();
					if (curDis < minDis)
						minDis = curDis;
					if (curDis > maxDis)
						maxDis = curDis;
					//if (nodeId == 79133)
					//	System.out.println("79131: " + p.getLeft());
				}
			}
		}
		System.out.println("Total Nodes: " + count + " Minimal weight: "  + minDis + " Maximal Weight: " + maxDis);
		
		//
		int totalPairs = 15000;
		int groupNum = 10;
		ArrayList<Pair<Integer,Integer>> []queryPairs=new ArrayList[groupNum];
		for (int i=0; i<queryPairs.length;i++) 
			queryPairs[i]=new ArrayList<Pair<Integer,Integer>>(totalPairs);
		
		
		System.out.println("Generating query pairs ");
		int num =  0;
		while (num < totalPairs){
			int curNode = generator.nextInt(size);
			//int curNode = 79131;
			//System.out.println(num + " pair");
			generateQueryPairs(curNode, groupNum, maximal, queryPairs);	
			num++;
			if (num % 1000 == 0) 
				System.out.println(num + " pairs generated");
		}
		System.out.println("Outputting query pairs ");
		
		String[] fileName = new String[groupNum];
		String prefix = "querypair\\monday\\monday_q_";
		for (int i = 0; i < groupNum; i++){
			fileName[i] = prefix + i + ".txt";
		}
		try{
			for (int i = 0; i < groupNum; i++){
				FileWriter fstream = new FileWriter(fileName[i]);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(queryPairs[i].size() + "\n");
				for (int j = 0; j < queryPairs[i].size(); j++){
					out.write(queryPairs[i].get(j).getLeft() + "\t" + queryPairs[i].get(j).getRight() + "\n");
				}
				out.close();
			}			
		}
		catch(IOException io){
			System.err.println(io.toString());
		    System.exit(1);
		}
		
		System.out.println("Finished Generating Query Pairs");	
		
	}
}
