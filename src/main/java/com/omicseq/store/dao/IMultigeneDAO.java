package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.Multigene;
import com.omicseq.pathway.PathWay;

public interface IMultigeneDAO extends IGenericDAO<Multigene> {
	Integer getSequenceId(String source);
	
	void updateMultigeneById(Integer multigeneId, Integer[] geneIds);

	List<Multigene> fuzzyQuery(String string, String upperCase, int maxValue);

	void updateStatus(Integer multigeneId, Short status, String remark);
	
	Integer updateSearchTimes();
}
