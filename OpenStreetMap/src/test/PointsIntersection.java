package test;

import java.io.*;
import java.util.*;

import object.*;

public class PointsIntersection {

	/**
	 * @param args
	 */
	static LocationInfo localA = new LocationInfo(34.0350928, -118.3310597);
	
	static LocationInfo localB = new LocationInfo(34.0360852, -118.3212048);
	
	static LocationInfo localC = new LocationInfo(34.037, -118.3255);
	
	static LocationInfo localD = new LocationInfo(34.03, -118.3255);
	
	static LocationInfo localI;
	
	static Landmark landA;
	
	static Landmark landB;
	
	static Landmark landC;
	
	static Landmark landD;
	
	static Landmark landI;
	
	public static class Landmark {
		long id = 0;
		double latitude = 0;
		double longitude = 0;
		double x = 0;
		double y = 0;		

		public boolean equals(Object o) {
			if(this == o) return true;
			if (o == null) return false;
			if(!this.getClass().isInstance(o)) return false;
			Landmark ol = (Landmark)o;

			return (this.id == ol.id );
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		prepareLandmark();
		generateKML();
		computeIntersection();
		generateKMLNode();
	}
	
	private static void prepareLandmark() {
		landA = new Landmark();
		landA.latitude = localA.getLatitude();
		landA.longitude = localA.getLongitude();
		landB = new Landmark();
		landB.latitude = localB.getLatitude();
		landB.longitude = localB.getLongitude();
		landC = new Landmark();
		landC.latitude = localC.getLatitude();
		landC.longitude = localC.getLongitude();
		landD = new Landmark();
		landD.latitude = localD.getLatitude();
		landD.longitude = localD.getLongitude();
	}
	
	public static void generateKMLNode() {
		System.out.println("generate node kml...");
		try {
			FileWriter fstream = new FileWriter("file/test_intersect.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			String strLine = "<kml><Document>";
			strLine += "<Placemark>";
			strLine += "<name>Intersection</name>";
			strLine += "<description>";
			strLine += "Id:1";
			strLine += "</description>";
			strLine += "<Point><coordinates>";
			// strLine += localI.getLongitude() + "," + localI.getLatitude();
			strLine += landI.longitude + "," + landI.latitude;
			strLine += ",0</coordinates></Point>";
			strLine += "</Placemark>";
			strLine += "</Document></kml>";
			out.write(strLine);
			out.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("generate node kml finish!");
	}

	public static void computeIntersection() {
		System.out.println("compute intersection...");
		// localI = SimplePolylineIntersection(localA, localB, localC, localD);
		landI = SimplePolylineIntersection2(landA, landB, landC, landD);
		System.out.println("compute intersection finish!");
	}
	
	private static Landmark SimplePolylineIntersection2(Landmark latlong1,Landmark latlong2,Landmark latlong3,Landmark latlong4) {
	    //Line segment 1 (p1, p2)
	    double A1 = latlong2.latitude - latlong1.latitude;
	    double B1 = latlong1.longitude - latlong2.longitude;
	    double C1 = A1*latlong1.longitude + B1*latlong1.latitude;
	    
	    //Line segment 2 (p3,  p4)
	    double A2 = latlong4.latitude - latlong3.latitude;
	    double B2 = latlong3.longitude - latlong4.longitude;
	    double C2 = A2*latlong3.longitude + B2*latlong3.latitude;
	
	    double determinate = A1*B2 - A2*B1;
	
	    Landmark intersection;
	    if(determinate != 0)
	    {
	        double x = (B2*C1 - B1*C2)/determinate;
	        double y = (A1*C2 - A2*C1)/determinate;
	        
	        Landmark intersect = new Landmark();
	        intersect.latitude = y;
	        intersect.longitude = x;
	        
	        //if(inBoundedBox(latlong1, latlong2, intersect) && inBoundedBox(latlong3, latlong4, intersect))
	        	intersection = intersect;
	        //else
	        //	intersection = null;
	    }
	    else //lines are parrallel
	        intersection = null; 
	        
	    return intersection;
	}
	
	public static LocationInfo SimplePolylineIntersection(LocationInfo latlong1,LocationInfo latlong2,LocationInfo latlong3,LocationInfo latlong4) {
	    //Line segment 1 (p1, p2)
	    double A1 = latlong2.getLatitude() - latlong1.getLatitude();
	    double B1 = latlong1.getLongitude() - latlong2.getLongitude();
	    double C1 = A1*latlong1.getLongitude() + B1*latlong1.getLatitude();
	    
	    //Line segment 2 (p3,  p4)
	    double A2 = latlong4.getLatitude() - latlong3.getLatitude();
	    double B2 = latlong3.getLongitude() - latlong4.getLongitude();
	    double C2 = A2*latlong3.getLongitude() + B2*latlong3.getLatitude();
	
	    double determinate = A1*B2 - A2*B1;
	
	    LocationInfo intersection;
	    if(determinate != 0)
	    {
	        double x = (B2*C1 - B1*C2)/determinate;
	        double y = (A1*C2 - A2*C1)/determinate;
	        
	        LocationInfo intersect = new LocationInfo(y, x);
	        
	        //if(inBoundedBox(latlong1, latlong2, intersect) && inBoundedBox(latlong3, latlong4, intersect))
	        	intersection = intersect;
	        //else
	        //	intersection = null;
	    }
	    else //lines are parrallel
	        intersection = null; 
	        
	    return intersection;
	}

	//latlong1 and latlong2 represent two coordinates that make up the bounded box
	//latlong3 is a point that we are checking to see is inside the box
	public static boolean inBoundedBox(LocationInfo latlong1, LocationInfo latlong2, LocationInfo latlong3) {
	    boolean betweenLats;
	    boolean betweenLons;
	    
	    if(latlong1.getLatitude() < latlong2.getLatitude())
	        betweenLats = (latlong1.getLatitude() <= latlong3.getLatitude() && latlong2.getLatitude() >= latlong3.getLatitude());
	    else
	        betweenLats = (latlong1.getLatitude() >= latlong3.getLatitude() && latlong2.getLatitude() <= latlong3.getLatitude());
	        
	    if(latlong1.getLongitude() < latlong2.getLongitude())
	        betweenLons = (latlong1.getLongitude() <= latlong3.getLongitude() && latlong2.getLongitude() >= latlong3.getLongitude());
	    else
	        betweenLons = (latlong1.getLongitude() >= latlong3.getLongitude() && latlong2.getLongitude() <= latlong3.getLongitude());
	    
	    return (betweenLats && betweenLons);
	}
	
	public static void generateKML() {
		System.out.println("generate kml...");
		try {
			FileWriter fstream = new FileWriter("file/test_line.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			
			String kmlStr = "<Placemark><name>Way:1</name>";
			kmlStr += "<description>";
			kmlStr += "name:test way 1\r\n";
			kmlStr += "</description>";
			kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
			kmlStr += localA.getLongitude() + "," + localA.getLatitude() + ",0 ";
			kmlStr += localB.getLongitude() + "," + localB.getLatitude() + ",0 ";
			kmlStr += "</coordinates></LineString>";
			kmlStr += "<Style><LineStyle>";
			kmlStr += "<width>1</width>";
			kmlStr += "</LineStyle></Style></Placemark>\n";
			out.write(kmlStr);
			
			kmlStr = "<Placemark><name>Way:2</name>";
			kmlStr += "<description>";
			kmlStr += "name:test way 2\r\n";
			kmlStr += "</description>";
			kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
			kmlStr += localC.getLongitude() + "," + localC.getLatitude() + ",0 ";
			kmlStr += localD.getLongitude() + "," + localD.getLatitude() + ",0 ";
			kmlStr += "</coordinates></LineString>";
			kmlStr += "<Style><LineStyle>";
			kmlStr += "<width>1</width>";
			kmlStr += "</LineStyle></Style></Placemark>\n";
			out.write(kmlStr);

			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("generate kml finish!");
	}

}
