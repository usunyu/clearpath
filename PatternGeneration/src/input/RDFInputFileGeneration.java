package input;

import java.io.*;
import java.sql.*;
import java.util.*;

import objects.*;

public class RDFInputFileGeneration {

	/**
	 * @param file
	 */
	static String root = "file";
	// for write link file
	static String linkFileCA = "RDF_Link.txt";
	// for write node file
	static String nodeFileCA = "RDF_Node.txt";
	/**
	 * @param database
	 */
	static String urlHome = "jdbc:oracle:thin:@gd2.usc.edu:1521/navteq";
	static String userName = "NAVTEQRDF";
	static String password = "NAVTEQRDF";
	static Connection connHome = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}




