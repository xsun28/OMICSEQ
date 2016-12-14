package com.omicseq.bean;

import java.util.Collections;
import java.util.List;


import com.omicseq.domain.MiRNASample;

public class MiRankSampleResult {

	private List<MiRNASample> sampleItemList = Collections.emptyList();
	private Integer total = 0;
	private String errorMesage;
	private Double usedTime;
	private String url;
	
	private Integer total_all = 0;

	public List<MiRNASample> getSampleItemList() {
		return sampleItemList;
	}

	public void setSampleItemList(List<MiRNASample> sampleItemList) {
		this.sampleItemList = sampleItemList;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getErrorMesage() {
		return errorMesage;
	}

	public void setErrorMesage(String errorMesage) {
		this.errorMesage = errorMesage;
	}

	public Double getUsedTime() {
		return usedTime;
	}

	public void setUsedTime(Double usedTime) {
		this.usedTime = usedTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getTotal_all() {
		return total_all;
	}

	public void setTotal_all(Integer total_all) {
		this.total_all = total_all;
	}
	
}
