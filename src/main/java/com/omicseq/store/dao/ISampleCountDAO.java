package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.SampleCount;

/**
 * @author Min.Wang
 *
 */
public interface ISampleCountDAO extends IGenericDAO<SampleCount>  {

	List<SampleCount> findSampleCountById(Integer sampleId);

    void removeBySampleId(Integer sampleId);
	
}
