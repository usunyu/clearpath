package global;

import java.io.*;

final public class OSMResLoader {
	public static InputStream load(String path) {
		InputStream input = OSMResLoader.class.getResourceAsStream(path);
		if(input == null) {
			input = OSMResLoader.class.getResourceAsStream(OSMParam.SEGMENT + path);
		}
		return input;
	}
}
