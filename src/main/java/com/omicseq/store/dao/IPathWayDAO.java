package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.pathway.PathWay;

public interface IPathWayDAO extends IGenericDAO<PathWay>  {

	Integer getSequenceId(String source);

	void updatePathWayById(Integer pathId, Integer[] geneIds);

	List<PathWay> fuzzyQuery(String string, String upperCase, int maxValue);

	void updateStatus(Integer pathId, Short status);

	PathWay findOne(String pathwayName);
}
