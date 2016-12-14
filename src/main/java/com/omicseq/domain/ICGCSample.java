package com.omicseq.domain;

import java.util.List;
import java.util.Map;

public class ICGCSample {

	private String donorId;
	private String projectCode;
	private Map<String, String> metaDataMap;
	private List<Map<String, String>> sampleDataMap;

	public Map<String, String> getMetaDataMap() {
		return metaDataMap;
	}

	public void setMetaDataMap(Map<String, String> metaDataMap) {
		this.metaDataMap = metaDataMap;
	}

	public List<Map<String, String>> getSampleDataMap() {
		return sampleDataMap;
	}

	public void setSampleDataMap(List<Map<String, String>> sampleDataMap) {
		this.sampleDataMap = sampleDataMap;
	}

	public String getDonorId() {
		return donorId;
	}

	public void setDonorId(String donorId) {
		this.donorId = donorId;
	}

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

}
