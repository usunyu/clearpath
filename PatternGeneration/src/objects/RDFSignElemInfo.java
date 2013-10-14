package objects;

public class RDFSignElemInfo {
	int destNumber;
	int entryNumber;
	String entryType;
	int textNumber;
	String textType;
	String text;
	String directionCode;
	
	public RDFSignElemInfo(int destNumber, int entryNumber, String entryType, int textNumber, String textType, String text, String directionCode) {
		this.destNumber = destNumber;
		this.entryNumber = entryNumber;
		this.entryType = entryType;
		this.textNumber = textNumber;
		this.textType = textType;
		this.text = text;
		this.directionCode = directionCode;
	}
	
	public int getDestNumber() {
		return destNumber;
	}
	
	public int getEntryNumber() {
		return entryNumber;
	}
	
	public String getEntryType() {
		return entryType;
	}
	
	public int getTextNumber() {
		return textNumber;
	}
	
	public String getTextType() {
		return textType;
	}
	
	public String getText() {
		return text;
	}
	
	public String getDirectionCode() {
		return directionCode;
	}
}
