package com.omicseq.domain;

public class MiRNA extends BaseDomain{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6626278438016127590L;
	
	private Integer miRNAId;
	private String miRNAName;
	private Integer deleted;
	private String createTimeStamp;
	
	
	public Integer getMiRNAId() {
		return miRNAId;
	}
	public void setMiRNAId(Integer miRNAId) {
		this.miRNAId = miRNAId;
	}
	public String getMiRNAName() {
		return miRNAName;
	}
	public void setMiRNAName(String miRNAName) {
		this.miRNAName = miRNAName;
	}
	public Integer getDeleted() {
		return deleted;
	}
	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}
	public String getCreateTimeStamp() {
		return createTimeStamp;
	}
	public void setCreateTimeStamp(String createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
	}
	
}
