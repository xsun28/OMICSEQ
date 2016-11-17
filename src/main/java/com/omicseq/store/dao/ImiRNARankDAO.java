package com.omicseq.store.dao;

import com.omicseq.domain.MiRNARank;

public interface ImiRNARankDAO extends IGenericDAO<MiRNARank> {
	
	Integer count(Integer miRNAId);

	Integer count_all(Integer miRNAId);
}
