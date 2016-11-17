package com.omicseq.robot.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.domain.StatisticInfo;
import com.omicseq.message.MemMessageProducer;
import com.omicseq.message.MemMessageTool;

/**
 * 
 * 
 * @author zejun.du
 */
public class FileInfoProducer extends MemMessageProducer<StatisticInfo> {
    private static Logger logger = LoggerFactory.getLogger(FileInfoProducer.class);
    private static FileInfoProducer single = new FileInfoProducer();

    private FileInfoProducer() {
    }

    public static FileInfoProducer getInstance() {
        return single;
    }

    public Integer size() {
        return MemMessageTool.getInstance().size(getClass().getName());
    }

    public void updateThreads(Integer threads) {
        if (logger.isDebugEnabled()) {
            logger.debug("updating message threads to {}.", threads);
        }
        MemMessageTool.getInstance().updateThreads(getClass().getName(), threads);
        if (logger.isDebugEnabled()) {
            logger.debug("updated message threads to {}.", threads);
        }
    }
}
