package map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class GenerateKMLMap {

	/**
	 * @param file
	 */
	static String root = "file";
	static String osmFile = "map.osm";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readOsmFile();
	}

	public static void readOsmFile() {
		System.out.println("read osm file...");
		try {
			File file = new File(root + "/" + osmFile);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("node");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
		 
					System.out.println("id : " + eElement.getAttribute("id")
							+ ", lat : " + eElement.getAttribute("lat")
							+ ", lon : " + eElement.getAttribute("lon"));
		 
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("read osm file finish!");
	}
}
