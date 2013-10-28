package main;

import process.*;

public class RDFMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Step1: RDFInputFileGeneration...\n");
		RDFInputFileGeneration.main(args);
		
		System.out.println("Step2: RDFMatchSensorLink...\n");
		RDFMatchSensorLink.main(args);
		
		System.out.println("Step3(Optional): RDFOutputKMLGeneration...\n");
		RDFOutputKMLGeneration.main(args);
		
		System.out.println("Step4: RDFAdjListPattern...\n");
		RDFAdjListPattern.main(args);
	}

}
