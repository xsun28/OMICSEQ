package com.omicseq.domain;

import com.omicseq.annotation.NonPersistent;

public class VariationGene extends BaseDomain {

	/**
	 * 变异基因信息存储
	 */
	private static final long serialVersionUID = 708537768386025617L;
	
	private String variationId;
	
	private String chrom;
	
	private Integer chromStart;
	
	private Integer chromEnd;
	
	private String strand;
	
	private Integer binId;
	
	@NonPersistent
	private String countType;

	public String getVariationId() {
		return variationId;
	}

	public void setVariationId(String variationId) {
		this.variationId = variationId;
	}

	public String getChrom() {
		return chrom;
	}

	public void setChrom(String chrom) {
		this.chrom = chrom;
	}

	public Integer getChromStart() {
		return chromStart;
	}

	public void setChromStart(Integer chromStart) {
		this.chromStart = chromStart;
	}

	public Integer getChromEnd() {
		return chromEnd;
	}

	public void setChromEnd(Integer chromEnd) {
		this.chromEnd = chromEnd;
	}

	public String getStrand() {
		return strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public int getBinId() {
		return binId;
	}

	public void setBinId(Integer binId) {
		this.binId = binId;
	}

	public String getCountType() {
		return countType;
	}

	public void setCountType(String countType) {
		this.countType = countType;
	}

}
