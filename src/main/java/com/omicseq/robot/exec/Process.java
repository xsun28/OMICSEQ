package com.omicseq.robot.exec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.HashDB;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.robot.message.FileInfoProducer;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.MiscUtils;
import com.omicseq.utils.ThreadUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class Process extends AbstractLifeCycle {
    private static Logger logger = LoggerFactory.getLogger(Process.class);
    private static Process single = new Process();
    private IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    private IHashDBDAO hashDBDAO = DAOFactory.getDAO(IHashDBDAO.class);
    private int limit = 80;// 每次获取10条数据进行处理
    private int sleep = 60;// 线程暂停时间 second 默认1分钟
    private Boolean input;

    private Process() {
    }

    public void setInput(Boolean input) {
        this.input = input;
    }

    public void refresh() {
        SampleCache.getInstance().refresh();
    }

    @Override
    public void start() {
        if (isStoped()) {
            super.start();
            this.run();
        } else {
            logger.info("Process thread is running!");
        }
    }

    private void run() {
        // 添加重复处理
        Set<Integer> cache = new HashSet<Integer>(5);
        while (true) {
            if (isStoped()) {
                return;
            }
            Integer size = FileInfoProducer.getInstance().size();
            if (size > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("wait {}  after ", size);
                }
                ThreadUtils.sleep(sleep * 1000);
                continue;
            }
            HashDB mt = hashDBDAO.getByKey("message_threads");
            if (null != mt && null != mt.getValue()) {
                FileInfoProducer.getInstance().updateThreads(Integer.valueOf(mt.getValue(), 10));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("runinng processing ");
            }
            List<StatisticInfo> coll = statisticInfoDAO.findUnProcessed(MiscUtils.getServerIP(), this.input, 0, limit);
            if (CollectionUtils.isNotEmpty(coll)) {
                int count = 0;
                for (StatisticInfo info : coll) {
//                	info.setPath(info.getPath().replace("/files/download/arrayexpress", "D:/ArrayExpress/txt  table"));
                    if (logger.isDebugEnabled()) {
                        logger.debug("send {} to processing. ", info);
                    }
                    Integer sampleId = info.getSampleId();
                    if (cache.contains(sampleId)) {
                        continue;
                    }
                    FileInfoProducer.getInstance().produce(info);
                    cache.add(sampleId);
                    ++count;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("processing data size .{} ", count);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Not data need processing .");
                }
            }
            ThreadUtils.sleep(sleep * 1000);
        }
    }

    public static void main(String[] args) {
        logger.debug("This server is {} ", MiscUtils.getServerIP());
        if (null != args) {
            single.setInput(Boolean.valueOf(args[0]));
        }
        single.start();
    }

    public static Process getInstance() {
        return single;
    }

}
