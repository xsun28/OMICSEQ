package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

/**
 * 
 * 
 * @author zejun.du
 */
public interface ISampleDAO extends IGenericDAO<Sample> {
    /**
     * @param start
     * @param limit
     * @return
     */
    List<Sample> loadSampleList(Integer start, Integer limit);

    Sample getBySampleId(Integer sampleId);

    /**
     * @param url
     * @return
     */
    Sample getByUrl(String url);

    /**
     * 根据sample_id查询 所有数据
     * 
     * @param sampleIds
     * @return
     */
    List<Sample> listBySampleIds(List<Integer> sampleIds);

    /**
     * @return
     */
    Integer getSequenceId(SourceType source);

    /**
     * @param url
     */
    void removeByUrl(String url);
    /**
     * @param _id
     */
    void removeBy_id(String _id);
    
	Integer count(List<Integer> sampleIds, List<Integer> intSourceList,
			List<Integer> intEtypeList);
	
	List<Integer> findSampleIdBySourceAndEtype(List<Integer> sampleIds,
			List<Integer> intSourceList, List<Integer> intEtypeList);
	
	Integer[] countProcessAndInProcess(String showTab);

}
