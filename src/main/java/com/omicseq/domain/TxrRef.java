package com.omicseq.domain;

/**
 * @author Min.Wang
 *
 */
public class TxrRef extends BaseDomain  {

	private static final long serialVersionUID = -1992580067479502320L;
	// tx_id
	private String ucscName;
	private String mRNA;
	private String spID;
	private String spDisplayID;
	private String geneSymbol;
	private String refseq;
	private String protAcc;
	private String description;
	private String alias;

	public String getUcscName() {
		return ucscName;
	}

	public void setUcscName(String ucscName) {
		this.ucscName = ucscName;
	}

	public String getmRNA() {
		return mRNA;
	}

	public void setmRNA(String mRNA) {
		this.mRNA = mRNA;
	}

	public String getSpID() {
		return spID;
	}

	public void setSpID(String spID) {
		this.spID = spID;
	}

	public String getSpDisplayID() {
		return spDisplayID;
	}

	public void setSpDisplayID(String spDisplayID) {
		this.spDisplayID = spDisplayID;
	}

	public String getGeneSymbol() {
		return geneSymbol;
	}

	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}

	public String getRefseq() {
		return refseq;
	}

	public void setRefseq(String refseq) {
		this.refseq = refseq;
	}

	public String getProtAcc() {
		return protAcc;
	}

	public void setProtAcc(String protAcc) {
		this.protAcc = protAcc;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
