package com.omicseq.domain;

public class SummaryTrackData extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7604133122645012833L;
	
	private Integer geneId;
	private String 	sampleId_tumor_normal;
    private Integer source;
    private Integer etype;
    private String sampleCode;
    private String cellType;
    
    
    private Double tumorCount;
    private Double normalCount;
    private Double tumorDiffNormalCount;
    
    
	public Integer getGeneId() {
		return geneId;
	}
	public void setGeneId(Integer geneId) {
		this.geneId = geneId;
	}
	public String getSampleId_tumor_normal() {
		return sampleId_tumor_normal;
	}
	public void setSampleId_tumor_normal(String sampleId_tumor_normal) {
		this.sampleId_tumor_normal = sampleId_tumor_normal;
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
	public String getSampleCode() {
		return sampleCode;
	}
	public void setSampleCode(String sampleCode) {
		this.sampleCode = sampleCode;
	}
	public Double getTumorCount() {
		return tumorCount;
	}
	public void setTumorCount(Double tumorCount) {
		this.tumorCount = tumorCount;
	}
	public Double getNormalCount() {
		return normalCount;
	}
	public void setNormalCount(Double normalCount) {
		this.normalCount = normalCount;
	}
	public Double getTumorDiffNormalCount() {
		return tumorDiffNormalCount;
	}
	public void setTumorDiffNormalCount(Double tumorDiffNormalCount) {
		this.tumorDiffNormalCount = tumorDiffNormalCount;
	}
	public String getCellType() {
		return cellType;
	}
	public void setCellType(String cellType) {
		this.cellType = cellType;
	}
}
