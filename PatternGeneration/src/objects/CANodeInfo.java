package objects;

public class CANodeInfo {
	int nodeId;
	int newNodeId;
	int networkId;
	String nodeType;
	int minLinkClass;
	String nodeName;
	PairInfo location;
	String sourceId1;
	String sourceRef1;

	public CANodeInfo(int nodeId, int newNodeId, int networkId,
			String nodeType, int minLinkClass, String nodeName,
			PairInfo location, String sourceId1, String sourceRef1) {
		this.nodeId = nodeId;
		this.newNodeId = newNodeId;
		this.networkId = networkId;
		this.nodeType = nodeType;
		this.minLinkClass = minLinkClass;
		this.nodeName = nodeName;
		this.location = location;
		this.sourceId1 = sourceId1;
		this.sourceRef1 = sourceRef1;
	}

	public CANodeInfo(int nodeId, int newNodeId, PairInfo location) {
		this.nodeId = nodeId;
		this.newNodeId = newNodeId;
		this.location = location;
	}

	/**
	 * Unique identifier
	 */
	public int getNodeId() {
		return nodeId;
	}

	/**
	 * New unique identifier
	 */
	public int getNewNodeId() {
		return newNodeId;
	}

	/**
	 * Network to which this node belongs
	 */
	public int getNetworkId() {
		return networkId;
	}

	/**
	 * Classifies the node, e.g. type of junction
	 */
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * Min Functional classification of the links through this node (largest road).
	 */
	public int getMinLinkClass() {
		return minLinkClass;
	}

	/**
	 * Descriptive node name
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * Latitude, Longitude in decimal degrees
	 */
	public PairInfo getLocation() {
		return location;
	}

	/**
	 * Source identifier of this node
	 */
	public String getSourceId1() {
		return sourceId1;
	}

	/**
	 * Reference for this source identifier (e.g. Navteq, Telenav, TMC)
	 */
	public String getSourceRef1() {
		return sourceRef1;
	}
}
