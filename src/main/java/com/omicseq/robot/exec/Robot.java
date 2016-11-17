package com.omicseq.robot.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.core.AbstractLifeCycle;

public class Robot extends AbstractLifeCycle {
    private static Logger logger = LoggerFactory.getLogger(DownLoad.class);
    private static Robot single = new Robot();

    private Robot() {

    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        super.start();
        exec();
    }

    public void exec() {
        if (logger.isDebugEnabled()) {
            logger.debug("Start robot.");
        }
        if (isStoped()) {
            return;
        }
    }

    public static void main(String[] args) {
        single.start();
    }
}
