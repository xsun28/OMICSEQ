package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.GeneIdMapping;

/**
 * 
 * 
 * @author zejun.du
 */
public interface IGeneIdMappingDAO extends IGenericDAO<GeneIdMapping> {

    /**
     * @param key
     * @return
     */
    GeneIdMapping getByGeneId(Integer key);

    /**
     * @param start
     * @param limit
     * @return
     */
    List<GeneIdMapping> list(Integer start, Integer limit);

    /**
     * 
     */
    void clean();

}
