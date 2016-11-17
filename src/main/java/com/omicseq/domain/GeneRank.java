package com.omicseq.domain;

import com.omicseq.annotation.NonPersistent;

/**
 * @author Min.Wang
 *
 */
public class GeneRank extends BaseDomain {

	private static final long serialVersionUID = -3784984947076212665L;
	private Integer geneId;
	private Integer sampleId;
	//实验数据来源,TCGA等
	private Integer source;
	//试验类型,CHIQ-SEQ,RAN-SEQ
	private Integer etype;
	
	private Integer fromType; //1:human 2:mouse
	
	@NonPersistent
	private Integer tssTesRank;
	private Double tssTesCount;
	
	@NonPersistent
	private Integer tss5kRank;
	private Double tss5kCount;
	//tss_tes_5k_rank
	private Integer tssT5Rank;
	//tss_tes_5k_count
	private Double tssT5Count;
	
	private Double mixturePerc;
	private Double tssTesPerc;
	private Double tss5kPerc;
	
	private Integer totalCount;
	
	private Long createdTimestamp;
	
	@NonPersistent
	private String seqName;
	@NonPersistent
	private Integer start;
	@NonPersistent
	private Integer end;

	public Integer getGeneId() {
		return geneId;
	}

	public void setGeneId(Integer geneId) {
		this.geneId = geneId;
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

	public Integer getTssTesRank() {
		return tssTesRank;
	}

	public void setTssTesRank(Integer tssTesRank) {
		this.tssTesRank = tssTesRank;
	}

	public Double getTssTesCount() {
		return tssTesCount;
	}

	public void setTssTesCount(Double tssTesCount) {
		this.tssTesCount = tssTesCount;
	}

	public Integer getTss5kRank() {
		return tss5kRank;
	}

	public void setTss5kRank(Integer tss5kRank) {
		this.tss5kRank = tss5kRank;
	}

	public Double getTss5kCount() {
		return tss5kCount;
	}

	public void setTss5kCount(Double tss5kCount) {
		this.tss5kCount = tss5kCount;
	}

	public Integer getTssT5Rank() {
		return tssT5Rank;
	}

	public void setTssT5Rank(Integer tssT5Rank) {
		this.tssT5Rank = tssT5Rank;
	}

	public Double getTssT5Count() {
		return tssT5Count;
	}

	public void setTssT5Count(Double tssT5Count) {
		this.tssT5Count = tssT5Count;
	}

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

	public Double getMixturePerc() {
		return mixturePerc;
	}

	public void setMixturePerc(Double mixturePerc) {
		this.mixturePerc = mixturePerc;
	}

	public Double getTssTesPerc() {
		return tssTesPerc;
	}

	public void setTssTesPerc(Double tssTesPerc) {
		this.tssTesPerc = tssTesPerc;
	}

	public Double getTss5kPerc() {
		return tss5kPerc;
	}

	public void setTss5kPerc(Double tss5kPerc) {
		this.tss5kPerc = tss5kPerc;
	}

	public String getSeqName() {
		return seqName;
	}

	public void setSeqName(String seqName) {
		this.seqName = seqName;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Integer getFromType() {
		return fromType;
	}

	public void setFromType(Integer fromType) {
		this.fromType = fromType;
	}
	
}
