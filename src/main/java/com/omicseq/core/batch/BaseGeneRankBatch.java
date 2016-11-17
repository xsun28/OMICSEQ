package com.omicseq.core.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.IThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.core.GeneCache;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public abstract class BaseGeneRankBatch extends AbstractLifeCycle {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    protected static GeneCache geneCache = GeneCache.getInstance();
    protected static ImiRNARankDAO miRankDao = DAOFactory.getDAO(ImiRNARankDAO.class); 
    protected static ImiRNASampleDAO miRNASampleDao = DAOFactory.getDAO(ImiRNASampleDAO.class);
    protected static ImiRNADAO miRnadao = DAOFactory.getDAOByTableType(ImiRNADAO.class, "new");
    
    protected int threads = 3;

    protected List<GeneRankCriteria> buildCriterias() {
        return buildCriterias(false);
    }

    protected List<GeneRankCriteria> buildCriterias(boolean all) {
        List<GeneRankCriteria> rs = new ArrayList<GeneRankCriteria>(5);
        rs.add(new GeneRankCriteria());
        if (all) {
            // 添加组合查询 条件 ,暂未实现
            Object[] objs = new Object[] { SourceType.TCGA, SourceType.ENCODE, SourceType.Roadmap,  ExperimentType.CHIP_SEQ_TF, ExperimentType.RNA_SEQ };
            int len = objs.length;
            int bit = 1 << len;
            for (int i = 1; i <= bit; i++) {
                List<Object> list = new ArrayList<Object>(5);
                for (int j = 0; j < len; j++) {
                    if ((1 << j & i) != 0) {
                        list.add(objs[j]);
                    }
                }
                if (list.size() == 0 || list.size() == len) {
                    continue;
                }
                GeneRankCriteria criteria = new GeneRankCriteria();
                criteria.setSourceList(getSourceTypeList(list));
                criteria.setEtypeList(getExperimentTypeList(list));
                rs.add(criteria);
            }
        }
        return rs;
    }

    private List<Integer> getSourceTypeList(List<Object> list) {
        List<Integer> rs = new ArrayList<Integer>(5);
        for (Object obj : list) {
            if (obj instanceof SourceType) {
                rs.add(((SourceType) obj).value());
            }
        }
        return CollectionUtils.isEmpty(rs) ? null : rs;
    }

    private List<Integer> getExperimentTypeList(List<Object> list) {
        List<Integer> rs = new ArrayList<Integer>(5);
        for (Object obj : list) {
            if (obj instanceof ExperimentType) {
                rs.add(((ExperimentType) obj).getValue());
            }
        }
        return CollectionUtils.isEmpty(rs) ? null : rs;
    }

    static abstract class BaseCallable<T, R extends BaseGeneRankBatch> implements Callable<T> {
        protected R ref;
        protected List<GeneRankCriteria> criteries;

        public BaseCallable(R ref, List<GeneRankCriteria> criteries) {
            this.ref = ref;
            this.criteries = criteries;
        }
    }

    protected List<WaitFutureTask<Object>> buildTasks() {
        List<GeneRankCriteria> criterias = buildCriterias();
        List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(criterias.size());
        Semaphore semaphore = new Semaphore(3);
        int size = criterias.size();
        int batch = size <= threads ? 1 : size % threads == 0 ? size / threads : size / threads + 1;
        for (int i = 0; i < threads; i++) {
            int fromIndex = i * batch;
            int toIndex = fromIndex + batch;
            toIndex = size < toIndex ? size : toIndex;
            List<GeneRankCriteria> sublist = criterias.subList(fromIndex, toIndex);
            WaitFutureTask<Object> task = new WaitFutureTask<Object>(getCallable(sublist), semaphore);
            tasks.add(task);
            if (toIndex >= size) {
                break;
            }
        }
        return tasks;
    }

    public void refresh() {
        final IThreadTaskPoolsExecutor executor = ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor();
        FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    start();
                    List<WaitFutureTask<Object>> tasks = buildTasks();
                    executor.blockRun(tasks, 100l, TimeUnit.DAYS);
                } catch (Exception e) {
                    logger.error("文件生成出错", e);
                } finally {
                    stop();
                }
                return Boolean.TRUE;
            }
        });
        executor.run(task);
    }

    protected abstract Callable<Object> getCallable(List<GeneRankCriteria> criteria);

}
