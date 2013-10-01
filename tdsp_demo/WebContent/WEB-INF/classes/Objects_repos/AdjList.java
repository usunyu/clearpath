package Objects_repos;

public class AdjList {

	public AdjList(String st_node, String end_node, int time_of_day,
			int travel_time) {
		super();
		this.st_node = st_node;
		this.end_node = end_node;
		this.time_of_day = time_of_day;
		this.travel_time = travel_time;
	}
	String st_node;
	String end_node;
	int time_of_day;
	int travel_time;
	public String getSt_node() {
		return st_node;
	}
	public void setSt_node(String st_node) {
		this.st_node = st_node;
	}
	public String getEnd_node() {
		return end_node;
	}
	public void setEnd_node(String end_node) {
		this.end_node = end_node;
	}
	public int getTime_of_day() {
		return time_of_day;
	}
	public void setTime_of_day(int time_of_day) {
		this.time_of_day = time_of_day;
	}
	public int getTravel_time() {
		return travel_time;
	}
	public void setTravel_time(int travel_time) {
		this.travel_time = travel_time;
	}
	@Override
	public String toString() {
		return "AdjList [st_node=" + st_node + ", end_node=" + end_node
				+ ", time_of_day=" + time_of_day + ", travel_time="
				+ travel_time + "]";
	}
}
