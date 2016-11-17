package com.omicseq.domain;

public class VariationRank extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8313596670262714729L;
	
	private String variationId;
	
	private Integer sampleId;
	
	//实验数据来源,TCGA等
	private Integer source;
	//试验类型,CHIQ-SEQ,RAN-SEQ
	private Integer etype;

	private Double readCount;
	
	private Double mixturePerc;
	
	private long createdTimestamp;
	
	private Integer totalCount;
	
	private Integer orderNo;

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public String getVariationId() {
		return variationId;
	}

	public void setVariationId(String variationId) {
		this.variationId = variationId;
	}

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
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

	public Double getReadCount() {
		return readCount;
	}

	public void setReadCount(Double readCount) {
		this.readCount = readCount;
	}

	public Double getMixturePerc() {
		return mixturePerc;
	}

	public void setMixturePerc(Double mixturePerc) {
		this.mixturePerc = mixturePerc;
	}

	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
}
