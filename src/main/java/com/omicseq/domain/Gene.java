package com.omicseq.domain;

import com.omicseq.annotation.NonPersistent;

public class Gene extends BaseDomain  {

	private static final long serialVersionUID = 7668818174198875433L;
	private Integer geneId;
	private String seqName;
	private Integer start;
	private Integer end;
	private Integer width;
	private String txName;
	private String strand;
	
	private Integer geneLength;
	private Integer exonNum;
	private Integer exonLength;
	
	private String geneName;
	private Integer entrezId;
	//relational  key ,Human Gene 关联 Mouse Gene
	private String relKey;
	
	public Integer getEntrezId() {
		return entrezId;
	}

	public void setEntrezId(Integer entrezId) {
		this.entrezId = entrezId;
	}

	public String getRelKey() {
		return relKey;
	}

	public void setRelKey(String relKey) {
		this.relKey = relKey;
	}

	@NonPersistent
	private String countType;

	public Integer getGeneId() {
		return geneId;
	}

	public void setGeneId(Integer geneId) {
		this.geneId = geneId;
	}

	public String getSeqName() {
		return seqName;
	}

	public void setSeqName(String seqName) {
		this.seqName = seqName;
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

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getTxName() {
		return txName;
	}

	public void setTxName(String txName) {
		this.txName = txName;
	}

	public String getStrand() {
		return strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public String getCountType() {
		return countType;
	}

	public void setCountType(String countType) {
		this.countType = countType;
	}

	public Integer getGeneLength() {
		return geneLength;
	}

	public void setGeneLength(Integer geneLength) {
		this.geneLength = geneLength;
	}

	public Integer getExonNum() {
		return exonNum;
	}

	public void setExonNum(Integer exonNum) {
		this.exonNum = exonNum;
	}

	public Integer getExonLength() {
		return exonLength;
	}

	public void setExonLength(Integer exonLength) {
		this.exonLength = exonLength;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	
}
