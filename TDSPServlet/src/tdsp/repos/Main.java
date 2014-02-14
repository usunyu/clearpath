package tdsp.repos;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.Map;

public class Main {
        static int numNodes, Lsp = 0;
        static int graph[][];
        static int[][] tdsp_new;
        static double[][][] graphTDSP, graphTDSP2;
        static int start, end, time;

        static double[][] nodes;
        static Map<Integer, Integer> midPoints;
        static double[][] midPointOrds;
        
    public static void main(String[] args) {
         writeFile();
    }
  
    private static void writeFile(){
        //readListTDSP();
        try{
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\Nodes_2.csv"));

            int nodeNum = Integer.parseInt(br.readLine().split(",")[0]);
            double[][] nodes = new double[nodeNum][2];
            String line;
            int nodeID = 0;
            while( (line = br.readLine()) != null){
                String[] coordinates = line.split(",");
                nodes[nodeID][0] = Double.parseDouble( coordinates[1]);
                nodes[nodeID][1] = Double.parseDouble( coordinates[2] );
                nodeID++;
            }
            br.close();

            /*br = new BufferedReader(new FileReader("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\Links_all_2.csv"));
            int linkNum = Integer.parseInt(br.readLine().split(",")[0]);
            Map<Integer, double[]> midPoints = new HashMap<Integer, double[]>();

            while( (line = br.readLine()) != null){
                String[] coordinates = line.split(",");
                int link_key = Integer.parseInt(coordinates[2].substring(1))*100 + Integer.parseInt(coordinates[3].substring(1));
                double[] ords = new double[2];
                ords[0] = Double.parseDouble( coordinates[4].substring( coordinates[4].indexOf('(')+1 ) );
                ords[1] = Double.parseDouble( coordinates[5].substring( coordinates[5].indexOf('-'), coordinates[5].indexOf(')') ) );
                System.out.println("link "+link_key+": "+ords[0]+", "+ords[1]);
                midPoints.put(link_key, ords);
            }
            br.close();
            System.out.println("midPoints size:"+midPoints.size());*/

            FileOutputStream fos = new FileOutputStream("C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\TDSPData.obj");
            ObjectOutputStream oos = new ObjectOutputStream(fos);

           //write GraphTDSP
            oos.writeInt(nodeNum);
            oos.writeInt(nodeNum);
            oos.writeInt(60);
         
            //write nodes
            oos.writeInt(nodes.length);
            for(int i=0; i<nodes.length; i++){
                oos.writeDouble(nodes[i][0]);
                oos.writeDouble(nodes[i][1]);
            }

           /* //write midPoints
            oos.writeInt(midPoints.size());
            for(Map.Entry<Integer, double[]> entry : midPoints.entrySet()){
                oos.writeInt(entry.getKey());
                oos.writeDouble(entry.getValue()[0]);
                oos.writeDouble(entry.getValue()[1]);
            }*/
            oos.close();
            fos.close();
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.exit(-1);
        }
   }
        
    /*private static void readListTDSP() {

    	System.out.println("Read List Called");
	     String inFile = "C:\\Users\\Shireesh\\Desktop\\IMSC DR\\My Work\\March Work\\AdjList.txt";
	        try {
	            RandomAccessFile file = new RandomAccessFile(inFile, "rw");
	            String tmp = file.readLine(), temp, temp2;
	            int i = 5572;

	            file.seek(0);
	            tmp = file.readLine();
	            System.out.println("Initializing Graph");
	            graphTDSP = new double[i][i][60];
	            System.out.println("Graph Initialized");
	            for(int i1=0;i1<i;i1++)
	            	for(int j1=0;j1<i;j1++)
	            		for(int k1=0;k1<60;k1++)
	            			graphTDSP[i1][j1][k1] = -2.0;
	            
	            tdsp_new = new int[i][i];
	            numNodes = i;
	            
	            i = 0;
	            while (tmp != null) {
	            	//System.out.println(i);
	            	
	            	if(!(tmp.equals("NA")))
	            		{
	            	
	            		StringTokenizer sT = new StringTokenizer(tmp, ";");
	            		
		                int j = 0, k=0;
		                while (sT.hasMoreTokens()) {
		                	temp = sT.nextToken(); 
		                	//System.out.println(temp);
		                	j = Integer.parseInt(temp.substring(1, temp.indexOf("(")));
		                	//System.out.println(j);
		                    String type = temp.substring(temp.indexOf("(")+1, temp.indexOf(")"));
		                    //System.out.println(type);
		                    if(type.equals("V"))
		                    {
		                    	k = 0;
			                    
			                    StringTokenizer sT2 = new StringTokenizer(temp, ",");
			                    while(sT2.hasMoreTokens()) {
			                        temp2 = sT2.nextToken();
			                        if(temp2.indexOf(":")!= -1)
			                        graphTDSP[i][j][k++] = Double.parseDouble(temp2.substring(temp2.indexOf(":")+1));
			                        else
			                        graphTDSP[i][j][k++] = Double.parseDouble(temp2);
			                    }
		                    }
		                    else
		                    {
		                    	for(k=0;k<60;k++)
		                    		graphTDSP[i][j][k] = Double.parseDouble(temp.substring(temp.indexOf(":")+1));
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
	    }*/

}