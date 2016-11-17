package com.omicseq.statistic.variation;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
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
import com.omicseq.core.SampleCache;
import com.omicseq.core.SampleCountCache;
import com.omicseq.core.VariationGeneCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.ObjectPair;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SampleCount;
import com.omicseq.domain.StatisticResult;
import com.omicseq.domain.VariationGene;
import com.omicseq.domain.VariationRank;
import com.omicseq.exception.StatisticException;
import com.omicseq.statistic.ComparatorFactory;
import com.omicseq.utils.ConvertUtil;
import com.omicseq.utils.MathUtils;
import com.omicseq.utils.ResourceLoadUtils;

public abstract class AbstractVariationRankStatistic implements	IVariationRankStatistic {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static List<ObjectPair<String, GeneCountType>> genePostionPairList = new ArrayList<ObjectPair<String, GeneCountType>>();
    static {
        genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.TES.csv", GeneCountType.tss_tes));
    }
    
    ConcurrentMap<String, List<VariationGene>> geneListMap = new ConcurrentHashMap<String, List<VariationGene>>();
	ConcurrentMap<String, KDTree> kdTreeMap = new ConcurrentHashMap<String, KDTree>();
	
	@Override
	public StatisticResult computeRank(List<ObjectPair<Integer, String>> sampleIdPairList) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public AbstractVariationRankStatistic() {
		super();
		// init kd tree map.
        initKdTreeMap();
	}
	
	private void initKdTreeMap() {
        Set<String> geneKeySet = new HashSet<String>();
        for (ObjectPair<String, GeneCountType> objectPair : genePostionPairList) {
            List<VariationGene> geneList = readGene(objectPair.getObject1(), objectPair.getObject2().name());
            geneListMap.putIfAbsent(objectPair.getObject2().name(), geneList);
            for (VariationGene gene : geneList) {
                String key = StringUtils.trimToEmpty(gene.getChrom()).toLowerCase();
                KDTree kdTree = kdTreeMap.get(key);
                if (kdTree == null) {
                    kdTree = new KDTree(2);
                    kdTreeMap.put(key, kdTree);
                }

                String geneKey = generateKey(gene);
                if (geneKeySet.add(geneKey)) {
                    kdTree.insert(new double[] { gene.getChromStart(), gene.getChromEnd()}, gene);
                }
            }
        }
    }
	
	private List<VariationGene> readGene(String fileName, String countType) {
        try {
            CSVReader reader = new CSVReader(new StringReader(ResourceLoadUtils.load(fileName)), ',');
            // skip the one
            reader.readNext();
            ColumnPositionMappingStrategy<VariationGene> strat = new ColumnPositionMappingStrategy<VariationGene>();
            strat.setType(VariationGene.class);
            String[] columns = new String[] { "chrom", "chromStart", "chromEnd", "width", "strand" }; // the
                                                                                              // fields
                                                                                              // to
                                                                                              // bind
                                                                                              // do
                                                                                              // in
                                                                                              // your
                                                                                              // JavaBean
            strat.setColumnMapping(columns);

            CsvToBean<VariationGene> csv = new CsvToBean<VariationGene>();
            List<VariationGene> tssList = csv.parse(strat, reader);
            List<VariationGene> finalGeneList = new ArrayList<VariationGene>();
            Set<String> removedChrSet = new HashSet<String>();
            Set<String> addChrSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(tssList)) {
                // Set<String> geneKeySet = new HashSet<String>();
                for (VariationGene gene : tssList) {
                	if(!"chr19".equals(gene.getChrom())){
                    	continue;
                    } 
                	
                    if (gene.getChrom().contains("_")
                            || StringUtils.trimToEmpty(gene.getChrom()).toLowerCase().equalsIgnoreCase("chrm")) {
                        removedChrSet.add(gene.getChrom());
                        continue;
                    } else {
                        addChrSet.add(gene.getChrom());
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

    private String generateKey(VariationGene gene) {
        String key = StringUtils.trimToEmpty(gene.getChrom()).toLowerCase();
        return key + "_" + gene.getChromStart() + "_" + gene.getChromEnd();
    }

	@Override
	public StatisticResult computeRank(String filePath, Integer sampleId) {
		Sample sample = SampleCache.getInstance().get(sampleId);
        if (sample == null) {
            throw new StatisticException(" can't find sample for id : " + sampleId);
        }
        
        Iterator<ReadRecord> fileIterator = this.getFileIterator(filePath);
      
        StatisticResult statisticResult = computeCount(fileIterator, sample);
        List<VariationRank> geneRankList = statisticResult.getVariationRank();
        updateRank(geneRankList, sample);
        
        if (null != sampleId) {
            // update sample id.
            for (VariationRank geneRank : geneRankList) {
                geneRank.setSampleId(sampleId);
            }
        }
        Collections.sort(geneRankList, new Comparator<VariationRank>() {
            @Override
            public int compare(VariationRank o1, VariationRank o2) {
                if (null != o1.getVariationId() && null != o2.getVariationId()) {
                    return o1.getVariationId().compareTo(o2.getVariationId());
                } else {
                    return 0;
                }
            }
        });
        // fill in generank;
        statisticResult.setVariationRank(geneRankList);
        return statisticResult;
	}

	private void updateRank(List<VariationRank> geneRankList, Sample sample) {
		Comparator<VariationRank> geneComparator = new Comparator<VariationRank>() {
			@Override
			public int compare(VariationRank o1, VariationRank o2) {
				Double tssTesCount1 = o1.getReadCount();
				Double tssTesCount2 = o2.getReadCount();
				return tssTesCount2.compareTo(tssTesCount1);
			}
		};

        Collections.sort(geneRankList, geneComparator);
        
        if (isChipSeq(sample)) {
            // chip-seq, compute mixture rank
            for (VariationRank geneRank : geneRankList) {
                Double tssTesPerc = MathUtils.floor((geneRankList.indexOf(geneRank)+1)*1.000000/geneRankList.size());
                geneRank.setSource(sample.getSource());
                geneRank.setEtype(sample.getEtype());
                geneRank.setMixturePerc(tssTesPerc);
            }
        } else if (ExperimentType.RNA_SEQ.getValue().compareTo(sample.getEtype()) == 0) {
            // rna-seq,
            for (VariationRank geneRank : geneRankList) {
                Double tssTesPerc = MathUtils.floor((geneRankList.indexOf(geneRank)+1)*1.000000/geneRankList.size());
                geneRank.setSource(sample.getSource());
                geneRank.setEtype(sample.getEtype());
                geneRank.setMixturePerc(tssTesPerc);
            }
        }
	}

	private StatisticResult computeCount(Iterator<ReadRecord> fileIterator, Sample sample) {
		Map<String, Integer> countMaps = new HashMap<String, Integer>();
		Integer size = 0;
        Integer realCount = 0;
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
                    VariationGene gene = (VariationGene) ob;
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
                        logger.debug(" prcocess zie : " + size + ";");
                    }
                }

            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info(" read record failed ", e);
                }
            }
        }


        List<VariationRank> geneRankList = new ArrayList<VariationRank>();
        Integer geneSize = geneListMap.get(GeneCountType.tss_tes.name()).size();
        Set<String> geneIdSet = new HashSet<String>();
        Map<String, SampleCount> inputGeneCountMap = new HashMap<String, SampleCount>();
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
                                preSampleCount.setTssTesCount(preSampleCount.getTssTesCount()
                                        + sampleCount.getTssTesCount());
                            } else {
                                inputGeneCountMap.put(sampleCount.getVariationId(), sampleCount);
                            }
                        }
                    } else {
                        logger.warn(" sample : " + sample.getSampleId()
                                + " don't have input sample count list ; input sample id : "
                                + sample.getInputSampleIds());
                        statisticResult.setVariationRank(geneRankList);
                        return statisticResult;
                    }
                }

                // update count.
                for (SampleCount sampleCount : inputGeneCountMap.values()) {
                    sampleCount.setTssTesCount((realCount.doubleValue() / totalReadCount.doubleValue())
                            * sampleCount.getTssTesCount());
                }
            }
        }
        Collection<VariationGene> listAll =  VariationGeneCache.getInstance().genes();
        for (int i = 0; i < geneSize; i++) {
            // String key =
            // StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase()+
            // StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
            int index = 1;
            VariationGene tsstesGene = geneListMap.get(GeneCountType.tss_tes.name()).get(i);
            String variationId = VariationGeneCache.getInstance().getGeneIdStartAndEnd(tsstesGene.getChromStart(), tsstesGene.getChromEnd(),
                    tsstesGene.getChrom(), listAll);
            if(variationId == null)
            {
            	continue;
            }
            VariationRank geneRank = new VariationRank();
            geneRank.setVariationId(variationId);
//            geneRank.setChromStart(tsstesGene.getChromStart());
//            geneRank.setChromEnd(tsstesGene.getChromEnd());
//            geneRank.setChrom(tsstesGene.getChrom());
            if (!geneIdSet.add(variationId)) {
                continue;
            }

//            Double factor = 66.67;
            for (ObjectPair<String, GeneCountType> objectPair : genePostionPairList) {
                String countType = objectPair.getObject2().name();
                VariationGene gene = geneListMap.get(countType).get(i);
                String startEndKey = generateKey(gene);
                Integer count = countMaps.get(startEndKey);
                if (count == null) {
                    count = 0;
                }
                
                geneRank.setReadCount(count*1.0);
                geneRank.setTotalCount(realCount);

//                Integer width = 1000;
//                Double factorCount = (count.doubleValue() / width.doubleValue()) * factor;
//                SampleCount sampleCount = inputGeneCountMap.get(variationId);
//                if (sampleCount == null && !MapUtils.isEmpty((inputGeneCountMap))) {
//                    logger.warn(" sample : " + sample.getSampleId()
//                            + " don't have entire input sample count list ; input sample id : ");
//                    statisticResult.setGeneRankList(Collections.EMPTY_LIST);
//                    return statisticResult;
//                }
//                if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
//                    if (sampleCount != null) {
//                        factorCount = factorCount - sampleCount.getTssTesCount();
//                    }
//                    factorCount = MathUtils.floor(factorCount);
//                    geneRank.setReadCount(factorCount);
//                }
                index = index + 1;
            }
            geneRank.setCreatedTimestamp(System.currentTimeMillis());
            geneRankList.add(geneRank);
        }
        statisticResult.setReadCount(realCount);
        statisticResult.setVariationRank(geneRankList);
        return statisticResult;
	}
	
	private boolean isChipSeq(Sample sample) {
        return ExperimentType.CHIP_SEQ_TF.getValue().compareTo(sample.getEtype()) == 0
                || ExperimentType.DNASE_SEQ.getValue().compareTo(sample.getEtype()) == 0;
    }

	abstract Iterator<ReadRecord> getFileIterator(String filePath);
	
}
