package com.omicseq.domain;

public class Multigene extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer multigeneId;
	
	private String geneIds;
	
	private User user;
	
	private Integer searchTimes;
	
	private Long createdTimeStamp;
	
	private Integer status;
	
	private String remark;

	public Integer getMultigeneId() {
		return multigeneId;
	}

	public void setMultigeneId(Integer multigeneId) {
		this.multigeneId = multigeneId;
	}

	public String getGeneIds() {
		return geneIds;
	}

	public void setGeneIds(String geneIds) {
		this.geneIds = geneIds;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getSearchTimes() {
		return searchTimes;
	}

	public void setSearchTimes(Integer searchTimes) {
		this.searchTimes = searchTimes;
	}

	public Long getCreatedTimeStamp() {
		return createdTimeStamp;
	}

	public void setCreatedTimeStamp(Long createdTimeStamp) {
		this.createdTimeStamp = createdTimeStamp;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
