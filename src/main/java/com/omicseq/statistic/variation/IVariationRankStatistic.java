package com.omicseq.statistic.variation;

import java.util.List;

import com.omicseq.domain.ObjectPair;
import com.omicseq.domain.StatisticResult;

/**
 * @author Min.Wang
 * 
 */
public interface IVariationRankStatistic {

	StatisticResult computeRank(List<ObjectPair<Integer, String>> sampleIdPairList);

	StatisticResult computeRank(String samplePath, Integer sampleId);

	
}
