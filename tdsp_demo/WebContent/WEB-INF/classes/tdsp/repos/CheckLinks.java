package tdsp.repos;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import Objects.LinkInfo;
import Objects.PairInfo;

public class CheckLinks {

	private static String FILE_LINK = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\links_all.csv";
	static HashMap<Integer, LinkInfo> links = new HashMap<Integer, LinkInfo>();
	static boolean[][] TreeLinks = new boolean[8723][8723];
	static boolean [] Reachable = new boolean[8723];
	public static void main(String args[])
	{
		
		init();
		readFileInMemory();
		//printTree();
		checkConnectivity();
		//PrintConnectivityNodes(2);
		//CheckNow();
	}
	
	private static void CheckNow() {
		System.out.println("called");
		int count = 0;
		for(int i=0;i<8723;i++)
		{
			if(!Reachable[i])
			 {  count++;
				//System.out.println("Node "+i+" not reachable");
			 }
			 }
		System.out.println(count+" nodes not reachable");
	}

	private static void checkConnectivity() {
		for(int i=20;i<22;i++)
		{
			PrintConnectivityNodes(i);
			System.out.println();
			CheckNow();
			for(int j=0;j<8723;j++)
				Reachable[j]=false;
		}
		
	}

	private static void PrintConnectivityNodes(int i) {
		System.out.print(i+"->");
		for(int j=0;j<8723;j++)
		{
			if(TreeLinks[i][j])
			{	
				if(Reachable[j])
					continue;
				Reachable[j]= true;
				PrintConnectivityNodes(j);
			}
		}
		
	}

	private static void printTree() {
		for(int i=0;i<8723;i++)
		{	System.out.print("n"+i+":");
			for(int j=0;j<8723;j++)
				{if(TreeLinks[i][j])
					System.out.print("n"+j+";");
				}
			System.out.println();
		}
	}

	private static void init() {
		for(int i=0;i<8723;i++)
			for(int j=0;j<8723;j++)
				TreeLinks[i][j]= false;
		
		for(int i=0;i<8723;i++)
			Reachable[i]=false;
	}

	private static void readFileInMemory() {

		try {
			FileInputStream fstream = new FileInputStream(FILE_LINK);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(",");
				int LinkId = Integer.parseInt(nodes[0]);
				int FuncClass = Integer.parseInt(nodes[1]);
				String st_node = nodes[2];
				String end_node = nodes[3];
				if(FuncClass==2)
				{
					//System.out.println(Integer.parseInt(st_node.substring(1))+" "+Integer.parseInt(end_node.substring(1)));
					TreeLinks[Integer.parseInt(st_node.substring(1))][Integer.parseInt(end_node.substring(1))] = true;
					TreeLinks[Integer.parseInt(end_node.substring(1))][Integer.parseInt(st_node.substring(1))] = true;
				}
				int i = 4, count = 0;
				PairInfo[] pairs = new PairInfo[1000];
				while (i < nodes.length) {
					
					double lati = Double.parseDouble(nodes[i]);
					double longi = Double.parseDouble(nodes[i + 1]);
					pairs[count] = new PairInfo(lati, longi);
					count++;
					i = i + 2;
				}
				links.put(LinkId, new LinkInfo(LinkId, FuncClass, st_node,
						end_node, pairs, count));

			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}
	
}
