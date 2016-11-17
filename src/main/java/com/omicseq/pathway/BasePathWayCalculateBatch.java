package com.omicseq.pathway;

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
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public abstract class BasePathWayCalculateBatch extends AbstractLifeCycle {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    protected static GeneCache geneCache = GeneCache.getInstance();
    protected int threads = 3;

    static abstract class BaseCallable<T, R extends BasePathWayCalculateBatch> implements Callable<T> {
        protected R ref;

        public BaseCallable(R ref) {
            this.ref = ref;
        }
    }

    protected List<WaitFutureTask<Object>> buildTasks() {
        List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(1);
        Semaphore semaphore = new Semaphore(3);

        WaitFutureTask<Object> task = new WaitFutureTask<Object>(getCallable(), semaphore);
        tasks.add(task);
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

    protected abstract Callable<Object> getCallable();

}
