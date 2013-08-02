package objects;

import java.util.ArrayList;
import java.util.Arrays;

public class LinkInfo {
	public LinkInfo(String index2, int func_class, String start_node,
			String end_node, PairInfo[] nodes, int numPairs) {
		super();
		sensors = new ArrayList<Integer>();
		LinkId = index2;
		this.func_class = func_class;
		this.start_node = start_node;
		this.end_node = end_node;
		Nodes = nodes;
		this.numPairs = numPairs;
	}

	public LinkInfo(String linkId, int func_class, PairInfo[] nodes,
			int numPairs) {
		super();
		sensors = new ArrayList<Integer>();
		LinkId = linkId;
		this.func_class = func_class;
		Nodes = nodes;
		this.numPairs = numPairs;
	}

	public LinkInfo(String linkId, PairInfo[] nodes) {
		super();
		sensors = new ArrayList<Integer>();
		LinkId = linkId;
		Nodes = nodes;
	}

	public LinkInfo(String linkId, int funcClass, String st_name2,
			String st_node, String end_node, PairInfo[] nodes, int count) {
		super();
		sensors = new ArrayList<Integer>();
		LinkId = linkId;
		this.func_class = funcClass;
		this.st_name = st_name2;
		this.start_node = st_node;
		this.end_node = end_node;
		Nodes = nodes;
		this.numPairs = count;
	}
	
	public LinkInfo(String linkIndexId, String link_id, int func_class, String st_name, String start_node,
			String end_node, PairInfo[] nodes, int numPairs) {
		super();
		sensors = new ArrayList<Integer>();
		LinkId = linkIndexId;
		this.linkIdStr = link_id;
		this.func_class = func_class;
		this.st_name = st_name;
		this.start_node = start_node;
		this.end_node = end_node;
		Nodes = nodes;
		this.numPairs = numPairs;
	}

	/* Yu Sun Add Start */
	String id;
	int linkId;
	int startNode;
	int endNode;
	ArrayList<PairInfo> nodeList;
	SensorInfo sensor;
	String linkIdStr;
	String dirTravel;
	int speedCat;
	int direction;
	String allDir;
	ArrayList<SensorInfo> sensorList;
	
	public LinkInfo(String id, int linkId, int func_class, String st_name, int startNode, 
			int endNode, ArrayList<PairInfo> nodeList, String dirTravel, int speedCat, int direction) {
		super();
		this.id = id;
		this.linkId = linkId;
		this.func_class = func_class;
		this.st_name = st_name;
		this.startNode = startNode;
		this.endNode = endNode;
		this.nodeList = nodeList;
		this.dirTravel = dirTravel;
		this.speedCat = speedCat;
		this.direction = direction;
		sensorList = new ArrayList<SensorInfo>();
	}

	public LinkInfo(int linkId, int func_class, String st_name, int startNode, 
			int endNode, ArrayList<PairInfo> nodeList, String dirTravel, int speedCat, String allDir) {
		super();
		this.linkId = linkId;
		this.func_class = func_class;
		this.st_name = st_name;
		this.startNode = startNode;
		this.endNode = endNode;
		this.nodeList = nodeList;
		this.dirTravel = dirTravel;
		this.speedCat = speedCat;
		this.allDir = allDir;
		sensorList = new ArrayList<SensorInfo>();
	}

	public String getAllDir() {
		return allDir;
	}

	
	public String getId() {
		return id;
	}
	
	public int getIntLinkId() {
		return linkId;
	}
	
	public String getStrLinkId() {
		return linkIdStr;
	}
	
	public boolean containSensor(SensorInfo sensor) {
		for(int i = 0; i < sensorList.size(); i++) {
			if(sensorList.get(i).getSensorId() == sensor.getSensorId())
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
	
	public int getDirection() {
		return direction;
	}

	public String getDirTravel() {
		return dirTravel;
	}
	
	/* Yu Sun Add End */
	
	String LinkId;
	int func_class;
	String start_node;
	String end_node;
	String st_name;
	
	PairInfo Nodes[];
	int numPairs;
	public ArrayList<Integer> sensors;
	int closestSensor;
	
	public void setClosestSensor(int sensor) {
		closestSensor = sensor;
	}
	
	public int getClosestSensor() {
		return closestSensor;
	}

	public String getLinkId() {
		return LinkId;
	}

	public PairInfo[] getNodes() {
		return Nodes;
	}

	public void setNodes(PairInfo[] nodes) {
		Nodes = nodes;
	}

	public int getFunc_class() {
		return func_class;
	}

	public void setFunc_class(int func_class) {
		this.func_class = func_class;
	}

	public int getPairCount() {
		return numPairs;
	}

	public String getStart_node() {
		return start_node;
	}

	public void setStart_node(String start_node) {
		this.start_node = start_node;
	}

	public String getEnd_node() {
		return end_node;
	}

	public void setEnd_node(String end_node) {
		this.end_node = end_node;
	}

	public int getNumPairs() {
		return numPairs;
	}

	public void setNumPairs(int numPairs) {
		this.numPairs = numPairs;
	}

	@Override
	public String toString() {
		return "LinkInfo [LinkId=" + LinkId + ", func_class=" + func_class
				+ ", start_node=" + start_node + ", end_node=" + end_node
				+ ", numPairs=" + numPairs + ", sensors=" + sensors
				+ ", Nodes=" + Arrays.toString(Nodes) + "]";
	}

	public String getSt_name() {
		return st_name;
	}

	public void setSt_name(String st_name) {
		this.st_name = st_name;
	}

	public ArrayList<Integer> getSensors() {
		return sensors;
	}

	public void setSensors(ArrayList<Integer> sensors) {
		this.sensors = sensors;
	}

	public void setLinkId(String linkId) {
		LinkId = linkId;
	}

}
