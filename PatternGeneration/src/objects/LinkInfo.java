package objects;

import java.util.ArrayList;
import java.util.Arrays;

public class LinkInfo {
	public ArrayList<Integer> sensors;
	String linkIndexId;
	PairInfo nodes[];
	int numPairs;

	public LinkInfo(String linkIndexId, int linkId, int funcClass,
			String streetName, int startNode, int endNode,
			PairInfo[] nodes, int numPairs) {
		super();
		sensors = new ArrayList<Integer>();
		this.linkIndexId = linkIndexId;
		this.linkId = linkId;
		this.funcClass = funcClass;
		this.streetName = streetName;
		this.startNode = startNode;
		this.endNode = endNode;
		this.nodes = nodes;
		this.numPairs = numPairs;
	}
	
	public PairInfo[] getNodes() {
		return nodes;
	}
	
	public String getLinkIndexId() {
		return linkIndexId;
	}

	int linkId;
	int startNode;
	int endNode;
	ArrayList<PairInfo> nodeList;
	SensorInfo sensor;
	String dirTravel;
	int funcClass;
	String streetName;
	int speedCat;
	String allDir;
	ArrayList<SensorInfo> sensorList;

	public LinkInfo(int linkId, int funcClass, String streetName,
			int startNode, int endNode, ArrayList<PairInfo> nodeList,
			String dirTravel, int speedCat, String allDir) {
		super();
		this.linkId = linkId;
		this.funcClass = funcClass;
		this.streetName = streetName;
		this.startNode = startNode;
		this.endNode = endNode;
		this.nodeList = nodeList;
		this.dirTravel = dirTravel;
		this.speedCat = speedCat;
		this.allDir = allDir;
		sensorList = new ArrayList<SensorInfo>();
	}

	public int getFuncClass() {
		return funcClass;
	}

	public String getStreetName() {
		return streetName;
	}

	public String getAllDir() {
		return allDir;
	}

	public int getLinkId() {
		return linkId;
	}

	public boolean containSensor(SensorInfo sensor) {
		for (int i = 0; i < sensorList.size(); i++) {
			if (sensorList.get(i).getSensorId() == sensor.getSensorId())
				return true;
		}
		return false;
	}

	public void addSensor(SensorInfo sensor) {
		sensorList.add(sensor);
	}

	public ArrayList<SensorInfo> getSensorList() {
		return sensorList;
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

	public String getDirTravel() {
		return dirTravel;
	}
}
