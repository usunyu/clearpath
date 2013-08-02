package Objects;

import java.util.HashMap;

public class NodeConnectionValueLTT {
public NodeConnectionValueLTT(HashMap<Integer, Integer> nodes) {
		super();
		this.nodes = new HashMap<Integer,Integer>();
		this.nodes = nodes;
	}

public NodeConnectionValueLTT() {
	nodes = new HashMap<Integer,Integer>();
}

public HashMap<Integer,Integer> nodes = new HashMap<Integer,Integer>();

public HashMap<Integer, Integer> getNodes() {
	return nodes;
}

public void setNodes(HashMap<Integer, Integer> nodes) {
	this.nodes = nodes;
}

@Override
public String toString() {
	return "NodeConnectionValues [nodes=" + nodes + "]";
}

}
