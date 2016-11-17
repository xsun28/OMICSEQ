package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.common.SourceType;
import com.omicseq.domain.MiRNA;

public interface ImiRNADAO extends IGenericDAO<MiRNA> {
	Integer getSquence(SourceType source);
	
	List<MiRNA> loadMiRNAList(String name, Integer limit);
	
	MiRNA findByName(String name);
	
	void create(MiRNA m);

	List<MiRNA> fuzzyQuery(String feild, String query, Integer limit);
}
