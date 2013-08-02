package insertDatabase;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;

import Objects.UtilClass;

public class InsertDatabase {

	/**
	 * @param args
	 */
	// file
	static String root = "file";
	static String averageSpeedCleanFile = "Average_Speed_List_Clean.txt";
	// database
	static String urlHome = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
	// static String urlHome = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
	static String userName = "clearp";
	static String password = "clearp";
	static Connection connHome = null;
	// data struct
	static HashMap<Integer, double[]> sensorSpeedPattern = new HashMap<Integer, double[]>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	private static void readAverageFile() {
		System.out.println("read average file...");
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + "Monday_" + averageSpeedCleanFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] nodes = strLine.split(";");
				int sensorId = Integer.parseInt(nodes[0]);
				double speed = Double.parseDouble(nodes[1]);
				String time = nodes[2];

				if (sensorSpeedPattern.containsKey(sensorId)) {
					double[] tempArray = sensorSpeedPattern.get(sensorId);
					int index = UtilClass.getIndex(time);
					if (index >= 0 && index <= 59)
						tempArray[index] = speed;
				} else {
					double[] newArray = new double[60];
					int index = UtilClass.getIndex(time);
					if (index >= 0 && index <= 59)
						newArray[index] = speed;
					sensorSpeedPattern.put(sensorId, newArray);
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("read average file finish!");
	}

}
