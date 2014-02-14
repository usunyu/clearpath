package adj_lists;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BuildStaticFile {

	/**
	 * @author Ugur
	 * Used Once, to generate the static Dataset from the dynamic one. For each edge it computes the average weight
	 * and prints it to the file StaticData.txt.
	 *
	 * DataStaticGenerator.java will use this file as input
	 *
	 */

	public static void main(String[] args) {
		String inFile = "AdjList_Sunday.txt";
		String outFile = "StaticData_Sunday.txt";

		Pair<Integer,int []> pair;


        try {
        	PrintWriter out = new PrintWriter(new FileWriter(outFile));
        	RandomAccessFile file = new RandomAccessFile(inFile, "rw");

            String temp;

            System.out.println("Building file. Please wait...");
            int i = 0;
            StringBuilder line=null;
            while (	(temp=file.readLine()) != null ) {
            	line= new StringBuilder();
            	i++;
            	if (i==46702){
            		System.out.print("a");
            	}
            	System.out.println("completed "+i*100/111533+"%");
            	if (temp.equals("NA")){
            		out.write("NA\n");
            		continue;
            	}

                StringTokenizer sT = new StringTokenizer(temp, ";");
                int target, weight;

                while (sT.hasMoreTokens()) {

                	weight=0;
                	temp = sT.nextToken();
                    target=Integer.parseInt(temp.substring(temp.indexOf('n')+1, temp.indexOf('(')));
                    line.append("n"+target+":");

                    temp=temp.substring(temp.indexOf(':')+1);
                    StringTokenizer sT2 = new StringTokenizer(temp, ",");
                    while(sT2.hasMoreTokens()) {
                        String temp2 = sT2.nextToken();
                        weight+= Integer.parseInt(temp2);
                    }
                    line.append(weight/60+";");


                }
                line.append("\n");
                out.write(line.toString());
                out.flush();

            }

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
}
