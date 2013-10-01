package tdsp.preproc;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.StringTokenizer;

import Objects.LinkInfo;
import Objects.Node;
import Objects.NodeConnectionValues;
import Objects.NodeValues;
import Objects.PairInfo;
/**
 * This class is TDSP processing Java Code
 * for network 123 using Priority Queue Implementatation
 */
public class TDSPQueryListJavaCode123 {
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
	private static int length2;
	private static int length3;
	static int startNodeID;
	static int endNodeID;

	public static void main(String args[]) {
		long date1 = new Date().getTime();
		readFileInMemory();
		readFile();
		readListTDSP();
		int time;

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

	private static void readFileInMemory() {

		try {
			// System.out.println("READING LINKS FILE IN MEMORY");
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network123\\Edges.csv");
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

	private static void readFile() {
		try {
			// System.out.println("READING NODES FILE IN MEMORY FROM OBJ");
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network123\\TDSPData.obj");
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

	private static void readListTDSP() {

		try {
			// System.out.println("READING ADJ LIST IN MEMORY");
			FileInputStream fstream = new FileInputStream(
					"C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\Network123\\AdjList_All.txt");
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

		PriorityQueue<Node> priorityQ = new PriorityQueue<Node>(20,
				new Comparator<Node>() {
					public int compare(Node n1, Node n2) {
						return (n1.getNodeCost() - n2.getNodeCost());
					}
				});

		int i, len = graphTDSP.length, j, id;
		// System.out.println("len="+len);
		int w, arrTime;
		int[] c = new int[len];
		int[] parent = new int[len];

		for (i = 0; i < len; i++)
			parent[i] = -1;

		Iterator<Node> it;
		boolean qFlag = false;

		for (i = 0; i < len; i++) {
			if (i == start)
				c[i] = 0; // starting node
			else
				c[i] = 8000000; // indicating infinity
		}

		Node tempN, n, s = new Node(start, 0, time); // creating the starting
														// node with nodeId =
														// start and cost = 0
														// and arrival time =
														// time
		priorityQ.offer(s); // inserting s into the priority queue
		int shortestTime = 800000000;
		while ((n = priorityQ.poll()) != null) { // while Q is not empty

			int updTime = time;
			id = n.getNodeId();
			if (graphTDSP[id] == null)
				continue;
			/* System.out.println("Size of queue="+priorityQ.size());
			 System.out.println("\n\nPOPPED " + n.getNodeId() +
			 " with arrival time = " + n.getArrTime());*/

			if (n.getNodeId() == end) {
				
				shortestTime = n.getArrTime();
				break;
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
					
					//System.out.println("cost="+w);
					if (arrTime + w < c[key] /*&& arrTime + w < shortestTime*/) {
						c[key] = arrTime + w;
						parent[key] = id;
						it = priorityQ.iterator();
						while (it.hasNext() == true) {
							if ((tempN = it.next()).getNodeId() == key) {
								if (priorityQ.remove(tempN) == true) {
									tempN.setArrTime(c[key]);
									priorityQ.offer(tempN);
									qFlag = true;
								}
								break;
							}
						}

						if (qFlag == false) {
							priorityQ.offer(new Node(key, c[key], c[key])); // arrival
																		// time
																		// =
																		// c[i]

						} else
							qFlag = false;
					}
					i2++;

				}
			}
		}

		int temp;
		int[] nextNode = new int[len];
		for (i = 0; i < len; i++)
			nextNode[i] = -1;

		temp = end;
		while (temp != -1) {
			if (parent[temp] != -1)
				nextNode[parent[temp]] = temp;
			temp = parent[temp];
		}

		if (start == end) {
			System.out
					.println("Your starting node is the same as your ending node.");
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

}
