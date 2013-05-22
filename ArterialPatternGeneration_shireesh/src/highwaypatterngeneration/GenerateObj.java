/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package highwaypatterngeneration;


import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.Map;


public class GenerateObj {
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
            BufferedReader br = new BufferedReader(new FileReader("H:\\Jiayunge\\Nodes.csv"));

            int nodeNum = 496821;
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
            System.out.println(nodeNum);
            FileOutputStream fos = new FileOutputStream("H:\\Jiayunge\\TDSPData.obj");
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

            oos.close();
            fos.close();
            System.out.println("finished!!");
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.exit(-1);
        }
   }
}
