package com.omicseq.domain;

public class SymbolSynonyms extends BaseDomain {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String geneSymbol;
	private String synonyms;
	
	public String getGeneSymbol() {
		return geneSymbol;
	}
	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}
	public String getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}
	
	
}
