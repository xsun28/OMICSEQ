package com.omicseq.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitFutureTask<T> extends FutureTask<T> {
    private static Logger logger = LoggerFactory.getLogger(WaitFutureTask.class);
    private Semaphore semaphore;

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public WaitFutureTask(Callable<T> callable, Semaphore semaphore) {
        super(callable);
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Exception e) {
            logger.error("run thread failed!", e);
        } finally {
            if (semaphore != null) {
                semaphore.release();
            }
        }
    }

}
