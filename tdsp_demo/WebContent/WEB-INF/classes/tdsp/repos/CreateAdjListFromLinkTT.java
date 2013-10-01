package tdsp.repos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Objects.DistanceCalculator;
import Objects.LinkInfo;
import Objects.PairInfo;
import Objects.TimeShot;

public class CreateAdjListFromLinkTT {
	
	static HashMap<Integer, LinkInfo> links = new HashMap<Integer, LinkInfo>();
	static String FILE_LINK = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\links_all.csv";
    static ArrayList<TimeShot> SpeedNodes = new ArrayList<TimeShot>();
    //static TimeShot [] SpeedArray;
	private static double[] ArterialAverages;
	private static double[] HighwayAverages;
	public static void main(String args[])
	{
		readFileInMemory();
		getHighwayArterialAverageSpeeds();
		fillAdjList();
		putListToArray();

	}
	
	private static void getHighwayArterialAverageSpeeds() {
		
		System.out.println("GETTING AVERAGE SPEEDS FOR HIGHWAYS/ARTERIALS");
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br  = null;
		try
		{
			HighwayAverages = new double[60];
			fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\AverageSpeeds_Highways.txt");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count =0;
			while ((strLine = br.readLine()) != null) {
				HighwayAverages[count++] = Double.parseDouble(strLine.split(",")[1]);
			}
			in.close();fstream.close();
			
			ArterialAverages = new double[60];
			fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\AverageSpeeds_Arterials.txt");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			count =0;
			while ((strLine = br.readLine()) != null) {
				ArterialAverages[count++] = Double.parseDouble(strLine.split(",")[1]);
			}
			in.close();fstream.close();
			
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	private static void putListToArray() {

		System.out.println("WRITING FILE FOR ADJACENCY LIST...");
		FileWriter fstream = null;
		BufferedWriter out = null;
		try
		{
		fstream = new FileWriter("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\AdjList.txt");
		out = new BufferedWriter(fstream);

		for(int i=0;i<5572;i++)
		{
			
			boolean flag = false;
			for(int j=0;j<SpeedNodes.size();j++)
			{
				//Get Nodes with start index i
				TimeShot ts = SpeedNodes.get(j);
				if(Integer.parseInt(ts.getStartNode().substring(1))==i)
				{
					flag = true;
				    double[] travelTime = ts.getTravelTime();
				    int count=0;
				  //Check if the end node is V on anything !=1000.0
				    for(int k=0;k<59;k++)
				     {
				    	 if(travelTime[k+1]==travelTime[k] && travelTime[k]!=1000.0)
				    	 {
				    		 count++;
				    	 }
				     }
				    if(count == 60)
				    {
				    	out.write(ts.getEndNode()+"(F):"+travelTime[0]*60.0+";");
				    	continue;
				    }
					
				    out.write(ts.getEndNode()+"(V):");
				    for(int k=0;k<60;k++)
				     {
				    	 if(travelTime[k]==1000.0)
				    	 {
				    		 double distance = ts.getDistance();
				    		 double speed = getAverageTravelTime(ts.getFuncClass(),k);
				    		 int time = (int)((distance*60.0*60.0)/speed);
				    		 //System.out.println(distance+" "+speed+" "+time);
				    		 if(k!=59)
				    		  out.write(time+",");
				    		 else
				    	      out.write(time+";");
				    	 }
				    	 else
				    	 {
				    		 if(k!=59)
					    		 out.write((int)(travelTime[k]*60.0)+",");
					    		 else
					    		 out.write((int)(travelTime[k]*60.0)+";");
				    	 }
				     }
				   
				// If 1000.0 then get Average speed for every index 0->60 depending on FuncClass 1/2
				// Else if V/F write appropriately to the file.
				}
				
			}
			if(!flag)
				out.write("NA");
			out.write("\n");
		}
		out.close();
		}
		catch(Exception e)
		{
			System.err.println(e.toString());
		}
	}

	private static double getAverageTravelTime(int funcClass, int k) {
		if(funcClass == 1)
		{
			return ArterialAverages[k];
		}
		else if(funcClass ==2)
		{
			return HighwayAverages[k];
		}
		return 0.0;
	}

	private static void readFileInMemory() {

		System.out.println("READING LINK FILE AND GENERATING NODES");
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

	private static void fillAdjList(){
		System.out.println("GETTING LINKS AND ADDING TO TIMESHOTS");

		int i = 0;
		Set<Integer> keys = links.keySet();
		Iterator<Integer> iter = keys.iterator();
		while (i < keys.size()) {
			LinkInfo link = links.get(iter.next());
			double []  travelTime = getTravelTimeFromFile(link);
			SpeedNodes.add(new TimeShot(link.getStart_node(), link.getEnd_node(), link.getFunc_class(), getDistance(link), travelTime));
			i++;
		}
		
		
	}

	private static double[] getTravelTimeFromFile(LinkInfo link) {
		double [] travelTime = new double[60];
		try {
			FileInputStream fstream = new FileInputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\Output_Files\\"+link.getLinkId()+".txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int lineNumber =0;
			
			while ((strLine = br.readLine()) != null) {
			//System.out.println(strLine);	
			if(lineNumber == 2 && strLine.equals("Average"))
			{	
			int x =0;
			for(;x<60;x++)
			{
				travelTime[x] = 1000.0;
			}
			return travelTime;
			}
			if(lineNumber < 2)
			{	lineNumber++;
				continue;
			}
			if(strLine.equals("Average")&& lineNumber>2)
			{
				int x = lineNumber-2;
				for(;x<60;x++)
				{
					travelTime[x] = 1000.0;
				}
				return travelTime;
			}
			travelTime[lineNumber-2] = Double.parseDouble(strLine.split(",")[2]);
			lineNumber++;
			}

			return travelTime;
		}
			catch(Exception e)
			{
				int x =0;
				for(;x<60;x++)
				{
					travelTime[x] = 1000.0;
				}
				return travelTime;
			}
		
	}

	private static double getDistance(LinkInfo link) {
		PairInfo[] pairs = link.getNodes();
		double distance = 0.0;
		for (int i = 0; i < link.getPairCount() - 1; i++) {
			distance += DistanceCalculator.CalculationByDistance(pairs[i],
					pairs[i + 1]);
		}
		return distance;
	}
	
}
