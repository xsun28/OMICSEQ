package com.omicseq.core;

/**
 * 
 * @author zejun.du
 */
public interface ILifeCycle {

    /**
     * 
     */
    void start();

    /**
     * @return
     */
    boolean isRunning();

    /**
     * 
     */
    void stop();

    /**
     * @return
     */
    boolean isStoped();
}
