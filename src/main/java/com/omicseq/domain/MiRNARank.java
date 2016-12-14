package com.omicseq.domain;

public class MiRNARank extends BaseDomain{
	private static final long serialVersionUID = 1L;
	
	private Integer miRNAId ;
	private Integer miRNASampleId ;
	private Integer source;
	private Integer etype;
	private Double mixtureperc;
	private Double read;
	public Double getMixtureperc() {
		return mixtureperc;
	}
	public void setMixtureperc(Double mixtureperc) {
		this.mixtureperc = mixtureperc;
	}
	private Integer totalCount;
	private String createTimeStamp;
	public Integer getMiRNAId() {
		return miRNAId;
	}
	public void setMiRNAId(Integer miRNAId) {
		this.miRNAId = miRNAId;
	}
	public Integer getMiRNASampleId() {
		return miRNASampleId;
	}
	public void setMiRNASampleId(Integer miRNASampleId) {
		this.miRNASampleId = miRNASampleId;
	}
	public Integer getSource() {
		return source;
	}
	public void setSource(Integer source) {
		this.source = source;
	}
	public Integer getEtype() {
		return etype;
	}
	public void setEtype(Integer etype) {
		this.etype = etype;
	}
	public Double getRead() {
		return read;
	}
	public void setRead(Double read) {
		this.read = read;
	}
	public String getCreateTimeStamp() {
		return createTimeStamp;
	}
	public void setCreateTimeStamp(String createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
	}
	public Integer getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	
	
}
