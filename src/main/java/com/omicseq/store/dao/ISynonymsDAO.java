package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.SymbolSynonyms;

public interface ISynonymsDAO extends IGenericDAO<SymbolSynonyms> {
	
	List<SymbolSynonyms> fuzzyQuery(String feild, String query, Integer limit);
}
