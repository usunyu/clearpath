package tdsp.preproc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import library.FibonacciHeap;
import library.FibonacciHeapNode;
import Objects.LinkInfo;
import Objects.Node;
import Objects.NodeConnectionValues;
import Objects.NodeValues;

/**
 * This class is TDSP processing Java Code
 * for network 1234 using Fibonacci Heap Implementatation
 * This has optimization using 10x10 grids but didn't help much in this version
 * @see FibonacciHeap
 * @see FibonacciHeapNode
 */
public class TDSPQueryListJavaCodeFiboGrid1234 {
	private static final long serialVersionUID = 1L;

	private static int Lsp = 0;
	private static NodeConnectionValues[] graphTDSP;

	static double[][] nodes;
	static int[] nodesType;
	static Map<Integer, Integer> midPoints;
	static double[][] midPointOrds;
	static LinkInfo links[] = new LinkInfo[100000]; // 1000000
	static int link_count = 0;
	private static int length1;
	private static int length3;
	static int startNodeID;
	static int endNodeID;
	static int length2;
	private static HashMap<Integer,Integer> nodeGridOutTime = new HashMap<Integer,Integer>();
	private static HashMap<Integer,Integer> GridToGridTime = new HashMap<Integer,Integer>();
	private static HashMap<Integer,Integer> nodeGrids = new HashMap<Integer,Integer>();
	
	public static void main(String args[]) {
		long date1 = new Date().getTime();
		//readFileInMemory();
		readFile();
		int time;
		String day = args[4];
		readListTDSP(day);
		readNodeGridInfo();
		readNodeToGridTime();
		readGridToGridTime();
		
		if (args[0].equals("False")) {
			String[] startOrds = args[1].split(",");
			String[] endOrds = args[2].split(",");
			startNodeID = findNN(Double.parseDouble(startOrds[0]),
					Double.parseDouble(startOrds[1]));
			endNodeID = findNN(Double.parseDouble(endOrds[0]),
					Double.parseDouble(endOrds[1]));
			time = Integer.parseInt(args[3]);
		} else {
			startNodeID = Integer.parseInt(args[1].substring(1));
			endNodeID = Integer.parseInt(args[2].substring(1));
			time = Integer.parseInt(args[3]);
		}
		//getMaxTime();

		tdsp(startNodeID, endNodeID, time);
		System.out.println("-" + startNodeID + "-" + endNodeID + "-" + time);
		long date2 = new Date().getTime();
		System.out.println((date2 - date1) / 1000.0 + "Seconds");

	}

	private static void readNodeGridInfo() {
		try {

			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234\\NodeGridInfo.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				  String [] nodes = strLine.split(",");
				  nodeGrids.put(Integer.parseInt(nodes[0]), Integer.parseInt(nodes[1]));
				  }
			  in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
	}

	private static void readNodeToGridTime() {
		
		try {

			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234\\NodeBPTime.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				  String [] nodes = strLine.split(",");
				  nodeGridOutTime.put(Integer.parseInt(nodes[0]), Integer.parseInt(nodes[1]));
				  }
			  in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
private static void readGridToGridTime() {
		
		try {
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234\\GridBPTime.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				  String [] nodes = strLine.split(" ");
				  GridToGridTime.put(Integer.parseInt(nodes[0]+""+nodes[1]),Integer.parseInt(nodes[2]));
				  }
			  in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	

	private static int findNN(double latitude, double longitude) {
		int NNID = 1;
		double minDistance = (latitude - nodes[0][0])
				* (latitude - nodes[0][0]) + (longitude - nodes[0][1])
				* (longitude - nodes[0][1]);
		for (int i = 1; i < nodes.length; i++) {
			double dist = (latitude - nodes[i][0]) * (latitude - nodes[i][0])
					+ (longitude - nodes[i][1]) * (longitude - nodes[i][1]);
			if (dist < minDistance) {
				NNID = i;
				minDistance = dist;
			}
		}
		return NNID;
	}

	/*private static void readFileInMemory() {

		try {
			// System.out.println("READING LINKS FILE IN MEMORY");
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234\\Edges.csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				  String [] nodes = strLine.split(",");
					int FuncClass = Integer.parseInt(nodes[1]);
					String st_node = nodes[3];
					String end_node = nodes[4];
					String index = st_node.substring(1)+""+end_node.substring(1);
					String LinkIdIndex = String.valueOf(nodes[0])+""+index;
					long LinkId = Long.valueOf(LinkIdIndex);
					int i = 5, count = 0;
					PairInfo[] pairs = new PairInfo[1000];
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
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}
*/
	private static void readFile() {
		try {
			// System.out.println("READING NODES FILE IN MEMORY FROM OBJ");
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234\\TDSPData.obj");
			ObjectInputStream ois = new ObjectInputStream(fstream);

			length1 = ois.readInt();
			length2 = ois.readInt();
			length3 = ois.readInt();
			graphTDSP = new NodeConnectionValues[length1];
		
			int nodeNum = ois.readInt();
			nodes = new double[nodeNum][2];
			for (int i = 0; i < nodeNum; i++) {
				nodes[i][0] = ois.readDouble();
				nodes[i][1] = ois.readDouble();
			}

			ois.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(-1);
		}
	}

	private static void readListTDSP(String day) {

		try {
			// System.out.println("READING ADJ LIST IN MEMORY");
			FileInputStream fstream = new FileInputStream(getFileName(day));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader file = new BufferedReader(new InputStreamReader(in));
			String tmp, temp, temp2;
			tmp = file.readLine();

			int i = 0;
			while (tmp != null) {

				if (!(tmp.equals("NA"))) {
					graphTDSP[i] = new NodeConnectionValues();
					StringTokenizer sT = new StringTokenizer(tmp, ";");

					int j = 0, k = 0;
					while (sT.hasMoreTokens()) {
						temp = sT.nextToken();

						j = Integer.parseInt(temp.substring(1,
								temp.indexOf("(")));
						String type = temp.substring(temp.indexOf("(") + 1,
								temp.indexOf(")"));
						int values[] = new int[60];
						if (type.equals("V")) {
							k = 0;

							StringTokenizer sT2 = new StringTokenizer(temp, ",");

							while (sT2.hasMoreTokens()) {
								temp2 = sT2.nextToken();
								if (temp2.indexOf(":") != -1) {
									values[k++] = Integer.parseInt(temp2
											.substring(temp2.indexOf(":") + 1));
								} else {
									values[k++] = Integer.parseInt(temp2);

								}
							}
						} else {
							for (k = 0; k < length3; k++) {
								values[k] = Integer.parseInt(temp
										.substring(temp.indexOf(":") + 1));

							}
						}
						graphTDSP[i].nodes.remove(j);
						graphTDSP[i].nodes.put(j, new NodeValues(j, values));
					}

				}

				i++;
				tmp = file.readLine();

			}
			file.close();

		} catch (IOException io) {
			io.printStackTrace();
			System.exit(1);
		} catch (RuntimeException re) {
			re.printStackTrace();
			System.exit(1);
		}
	}

	private static void tdsp(int start, int end, int time) {
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
		  
		//int shortestTime = maxTime;
		   while (!(priorityQ.isEmpty())) { //while Q is not empty

	        	//System.out.println("Size of queue="+priorityQ.size());
	        	FibonacciHeapNode<Node> node = priorityQ.min();
	        	priorityQ.removeMin();
	        	n = node.getData();
	        	
	        	
			priorityQMap1.remove(n.getNodeId());	
			priorityQMap2.remove(n);
			
			int updTime = time;
			id = n.getNodeId();
			if (graphTDSP[id] == null)
				continue;
			

			if (n.getNodeId() == end) {
				shortestTime = n.getArrTime();

			}

			if (n.getNodeId() != start) {
				updTime = time + getTime(n.getArrTime());

				if (updTime > 59)
					updTime = 59;
			}

			if (graphTDSP[id].nodes != null && n.getArrTime() < shortestTime) {
				HashMap<Integer, NodeValues> hmap = graphTDSP[id].nodes;

				Set<Integer> keys = hmap.keySet();
				Iterator<Integer> iter = keys.iterator();
				 //System.out.println("size->"+keys.size());
				int i2 = 0;
				while (i2 < keys.size()) {
					int key = iter.next();
					NodeValues val = hmap.get(key);
					arrTime = n.getArrTime();
					w = val.getValues()[updTime];
					
					 if (arrTime + w < c[key]) {
                    	 c[key] = arrTime + w;
 						parent[key] = id;
 						
 						if(priorityQMap1.containsKey(key))
 						{
 						Node node_ = priorityQMap1.get(key);
 						node_.setArrTime(c[key]);
 						priorityQ.decreaseKey(priorityQMap2.get(node_),node_.getNodeCost());
 						}
 						else
 						{	int cost = c[key]+getCost(key, end);
 							Node node2 = new Node(key, cost, c[key]);
 							FibonacciHeapNode<Node> new_node2 = new FibonacciHeapNode<Node>(node2);
 							priorityQ.insert(new_node2,cost);  //arrival time = c[i]
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
		while (temp != -1) {
			if (parent[temp] != -1)
				nextNode[parent[temp]] = temp;
			temp = parent[temp];
		}

		if (start == end) {
			System.out.println("Your starting node is the same as your ending node.");
			System.out.println("" + nodes[start][0] + "," + nodes[start][1]
					+ ";0");
			return;
		} else {
			i = start;
			j = 1;
			System.out.println("n" + start + " " + nodes[i][0] + ","
					+ nodes[i][1] + ";");
			while (i != end && nextNode[i] != -1) {

				System.out.println("n" + nextNode[i] + " "
						+ nodes[nextNode[i]][0] + "," + nodes[nextNode[i]][1]
						+ ";");
				i = nextNode[i];
			}
			System.out.println("" + (double) c[end] / 60000.0);
			if (c[end] > Lsp) {
				Lsp = c[end];
			}
		}

	}

	private static int getTime(int arrTime) {
		int minutesTime = (int) (arrTime / 60000.0);
		if (minutesTime >= 0 && minutesTime < 7)
			return 0;
		else if (minutesTime >= 7 && minutesTime < 22)
			return 1;
		else if (minutesTime >= 22 && minutesTime < 37)
			return 2;
		else if (minutesTime >= 37 && minutesTime < 52)
			return 3;
		else
			return 4;

	}
	
	private static String getFileName(String day){
		return("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network1234\\AdjList_"+day+".txt");
	}

	private static int getCost(int n1, int n2){
		int n1ToGrid = nodeGridOutTime.get(n1);
		int n2ToGrid = nodeGridOutTime.get(n2);
		int grid1 = nodeGrids.get(n1);
		int grid2 = nodeGrids.get(n2);
		String key = String.valueOf(grid1)+""+String.valueOf(grid2);
		
		if(GridToGridTime.get(Integer.parseInt(key))!=null)
		{int gridToGrid = GridToGridTime.get(Integer.parseInt(key));
		
		return (n1ToGrid+n2ToGrid+gridToGrid);
		}
		else
			return (n1ToGrid+n2ToGrid);
		
	}
	
}
