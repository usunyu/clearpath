package objects;

public class PatternPairInfo {

	/**
	 * @param args
	 */
	String PatternPairId;
	int Node1;
	int Node2;
	int Intervals[];
	
	public PatternPairInfo(int node1, int node2, int intervals[]) {
		Node1 = node1;
		Node2 = node2;
		Intervals = intervals;
		PatternPairId = "n" + node1 + "n" + node2;
	}
	
	public int getStartNode() {
		return Node1;
	}
	
	public int getEndNode() {
		return Node2;
	}
	
	public int[] getIntervals() {
		return Intervals;
	}
}
