package com.omicseq.bean;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;


/**
 * @author Min.Wang
 *
 */
public class SampleResult {

	private List<SampleItem> sampleItemList = Collections.emptyList();
	private List<GeneItem> geneItemList = Collections.emptyList();
	private Integer total = 0;
	private String errorMesage;
	private Double usedTime;
	private String url;
	
	private Integer total_all = 0;

	public List<SampleItem> getSampleItemList() {
		return sampleItemList;
	}

	public void setSampleItemList(List<SampleItem> sampleItemList) {
		this.sampleItemList = sampleItemList;
	}

	public List<GeneItem> getGeneItemList() {
		return geneItemList;
	}

	public void setGeneItemList(List<GeneItem> geneItemList) {
		this.geneItemList = geneItemList;
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

	public void setUsedTime(long begin, long end) {
	    begin=begin/1000;
	    end=end/1000;
	    this.usedTime = ((double)end-begin)/(double)(1000*1000);
    }

	@Override
	public String toString() {
		return "SampleResult [sampleItemList=" + sampleItemList + ", geneItemList=" + geneItemList
				+ ", total=" + total + ", errorMesage=" + errorMesage + "]";
	}
	
    public GeneItem getCurrent() {
        if(CollectionUtils.isNotEmpty(geneItemList)){
            for (GeneItem item : geneItemList) {
                if (item.isUsedForQuery()) {
                    return item;
                }
            }
        }
        return null;
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
