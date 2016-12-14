package com.omicseq.web.service;

import java.util.List;

import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;

public interface IMouseSampleSearchService {
	SampleResult searchSample(String query, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);

	SampleResult searchSampleByGeneId(GeneItem geneItem, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);

	SampleResult advancedSearch(String geneSymbol, List<String> sourceList,
			List<String> etypeList, String cell, String detail,
			SortType sortType, Integer start, Integer limit);

}
