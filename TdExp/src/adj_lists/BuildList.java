package adj_lists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class BuildList {

	/**
	 * @author  Ugur
	 *
	 *
	 * input: 	road network in the form of adjecency list.
     *
	 *AdjList_Thursday.txt is a time-dependent raod network with approximately  47K modes. The format of the file is as follows
    *
	* n1(V): 6146,6377,6090, ,7688;n4(V):5917,6139,, 7401;
	* n0(V):6146,6377,6090, . ,7688;n2(V):6031,6258,
	* n3(V): ; n7(V):.; n9(V):;
	* Where
	* n0 is connected to n1 and n4   (1st file)
	* n1 is connect to n0; and n2 (2nd line)
	* n2 is connect to n3; and n7; and n9
	* ...
	* ...
	* ...
	* n47K is connected to ....
	* V stands for Variable travel time. The ideas is the following.  Lets assume  n8(F):5321  where F stands for Fixed we would not repeat the cost 5321  for all time instances to save space.
	*
	* output: 	A data stracture that resides in memory and contains the representation of the road network.
	* 			n1-> {(n2,[60]),(n100,[60]),()...()}
	*
	* it applies to both static and dynamic lists, depending on the constructor.
	* statistic function prints statistics for the memory consumption (remains low, less than 50MB )
	*/

	public Map<Integer, List<Pair<Integer,int []>>> adjacencyList = new HashMap<Integer, List<Pair<Integer,int[]>>>();
	public Map<Integer, List<Pair<Integer,Integer>>> staticList = new HashMap<Integer, List<Pair<Integer,Integer>>>();

	public int times=60;

	public BuildList() {									//Dynamic List Builder
		//String inFile = "AdjList_Thursday.txt"; //small network 
		String inFile = "CA_AdjList_Weekday_15.txt";

		Pair<Integer,int []> pair;

		System.out.println("Loading Dynamic File: " + inFile);

        try {
            RandomAccessFile file = new RandomAccessFile(inFile, "rw");

            file.seek(0);

            String temp;
            List<Pair<Integer,int[]>> list;

            System.out.println("Building graph. Please wait...");
            int i = 0;
            while (	(temp=file.readLine()) != null ) {
            	//System.out.println(i);
            	if(i%1000==0)
            	
                 System.out.println("completed "+i*100/496821+"%"); //Monday Graph  - large graph
               // System.out.println("completed "+i*100/111532+"%"); //Thursday Graph - small graph 
                 /* Note that A* would not give correct results with Thursday Graph  because nodes.csv is not consistent with Thursday graph */
                
                if (temp.equals("NA")){
            		adjacencyList.put(i,null);
            		i++;
            		continue;
            	}
            	list = new ArrayList<Pair<Integer, int[]>>();
            	adjacencyList.put(i,list);

                StringTokenizer sT = new StringTokenizer(temp, ";");
                int k=0, target;
                int [] times;
                while (sT.hasMoreTokens()) {
                	times= new int[60];

                	k = 0;
                    temp = sT.nextToken();
                    target=Integer.parseInt(temp.substring(temp.indexOf('n')+1, temp.indexOf('(')));

                    temp=temp.substring(temp.indexOf(':')+1);
                    StringTokenizer sT2 = new StringTokenizer(temp, ",");
                    while(sT2.hasMoreTokens()) {
                        String temp2 = sT2.nextToken();
                        times[k++] = Integer.parseInt(temp2);
                    }
                    pair = new Pair<Integer, int[]>(target, times);
                    list.add(pair);

                }
                i++;
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

	public BuildList(String staticDataFile) {				//static list builder
		String inFile = staticDataFile;

		Pair<Integer,Integer> pair;


        try {
            RandomAccessFile file = new RandomAccessFile(inFile, "rw");

            String temp;
            List<Pair<Integer,Integer>> list;

            System.out.println("Building graph. Please wait...");
            int i = 0;
            while (	(temp=file.readLine()) != null ) {
            	System.out.println("completed "+i*100/111532+"%");
                
            	if (temp.equals("NA")){
            		staticList.put(i,null);
            		i++;
            		continue;
            	}
            	list = new ArrayList<Pair<Integer, Integer>>();
            	staticList.put(i,list);

                StringTokenizer sT = new StringTokenizer(temp, ";");
                int target, weight=0;
                while (sT.hasMoreTokens()) {

                    temp = sT.nextToken();
                    target=Integer.parseInt(temp.substring(temp.indexOf('n')+1, temp.indexOf(':')));

                    weight=Integer.parseInt(temp.substring(temp.indexOf(':')+1));

                    pair = new Pair<Integer, Integer>(target, weight);
                    list.add(pair);

                }
                i++;
            }
            file.close();
        }
        catch (IOException io) {
            System.err.println(io.toString());
            System.err.println(io.getLocalizedMessage());

            System.exit(1);
        }
        catch (RuntimeException re) {
            System.err.println(re.toString());

            System.err.println(re.getLocalizedMessage());
            System.exit(1);
        }

	}


	public Map<Integer, List<Pair<Integer,int []>>> getList(){
		return adjacencyList;

	}

	public Map<Integer, List<Pair<Integer,Integer>>> getStaticList(){
		return staticList;

	}


	public int getSize(){
		return adjacencyList.keySet().size();
	}


	public int getTimes () {
		return this.times;
	}

	public int getStaticSize(){
		return staticList.keySet().size();
	}


	public void printMemStat(){						// prints statistics of memory consumption for the list data structure
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		System.out.println(sb);
		sb.append("free memory: " + format.format(freeMemory / 1024) + "kB\n");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "kB\n");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "kB\n");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "kB\n");

		System.out.println(sb);
	}

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		BuildList b = new BuildList();
//		int target=1;
//
//
//
//		List<Pair<Integer, int[]>> neighbors= b.adjacencyList.get(target);
//		System.out.println(target+"->");
//		for (Pair<Integer, int[]>n :neighbors){
//			System.out.print(n.getLeft()+": ");
//			for (int time: n.getRight()){
//				System.out.print(time+" ");
//
//			}
//			System.out.println();
//		}
//		System.out.println("size="+b.adjacencyList.keySet().size()+"!!");
//		System.out.println("size_correct=46936-30=46906");
//
//
//
//	}

}
