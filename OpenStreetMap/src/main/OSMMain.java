package main;

import osm2wkt.*;
import process.*;

public class OSMMain {

	/**
	 * @param
	 * put your *.osm file name here (not include suffix)
	 * the *.osm file should be in you file folder
	 */
	public static String osm = "minnesota";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			args = new String[1];
			args[0] = osm;
			System.out.println("\nstep1: osm2wkt...");
			Osm2Wkt.run(args);
			System.out.println("\nstep2: input file generation...");
			OSMInputFileGeneration.run(args);
			System.out.println("\nstep3: output file generation...");
			OSMOutputFileGeneration.run(args);
			System.out.println("\nstep4(optional): generate kml...");
			OSMGenerateKML.run(args);
			System.out.println("\nstep5: divide way to edge...");
			OSMDivideWayToEdge.run(args);
			System.out.println("\nstep6: generate adj list...");
			OSMGenerateAdjList.run(args);
			System.out.println("\nall done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
