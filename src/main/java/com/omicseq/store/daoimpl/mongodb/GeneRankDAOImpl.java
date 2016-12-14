package com.omicseq.store.daoimpl.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;

import com.mongodb.BasicDBObject;
import com.omicseq.common.SortType;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.utils.DateTimeUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class GeneRankDAOImpl extends GenericMongoDBDAO<GeneRank> implements IGeneRankDAO {

    @Override
    public void clean() {
        DateTime dt = DateTime.now();
        super.delete(new SmartDBObject());
        logger.debug("Clean generank data used {}", DateTimeUtils.diff(dt, DateTime.now()));
    }

    private SmartDBObject buildQueryByCriteria(GeneRankCriteria criteria) {
        Integer geneId = criteria.getGeneId();
        SmartDBObject query = new SmartDBObject();
        if (null != criteria.getSampleId()) {
            query.put("sampleId", criteria.getSampleId());
        }
        if (null != geneId) {
            query.put("geneId", geneId);
        }
        if (CollectionUtils.isNotEmpty(criteria.getSourceList())) {
            query = MongodbHelper.and(query, MongodbHelper.in("source", criteria.getSourceList().toArray()));
        }
        if (CollectionUtils.isNotEmpty(criteria.getEtypeList())) {
            query = MongodbHelper.and(query, MongodbHelper.in("etype", criteria.getEtypeList().toArray()));
        }
        if(null != criteria.getMixturePerc())
        {
          query.put("mixturePerc", new BasicDBObject("$lte", 0.01));
        }
        return query;
    }

    @Override
    public List<GeneRank> findByCriteria(GeneRankCriteria criteria) {
        return findByCriteria(criteria, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.omicseq.store.dao.IGeneRankDAO#findByCriteria(com.omicseq.store.dao
     * .IGeneRankDAO.GeneRankCriteria, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public List<GeneRank> findByCriteria(GeneRankCriteria criteria, Integer start, Integer limit) {
        SmartDBObject query = buildQueryByCriteria(criteria);
        SortType sortType = criteria.getSortType();
        if (sortType == null) {
            sortType = SortType.ASC;
        }
        query.addSort("mixturePerc", sortType);
//        query.addSort("etype", SortType.ASC);
//        query.addSort("tssTesCount", SortType.DESC);
//        List<BasicDBObject> sortList = new ArrayList<BasicDBObject>();
//        BasicDBObject sortObject = new SmartDBObject("mixturePerc", 1);
//        sortObject.append("tssTesCount", -1);
//        sortObject.append("etype", 1);
//        sortList.add(sortObject);
//        query.setSortList(sortList);
        return super.find(query, start, limit);
    }

    @Override
    public Integer count(GeneRankCriteria criteria) {
        SmartDBObject query = buildQueryByCriteria(criteria);
        return super.count(query);
    }

    @Override
    public Integer count(Integer sampleId) {
        if (null == sampleId || sampleId <= 0) {
            return 0;
        }
        SmartDBObject query = new SmartDBObject("sampleId", sampleId);
        return super.count(query);
    }

    @Override
    public List<GeneRank> listByGeneId(Integer geneId) {
        SmartDBObject query = new SmartDBObject("geneId", geneId);
        query.put("mixturePerc", new BasicDBObject("$lte", 0.01));
        query.addReturnFields("sampleId", "tss5kRank", "source", "etype");
        return super.find(query);
    }

    @Override
    public List<Integer> listRankByCriteria(GeneRankCriteria criteria, String field) {
        SmartDBObject query = buildQueryByCriteria(criteria);
        field = null == field ? "TSS5K" : field.toUpperCase();
        SortType sortType = SortType.ASC;
        String _field = "TSSTES".equals(field) ? "tssTesRank" : "TSS5K".equals(field) ? "tss5kRank" : "tssT5Rank";
        query.addReturnFields(_field);
        query.addSort(_field, sortType);
        return super.find(query, Integer.class);
    }

    @Override
    public List<Double> percentile(GeneRankCriteria criteria) {
        SmartDBObject query = buildQueryByCriteria(criteria);
        SortType sortType = SortType.ASC;
        query.addReturnFields("mixturePerc");
        query.addSort("mixturePerc", sortType);
        return super.find(query, Double.class);
    }

    @Override
    public void removeBySampleId(Integer sampleId) {
        super.delete(new SmartDBObject("sampleId", sampleId));
      /*  synchronized (sampleId + ".generank.locked") {
            int sharding[][] = new int[8][];
            sharding[0] = new int[] { 30000, 100000 };
            sharding[1] = new int[] { 25710, 30000 };
            sharding[2] = new int[] { 21410, 25710 };
            sharding[3] = new int[] { 17160, 21410 };
            sharding[4] = new int[] { 12860, 17160 };
            sharding[5] = new int[] { 8610, 12860 };
            sharding[6] = new int[] { 4310, 8610 };
            sharding[7] = new int[] { 0, 4310 };
            DateTime start = DateTime.now();
            for (int i = 0; i < sharding.length; i++) {
                int[] shard = sharding[i];
                SmartDBObject query = new SmartDBObject("sampleId", sampleId);
                SmartDBObject geneRange = new SmartDBObject("$gt", shard[0]);
                geneRange.put("$lte", shard[1]);
                query.put("geneId", geneRange);
                if (logger.isDebugEnabled()) {
                    logger.debug("remove:{}", query);
                }
                try {
                    super.delete(query);
                } catch (Exception e) {
                    logger.info("remove failed.", e);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("remove generank by sampleId:{} used {}.", sampleId, DateTimeUtils.used(start));
            }
        }*/
    }

    public static void main(String[] args) {
//        DAOFactory.getDAO(IGeneRankDAO.class).removeBySampleId(400015);
    	 GeneRankCriteria criteria = new GeneRankCriteria();
    	 criteria.setGeneId(30236);
    	 criteria.setMixturePerc(0.01);
    	 GeneRankDAOImpl g = new GeneRankDAOImpl();
    	 List<GeneRank> generanks = g.findByCriteria(criteria, 0, 10);
    	 System.out.println(generanks.size());
    }
}
