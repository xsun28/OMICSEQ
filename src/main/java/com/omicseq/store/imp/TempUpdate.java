package com.omicseq.store.imp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.omicseq.core.GeneIdMappingCache;
import com.omicseq.domain.GeneIdMapping;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class TempUpdate extends BaseImp {
    private static GeneIdMappingCache mappingCache = GeneIdMappingCache.getInstance();
    private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    private static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);

    public static void main(String[] args) {
        try {
            mappingCache.init();
            new TempUpdate().update();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private void update() {
        Collection<GeneIdMapping> values = mappingCache.values();
        Map<Integer, Set<Integer>> newids = new HashMap<Integer, Set<Integer>>(5);
        for (GeneIdMapping map : values) {
            Integer key = map.getNewId();
            if (!newids.containsKey(key)) {
                newids.put(key, new HashSet<Integer>(5));
            }
            newids.get(key).add(map.getOldId());
        }
        logger.debug("check exits geneid ");
        GeneRankCriteria criteria = new GeneRankCriteria();
        Map<Integer, Integer> entry = new HashMap<Integer, Integer>(5);
        Set<Integer> keys = newids.keySet();
        for (Integer key : keys) {
            Set<Integer> set = newids.get(key);
            for (Integer geneId : set) {
                criteria.setGeneId(geneId);
                List<GeneRank> coll = geneRankDAO.findByCriteria(criteria, 0, 1);
                if (CollectionUtils.isNotEmpty(coll)) {
                    entry.put(key, geneId);
                    break;
                }
            }
        }
        logger.debug("update old  geneid ");
        for (Integer key : keys) {
            Set<Integer> set = newids.get(key);
            if (entry.containsKey(key)) {
                Integer oldId = entry.get(key);
                set.remove(oldId);
                for (Integer geneId : set) {
                    logger.debug("update {} to {}", geneId, oldId);
                    geneDAO.update(geneId, oldId);
                }
            } else {
                logger.error("not find data by {}", set);
            }
        }

    }

}
