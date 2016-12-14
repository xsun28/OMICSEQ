package com.omicseq.bean;

/**
 * @author Min.Wang
 *
 */
public class ReadRecord {

	private Integer start;
	private Integer end;
	private String seqName;
	private String type;
	private String value;

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public String getSeqName() {
		return seqName;
	}

	public void setSeqName(String seqName) {
		this.seqName = seqName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ReadRecord [start=" + start + ", end=" + end + ", seqName=" + seqName + ", type="
				+ type + " value= " + value + " ]";
	}
	
}
