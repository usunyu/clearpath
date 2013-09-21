package Objects;

import java.util.HashMap;

/**
 * This class stores the NodeConnectionValues information and its getters/ setters.
 * This class stores for 1 node , the time of travel for the other nodes its connected to
 * So its a data structure for storing Adj Lists in memory
 * @see NodeValues
 */
public class NodeConnectionValues {
public NodeConnectionValues(HashMap<Integer, NodeValues> nodes) {
		super();
		this.nodes = new HashMap<Integer,NodeValues>();
		this.nodes = nodes;
	}

public NodeConnectionValues() {
	nodes = new HashMap<Integer,NodeValues>();
}

public HashMap<Integer,NodeValues> nodes = new HashMap<Integer,NodeValues>();

public HashMap<Integer, NodeValues> getNodes() {
	return nodes;
}

public void setNodes(HashMap<Integer, NodeValues> nodes) {
	this.nodes = nodes;
}

@Override
public String toString() {
	return "NodeConnectionValues [nodes=" + nodes + "]";
}

}
