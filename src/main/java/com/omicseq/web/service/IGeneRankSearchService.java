package com.omicseq.web.service;

import java.util.List;

import com.omicseq.domain.Gene;


public interface IGeneRankSearchService {
	
	List<Gene> searchGeneTop10BySampleId(Integer sampleId);

}
