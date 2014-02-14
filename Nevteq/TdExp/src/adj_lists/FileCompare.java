package adj_lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

public class FileCompare {
	public static void main(String args[]){
		String dirpath = "queryresult\\monday\\onepercentsmoothed\\WholeResult\\googletime\\timediff\\";
		int groupNum = 10;
		int minoverlap, maxoverlap;
		float aveoverlap;
		int minoverhead, aveoverhead, maxoverhead;
		int actMinoverhead, actAveoverhead, actMaxoverhead;
		//System.out.println("read file: " + dirpath);
		//int mioAve, maoAve;
		//float aoAve;
		//int mihAve, mahAve, ahAve;
		int min_count;
		int max_count;
		int total_count;
		int avePercent, actPercent;
		int sum_1, sum_2, sum_3, sum_4, sum_5, sum_6, sum_7, sum_8, sum_9, sum_10, sum_11;
		try{
			for (int group = 1; group < groupNum; group++){
				String fileName = dirpath + "smooth_ana_q_" + group + ".txt";
				System.out.println("read file: " + fileName);
				RandomAccessFile anafile = new RandomAccessFile(fileName, "r");
				String temp;
				String elements[] = new String[14];
				int line = 0;
				sum_1 = sum_2 = sum_3 = sum_4 = sum_5 = sum_6 = sum_7 = sum_8 = sum_9 = sum_10 = sum_11 = 0;
				min_count = Integer.MAX_VALUE;
				max_count = 0;
				total_count = 0;
				while ((temp=anafile.readLine()) != null ) {
					StringTokenizer sT = new StringTokenizer(temp, "\t");
					int k = 0;
					while (sT.hasMoreTokens()){
						elements[k] = sT.nextToken();
						//System.out.println(elements[k]);
						k++;
					}
					int src = Integer.parseInt(elements[0]);
					int trg = Integer.parseInt(elements[1]);
					int counter = Integer.parseInt(elements[2]);
					minoverlap = Integer.parseInt(elements[3]);
					aveoverlap = Float.parseFloat(elements[4]);
					maxoverlap = Integer.parseInt(elements[5]);
					
					avePercent = Integer.parseInt(elements[6]);
					
					minoverhead = Integer.parseInt(elements[7]);
					aveoverhead = Integer.parseInt(elements[8]);
					maxoverhead = Integer.parseInt(elements[9]);
					
					actPercent = Integer.parseInt(elements[10]);
					actMinoverhead = Integer.parseInt(elements[11]);
					actAveoverhead = Integer.parseInt(elements[12]);
					actMaxoverhead = Integer.parseInt(elements[13]);
					
					
					sum_1 += minoverlap;
					sum_2 += (int)aveoverlap;
					sum_3 += maxoverlap;
					sum_4 += minoverhead;
					sum_5 += aveoverhead;
					sum_6 += maxoverhead;
					sum_7 += actMinoverhead;
					sum_8 += actAveoverhead;
					sum_9 += actMaxoverhead;
					sum_10 += avePercent;
					sum_11 += actPercent;
					
					//System.out.println(src + " " + trg + " " + counter + " " + minoverlap + " " + aveoverlap + " " + maxoverlap + " " +minoverhead + " " +aveoverhead + " " +maxoverhead );
					
					if (counter > max_count)
						max_count = counter;
					if (counter < min_count)
						min_count = counter;
					total_count += counter;
					line++;
									
				}
				int Range = 60;
				System.out.println(min_count*100/Range + "\t" + total_count*100/(line*Range) + "\t" + max_count *100 / Range + "\t" + sum_1/line + "\t " + sum_2/line + "\t " + sum_3/line + "\t " + sum_4/line + "\t " + sum_5/line + "\t " 
				+ sum_6/line + "\t " + sum_7/line  + "\t " + sum_8/line +  "\t " + sum_9/line  + "\t " + sum_10/line +   "\t " + sum_11/line );
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//get the cirtical points and no of paths
		
		
		
		
		
		
		
		
		/*String fn1 = "pairs2\\qu1.txt";
		String fn2 = "pairs2\\qu2.txt";
	
		try {
			FileWriter stream = new FileWriter("pairs2\\compare.txt");
			BufferedWriter out = new BufferedWriter(stream);
			RandomAccessFile f1 = new RandomAccessFile(fn1, "rw");
			RandomAccessFile f2 = new RandomAccessFile(fn2, "rw");
			//String temp1 = f1.readLine();
			//String temp2 = f2.readLine();
			f1.seek(0);
			f2.seek(0);
			String temp1, temp2;
			int i = 0;
			while ((temp1 = f1.readLine()) != null &&(temp2 = f2.readLine()) != null){
				//System.out.println("Line: " + i + " : " + temp1 +  " " + temp2);
				i++;
				if (temp1.equals(temp2) == true)
					continue;
				else
					out.write("Line: " + i + " Not the same " + temp1 +  " " + temp2 + "\n");
					//System.out.println("Line: " + i + " Not the same " + temp1 +  " " + temp2);
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
}
