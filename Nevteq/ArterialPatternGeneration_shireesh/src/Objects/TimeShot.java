package Objects;

import java.util.Arrays;

public class TimeShot {
public TimeShot(String startNode, String endNode, int funcClass,
			double distance, double[] travelTime) {
		super();
		this.startNode = startNode;
		this.endNode = endNode;
		FuncClass = funcClass;
		this.distance = distance;
		TravelTime = travelTime;
	}

String startNode;
String endNode;
int FuncClass;
double distance;
double TravelTime [];

public String getStartNode() {
	return startNode;
}
public void setStartNode(String startNode) {
	this.startNode = startNode;
}
public String getEndNode() {
	return endNode;
}
public void setEndNode(String endNode) {
	this.endNode = endNode;
}
public double[] getTravelTime() {
	return TravelTime;
}
public void setTravelTime(double[] travelTime) {
	TravelTime = travelTime;
}
@Override
public String toString() {
	return "TimeShot [startNode=" + startNode + ", endNode=" + endNode
			+ ", FuncClass=" + FuncClass + ", distance=" + distance
			+ ", TravelTime=" + Arrays.toString(TravelTime) + "]";
}
public int getFuncClass() {
	return FuncClass;
}
public void setFuncClass(int funcClass) {
	FuncClass = funcClass;
}
public double getDistance() {
	return distance;
}
public void setDistance(double distance) {
	this.distance = distance;
}

}
