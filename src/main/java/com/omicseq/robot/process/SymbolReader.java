package com.omicseq.robot.process;

public class SymbolReader {
	Double read ;
	String symbol ;
	String barCode;
	Integer geneId;
	
	
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	public Double getRead() {
		return read;
	}
	public void setRead(Double read) {
		this.read = read;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Integer getGeneId() {
		return geneId;
	}
	public void setGeneId(Integer geneId) {
		this.geneId = geneId;
	}
	
}
