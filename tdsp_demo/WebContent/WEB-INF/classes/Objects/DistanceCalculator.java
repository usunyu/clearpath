package Objects;

/**
 * This class has 1 function which calculates the distance between a  
 * pair of coordinates using trignometry and another one which calculates
 * direction based on angle. 
 */
public class DistanceCalculator {
		  
		   static public double Radius;  
		   static public int NORTH = 0;
		   static public int SOUTH = 1;
		   static public int WEST = 3;
		   static public int EAST = 2;
		  
		   
		   /**
		    * Returns double distance in Miles. 
		    * It returns the distance based on Trignometry.
		    * @param  StartP  starting coordinate.
		    * @param  EndP    ending coordinate.
		    * @return      the distance in Miles.
		    */
		   public static double CalculationByDistance(PairInfo StartP, PairInfo EndP) {  
			  Radius = 6371* 0.621371192; 
		      double lat1 = StartP.getLati();  
		      double lat2 = EndP.getLati();  
		      double lon1 = StartP.getLongi();  
		      double lon2 = EndP.getLongi();  
		      double dLat = Math.toRadians(lat2-lat1);  
		      double dLon = Math.toRadians(lon2-lon1);  
		      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  
		         Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *  
		         Math.sin(dLon/2) * Math.sin(dLon/2);  
		      double c = 2 * Math.asin(Math.sqrt(a));  
		      return Radius * c;  
		   }  
		
		   public static void main(String args[])
		   {
			   System.out.println(CalculationByDistance(new PairInfo(34.28215, -118.74368), new PairInfo(34.28214, -118.73976)));
		   }

		   /**
		    * Returns integer direction of a Link. 
		    * @param  linkFound  link for which direction has to be found.
		    * @return      the direction of link.
		    */ 
		public static int getDirection(LinkInfo linkFound) {
			PairInfo [] pairs = linkFound.getNodes();
			PairInfo startNode = pairs[0];
			PairInfo endNode = pairs[linkFound.numPairs-1];
			double br= 0.0;
			br= Math.atan2(Math.sin(endNode.getLongi() - startNode.getLongi()) * Math.cos(endNode.getLati()), Math.cos(startNode.getLati()) * 
					Math.sin(endNode.getLati()) - Math.sin(startNode.getLati()) * Math.cos(endNode.getLati()) * Math.cos(endNode.getLongi() - startNode.getLongi())) % 
					(2 * Math.PI);

			//System.out.println(startNode+"->"+endNode+",Tan Angle="+br);
			if(br>=0.0 && br<1.0)
			{
			if(endNode.getLongi()>startNode.getLongi() || endNode.getLati()>startNode.getLati())	
				return EAST;
			else
				return WEST;
			}
			if(br>=1.0)
			{
				if(endNode.getLongi()>startNode.getLongi() || endNode.getLati()>startNode.getLati())	
					return NORTH;
				else
					return SOUTH;
				
			}
			if(br<-1.0)
			{
				if(endNode.getLongi()<startNode.getLongi() && endNode.getLati()>startNode.getLati())	
					return NORTH;
				else
					return SOUTH;
				
			}
			if(br>=-1.0 && br<0.0)
			{
				if(endNode.getLongi()<startNode.getLongi() && endNode.getLati()>startNode.getLati())	
					return WEST;
				else
					return EAST;
			}
			System.out.println(br);
			return 6;				
			
			
		}
		   
}
