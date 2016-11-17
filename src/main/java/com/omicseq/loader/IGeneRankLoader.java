package com.omicseq.loader;

import java.util.List;

import com.omicseq.common.SortType;
import com.omicseq.domain.CacheGeneRank;

/**
 * @author Min.Wang
 *
 */
public interface IGeneRankLoader {

	/**
	 * the start
	 * @param geneId
	 * @param sourceList
	 * @param etypeList
	 * @param sortType
	 * @param start
	 * @param limit
	 * @return
	 */
	List<CacheGeneRank> load(Integer geneId, List<Integer> sourceList, List<Integer> etypeList, SortType sortType, Double mixturePerc, Integer start, Integer limit);
	
}
