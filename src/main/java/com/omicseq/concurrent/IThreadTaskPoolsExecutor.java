package com.omicseq.concurrent;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.omicseq.exception.ThreadException;

public interface IThreadTaskPoolsExecutor {
    void init();

    /**
     * @param poolName
     * @param taskList
     */
    <T> void run(String poolName, List<? extends FutureTask<T>> taskList);

    /**
     * @param taskList
     */
    <T> void run(List<? extends FutureTask<T>> taskList);

    /**
     * @param poolName
     * @param task
     */
    <T> void run(String poolName, FutureTask<T> task);

    /**
     * @param task
     */
    <T> void run(FutureTask<T> task);

    /**
     * concurrent run and block the main thread
     * 
     * @param poolName
     * @param taskList
     * @param time
     * @param timeUnit
     */
    <T> void blockRun(String poolName, List<? extends WaitFutureTask<T>> taskList, Long time, TimeUnit timeUnit)
            throws ThreadException, InterruptedException;

    /**
     * @param taskList
     * @param time
     * @param timeUnit
     * @throws ThreadException
     * @throws InterruptedException
     */
    <T> void blockRun(List<? extends WaitFutureTask<T>> taskList, Long time, TimeUnit timeUnit) throws ThreadException,
            InterruptedException;
    void close();

}
