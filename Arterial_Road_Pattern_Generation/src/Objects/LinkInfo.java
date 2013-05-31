package Objects;

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
		this.link_id = link_id;
		this.func_class = func_class;
		this.st_name = st_name;
		this.start_node = start_node;
		this.end_node = end_node;
		Nodes = nodes;
		this.numPairs = numPairs;
	}
	
	public LinkInfo(String linkId, int funcClass, String st_name,
			String st_node, String end_node, ArrayList<PairInfo> nodeList) {
		super();
		sensors = new ArrayList<Integer>();
		LinkId = linkId;
		this.func_class = funcClass;
		this.st_name = st_name;
		this.start_node = st_node;
		this.end_node = end_node;
		this.nodeList = nodeList;
	}

	String LinkId;
	int func_class;
	String link_id;
	String start_node;
	String end_node;
	int numPairs;
	String st_name;
	public ArrayList<Integer> sensors;

	PairInfo Nodes[];
	ArrayList<PairInfo> nodeList;
	
	public String getPureLinkId() {
		return link_id;
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
