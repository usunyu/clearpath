package Objects;

import java.util.Arrays;
/**
 * This class stores the NodeValues information and its getters/ setters.
 * This class stores for 1 node , the time of travel for the 60 time intervals.
 * @see NodeConnectionValues
 */
public class NodeValues {

	public NodeValues(int node, int[] values) {
		super();
		this.node = node;
		this.values = values;
	}
	int node;
	int [] values = new int[60];
	public int getNode() {
		return node;
	}
	public void setNode(int node) {
		this.node = node;
	}
	public int[] getValues() {
		return values;
	}
	public void setValues(int[] values) {
		this.values = values;
	}
	@Override
	public String toString() {
		return "NodeValues [node=" + node + ", values=" + Arrays.toString(values) + "]";
	}
}
