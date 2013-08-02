package objects;

import java.io.*;

public class inputConfig {
	String root;
	String jdbc;
	String user;
	String password;
	int ArterialNum;
	int HighwayNum;

	public inputConfig(String config) {
		try {
			FileInputStream fstream = new FileInputStream(config);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			root = br.readLine().split(">")[1];
			ArterialNum = Integer.parseInt(br.readLine().split(">")[1]);
			HighwayNum = Integer.parseInt(br.readLine().split(">")[1]);
			jdbc = br.readLine().split(">")[1];
			user = br.readLine().split(">")[1];
			password = br.readLine().split(">")[1];

			br.close();
			in.close();
			fstream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getRoot() {
		return root;
	}

	public int getArterialNum() {
		return ArterialNum;
	}

	public int getHighwayNum() {
		return HighwayNum;
	}

	public String getJdbc() {
		return jdbc;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

}
