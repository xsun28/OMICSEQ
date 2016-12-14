package com.omicseq.pathway;

import com.omicseq.domain.BaseDomain;

public class PathWay extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4351733056424092348L;
	
	private Integer pathId;
	
	private String pathwayName;
	
	private String url;
	
	private String geneNames;
	
	private String geneIds;
	
	private Short status;
	
	private Long lastmodified;

	public String getPathwayName() {
		return pathwayName;
	}

	public void setPathwayName(String pathwayName) {
		this.pathwayName = pathwayName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGeneNames() {
		return geneNames;
	}

	public void setGeneNames(String geneNames) {
		this.geneNames = geneNames;
	}

	public Integer getPathId() {
		return pathId;
	}

	public void setPathId(Integer pathId) {
		this.pathId = pathId;
	}

	public String getGeneIds() {
		return geneIds;
	}

	public void setGeneIds(String geneIds) {
		this.geneIds = geneIds;
	}

	public Short getStatus() {
		return status;
	}

	public void setStatus(Short status) {
		this.status = status;
	}

	public Long getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(Long lastmodified) {
		this.lastmodified = lastmodified;
	}

}
