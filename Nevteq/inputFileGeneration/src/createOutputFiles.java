import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.awt.geom.*;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

public class createOutputFiles {
	public static void main(String[] args) {
		try {

			// System.out.println(args[0]);

			System.out.println("4 steps total");
			System.out.println("\nstage 1 begins");
			GenerateEdgesFile s1 = new GenerateEdgesFile();
			s1.run(args);
			System.out.println("\nstage 2 begins");
			CreateHighwayGridFile s2 = new CreateHighwayGridFile();
			s2.run(args);
			System.out.println("\nstage 3 begins");
			CreateCSVForArterials1to5 s3 = new CreateCSVForArterials1to5();
			s3.run(args);
			System.out.println("\nstage 4 begins");
			CreateCSVForHighwaysFinal1to5 s4 = new CreateCSVForHighwaysFinal1to5();
			s4.run(args);
			System.out.println("\n all finished!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
