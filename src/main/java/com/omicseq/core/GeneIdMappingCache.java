package com.omicseq.core;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.omicseq.domain.GeneIdMapping;
import com.omicseq.store.dao.IGeneIdMappingDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class GeneIdMappingCache extends AbstractCache<Integer, GeneIdMapping> implements IInitializeable {

    private static IGeneIdMappingDAO geneIdMappingDAO = DAOFactory.getDAO(IGeneIdMappingDAO.class);
    private static GeneIdMappingCache single = new GeneIdMappingCache(false);

    public GeneIdMappingCache(boolean lazy) {
        super(lazy);
    }

    @Override
    void doInit() {
        synchronized (GeneIdMappingCache.class) {
            Integer start = 0;
            Integer limit = 3000;
            List<GeneIdMapping> list = null;
            while (CollectionUtils.isNotEmpty(list = geneIdMappingDAO.list(start, limit))) {
                for (GeneIdMapping item : list) {
                    put(item.getOldId(), item);
                }
                start = start + limit;
            }
        }
    }

    @Override
    GeneIdMapping lazyLoad(Integer key) {
        return geneIdMappingDAO.getByGeneId(key);
    }

    public Integer getNewId(Integer geneId) {
        GeneIdMapping mapping = get(geneId);
        return null != mapping ? mapping.getNewId() : null;
    }

    public static GeneIdMappingCache getInstance() {
        return single;
    }

    public Collection<GeneIdMapping> values() {
        return super.values();
    }
}
