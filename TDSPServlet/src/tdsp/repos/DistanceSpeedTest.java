package tdsp.repos;

import Objects.DistanceCalculator;
import Objects.PairInfo;

public class DistanceSpeedTest {

	public static void main(String args[]){
		double distance = DistanceCalculator.CalculationByDistance(new PairInfo(34.22501,-119.25652), new PairInfo(34.07753,-117.60368));
		double speed = 40.0;
		double time = distance/speed;
		System.out.println(distance+" "+time*3600);
	}
}
