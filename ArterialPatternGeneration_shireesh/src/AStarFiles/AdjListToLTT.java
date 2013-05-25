/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AStarFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author clearp
 */
public class AdjListToLTT {

	public static void main(String args[]) {
		FileInputStream fstream = null;
		FileWriter f = null;
		BufferedWriter out = null;
		try {
			f = new FileWriter("H:\\Network1234\\AdjListLTT.txt");
			out = new BufferedWriter(f);

			fstream = new FileInputStream("H:\\Network1234\\AdjList_All.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader file = new BufferedReader(new InputStreamReader(in));
			String tmp = file.readLine();
			while (tmp != null) {

				if (!(tmp.equals("NA"))) {
					String[] str = tmp.split(";");
					for (int i = 0; i < str.length; i++) {
						out.write(str[i].substring(0, str[i].indexOf("(")) + ":");
						out.write(min(str[i].substring(str[i].indexOf(":") + 1).split(",")));
						out.write(";");
					}
				} else
					out.write(tmp);

				out.write("\n");
				tmp = file.readLine();
			}

		} catch (IOException ex) {
			Logger.getLogger(AdjListToLTT.class.getName()).log(Level.SEVERE,
					null, ex);
		} finally {
			try {
				out.close();
				fstream.close();
			} catch (IOException ex) {
				Logger.getLogger(AdjListToLTT.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

	}

	public static String min(String[] val) {
		int min = 10000000;
		for (int i = 0; i < val.length; i++) {
			if (min > Integer.parseInt(val[i])) {
				min = Integer.parseInt(val[i]);
			}
		}
		return String.valueOf(min);
	}
}
