package com.omicseq.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.exception.ThreadException;

public class ThreadTaskPoolsExecutor extends AbstractThreadTaskPoolsExecutor implements IThreadTaskPoolsExecutor {
    
    public static final String DEFAULT_POOL_NAME = "Default";
    
    protected final ConcurrentMap<String, ThreadPoolExecutor> taskPoolsMap = new ConcurrentHashMap<String, ThreadPoolExecutor>();

    private static ThreadTaskPoolsExecutor instance = new ThreadTaskPoolsExecutor();

    public static ThreadTaskPoolsExecutor getInstance() {
        return instance;
    }

    public static void setInstance(ThreadTaskPoolsExecutor instance) {
        ThreadTaskPoolsExecutor.instance = instance;
    }

    @Override
    protected void createTaskPool(String name, boolean createIfNotExist) {
        getTaskPool(name, createIfNotExist);
    }

    private ThreadPoolExecutor getTaskPool(String name, boolean createIfNotExist) {
        ThreadPoolExecutor taskPool = taskPoolsMap.get(name);
        if (taskPool == null && (DEFAULT_POOL_NAME.equals(name) || createIfNotExist)) {
            synchronized (ThreadTaskPoolsExecutor.class) {
                taskPool = taskPoolsMap.get(name);
                if (taskPool == null) {
                    Integer[] poolLimits = taskPoolLimitMap.get(name);
                    if (poolLimits == null) {
                        // default to min 3 and max 10
                        poolLimits = DEFAULT_POOL_SIZE;
                    }

                    taskPool = new ThreadPoolExecutor(poolLimits[0], poolLimits[1], 600L, TimeUnit.SECONDS,
                            new SynchronousQueue<Runnable>());
                    taskPoolsMap.putIfAbsent(name, taskPool);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Thread task pool(" + name + ") created with threads " + poolLimits[0] + "/"
                                + poolLimits[1]);
                    }
                }
            }
        }
        return taskPool;
    }

    @Override
    public <T> void blockRun(List<? extends WaitFutureTask<T>> taskList, Long time, TimeUnit timeUnit)
            throws ThreadException, InterruptedException {
        blockRun(DEFAULT_POOL_NAME, taskList, time, timeUnit);
    }

    @Override
    public <T> void blockRun(String poolName, List<? extends WaitFutureTask<T>> taskList, Long time, TimeUnit timeUnit)
            throws ThreadException, InterruptedException {
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        if (StringUtils.isBlank(poolName)) {
            poolName = DEFAULT_POOL_NAME;
        }

        ThreadPoolExecutor threadPoolExecutor = this.getTaskPool(poolName, false);

        if (threadPoolExecutor == null) {
            throw new ThreadException(" can't find thread pool: " + poolName);
        }

        Semaphore semaphore = new Semaphore(taskList.size());
        for (WaitFutureTask<T> task : taskList) {
            task.setSemaphore(semaphore);
        }

        for (WaitFutureTask<T> task : taskList) {
            semaphore.tryAcquire();
            threadPoolExecutor.submit(task);
        }

        semaphore.tryAcquire(taskList.size(), time, timeUnit);
    }

    @Override
    public <T> void run(String poolName, List<? extends FutureTask<T>> taskList) {
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        if (StringUtils.isBlank(poolName)) {
            poolName = DEFAULT_POOL_NAME;
        }

        ThreadPoolExecutor threadPoolExecutor = this.getTaskPool(poolName, false);

        if (threadPoolExecutor == null) {
            throw new ThreadException(" can't find thread pool: " + poolName);
        }

        for (FutureTask<T> task : taskList) {
            threadPoolExecutor.submit(task);
        }
    }

    @Override
    public <T> void run(List<? extends FutureTask<T>> taskList) {
        run(DEFAULT_POOL_NAME, taskList);
    }

    @Override
    public <T> void run(String poolName, FutureTask<T> task) {
        if (null == task) {
            return;
        }
        List<FutureTask<T>> taskList = new ArrayList<FutureTask<T>>(1);
        taskList.add(task);
        run(poolName, taskList);
    }

    @Override
    public <T> void run(FutureTask<T> task) {
        run(DEFAULT_POOL_NAME, task);
    }

    public void close() {
        synchronized (ThreadTaskPoolsExecutor.class) {
            Set<String> taskKeyList = taskPoolsMap.keySet();
            if (CollectionUtils.isNotEmpty(taskKeyList)) {
                for (String key : taskKeyList) {
                    taskPoolsMap.get(key).shutdown();
                }
            }
            taskPoolsMap.clear();
        }
    }

}
