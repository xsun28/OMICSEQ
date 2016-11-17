package com.omicseq.web.serviceimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.omicseq.bean.Paginator;
import com.omicseq.common.Constants;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.robot.message.FileInfoConsumer;
import com.omicseq.store.criteria.StatisticCriteria;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.MiscUtils;
import com.omicseq.web.service.IStatisticService;

@Service
public class StatisticService implements IStatisticService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);
    private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    private IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);

    @Override
    public List<StatisticInfo> initInfo(SourceType source) {
        DateTime dt = DateTime.now();
        File dir = new File(source.path());
        if (!dir.exists()) {
            throw new OmicSeqException(dir.getPath() + " not exits");
        } else {
            List<StatisticInfo> data = new ArrayList<StatisticInfo>(5);
            Set<String> names = new HashSet<String>(Arrays.asList(dir.list()));
            SmartDBObject query = new SmartDBObject("source", source.value());
            List<Sample> samples = sampleDAO.find(query);
            List<StatisticInfo> coll = statisticInfoDAO.find(query);
            Set<Integer> cache = new HashSet<Integer>(5);
            for (StatisticInfo info : coll) {
                cache.add(info.getSampleId());
            }

            for (Sample sample : samples) {
                if (cache.contains(sample.getSampleId())) {
                    continue;
                }
                String fname = FilenameUtils.getName(sample.getUrl());
                if (!names.contains(fname)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("not find {} from {} ", fname, dir.getPath());
                    }
                    continue;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("found {} from {} ", fname, dir.getPath());
                }
                StatisticInfo info = new StatisticInfo();
                info.setSampleId(sample.getSampleId());
                info.setSource(source.value());
                info.setPath(String.format("%s/%s", dir.getPath(), fname));
                info.setServerIp(MiscUtils.getServerIP());
                info.setPriority(StringUtils.equalsIgnoreCase("input", sample.getSettype()) ? 99 : 0);
                info.setState(StatisticInfoStatus.DEFAULT.value());
                data.add(info);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Prepare write {} records", data.size());
            }
            if (CollectionUtils.isNotEmpty(data)) {
                statisticInfoDAO.create(data);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("init {} used {} ", source.name(), DateTimeUtils.diff(dt, DateTime.now()));
            }
            return data;
        }
    }

    @Override
    public List<StatisticInfo> findByCriteria(StatisticCriteria criteria, Paginator paginator) {
        return statisticInfoDAO.findByCriteria(criteria, paginator);
    }
    
    public static void main(String[] args) {
    	SampleCache.getInstance().init();
    	GeneCache.getInstance().init();
    	StatisticService staticservice = new StatisticService();
    	staticservice.exec(101865);
	}
    
    @Override
    public void exec(Integer sampleId) {
        final StatisticInfo info = statisticInfoDAO.getBySampleId(sampleId);
        if (null == info) {
            throw new OmicSeqException("not found info by SampleId:" + sampleId);
        }
        String serverIP = MiscUtils.getServerIP();
        if (!serverIP.equals(info.getServerIp())) {
            String msg = String.format("current server is %s,file server is %s", serverIP, info.getServerIp());
            throw new OmicSeqException(msg);
        }
        FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                FileInfoConsumer.getInstance().consume(info);
                return true;
            }
        });
        ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
    }

    @Override
    public void check(final Boolean all, final SourceType source) {
        FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DateTime dt = DateTime.now();
                SmartDBObject query = new SmartDBObject("state", StatisticInfoStatus.PROCESSED.value());
                query.put("source", source.value());
                if (!Boolean.TRUE.equals(all)) {
                    query.put("serverIp", MiscUtils.getServerIP());
                }
                query.addSort("sampleId", SortType.ASC);
                query.addSort("priority", SortType.DESC);
                List<StatisticInfo> coll = statisticInfoDAO.find(query);
                for (StatisticInfo info : coll) {
                    FileInfoConsumer.getInstance().check(info);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("check generank data used {}", DateTimeUtils.used(dt));
                }
                return true;
            }
        });
        ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
    }

    @Override
    public void check(Integer sampleId) {
        SmartDBObject query = new SmartDBObject("sampleId", sampleId);
        query.put("state", StatisticInfoStatus.PROCESSED.value());
        List<StatisticInfo> coll = statisticInfoDAO.find(query);
        for (StatisticInfo info : coll) {
            FileInfoConsumer.getInstance().check(info);
        }
    }

}
