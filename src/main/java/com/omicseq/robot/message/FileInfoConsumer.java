package com.omicseq.robot.message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.Constants;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.domain.BaseDomain;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SampleCount;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.StatisticResult;
import com.omicseq.message.IMessageConsumer;
import com.omicseq.statistic.IRankStatistic;
import com.omicseq.statistic.StatisticFactory;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IGenericDAO;
import com.omicseq.store.dao.ISampleCountDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class FileInfoConsumer implements IMessageConsumer<StatisticInfo> {
    private static Logger logger = LoggerFactory.getLogger(FileInfoConsumer.class);
    private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    private static ISampleCountDAO sampleCountDAO = DAOFactory.getDAO(ISampleCountDAO.class);
    private static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    private static IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
    private static FileInfoConsumer single = new FileInfoConsumer();

    private FileInfoConsumer() {

    }

    public static FileInfoConsumer getInstance() {
        return single;
    }

    @Override
    public void consume(final StatisticInfo info) {
        Integer state = info.getState();
        if (StatisticInfoStatus.isProcessed(state) || StatisticInfoStatus.PROCESSING.value().equals(state)) {
            if (logger.isInfoEnabled()) {
                logger.info("{} state is {}", info, StatisticInfoStatus.parse(state));
            }
            return;
        }
        try {
            logger.debug("processing " + info);
            String fpath = info.getPath();
            SourceType source = SourceType.parse(info.getSource());
            IRankStatistic proc = StatisticFactory.get(source);
            if (null == proc) {
                logger.error("Not implemented format!");
                return;
            }
            // process file convert to
            Integer sampleId = info.getSampleId();
            DateTime dt = DateTime.now();
            Sample sample = sampleDAO.getBySampleId(sampleId);
            if (StatisticInfoStatus.FAILED.value().equals(state) || StatisticInfoStatus.UNCHECKED.value().equals(state)) {
                if (!SourceType.ICGC.equals(source) && !SourceType.TCGA.equals(source)) {
                    DateTime temp = DateTime.now();
                    geneRankDAO.removeBySampleId(sampleId);
                    if (logger.isDebugEnabled()) {
                        String msg = "clean SampleId:{},data[geneRank] used {}";
                        logger.debug(msg, sampleId, DateTimeUtils.diff(temp, DateTime.now()));
                    }
                }
                if (StringUtils.equalsIgnoreCase("input", sample.getSettype())) {
                    DateTime temp = DateTime.now();
                    sampleCountDAO.removeBySampleId(sampleId);
                    if (logger.isDebugEnabled()) {
                        String msg = "clean SampleId:{},data[sampleCount] used {}";
                        logger.debug(msg, sampleId, DateTimeUtils.diff(temp, DateTime.now()));
                    }
                }
            }
            // update state
            info.setState(StatisticInfoStatus.PROCESSING.value());
            statisticInfoDAO.update(info);
            //
            DateTime temp = DateTime.now();
            if (logger.isDebugEnabled()) {
//            	fpath = fpath.replace("/files/download/arrayexpress", "D:/ArrayExpress/txt  table");
                long b = FileUtils.sizeOf(new File(fpath));
                long kb = b / 1024;
                long mb = kb / 1024;
                logger.debug("process file:{};fileSize:{}B,{}KB,{}MB,{}GB", fpath, b, kb, mb, mb / 1024);
            }
            StatisticResult statisticResult = proc.computeRank(fpath, sampleId);
            if (logger.isDebugEnabled()) {
                long b = FileUtils.sizeOf(new File(fpath));
                long kb = b / 1024;
                long mb = kb / 1024;
                String msg = "computeRank sampleId:{};fileSize:{}B,{}KB,{}MB,{}GB;used {}";
                logger.debug(msg, sampleId, b, kb, mb, mb / 1024, DateTimeUtils.diff(temp, DateTime.now()));
            }
            List<GeneRank> data = statisticResult.getGeneRankList();
            // writeDataToDB(geneRankDAO, data);
            geneRankDAO.create(data);
            // update sample
            if (logger.isDebugEnabled()) {
                logger.debug("readCount is {}", statisticResult.getReadCount());
            }
            // update read count
            sample.setReadCount(statisticResult.getReadCount());
            // update meta data
            if (MapUtils.isNotEmpty(statisticResult.getMetaDataMap())) {
                Map<String, String> map = sample.descMap();
                map.putAll(statisticResult.getMetaDataMap());
                sample.descMap(map);
            }
            sampleDAO.update(sample);
            //
            if (StringUtils.equalsIgnoreCase("input", sample.getSettype())) {
                List<SampleCount> objs = new ArrayList<SampleCount>(data.size());
                for (GeneRank item : data) {
                    SampleCount obj = new SampleCount();
                    obj.setGeneId(item.getGeneId());
                    obj.setSampleId(item.getSampleId());
                    obj.setTss5kCount(item.getTss5kCount());
                    obj.setTssT5Count(item.getTssT5Count());
                    obj.setTssTesCount(item.getTssTesCount());
                    objs.add(obj);
                }
                sampleCountDAO.create(objs);
            }
            info.setState(StatisticInfoStatus.PROCESSED.value());
            statisticInfoDAO.update(info);
            updateFileInfoState(sampleId);
            logger.info("processed file {} used {} ", info, DateTimeUtils.used(dt));
            check(info, sample, data);
            logger.info("processed and checked file {} used {} ", info, DateTimeUtils.used(dt));
        } catch (Exception e) {
            info.setState(StatisticInfoStatus.FAILED.value());
            statisticInfoDAO.update(info);
            logger.error("Process file {} failed!", info, e);
            logger.error("Error:", e);
        } finally {
            System.gc();
        }
    }

    private void updateFileInfoState(Integer sampleId) {
        try {
            FileInfo fileInfo = fileInfoDAO.getBySampleId(sampleId);
            if (null != fileInfo) {
                fileInfo.setState(FileInfoStatus.PROCESSED.value());
            }
        } catch (Exception e) {
            logger.warn("update {} file info to state {} failed:", sampleId, FileInfoStatus.PROCESSED);
        }
    }

    @SuppressWarnings("unused")
    private <T extends BaseDomain> void writeDataToDB(IGenericDAO<T> dao, List<T> data) {
        // 分7个sharding,所以分7次写入
        int threads = 7;
        int size = data.size();
        int limt = size % threads == 0 ? size / threads : size / threads + 1;
        for (int i = 0; i < threads; i++) {
            int start = i * limt;
            if (start >= size) {
                break;
            }
            int end = start + limt;
            end = end > size ? size : end;
            List<T> rs = data.subList(start, end);
            if (logger.isDebugEnabled()) {
                logger.debug("batch write begin:{},end:{},size:{} ", start, end, rs.size());
            }
            dao.create(rs);
        }
    }

    public void check(StatisticInfo info) {
        GeneRankCriteria criteria = new GeneRankCriteria();
        Integer sampleId = info.getSampleId();
        criteria.setSampleId(sampleId);
        List<GeneRank> data = geneRankDAO.findByCriteria(criteria, 0, 101);
        Sample sample = sampleDAO.getBySampleId(sampleId);
        check(info, sample, data);
    }

    /**
     * <pre>
     * 一个就是每个实验都有32000条记录 统计count 
     * 然后就是查100条数据
     * 看count是否都为0
     * 
     * 根据sampleId查geneRank记录等于32745, tssT5Count,tss5kCount
     * </pre>
     * 
     * @param data
     * @param sample
     */
    public void check(final StatisticInfo info, Sample sample, List<GeneRank> data) {
        DateTime dt = DateTime.now();
        Integer sampleId = info.getSampleId();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Valid generank data by sampleId {}", sampleId);
            }
            if (CollectionUtils.isEmpty(data)) {
                logger.info(" {} data is empty ", sampleId);
                info.setState(StatisticInfoStatus.UNCHECKED.value());
                statisticInfoDAO.update(info);
                return;
            }
            if (SourceType.TCGA.value().equals(info.getSource()) || SourceType.ICGC.value().equals(info.getSource())) {
                return;
            }
//            Integer count = data.size();// geneRankDAO.count(sampleId);
//            if (count != 32745) {
//                logger.info(" {} count is {} ne 32745 ", sampleId, count);
//                StatisticInfoStatus status = count % 32745 == 0 ? StatisticInfoStatus.FAILED
//                        : StatisticInfoStatus.UNCHECKED;
//                info.setState(status.value());
//                statisticInfoDAO.update(info);
//                return;
//            }
            if (null == sample.getReadCount() || sample.getReadCount() <= 0) {
                logger.info(" {} readCount is {}", sampleId, sample.getReadCount());
                info.setState(StatisticInfoStatus.UNCHECKED.value());
                statisticInfoDAO.update(info);
                return;
            }

            if (CollectionUtils.isNotEmpty(data) && data.size() > 100) {
                int cnt = 0;
                for (int i = 0; i < 100; i++) {
                    GeneRank obj = data.get(i);
                    if (obj.getTssTesCount() == null || obj.getTssTesCount() == 0 || obj.getTss5kCount() == null
                            || obj.getTss5kCount() == 0) {
                        cnt++;
                    }
                }
                if (cnt >= 90) {
                    logger.info("sample:{}, TsstesCount<=0 or Tss5kCount<=0 count is {}. ", sampleId, cnt);
                    info.setState(StatisticInfoStatus.UNCHECKED.value());
                    statisticInfoDAO.update(info);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("check {} failed!", info, e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Valid generank  {},used {} ", sampleId, DateTimeUtils.used(dt));
            }
        }
    }

    public void createSampleCount(StatisticInfo info) {
        String fpath = info.getPath();
        SourceType source = SourceType.parse(info.getSource());
        IRankStatistic proc = StatisticFactory.get(source);
        Integer sampleId = info.getSampleId();
        Sample sample = sampleDAO.getBySampleId(sampleId);
        DateTime dt = DateTime.now();
        StatisticResult statisticResult = proc.computeRank(fpath, sampleId);
        if (logger.isDebugEnabled()) {
            long b = FileUtils.sizeOf(new File(fpath));
            long kb = b / 1024;
            long mb = kb / 1024;
            String msg = "computeRank sampleId:{};fileSize:{}B,{}KB,{}MB,{}GB;used {}";
            logger.debug(msg, sampleId, b, kb, mb, kb / 1024, DateTimeUtils.used(dt));
        }
        List<GeneRank> data = statisticResult.getGeneRankList();
        //
        if (StringUtils.equalsIgnoreCase("input", sample.getSettype())) {
            List<SampleCount> objs = new ArrayList<SampleCount>(data.size());
            for (GeneRank item : data) {
                SampleCount obj = new SampleCount();
                obj.setGeneId(item.getGeneId());
                obj.setSampleId(item.getSampleId());
                obj.setTss5kCount(item.getTss5kCount());
                obj.setTssT5Count(item.getTssT5Count());
                obj.setTssTesCount(item.getTssTesCount());
                objs.add(obj);
            }
            sampleCountDAO.create(objs);
        }
    }

}
