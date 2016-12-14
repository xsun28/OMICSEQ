package com.omicseq.statistic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.GeneCountType;
import com.omicseq.common.SourceType;
import com.omicseq.core.EnsemblGeneCache;
import com.omicseq.core.EntrezeSymbolCache;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.ICGCSample;
import com.omicseq.domain.ObjectPair;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticResult;
import com.omicseq.domain.TxrRef;
import com.omicseq.exception.StatisticException;
import com.omicseq.utils.ConvertUtil;
import com.omicseq.utils.MathUtils;

/**
 * @author Min.Wang
 *
 */
public class ICGCRankStatistic   implements IRankStatistic   {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static List<ObjectPair<String, GeneCountType>> genePostionPairList = new ArrayList<ObjectPair<String, GeneCountType>>();
	static {
		genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.TES.csv", GeneCountType.tss_tes));
		EnsemblGeneCache.getInstance().init();
	}
	
	
	@Override
	public StatisticResult computeRank(String samplePath, Integer sampleId) {
		ICGCSample icgcSample = readData(samplePath);
		return processSample(icgcSample, sampleId);
		/*
		if (CollectionUtils.isEmpty(icgcSampleList)) {
			return null;
		}
		System.out.println("icgc sample size is : " + icgcSampleList.size());
		List<StatisticResult> resultList = new ArrayList<StatisticResult>();
		for (ICGCSample icgcSample : icgcSampleList) {}
		*/
	}

	private void updateRank(List<GeneRank> geneRankList) {
		for (ObjectPair<String, GeneCountType> pair : genePostionPairList) {
			//String countType = pair.getObject2().name();
			Comparator<GeneRank> geneComparator = ComparatorFactory.getTssTesComparator();;
			Collections.sort(geneRankList, geneComparator);
			// inverse update
			Map<Double, Integer> countRankMap = new HashMap<Double, Integer>();
			Integer rank = 0;
			Set<String> entrySet = new HashSet<String>();
			Integer geneSize = geneRankList.size();
			for (int i = 0; i < geneRankList.size(); i++) {
				GeneRank geneRank = geneRankList.get(i);
				Double count = geneRank.getTssTesCount();
				String key = geneRank.getSeqName() + "_" + "start_"+geneRank.getStart() + "_end" + geneRank.getEnd();
				if (entrySet.add(key)) {
					rank = rank + 1;
				}
				
				Integer position = countRankMap.get(count);
				if (position == null) {
					position = rank;
					countRankMap.put(count, position);
				}
				if (count == 0.0) {
					// upate 
					position = geneSize;
				}
				geneRank.setTssTesRank(position);
			}
		}
		
		//geneRank.setMixtureRank(geneRank.getTssTesRank());
		Double geneCount = ((Integer)geneRankList.size()).doubleValue();
		for (GeneRank geneRank : geneRankList) {
			//geneRank.setMixtureRank(geneRank.getTssTesRank());
			Double tssTesPerc = MathUtils.floor(geneRank.getTssTesRank().doubleValue() / geneCount);
			geneRank.setTssTesPerc(tssTesPerc);
			geneRank.setTotalCount(geneRankList.size());
			geneRank.setTss5kPerc(1.0);
			geneRank.setMixturePerc(tssTesPerc);
		}
		// update count ==
	}
	
	private ICGCSample readData(String filePath) {
		try {
			BufferedReader bufferedReader;
			if (filePath.endsWith(".gz")) {
				bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath))));
			} else {
				bufferedReader = new BufferedReader(new FileReader(filePath));
			}
			
			String line = "";
			Integer count = 0;
			Map<Integer, String> headerMap = new HashMap<Integer, String>();
			Set<String> sampleHeaderSet = new HashSet<String>();
			//Map<String,List<String>> valueListMap = new HashMap<String, List<String>>();
			ICGCSample currentICGCSample = new ICGCSample();
			//List<ICGCSample> icgcSampleList = new ArrayList<ICGCSample>();
			List<Map<String, String>> currentValueMapList = new ArrayList<Map<String,String>>();
			Map<String, String> metaDataMap =  new HashMap<String, String>();
			currentICGCSample.setMetaDataMap(metaDataMap);
			currentICGCSample.setSampleDataMap(currentValueMapList);
			while(StringUtils.isNoneBlank(line = bufferedReader.readLine())) {
				count = count + 1;
				if (count > 1) {
					String[] lineArray = line.split("\t");
					String donorId = lineArray[0];
					String projectCode = lineArray[1];
					currentICGCSample.setDonorId(donorId);
					currentICGCSample.setProjectCode(projectCode);
					Integer valueCount = 0;
					Map<String, String> valueMap = new HashMap<String, String>();
					for (String element : lineArray) {
						valueCount = valueCount + 1;
						String header = headerMap.get(valueCount);
						if (sampleHeaderSet.contains(header) && metaDataMap != null) {
							metaDataMap.put(header, element);
						} else if ("gene_stable_id".equalsIgnoreCase(header) || "normalized_expression_level".equalsIgnoreCase(header)){
							valueMap.put(header, element);
						}
					}
					currentValueMapList.add(valueMap);
				} else {
					String[] lineArray = line.split("\t");
					Integer headerCount = 0;
					Integer sampleCount = -1;
					for (String element : lineArray) {
						headerCount = headerCount + 1;
						headerMap.put(headerCount, element);
						if ("gene_build_version".equalsIgnoreCase(element)) {
							sampleCount = headerCount;
						}
						if (sampleCount != -1) {
							sampleHeaderSet.add(element);
						}
					}
					sampleHeaderSet.add("icgc_donor_id");
					sampleHeaderSet.add("project_code");
					sampleHeaderSet.add("icgc_sample_id");
				}
			}
			
			//return icgcSampleList;
			return currentICGCSample;
		} catch (Exception e) {
			logger.error(" read data from file : " + filePath + " failed", e);
			return null;
		}
	}

	private StatisticResult processSample(ICGCSample icgcSample, Integer sampleId) {
		StatisticResult statisticResult = new StatisticResult();
		statisticResult.setMetaDataMap(icgcSample.getMetaDataMap());
		Map<Integer, GeneRank> geneRankMap = new HashMap<Integer, GeneRank>();
		List<GeneRank> geneRankList = new ArrayList<GeneRank>();
		statisticResult.setGeneRankList(geneRankList);
		Set<Integer> geneIdSet = new HashSet<Integer>();
		for (Map<String, String>  dataMap : icgcSample.getSampleDataMap()) {
			String geneId = dataMap.get("gene_stable_id");
			geneId = extractEnsemblId(geneId);
			//String igcgSampleId = dataMap.get("icgc_sample_id");
			List<String> refseqList = EnsemblGeneCache.getInstance().getRefseq(geneId);
			if (CollectionUtils.isEmpty(refseqList)) {
				List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneId);
				if (CollectionUtils.isNotEmpty(txrRefList)) {
					refseqList = new ArrayList<String>();
					for (TxrRef txrRef : txrRefList) {
						refseqList.add(txrRef.getRefseq());
					}
				}
			}
			if (CollectionUtils.isEmpty(refseqList)) {
				geneId = EntrezeSymbolCache.getInstance().getGeneSymbol(geneId);
				if (StringUtils.isNotBlank(geneId)) {
					List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneId);
					if (CollectionUtils.isNotEmpty(txrRefList)) {
						refseqList = new ArrayList<String>();
						for (TxrRef txrRef : txrRefList) {
							refseqList.add(txrRef.getRefseq());
						}
					}
				}
			}
		
			
			Double tssTesCount = ConvertUtil.toDouble(dataMap.get("normalized_expression_level"), 0.0);
			Sample sample = SampleCache.getInstance().getSampleById(sampleId);
			if (CollectionUtils.isNotEmpty(refseqList)) {
				for (String refseq : refseqList) {
					if (StringUtils.isNotBlank(refseq)) {
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if (gene != null) {
							//TODO, need check it.
							if (!geneIdSet.add(gene.getGeneId())) {
								continue;
							}
							GeneRank geneRank = geneRankMap.get(gene.getGeneId());
							if (geneRank != null) {
								geneRank.setTssTesCount(Math.max(tssTesCount, geneRank.getTssTesCount()));
							} else {
								geneRank = new GeneRank();
								geneRank.setGeneId(gene.getGeneId());
								geneRank.setSampleId(sample.getSampleId());
								geneRank.setTss5kCount(0.0);
								geneRank.setTssT5Count(0.0);
								geneRank.setTssTesCount(tssTesCount);
								geneRank.setSource(SourceType.ICGC.value());
								geneRank.setSeqName(gene.getSeqName());
								geneRank.setStart(gene.getStart());
								geneRank.setEnd(gene.getEnd());
								geneRank.setEtype(sample.getEtype());
								geneRank.setSource(sample.getSource());
								geneRankMap.put(gene.getGeneId(), geneRank);
								geneRankList.add(geneRank);
							}
						}
					}
				}
			}
		}
		
		updateRank(geneRankList);
		Collections.sort(geneRankList, ComparatorFactory.getGeneIdComparator());
		return statisticResult;
	}
	
	private String extractEnsemblId(String value) {
		if (value.startsWith("ENSEMBL:")) {
			value = value.replace("ENSEMBL:", "");
			value = value.split("\\.")[0];
			return value;
		} else {
			return value;
		}
	}
	
	@Override
	public StatisticResult computeRank(List<ObjectPair<Integer, String>> sampleIdPairList) {
		throw new StatisticException(this.getClass() + " don't implement pair computeRank method ");
	}

	

	public static void main(String[] args) {
		GeneCache.getInstance().init();
		TxrRefCache.getInstance().init();
		SampleCache.getInstance().init();
		ICGCRankStatistic icgcRankStatistic = new ICGCRankStatistic();
		StatisticResult rs = icgcRankStatistic.computeRank("F:/SA313760.tsv", 306700);
		System.out.println(CollectionUtils.size(rs.getGeneRankList()));
	}

}
