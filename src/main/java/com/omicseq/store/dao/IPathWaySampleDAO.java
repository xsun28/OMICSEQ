package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.pathway.PathWaySample;
import com.omicseq.store.criteria.PathWayCriteria;

public interface IPathWaySampleDAO extends IGenericDAO<PathWaySample>  {

	Integer countTotal(PathWayCriteria criteria) throws Exception;

	List<Integer> findSampleIdByPathId(PathWayCriteria criteria) throws Exception;

}
