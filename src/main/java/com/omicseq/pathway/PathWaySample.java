package com.omicseq.pathway;

import com.omicseq.domain.BaseDomain;

public class PathWaySample extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7712571371696408374L;
	private String variationName;
	public String getVariationName() {
		return variationName;
	}
	public void setVariationName(String variationName) {
		this.variationName = variationName;
	}
	private int pathId;
	private Integer sampleId;
	private String pathWayName;
	private Double avgA;
	private Double b;
	private Double rank;
	private Integer source;
	private Integer etype;
//	private Integer count;
//	private Double tss5kPerc;
//	private Double tss5kCount;
//	private Double tssTesCount;
	public int getPathId() {
		return pathId;
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public String getPathWayName() {
		return pathWayName;
	}
	public void setPathWayName(String pathWayName) {
		this.pathWayName = pathWayName;
	}
	public Integer getSampleId() {
		return sampleId;
	}
	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
	}
	public Double getRank() {
		return rank;
	}
	public void setRank(Double rank) {
		this.rank = rank;
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
//	public Integer getCount() {
//		return count;
//	}
//	public void setCount(Integer count) {
//		this.count = count;
//	}
//	public Double getTss5kPerc() {
//		return tss5kPerc;
//	}
//	public void setTss5kPerc(Double tss5kPerc) {
//		this.tss5kPerc = tss5kPerc;
//	}
//	public Double getTss5kCount() {
//		return tss5kCount;
//	}
//	public void setTss5kCount(Double tss5kCount) {
//		this.tss5kCount = tss5kCount;
//	}
//	public Double getTssTesCount() {
//		return tssTesCount;
//	}
//	public void setTssTesCount(Double tssTesCount) {
//		this.tssTesCount = tssTesCount;
//	}
	
}
