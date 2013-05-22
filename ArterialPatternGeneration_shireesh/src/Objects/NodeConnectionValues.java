package Objects;

import java.util.HashMap;

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
