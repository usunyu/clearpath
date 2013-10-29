package main;

import global.OSMParam;

import java.io.*;

import controller.*;
import osm2wkt.*;

public class OSMMain {

	/**
	 * @param
	 * put your *.osm file name here (not include suffix)
	 * the *.osm file should be in you file folder
	 */
	public static String osm = "los_angeles";
	
	public static void checkFileExist(String file) {
		String path = OSMParam.root + OSMParam.SEGMENT + file + ".osm";
		File f = new File(path);
		
		if(!f.exists()) {
			System.err.println("Can not find " + path + ", program exit!");
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		try {
			if(args.length > 0) {
				osm = args[0];
			}
			else {
				args = new String[1];
				args[0] = osm;
			}
			checkFileExist(osm);
			System.out.println("Step1: osm2wkt...\n");
			Osm2Wkt.run(args);
			System.out.println("Step2: input file generation...\n");
			OSMInputFileGeneration.run(args);
			System.out.println("Step3: output file generation...\n");
			OSMOutputFileGeneration.run(args);
			System.out.println("Step4(optional): generate kml...\n");
			OSMGenerateKML.run(args);
			System.out.println("Step5: divide way to edge...\n");
			OSMDivideWayToEdge.run(args);
			System.out.println("Step6: generate adj list...\n");
			OSMGenerateAdjList.run(args);
			System.out.println("All done!\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
