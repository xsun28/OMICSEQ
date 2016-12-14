package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.GeneRank;
import com.omicseq.domain.VariationRank;
import com.omicseq.store.criteria.GeneRankCriteria;

/**
 * @author Min.Wang
 * 
 */
public interface IVariationRankDAO extends IGenericDAO<VariationRank> {
    void clean();

    /**
     * where geneId= ? order by tss5kRank asc
     * 
     * @param geneId
     * @return
     */
    List<VariationRank> findByCriteria(GeneRankCriteria criteria, Integer start, Integer limit);

    /**
     * @param criteria
     * @return
     */
    List<VariationRank> findByCriteria(GeneRankCriteria criteria);

    /**
     * @param criteria
     * @return
     */
    Integer count(GeneRankCriteria criteria);

    Integer count(Integer sampleId);

    /**
     * 根据基因获取Rank数据
     * 
     * @param geneId
     * @param field
     * @return
     */
    List<Integer> listRankByCriteria(GeneRankCriteria criteria, String field);

    /**
     * 根据geneId 查询所有数据
     * 
     * @param geneId
     * @return
     */
    List<VariationRank> listByGeneId(Integer geneId);

    /**
     * @param sampleId
     */
    void removeBySampleId(Integer sampleId);

    /**
     * @param criteria
     * @return
     */
    List<Double> percentile(GeneRankCriteria criteria);

}
