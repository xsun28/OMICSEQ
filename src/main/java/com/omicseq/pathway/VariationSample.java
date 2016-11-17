package com.omicseq.pathway;

import com.omicseq.domain.BaseDomain;

public class VariationSample extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = 670713878287558047L;
	private String variationId;
	private Integer sampleId;
	private String variationName;
	private Double avgA;
	private Double b;
	private Double rank;
	private Integer source;
	private Integer etype;
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
	public String getVariationName() {
		return variationName;
	}
	public void setVariationName(String variationName) {
		this.variationName = variationName;
	}
	public Double getAvgA() {
		return avgA;
	}
	public void setAvgA(Double avgA) {
		this.avgA = avgA;
	}
	public Double getB() {
		return b;
	}
	public void setB(Double b) {
		this.b = b;
	}
	public Double getRank() {
		return rank;
	}
	public void setRank(Double rank) {
		this.rank = rank;
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
	
	
}
