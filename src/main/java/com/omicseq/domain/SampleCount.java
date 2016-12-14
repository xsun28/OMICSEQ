package com.omicseq.domain;

/**
 * @author Min.Wang
 *
 */
public class SampleCount  extends BaseDomain  {

	private static final long serialVersionUID = -54251737419742685L;
	
	private Integer sampleId;
	private Integer geneId;
	private String variationId;
	private Double tssTesCount;
	private Double tss5kCount;
	private Double tssT5Count;
	

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
	}

	public Integer getGeneId() {
		return geneId;
	}

	public void setGeneId(Integer geneId) {
		this.geneId = geneId;
	}

	public Double getTssTesCount() {
		return tssTesCount;
	}

	public void setTssTesCount(Double tssTesCount) {
		this.tssTesCount = tssTesCount;
	}

	public Double getTss5kCount() {
		return tss5kCount;
	}

	public void setTss5kCount(Double tss5kCount) {
		this.tss5kCount = tss5kCount;
	}

	public Double getTssT5Count() {
		return tssT5Count;
	}

	public void setTssT5Count(Double tssT5Count) {
		this.tssT5Count = tssT5Count;
	}

	public String getVariationId() {
		return variationId;
	}

	public void setVariationId(String variationId) {
		this.variationId = variationId;
	}

}
