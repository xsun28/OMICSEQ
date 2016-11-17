package com.omicseq.web.service;

import java.util.List;

import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.domain.MiRNA;
import com.omicseq.pathway.PathWay;

public interface ISearchMiRNAService {
	
	SampleResult  searchMiRNA(String query, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);
	
	List<MiRNA> search(String query);

	List<String> searchTopMiRNA(int miRNASampleId ,int size); 
}
