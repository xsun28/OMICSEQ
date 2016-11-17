package com.omicseq.store.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.GeneIdMappingCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;

public class GeneRankImp extends BaseImp {
    private static final int threads = 7;
    private static IGeneRankDAO dao = DAOFactory.getDAO(IGeneRankDAO.class);
    private static GeneRankImp single = new GeneRankImp();
    private static GeneIdMappingCache mappingCache = GeneIdMappingCache.getInstance();
    private List<GeneRank> sampleList = new ArrayList<GeneRank>(5);
    private Integer sampleId = null;
    private long cnt1 = 0;
    private long cnt2 = 0;
    private DateTime start = null;

    public static void main(String[] args) throws Exception {
        try {
            mappingCache.init();
            dao.clean();
            single.start = DateTime.now();
            if (null != args && args.length != 0) {
                for (String file : args) {
                    single.impl(file);
                }
            } else {
                String file = "./src/test/resources/gene.rank.hg19.csv";
                single.impl(file);
            }
            single.writeToDB();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    @Override
    void doProcess(String[] lines) {
        // s.id,gene.id,gene.tss.tes.rank,gene.tss.tes.counts,gene.tss.5k.rank,
        // gene.tss.5k.counts,gene.tss.tes.5k.rank,gene.tss.tes.5k.counts
        if ("s.id".equalsIgnoreCase(lines[0])) {
            return;
        }
        Integer sid = toInteger(lines, 0, "sampleId", "");
        if (null == sampleId) {
            sampleId = sid;
        }
        if (!sampleId.equals(sid)) {
            writeToDB();
            sampleId = sid;
        }
        try {
            GeneRank obj = new GeneRank();
            obj.setSampleId(sid);
            Integer geneId = toInteger(lines, 1, "geneId", "SampleId:" + sid);
            geneId = mappingCache.getNewId(geneId);
            obj.setGeneId(geneId);
            obj.setSource(SourceType.ENCODE.value());
            obj.setEtype(ExperimentType.CHIP_SEQ_TF.getValue());
            String msg = String.format("SampleId:[%s],GeneId:[%s]", sid, obj.getGeneId());
            obj.setTssTesRank(toInteger(lines, 2, "tssTesRank", msg));
            obj.setTssTesCount(toDouble(lines, 3, "tssTesCount", msg));
            obj.setTss5kRank(toInteger(lines, 4, "tss5kRank", msg));
            obj.setTss5kCount(toDouble(lines, 5, "tss5kCoun", msg));
            obj.setTssT5Rank(toInteger(lines, 6, "tssT5Rank", msg));
            obj.setTssT5Count(toDouble(lines, 7, "tssT5Count", msg));
            sampleList.add(obj);
        } catch (Exception e) {
            String msg = "导入数据异常:" + StringUtils.join(lines, ",");
            logger.error(msg, e);
        }
    }

    private Double toDouble(String[] lines, int i, String field, String msg) {
        try {
            return toDouble(lines[i]);
        } catch (Exception e) {
            logger.error("{}:{}", msg, field);
        }
        return null;
    }

    private Integer toInteger(String[] lines, int i, String field, String msg) {
        try {
            return toInteger(lines[i]);
        } catch (Exception e) {
            logger.error("{}:{}", msg, field);
        }
        return null;
    }

    private void writeToDB() {
        if (CollectionUtils.isEmpty(sampleList)) {
            return;
        }
        cnt1 += sampleList.size();
        DateTime dt = DateTime.now();
        List<GeneRank> temp = new ArrayList<GeneRank>(sampleList);
        sampleList.clear();
        List<GeneRank> data = new ArrayList<GeneRank>(5);
        Map<Integer, Integer> idxMap = new HashMap<Integer, Integer>(5);
        for (GeneRank item : temp) {
            Integer geneId = item.getGeneId();
            Integer idx = idxMap.get(geneId);
            if (null == idx) {
                data.add(item);
                idxMap.put(geneId, data.indexOf(item));
            } else {
                data.set(idx, item);
            }
        }
        int size = data.size();
        cnt2 += size;
        int limt = size % threads == 0 ? size / threads : size / threads + 1;
        logger.debug("分{}线程写入总记录数{},每线程写入{}行", threads, size, limt);
        try {
            Semaphore semaphore = new Semaphore(threads);
            List<WaitFutureTask<Boolean>> taskList = new ArrayList<WaitFutureTask<Boolean>>(7);
            for (int i = 0; i < threads; i++) {
                int start = i * limt;
                if (start >= size) {
                    break;
                }
                int end = start + limt;
                end = end > size ? size : end;
                List<GeneRank> rs = data.subList(start, end);
                logger.debug("start begin write begin:{},end:{},size:{} ", start, end, rs.size());
                WaitFutureTask<Boolean> task = new WaitFutureTask<Boolean>(new GeneRankCall(rs), semaphore);
                taskList.add(task);
            }

            ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(taskList, 1l, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.error("write to db failed!", e);
        }
        data.clear();
        idxMap.clear();
        logger.debug("writed {} records used {} ", size, DateTimeUtils.diff(dt, DateTime.now()));
        logger.debug("完成导入文件数据{}行,实际导入数据库记录{}条", cnt1, cnt2);
        logger.debug("已执行:{} ", DateTimeUtils.diff(start, DateTime.now()));
    }

    class GeneRankCall implements Callable<Boolean> {
        private List<GeneRank> data;

        public GeneRankCall(List<GeneRank> rs) {
            this.data = rs;
        }

        @Override
        public Boolean call() throws Exception {
            if (CollectionUtils.isNotEmpty(data)) {
                DateTime dt = DateTime.now();
                dao.create(data);
                logger.debug("Writed data used {}", DateTimeUtils.diff(dt, DateTime.now()));
            }
            return Boolean.TRUE;
        }
    }
}
