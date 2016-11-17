package com.omicseq.statistic;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.javaml.core.kdtree.KDTree;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.MapUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import com.omicseq.bean.ReadRecord;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.GeneCountType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.SampleCountCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.ObjectPair;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SampleCount;
import com.omicseq.domain.StatisticResult;
import com.omicseq.exception.StatisticException;
import com.omicseq.utils.ConvertUtil;
import com.omicseq.utils.MathUtils;
import com.omicseq.utils.ResourceLoadUtils;

/**
 * @author Min.Wang
 * 
 */
public abstract class AbstractRankStatistic implements IRankStatistic {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static List<ObjectPair<String, GeneCountType>> genePostionPairList = new ArrayList<ObjectPair<String, GeneCountType>>();
    static {
        genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.TES.csv", GeneCountType.tss_tes));
        genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.5k.csv", GeneCountType.tss_5k));
        genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.TES.5k.csv", GeneCountType.tes_5k));
    }

    ConcurrentMap<String, List<Gene>> geneListMap = new ConcurrentHashMap<String, List<Gene>>();

    ConcurrentMap<String, KDTree> kdTreeMap = new ConcurrentHashMap<String, KDTree>();

    protected AbstractRankStatistic() {
        super();
        // init kd tree map.
        initKdTreeMap();
    }
    
    public StatisticResult computeRank(List<ObjectPair<Integer, String>> sampleIdPairList) {
    	throw new StatisticException(this.getClass() + " don't implement pair computeRank method ");
    }

    @Override
    public StatisticResult computeRank(String samplePath, Integer sampleId) {
        Sample sample = SampleCache.getInstance().get(sampleId);
        if (sample == null) {
            throw new StatisticException(" can't find sample for id : " + sampleId);
        }
        if (!this.validEtype(sample)) {
            throw new StatisticException(" etype: " + sample.getEtype() + " isn't invalid ");
        }
        if (!validInput(sample)) {
            logger.info("check {} InputSampleIds {} ", sampleId, sample.getInputSampleIds());
            throw new StatisticException(" input sample ids : " + sample.getInputSampleIds() + " don't been run ");
        }
        Iterator<ReadRecord> fileIterator = this.getFileIterator(samplePath);
        StatisticResult statisticResult = computeCount(fileIterator, sample);
        List<GeneRank> geneRankList = statisticResult.getGeneRankList();
        updateRank(geneRankList, sample);
        if (null != sampleId) {
            // update sample id.
            for (GeneRank geneRank : geneRankList) {
                geneRank.setSampleId(sampleId);
            }
        }
//        Collections.sort(geneRankList, new Comparator<GeneRank>() {
//            @Override
//            public int compare(GeneRank o1, GeneRank o2) {
//                if (null != o1.getGeneId() && null != o2.getGeneId()) {
//                    return o1.getGeneId().compareTo(o2.getGeneId());
//                } else {
//                    return 0;
//                }
//            }
//        });
        // fill in generank;
        statisticResult.setGeneRankList(geneRankList);
        return statisticResult;
    }

    private boolean validInput(Sample sample) {
        if (StringUtils.equalsIgnoreCase("input", sample.getSettype())) {
            return true;
        }
        if (StringUtils.isBlank(sample.getInputSampleIds())) {
            return true;
        }
        String[] idArray = sample.getInputSampleIds().split(",");
        for (String id : idArray) {
            Sample inputSample = SampleCache.getInstance().get(ConvertUtil.toInteger(id, 0));
            if (inputSample == null) {
                return false;
            }
        }

        return true;
    }

    private boolean validEtype(Sample sample) {
        return isChipSeq(sample) || ExperimentType.RNA_SEQ.getValue().compareTo(sample.getEtype()) == 0;
    }

    private StatisticResult computeCount(Iterator<ReadRecord> fileIterator, Sample sample) {
        Map<String, Integer> countMaps = new HashMap<String, Integer>();
        Integer size = 0;
        Integer realCount = 0;
        Long startTime = System.currentTimeMillis();
        while (true) {
            try {
            	if (!fileIterator.hasNext()) {
            		break;
            	}
                ReadRecord samRecord = fileIterator.next();
                size = size + 1;
                if (samRecord == null) {
                    break;
                }
                String ref = samRecord.getSeqName();
                if (ref.equalsIgnoreCase("random")) {
                    continue;
                }
                if (ref.equalsIgnoreCase("hap")) {
                    continue;
                }
                if (ref.equalsIgnoreCase("chrM")) {
                    continue;
                }
                Integer start = samRecord.getStart();
                Integer end = samRecord.getEnd();
                if (end == 0) {
                    continue;
                }
                // 0<gene.start<start;
                // end<gene.end<maxend
                /*
                 * String strand = "+"; if (samRecord.getFlags() == 16) { strand
                 * = "-"; } // don't need strand match
                 */
                KDTree kdTree = kdTreeMap.get(StringUtils.trimToEmpty(ref).toLowerCase());
                if (kdTree == null) {
                    continue;
                }
                // contain:new double[]{0, end.doubleValue()}, new
                // double[]{start.doubleValue(), maxEnd}
                // overlap:
                // Object[] geneList = kdTree.range(new double[]{0,
                // end.doubleValue()}, new double[]{start.doubleValue(),
                // maxEnd});
                Object[] geneList = kdTree.range(new double[] { 0, start.doubleValue() },
                        new double[] { end.doubleValue(), Integer.MAX_VALUE });
                for (Object ob : geneList) {
                    Gene gene = (Gene) ob;
                    String genekey = generateKey(gene);
                    Integer count = countMaps.get(genekey);
                    if (count == null) {
                        count = 0;
                    }
                    count = count + 1;
                    countMaps.put(genekey, count);
                }

                realCount = realCount + 1;

                if (logger.isDebugEnabled()) {
                    if (size % 10000 == 0) {
                        logger.debug(" prcocess zie : " + size + "; using time : "
                                + String.valueOf(System.currentTimeMillis() - startTime));
                    }
                }

            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info(" read record failed ", e);
                }
            }
        }

        // generate gene key.

        // Double factor = realCount.doubleValue() / Math.pow(10, 6);
        List<GeneRank> geneRankList = new ArrayList<GeneRank>();
        Integer geneSize = geneListMap.get(GeneCountType.tss_tes.name()).size();
        Set<Integer> geneIdSet = new HashSet<Integer>();
        Map<Integer, SampleCount> inputGeneCountMap = new HashMap<Integer, SampleCount>();
        StatisticResult statisticResult = new StatisticResult();
        if (!"input".equalsIgnoreCase(sample.getSettype()) && isChipSeq(sample)) {
            String sampleIds = sample.getInputSampleIds();
            if (StringUtils.isNoneBlank(sampleIds)) {
                String[] idArray = sampleIds.split(",");
                Integer totalReadCount = 0;
                for (String id : idArray) {
                    Sample inputSample = SampleCache.getInstance().get(ConvertUtil.toInteger(id, 0));
                    if (null == inputSample || null == inputSample.getReadCount()) {
                        if (logger.isInfoEnabled()) {
                            logger.info("sample {} is null or readCount is null ", id);
                        }
                    }
                    Integer readCount = inputSample.getReadCount();
                    totalReadCount = totalReadCount + readCount;
                    List<SampleCount> sampleCountList = SampleCountCache.getInstance().getSampleCountById(
                            ConvertUtil.toInteger(id, 0));
                    if (CollectionUtils.isNotEmpty(sampleCountList)) {
                        for (SampleCount sampleCount : sampleCountList) {
                            SampleCount preSampleCount = inputGeneCountMap.get(sampleCount.getGeneId());
                            if (preSampleCount != null) {
                                preSampleCount.setTss5kCount(preSampleCount.getTss5kCount()
                                        + sampleCount.getTss5kCount());
                                preSampleCount.setTssTesCount(preSampleCount.getTssTesCount()
                                        + sampleCount.getTssTesCount());
                                preSampleCount.setTssT5Count(preSampleCount.getTssT5Count()
                                        + sampleCount.getTssT5Count());
                            } else {
                                inputGeneCountMap.put(sampleCount.getGeneId(), sampleCount);
                            }
                        }
                    } else {
                        logger.warn(" sample : " + sample.getSampleId()
                                + " don't have input sample count list ; input sample id : "
                                + sample.getInputSampleIds());
                        statisticResult.setGeneRankList(geneRankList);
                        return statisticResult;
                    }
                }

                // update count.
                for (SampleCount sampleCount : inputGeneCountMap.values()) {
                    sampleCount.setTss5kCount((realCount.doubleValue() / totalReadCount.doubleValue())
                            * sampleCount.getTss5kCount());
                    sampleCount.setTssTesCount((realCount.doubleValue() / totalReadCount.doubleValue())
                            * sampleCount.getTssTesCount());
                    sampleCount.setTssT5Count(((realCount.doubleValue() / totalReadCount.doubleValue()) * sampleCount
                            .getTssT5Count()));
                }
            }
        }

        for (int i = 0; i < geneSize; i++) {
            // String key =
            // StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase()+
            // StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
            int index = 1;
            Gene tsstesGene = geneListMap.get(GeneCountType.tss_tes.name()).get(i);
            Integer geneId = GeneCache.getInstance().getGeneIdStartAndEnd(tsstesGene.getStart(), tsstesGene.getEnd(),
                    tsstesGene.getSeqName());
            GeneRank geneRank = new GeneRank();
            geneRank.setGeneId(geneId);
            geneRank.setStart(tsstesGene.getStart());
            geneRank.setEnd(tsstesGene.getEnd());
            geneRank.setSeqName(tsstesGene.getSeqName());
            if (!geneIdSet.add(geneId)) {
                continue;
            }

            Double factor = 66.67;
            for (ObjectPair<String, GeneCountType> objectPair : genePostionPairList) {
                String countType = objectPair.getObject2().name();
                Gene gene = geneListMap.get(countType).get(i);
                String startEndKey = generateKey(gene);
                Integer count = countMaps.get(startEndKey);
                if (count == null) {
                    count = 0;
                }

                Integer width = gene.getWidth();
                Double factorCount = (count.doubleValue() / width.doubleValue()) * factor;
                SampleCount sampleCount = inputGeneCountMap.get(geneId);
                if (sampleCount == null && !MapUtils.isEmpty((inputGeneCountMap))) {
                    logger.warn(" sample : " + sample.getSampleId()
                            + " don't have entire input sample count list ; input sample id : ");
                    statisticResult.setGeneRankList(Collections.EMPTY_LIST);
                    return statisticResult;
                }
                if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
                    if (sampleCount != null) {
                        factorCount = factorCount - sampleCount.getTssTesCount();
                    }
                    factorCount = MathUtils.floor(factorCount);
                    geneRank.setTssTesCount(factorCount);
                } else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
                    if (sampleCount != null) {
                        factorCount = factorCount - sampleCount.getTss5kCount();
                    }
                    factorCount = MathUtils.floor(factorCount)/(realCount.doubleValue()/1000000);
                    geneRank.setTss5kCount(factorCount);
                } else {
                    if (sampleCount != null) {
                        factorCount = factorCount - sampleCount.getTssT5Count();
                    }
                    factorCount = MathUtils.floor(factorCount);
                    geneRank.setTssT5Count(factorCount);
                }
                index = index + 1;
            }
            geneRank.setCreatedTimestamp(System.currentTimeMillis());
            geneRankList.add(geneRank);
        }
        statisticResult.setReadCount(realCount);
        statisticResult.setGeneRankList(geneRankList);
        return statisticResult;
    }

    private void updateRank(List<GeneRank> geneRankList, Sample sample) {
        for (ObjectPair<String, GeneCountType> pair : genePostionPairList) {
            String countType = pair.getObject2().name();

            Comparator<GeneRank> geneComparator = null;
            if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
                geneComparator = ComparatorFactory.getTssTesComparator();
            } else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
                geneComparator = ComparatorFactory.getTss5kComparator();
            } else {
                geneComparator = ComparatorFactory.getTssTes5kComparator();
            }

            Collections.sort(geneRankList, geneComparator);
            // inverse update
            Map<Double, Integer> countRankMap = new HashMap<Double, Integer>();
            Integer rank = 0;
            Set<String> entrySet = new HashSet<String>();
            int geneSize = computeUniqueSize(geneListMap.get(countType));
            Double minCount = 0.0;
            for (int i = 0; i < geneRankList.size(); i++) {
                GeneRank geneRank = geneRankList.get(i);
                if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
                    minCount = Math.min(minCount, geneRank.getTssTesCount());
                } else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
                    minCount = Math.min(minCount, geneRank.getTss5kCount());
                } else {
                    minCount = Math.min(minCount, geneRank.getTssT5Count());
                }
            }

            for (int i = 0; i < geneRankList.size(); i++) {
                GeneRank geneRank = geneRankList.get(i);
                Double count = null;
                String key = "";
                if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
                    count = geneRank.getTssTesCount();
                    key = geneRank.getSeqName() + "_start_" + geneRank.getStart() + "_end" + geneRank.getEnd();
                } else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
                    count = geneRank.getTss5kCount();
                    key = geneRank.getSeqName() + "_start_" + geneRank.getStart();
                } else {
                    count = geneRank.getTssT5Count();
                    key = geneRank.getSeqName() + "_end_" + geneRank.getEnd();
                }

                if (entrySet.add(key)) {
                    rank = rank + 1;
                }

                Integer position = countRankMap.get(count);
                if (position == null) {
                    position = rank;
                    countRankMap.put(count, position);
                }

                if (count.compareTo(minCount) == 0) {
                    position = geneSize;
                }

                if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
                    geneRank.setTssTesRank(position);
                } else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
                    geneRank.setTss5kRank(position);
                } else {
                    geneRank.setTssT5Rank(position);
                }
            }
        }

        Double tssTesTotal = GeneCache.getInstance().getTssTesTotal().doubleValue();
        Double tss5kTotal = GeneCache.getInstance().getTss5kTotal().doubleValue();
        if (isChipSeq(sample)) {
            // chip-seq, compute mixture rank
            for (GeneRank geneRank : geneRankList) {
                Double tssTesPerc = MathUtils.floor(geneRank.getTssTesRank().doubleValue() / tssTesTotal);
                Double tss5kPerc = MathUtils.floor(geneRank.getTss5kRank().doubleValue() / tss5kTotal);
                geneRank.setSource(sample.getSource());
                geneRank.setEtype(sample.getEtype());
                geneRank.setTssTesPerc(tssTesPerc);
                geneRank.setTss5kPerc(tss5kPerc);
                geneRank.setMixturePerc(Math.min(tssTesPerc, tss5kPerc));
            }
        } else if (ExperimentType.RNA_SEQ.getValue().compareTo(sample.getEtype()) == 0) {
            // rna-seq,
            for (GeneRank geneRank : geneRankList) {
                // geneRank.setMixtureRank(geneRank.getTssTesRank());
                Double tssTesPerc = MathUtils.floor(geneRank.getTssTesRank().doubleValue() / tssTesTotal);
                geneRank.setTssTesPerc(tssTesPerc);
                geneRank.setTss5kPerc(1.0);
                geneRank.setSource(sample.getSource());
                geneRank.setEtype(sample.getEtype());
                geneRank.setMixturePerc(tssTesPerc);
            }
        }
        // update count ==
    }

    private boolean isChipSeq(Sample sample) {
        return ExperimentType.CHIP_SEQ_TF.getValue().compareTo(sample.getEtype()) == 0
                || ExperimentType.DNASE_SEQ.getValue().compareTo(sample.getEtype()) == 0;
    }

    abstract Iterator<ReadRecord> getFileIterator(String filePath);

    private int computeUniqueSize(List<Gene> geneList) {
        int count = 0;
        Set<String> geneKeySet = new HashSet<String>();
        for (Gene gene : geneList) {
            String geneKey = this.generateKey(gene);
            if (geneKeySet.add(geneKey)) {
                count = count + 1;
            }
        }

        return count;
    }

    private void initKdTreeMap() {
        Set<String> geneKeySet = new HashSet<String>();
        for (ObjectPair<String, GeneCountType> objectPair : genePostionPairList) {
            List<Gene> geneList = readGene(objectPair.getObject1(), objectPair.getObject2().name());
            geneListMap.putIfAbsent(objectPair.getObject2().name(), geneList);
            for (Gene gene : geneList) {
                String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase();
                KDTree kdTree = kdTreeMap.get(key);
                if (kdTree == null) {
                    kdTree = new KDTree(2);
                    kdTreeMap.put(key, kdTree);
                }

                String geneKey = generateKey(gene);
                if (geneKeySet.add(geneKey)) {
                    kdTree.insert(new double[] { gene.getStart().doubleValue(), gene.getEnd().doubleValue() }, gene);
                }
            }
        }
    }

    private List<Gene> readGene(String fileName, String countType) {
        try {
            CSVReader reader = new CSVReader(new StringReader(ResourceLoadUtils.load(fileName)), ',');
            // skip the one
            reader.readNext();
            ColumnPositionMappingStrategy<Gene> strat = new ColumnPositionMappingStrategy<Gene>();
            strat.setType(Gene.class);
            String[] columns = new String[] { "seqName", "start", "end", "width", "strand" }; // the
                                                                                              // fields
                                                                                              // to
                                                                                              // bind
                                                                                              // do
                                                                                              // in
                                                                                              // your
                                                                                              // JavaBean
            strat.setColumnMapping(columns);

            CsvToBean<Gene> csv = new CsvToBean<Gene>();
            List<Gene> tssList = csv.parse(strat, reader);
            List<Gene> finalGeneList = new ArrayList<Gene>();
            Set<String> removedChrSet = new HashSet<String>();
            Set<String> addChrSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(tssList)) {
                // Set<String> geneKeySet = new HashSet<String>();
                for (Gene gene : tssList) {
                    if (gene.getSeqName().contains("_")
                            || StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase().equalsIgnoreCase("chrm")) {
                        removedChrSet.add(gene.getSeqName());
                        continue;
                    } else {
                        addChrSet.add(gene.getSeqName());
                    }
                    gene.setCountType(countType);
                    finalGeneList.add(gene);
                }
            }

            return finalGeneList;
        } catch (Exception e) {
            logger.error(" read gene from file failed ", e);
            return null;
        }
    }

    private String generateKey(Gene gene) {
        String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase();
        return key + "_" + gene.getStart() + "_" + gene.getEnd();
    }

}
