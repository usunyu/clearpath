package Objects;

/**
 * This class stores the Grid information and its getters/ setters
 */
public class GridInfo {

    public GridInfo(int gridId, PairInfo start, PairInfo end) {
		super();
		this.gridId = gridId;
		this.start = start;
		this.end = end;
	}
	int gridId;
    PairInfo start;
    PairInfo end;
	public int getGridId() {
		return gridId;
	}
	public void setGridId(int gridId) {
		this.gridId = gridId;
	}
	public PairInfo getStart() {
		return start;
	}
	public void setStart(PairInfo start) {
		this.start = start;
	}
	public PairInfo getEnd() {
		return end;
	}
	public void setEnd(PairInfo end) {
		this.end = end;
	}
	@Override
	public String toString() {
		return "GridInfo [gridId=" + gridId + ", start=" + start + ", end="
				+ end + "]";
	}
    
}
