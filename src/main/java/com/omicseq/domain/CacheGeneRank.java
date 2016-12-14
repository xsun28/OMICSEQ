package com.omicseq.domain;

import java.io.Serializable;

public class CacheGeneRank implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5425173741974211375L;
	private Integer sampleId;
	private Integer tss5kRank;
	private Double tss5kCount;
	private Double tssTesCount;
	private Double mixturePerc;
	private Double tss5kPerc;
	private Integer total;
	private Integer etype;

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
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

    public Double getMixturePerc() {
        return mixturePerc;
    }

    public void setMixturePerc(Double mixturePerc) {
        this.mixturePerc = mixturePerc;
    }

    public Double getTss5kPerc() {
        return tss5kPerc;
    }

    public void setTss5kPerc(Double tss5kPerc) {
        this.tss5kPerc = tss5kPerc;
    }

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Double getTssTesCount() {
		return tssTesCount;
	}

	public void setTssTesCount(Double tssTesCount) {
		this.tssTesCount = tssTesCount;
	}

	public Integer getEtype() {
		return etype;
	}

	public void setEtype(Integer etype) {
		this.etype = etype;
	}
    
}
