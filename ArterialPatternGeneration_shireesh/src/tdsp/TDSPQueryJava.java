/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tdsp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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
 *
 * @author clearp
 */
public class TDSPQueryJava {

        NodeConnectionValues[] graphTDSP;
        double[][] nodes;
	int[] nodesType;
	Map<Integer, Integer> midPoints;
	double[][] midPointOrds;
	LinkInfo links[] = new LinkInfo[100000]; // 1000000
	int link_count = 0;
	int length1;
	int length3;
	int startNodeID;
	int endNodeID;
	int length2;

        public TDSPQueryJava(){

            readFile();
	    readListTDSP();
       }

        public int process(String update, String start, String end){
            if (update.equals("False")) {
			String[] startOrds = start.split(",");
			String[] endOrds = end.split(",");
			startNodeID = findNN(Double.parseDouble(startOrds[0]),
					Double.parseDouble(startOrds[1]));
			endNodeID = findNN(Double.parseDouble(endOrds[0]),
					Double.parseDouble(endOrds[1]));
		} else {
			startNodeID = Integer.parseInt(start.substring(1));
			endNodeID = Integer.parseInt(end.substring(1));
		}
            return tdsp(startNodeID, endNodeID);
        }
	
	private int findNN(double latitude, double longitude) {
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

	private void readFile() {
		try {
			// System.out.println("READING NODES FILE IN MEMORY FROM OBJ");
			FileInputStream fstream = new FileInputStream(
					"H:\\Network1234\\TDSPData.obj");
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

	private void readListTDSP() {

		try {
			// System.out.println("READING ADJ LIST IN MEMORY");
			FileInputStream fstream = new FileInputStream(
					"H:\\Network1234\\AdjListLTT.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader file = new BufferedReader(new InputStreamReader(in));
			String tmp, temp;
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
								temp.indexOf(":")));
						int values[] = new int[60];
						for (k = 0; k < length3; k++) {
								values[k] = Integer.parseInt(temp
										.substring(temp.indexOf(":") + 1));
			
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

	private int tdsp(int start, int end) {
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

		Node n, s = new Node(start, 0, 0); 			// creating the starting
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

			id = n.getNodeId();
			if (graphTDSP[id] == null)
				continue;


			if (n.getNodeId() == end) {
				break;
			}


			if (graphTDSP[id].nodes != null) {
				HashMap<Integer, NodeValues> hmap = graphTDSP[id].nodes;

				Set<Integer> keys = hmap.keySet();
				Iterator<Integer> iter = keys.iterator();
				 //System.out.println("size->"+keys.size());
				int i2 = 0;
				while (i2 < keys.size()) {
					int key = iter.next();
					NodeValues val = hmap.get(key);
					arrTime = n.getArrTime();
					w = val.getValues()[0];

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
                                                node_.setNodeCost(c[key]);
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

		if (start == end) {
			return 0;
		} else {
		        return ((int)c[end]/1000);
                        }

	}

	public static void main(String args[]){
            TDSPQueryJava tdsp = new TDSPQueryJava();
            System.out.println(tdsp.process("False","34.1517,-118.90905","34,-117.9"));
        }
}
