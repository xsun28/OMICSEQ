package com.omicseq.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zejun.du
 */
public class AbstractLifeCycle implements ILifeCycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AtomicBoolean control = new AtomicBoolean(false);

    /**
     * 
     */
    @Override
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Ready  to start.");
        }
        control.compareAndSet(false, true);
    }

    @Override
    public boolean isRunning() {
        return control.get();
    }

    /**
     * 
     */
    @Override
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("Prepare to stoped.");
        }
        control.compareAndSet(true, false);
    }

    @Override
    public boolean isStoped() {
        return !control.get();
    }
}
