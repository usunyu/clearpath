package adj_lists;

import java.io.*;
import java.util.Formatter;
import java.util.Scanner;
import java.util.StringTokenizer;

public class EdgesToList {

	/**
	 * @param args
	 */
	
	public static double eucledian(double s_x, double s_y, double d_x, double d_y) {	//the eucledian distance in meters according to haversine formula
		// TODO Auto-generated method stub
		int R=6371;
		double lat1 = Math.toRadians(s_x);
		double lat2 = Math.toRadians(d_x);
		double dLat = Math.toRadians(d_x-s_x);
		double dLon = Math.toRadians(d_y-s_y);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		
		
		return d*1000;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
//		eucledian(22.337272, 114.267726, 22.336622, 114.268399);
		/*String inputName = "GoogleData2.txt";
		String outputName = "GoogleData_Monday.txt";
		try{
			RandomAccessFile file = new RandomAccessFile(inputName, "rw");
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputName,true));
			String temp;
            while (	(temp=file.readLine()) != null ) {
            	if (temp.equals("")){
            		writer.write("NA\n");
            	}
            	else{
            		writer.write(temp);
            		writer.write("\n");
            	}
            }
            file.close();
            writer.close();
            System.out.println("finished");
		}
		catch(IOException io){
			 System.err.println(io.toString());
		     System.exit(1);
		}*/
				
		
		//OUTPUT AN STATIC GOOGLE DATA FILE
		
		String inFile = "Edges.csv";
		String googleString = "GoogleData2.txt";
		
		 try {
	        	RandomAccessFile file = new RandomAccessFile(inFile, "rw");
	        	
	        	File googleFile = new File(googleString);
	        	File outputTemp = new File ("temp.txt");
	        	File security=new File("security.txt");
	        	
	            String temp;
	            System.out.println("Building file. Please wait...");
	            int i = 0;
	            int soFar=-1;
	            
	            while (	(temp=file.readLine()) != null ) {
	            	
		        	

		        	
		        	BufferedWriter writer = new BufferedWriter(new FileWriter(googleFile,true));
	            	i++;
	            	System.out.println("completed "+i*100/183945+"%");
	            	
	            	
	                StringTokenizer sT = new StringTokenizer(temp, ",");
	                
	                int source, target, type, weight = 0;
	                double s_x, s_y, d_x, d_y;
	                double distance,speed=0;
	                
	                sT.nextToken();
	                type=Integer.parseInt(sT.nextToken());
	                sT.nextToken();
	                source=Integer.parseInt(sT.nextToken().substring(1));
	                target=Integer.parseInt(sT.nextToken().substring(1));
	                	                
	                s_x=Double.parseDouble(sT.nextToken());
	                s_y=Double.parseDouble(sT.nextToken());
	                
	                d_x=Double.parseDouble(sT.nextToken());
	                d_y=Double.parseDouble(sT.nextToken());
	                
	                distance=eucledian(s_x,s_y,d_x,d_y);
	                switch (type) {
	                	case 1:speed=31.2928;
	                	case 2:speed=24.5872;
	                	case 3:speed=15.6464;
	                	case 4:speed=13.4112;
	                	
	                }
	                
	                weight=(int)Math.floor(distance*1000/speed);
	                
	                
	                String tmpStr="n"+target+":"+weight+";";	                
	                               	
                	if (source>=(soFar+1)){
                		for (int ii=0;ii<(source-soFar-1);ii++)
                    		writer.write("\n");
                		writer.write(tmpStr);
                		writer.write("\n");
                		writer.close();
                		soFar=source;
                	}
                	else if (source<(soFar+1)) {
                		writer.close();
                		BufferedReader reader = new BufferedReader(new FileReader(googleFile));
                		BufferedWriter writerTemp = new BufferedWriter(new FileWriter(outputTemp));
                		int counter=0;
                		String currentLine;
                		while((currentLine = reader.readLine()) != null) {
                			if (counter==source){
                				currentLine=currentLine.concat(tmpStr);

                			}
//                			System.out.println(currentLine.trim());
                			
                			writerTemp.write(currentLine);
                			writerTemp.write("\n");
                			counter++;
                		}
                		reader.close();
                		writerTemp.close();

                		googleFile.delete();

                		try { 
                			while (!outputTemp.renameTo(googleFile)){
                				
                			}
                		}
                		catch (SecurityException io){

                		}
                		
                	}
	           
	                
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
