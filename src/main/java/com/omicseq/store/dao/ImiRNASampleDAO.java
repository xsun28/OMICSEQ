package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.common.SourceType;
import com.omicseq.domain.MiRNASample;

public interface ImiRNASampleDAO extends IGenericDAO<MiRNASample> {
	
	 Integer getSequenceId(SourceType source);
	 
	 List<MiRNASample> loadMiRNASampleList(Integer start, Integer limit);

	MiRNASample getByMiRNASampleId(Integer miRNAampleId);
}
