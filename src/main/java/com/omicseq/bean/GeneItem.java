package com.omicseq.bean;

/**
 * @author Min.Wang
 *
 */
public class GeneItem implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Integer geneId;
	private String txName;
	private String seqName;
	private String seqNameShort;
	private Integer start;
	private Integer end;
	private boolean usedForQuery;
	private String strand;
	private String geneSymbol;
	private String entrezId;
	private String relKey;
	
	public Integer getGeneId() {
		return geneId;
	}

	public void setGeneId(Integer geneId) {
		this.geneId = geneId;
	}

	public String getTxName() {
		return txName;
	}

	public void setTxName(String txName) {
		this.txName = txName;
	}

	public String getSeqName() {
		return seqName;
	}

	public void setSeqName(String seqName) {
		this.seqName = seqName;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public boolean isUsedForQuery() {
		return usedForQuery;
	}

	public void setUsedForQuery(boolean usedForQuery) {
		this.usedForQuery = usedForQuery;
	}

	public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getSeqNameShort() {
        return seqNameShort;
    }

    public void setSeqNameShort(String seqNameShort) {
        this.seqNameShort = seqNameShort;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    @Override
	public String toString() {
		return "GeneItem [geneId=" + geneId + ", txName=" + txName + ", seqName=" + seqName + ", seqNameShort=" + seqNameShort
				+ ", start=" + start + ", end=" + end + ", usedForQuery=" + usedForQuery + ", strand=" + strand + ", geneSymbol=" + geneSymbol + "]";
	}

	public String getEntrezId() {
		return entrezId;
	}

	public void setEntrezId(String entrezId) {
		this.entrezId = entrezId;
	}

	public String getRelKey() {
		return relKey;
	}

	public void setRelKey(String relKey) {
		this.relKey = relKey;
	}
	
}
