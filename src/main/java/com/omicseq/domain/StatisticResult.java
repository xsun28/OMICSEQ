package com.omicseq.domain;

import java.util.List;
import java.util.Map;

/**
 * @author Min.Wang
 *
 */
public class StatisticResult {
	
	private Map<String, String> metaDataMap; 
	private List<GeneRank> geneRankList;
	private List<VariationRank> variationRank;
	private Integer readCount;

	public List<GeneRank> getGeneRankList() {
		return geneRankList;
	}

	public void setGeneRankList(List<GeneRank> geneRankList) {
		this.geneRankList = geneRankList;
	}

	public Integer getReadCount() {
		return readCount;
	}

	public void setReadCount(Integer readCount) {
		this.readCount = readCount;
	}

	public Map<String, String> getMetaDataMap() {
		return metaDataMap;
	}

	public void setMetaDataMap(Map<String, String> metaDataMap) {
		this.metaDataMap = metaDataMap;
	}

	public List<VariationRank> getVariationRank() {
		return variationRank;
	}

	public void setVariationRank(List<VariationRank> variationRank) {
		this.variationRank = variationRank;
	}
	
}
