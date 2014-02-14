package controller;

import global.*;
import model.*;
import osm2wkt.*;

public class OSMGenerateAll {

	public static void main(String[] args) {
		try {
			OSMParam.paramConfig();
			OSMInput.checkFileExist(OSMParam.root + OSMParam.SEGMENT + OSMParam.name + ".osm");
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
