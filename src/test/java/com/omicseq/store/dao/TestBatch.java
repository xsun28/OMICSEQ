package com.omicseq.store.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.IThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.batch.BaseGeneRankBatch;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public abstract class TestBatch extends AbstractLifeCycle{
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    protected static SampleCache sampleCache = SampleCache.getInstance();
    protected static GeneCache geneCache = GeneCache.getInstance();

    protected int threads = 96;
    
    protected List<WaitFutureTask<Object>> buildTasks() {
//        List<GeneRankCriteria> criterias = buildCriterias();
        List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>();
        Semaphore semaphore = new Semaphore(threads);
        int size = 30274;
        int batch = size <= threads ? 1 : size % threads == 0 ? size / threads : size / threads + 1;
        for (int i = 0; i < batch; i++) {
            int fromIndex = i * threads;
            int toIndex = fromIndex + threads;
            toIndex = size < toIndex ? size : toIndex;
            List<Integer> geneIds = new ArrayList<Integer>(); //TODO
            for(int j=fromIndex;j<toIndex;j++){
            	geneIds.add(j+1);
            }
            WaitFutureTask<Object> task = new WaitFutureTask<Object>(getCallable(geneIds), semaphore);
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

    static abstract class BaseCallable<T, R extends TestBatch> implements Callable<T> {
        protected R ref;
        protected List<Integer> geneIds;

        public BaseCallable(R ref, List<Integer> geneIds) {
            this.ref = ref;
            this.geneIds = geneIds;
        }
    }
    
    protected abstract Callable<Object> getCallable(List<Integer> criteria);
}
