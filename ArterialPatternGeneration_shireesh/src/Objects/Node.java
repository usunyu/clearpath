package Objects;

import java.util.Vector;

public class Node {
	 private int nodeId, cost, arrTime;
     private Vector dist; // denoted by 'c' in the algorithm

	  public Node(int id, int c, int t) {
         nodeId = id;
         cost = c;
         arrTime = t;
         dist = new Vector(10, 5);
     }
     public int getNodeId() {
         return nodeId;
     }
     public int getNodeCost() {
         return cost;
     }
     public int getArrTime() {
         return arrTime;
     }
     public void setArrTime(int t) {
         arrTime = t;
     }
	@Override
	public String toString() {
		return "Node [nodeId=" + nodeId + ", cost=" + cost + ", arrTime="
				+ arrTime + ", dist=" + dist + "]";
	}

    public void setNodeCost(int i) {
        cost = i;
    }

}
