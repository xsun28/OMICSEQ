package com.omicseq.web.service;

import java.util.List;
import java.util.Map;

import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.domain.Comment;
import com.omicseq.domain.Gene;
import com.omicseq.domain.Sample;

/**
 * @author Min.Wang
 *
 */
public interface ISampleSearchService {

	/**
	 * @param query
	 * @param start
	 * @param limit
	 * @return
	 */
	SampleResult searchSample(String query, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);
	
	/**
	 * @param geneIdpem
	 * @param start
	 * @param limit
	 * @return
	 */
	SampleResult searchSampleByGeneId(GeneItem geneItem, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);

    List<String> findFactorMapingNotExist();

	SampleResult searchSampleByPathway(String pathwayName, Integer pathwayId, List<String> sourceList, List<String> experimentsList,
			SortType sortType, Integer start, int pageSize);

	SampleResult searchSampleByMultigene(String multigene,List<String> sourceList, List<String> experimentsList,
			SortType sortType, Integer start, int pageSize);
	
	Map<String,Gene> searchTop5Genes(int sampleId ,int size);

	SampleResult searchSample(String cell, String factor,
			List<String> sourceList, List<String> experimentsList,
			SortType sortType, Integer start, int pageSize);

	List<Sample> findSamplesByCell(String cell, String factor, List<String> sourceList, List<String> experimentsList, Integer start, Integer limit);

	SampleResult findSampleByGenomicRegion(String genomicRegion, Integer start, Integer pageSize);

    List<String> regionList(String bedPath);

    void modifyCell(String cellOld , String cellNew);

	void modifyDetail(String cell , String factorOld, String factorNew, Integer sampleId);

	void modifyLab(String labOld, String labNew);
	
	/**
	 * @param geneSymbol
	 * @param start
	 * @param limit
	 * @return
	 */
	SampleResult advancedSearch(String geneSymbol, List<String> sourceList, List<String> etypeList, String cell, String detail, SortType sortType, Integer start, Integer limit);

	Integer[] findProcessedAndInProcessSample(String showTab);
	
	List<Comment> showCommentsBySampleId(Integer sampleId);
}
