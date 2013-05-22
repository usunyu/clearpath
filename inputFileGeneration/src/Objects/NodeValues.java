package Objects;

import java.util.Arrays;

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
	return "NodeValues [node=" + node + ", values=" + Arrays.toString(values)
			+ "]";
}
}
