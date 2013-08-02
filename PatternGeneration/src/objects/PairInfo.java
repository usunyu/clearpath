package objects;

public class PairInfo {
	public PairInfo(double lati, double longi) {
		super();
		this.lati = lati;
		this.longi = longi;
	}

	public PairInfo() {
		// TODO Auto-generated constructor stub
	}

	double lati;
	double longi;

	public double getLati() {
		return lati;
	}

	public void setLati(double lati) {
		this.lati = lati;
	}

	public double getLongi() {
		return longi;
	}

	public void setLongi(double longi) {
		this.longi = longi;
	}

	@Override
	public String toString() {
		return "PairInfo [lati=" + lati + ", longi=" + longi + "]";
	}

	public String getQuery() {
		return String.valueOf(longi + "," + lati);
	}

	public boolean equal(PairInfo pairInfo) {
		if (lati == pairInfo.getLati() && longi == pairInfo.getLongi())
			return true;
		else
			return false;
	}
}
