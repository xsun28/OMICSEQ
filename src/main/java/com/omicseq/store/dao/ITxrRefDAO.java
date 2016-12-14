package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.TxrRef;

/**
 * @author Min.Wang
 *
 */
public interface ITxrRefDAO extends IGenericDAO<TxrRef>{
	
	List<TxrRef> fuzzyQuery(String feild, String query, Integer limit);
	
	List<TxrRef> loadTxrRefList(Integer start, Integer limit);

    List<TxrRef> findByGeneSymbol(String geneSymbol);

    void delete(List<TxrRef> list);
	
}
