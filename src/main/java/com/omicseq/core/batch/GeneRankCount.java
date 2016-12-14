package com.omicseq.core.batch;

import java.util.List;
import java.util.concurrent.Callable;

import com.omicseq.core.IInitializeable;
import com.omicseq.store.cache.CacheProviderFactory;
import com.omicseq.store.cache.ICacheProvider;
import com.omicseq.store.criteria.GeneRankCriteria;

public class GeneRankCount extends BaseGeneRankBatch implements IInitializeable {
    private static GeneRankCount single = new GeneRankCount();

    private GeneRankCount() {
    }

    @Override
    protected List<GeneRankCriteria> buildCriterias() {
        return super.buildCriterias(true);
    }

    static class CountCallable extends BaseCallable<Object, GeneRankCount> {

        public CountCallable(GeneRankCount ref, List<GeneRankCriteria> criteries) {
            super(ref, criteries);
        }

        @Override
        public Object call() throws Exception {
            try {
                ref.start();
                for (GeneRankCriteria criteria : criteries) {
                    List<Integer> geneIds = geneCache.getGeneIds();
                    for (Integer geneId : geneIds) {
                        criteria.setGeneId(geneId);
                        Integer count = geneRankDAO.count(criteria);
                        ICacheProvider cacheProvider = CacheProviderFactory.getLocalCacheProvider();
                        cacheProvider.set(criteria.generateKey(GeneRankCriteria.CacheCountTempalte), count, 3600l);
                    }
                }
            } catch (Exception e) {
                ref.logger.error("刷新GeneRank计数出错:", e);
            } finally {
                ref.stop();
            }
            return Boolean.TRUE;
        }

    }

    @Override
    protected Callable<Object> getCallable(List<GeneRankCriteria> criteria) {
        return new CountCallable(new GeneRankCount(), criteria);
    }

    @Override
    public void init() {
        single.refresh();
    }

    public static void main(String[] args) {
        // new GeneChart().chart(35650, 500, 250);
        // single.refresh();
        List<GeneRankCriteria> rs = single.buildCriterias();
        System.out.println(rs.size());
        System.out.println(rs);
    }

    public static GeneRankCount getInstance() {
        return single;
    }

}
