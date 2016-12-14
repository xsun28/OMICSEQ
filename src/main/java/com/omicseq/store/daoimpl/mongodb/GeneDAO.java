/**
 * 
 */
package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.SortType;
import com.omicseq.domain.Gene;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.helper.MongodbHelper;

/**
 * 
 * 
 * @author zejun.du
 */
public class GeneDAO extends GenericMongoDBDAO<Gene> implements IGeneDAO {

    @Override
    public Gene getByStartAndEnd(Integer start, Integer end) {
        SmartDBObject query = MongodbHelper.and(new SmartDBObject("start", start), new SmartDBObject("end", end));
        return super.findOne(query);
    }

    @Override
    public List<Gene> loadGeneList(Integer start, Integer limit) {
        SmartDBObject query = new SmartDBObject();
        query.addSort("geneId", SortType.ASC);
        return super.find(query, start, limit);
    }

    @Override
    public Gene getByName(String name) {
        return super.findOne(new SmartDBObject("txName", name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Gene> fuzzyQuery(String feild, String query, Integer limit) {
        if (StringUtils.isBlank(feild)) {
            return Collections.EMPTY_LIST;
        }
        return super.find(MongodbHelper.startLike(feild, query), 0, limit);
    }

    @Override
    public void clean() {
        super.delete(new SmartDBObject());
    }

    @Override
    public void update(Integer geneId, Integer oldId) {
        Gene gene = super.findOne(new SmartDBObject("geneId", geneId));
        if (null != gene) {
            gene.setGeneId(oldId);
            update(gene);
        } else {
            logger.error("not find gene by {} ",geneId);
        }
    }

}
