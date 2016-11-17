package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.Gene;

public interface IGeneDAO extends IGenericDAO<Gene> {

    /**
     * @param start
     * @param end
     * @return
     */
    Gene getByStartAndEnd(Integer start, Integer end);

    /**
     * @param start
     * @param limit
     * @return
     */
    List<Gene> loadGeneList(Integer start, Integer limit);

    /**
     * @param name
     * @return
     */
    Gene getByName(String name);
    
	List<Gene> fuzzyQuery(String feild, String query, Integer limit);

    /**
     * 
     */
    void clean();

    /**
     * @param geneId
     * @param oldId
     */
    void update(Integer geneId, Integer oldId);
}
