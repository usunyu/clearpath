package highwayPattern;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.*;

public class CityArea {

	/**
	 * @param args
	 */
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data structure
	static ArrayList<String> areaList = new ArrayList<String>();
	static HashMap<String, ArrayList<PairInfo>> areaMap = new HashMap<String, ArrayList<PairInfo>>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getCity();
		generateAreaKML();
	}

	private static void generateAreaKML() {
		System.out.println("generate area kml...");
		try {
			FileWriter fstream = new FileWriter("City_Area.kml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<kml><Document>");
			for (int i = 0; i < areaList.size(); i++) {
				String area = areaList.get(i);
				ArrayList<PairInfo> nodeList = areaMap.get(area);
				String kmlStr = "<Placemark><name>Area:" + area + "</name>";
				kmlStr += "<description>" + area + "</description>";
				kmlStr += "<LineString><tessellate>1</tessellate><coordinates>";
				for (int j = 0; j < nodeList.size(); j++) {
					PairInfo node = nodeList.get(j);
					kmlStr += node.getLongi() + "," + node.getLati() + ",0 ";
				}
				kmlStr += "</coordinates></LineString></Placemark>\n";

				out.write(kmlStr);
			}
			out.write("</Document></kml>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("generate area kml finish!");
	}

	private static void getCity() {
		System.out.println("get city...");
		try {
			Connection con = null;
			String sql = null;
			PreparedStatement pstatement = null;
			ResultSet res = null;
			con = getConnection();
			// assume this is sensing the specified street
			sql = "SELECT polygon_nm, geom FROM navtech.adminbndy4 WHERE polygon_nm = 'SAN JOSE'";
			pstatement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = pstatement.executeQuery();
			while (res.next()) {
				String area = res.getString(1);
				areaList.add(area);
				STRUCT st = (STRUCT) res.getObject(2);
				JGeometry geom = JGeometry.load(st);
				double[] points = geom.getOrdinatesArray();
				ArrayList<PairInfo> nodeList = new ArrayList<PairInfo>();
				for (int i = 0; i < geom.getNumPoints(); i++) {
					double latitude = points[i * 2 + 1];
					double longitude = points[i * 2 + 0];
					PairInfo node = new PairInfo(latitude, longitude);
					nodeList.add(node);
				}
				// double lati = geom.getPoint()[1];
				// double lon = geom.getPoint()[0];
				// nodeList.add(new PairInfo(lati, lon));

				areaMap.put(area, nodeList);
			}
			res.close();
			pstatement.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("get city finish!");
	}

	private static Connection getConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			connHome = DriverManager.getConnection(urlHome, userName, password);
			return connHome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connHome;

	}

}
