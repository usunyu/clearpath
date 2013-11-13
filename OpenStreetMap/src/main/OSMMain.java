package main;

import global.*;

import controller.*;
import model.*;
import osm2wkt.*;

public class OSMMain {

	/**
	 * @param
	 * put your *.osm file name here (not include suffix)
	 * the *.osm file should be in you file folder
	 */
	public static String osm = "los_angeles";
	
	public static void main(String[] args) {
		try {
			if(args.length > 0) {
				osm = args[0];
			}
			else {
				args = new String[1];
				args[0] = osm;
			}
			OSMInput.checkFileExist(OSMParam.root + OSMParam.SEGMENT + osm + ".osm");
			
			System.out.println("Step1: osm2wkt...");
			Osm2Wkt.run(args);
			System.out.println();
			System.out.println("Step2: input file generation...");
			OSMInputFileGeneration.run(args);
			System.out.println();
			System.out.println("Step3: output file generation...");
			OSMOutputFileGeneration.run(args);
			System.out.println();
			System.out.println("Step4: divide way to edge...");
			OSMDivideWayToEdge.run(args);
			System.out.println();
			System.out.println("Step5(optional): generate kml...");
			OSMGenerateKML.run(args);
			System.out.println();
			System.out.println("Step6: generate adj list...");
			OSMGenerateAdjList.run(args);
			System.out.println();
			System.out.println("All done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
