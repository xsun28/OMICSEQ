package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.common.SortType;
import com.omicseq.domain.GeneIdMapping;
import com.omicseq.store.dao.IGeneIdMappingDAO;

/**
 * 
 * 
 * @author zejun.du
 */
public class GeneIdMappingDAO extends GenericMongoDBDAO<GeneIdMapping> implements IGeneIdMappingDAO {

    @Override
    public GeneIdMapping getByGeneId(Integer geneId) {
        SmartDBObject query = new SmartDBObject("geneId", geneId);
        return findOne(query);
    }

    @Override
    public List<GeneIdMapping> list(Integer start, Integer limit) {
        SmartDBObject query = new SmartDBObject();
        query.addSort("id", SortType.ASC);
        return super.find(query, start, limit);
    }

    @Override
    public void clean() {
        super.delete(new SmartDBObject());
    }

}
