package com.omicseq.web.service;

import java.util.List;

import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;

public interface IVariationGeneService {
	
	SampleResult searchSample(String query, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);

	SampleResult searchSampleByVariationGenes(String variationGenes,
			List<String> sourceList, List<String> etypeList, SortType sortType,
			Integer start, Integer limit);

}
