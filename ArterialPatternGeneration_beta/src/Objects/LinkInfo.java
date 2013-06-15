package Objects;

import java.util.ArrayList;
import java.util.Arrays;

public class LinkInfo {
	// id = link_id + ref_id + nref_id
	// String indexId;
	long linkId;
	int funcClass;
	String streetName;
	int startNode;
	int endNode;
	ArrayList<PairInfo> nodeList;
	String dirTravel;
	int speedCat;
	int direction;

	SensorInfo sensor;

	public LinkInfo(long linkId, int funcClass, String streetName,
			int startNode, int endNode, ArrayList<PairInfo> nodeList,
			String dirTravel, int speedCat, int direction) {
		super();
		this.linkId = linkId;
		this.funcClass = funcClass;
		this.streetName = streetName;
		this.startNode = startNode;
		this.endNode = endNode;
		this.nodeList = nodeList;
		this.dirTravel = dirTravel;
		this.speedCat = speedCat;
		this.direction = direction;
	}

	// public String getIndexId() {
	// return indexId;
	//	}

	public long getIntLinkId() {
		return linkId;
	}

	public void setSensor(SensorInfo sensor) {
		this.sensor = sensor;
	}

	public SensorInfo getSensor() {
		return sensor;
	}

	public ArrayList<PairInfo> getNodeList() {
		return nodeList;
	}

	public int getStartNode() {
		return startNode;
	}

	public int getEndNode() {
		return endNode;
	}

	public int getSpeedCat() {
		return speedCat;
	}

	public int getDirection() {
		return direction;
	}

	public String getDirTravel() {
		return dirTravel;
	}

	public String getStreetName() {
		return streetName;
	}

	@Override
	public String toString() {
		String nodeListStr = "";
		for (int i = 0; i < nodeList.size(); i++) {

		}

		return "LinkInfo [LinkId=" + linkId + ", FuncClass=" + funcClass
				+ ", StartNode=" + startNode + ", EndNode=" + endNode
				+ ", NodeList=" + nodeListStr + ", DirTravel=" + dirTravel
				+ ", SpeedCat=" + speedCat + ", Direction=" + direction
				+ ", Sensor=" + sensor.toString() + "]";
	}
}
