package com.omicseq.domain;


public class FactorDes extends BaseDomain {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -890554477765691279L;

	private String factor;
	
	private String factorDesc;

	public String getFactor() {
		return factor;
	}

	public void setFactor(String factor) {
		this.factor = factor;
	}

	public String getFactorDesc() {
		return factorDesc;
	}

	public void setFactorDesc(String factorDesc) {
		this.factorDesc = factorDesc;
	}

}
