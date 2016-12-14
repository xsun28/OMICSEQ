package com.omicseq.statistic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.GeneCountType;
import com.omicseq.common.SourceType;
import com.omicseq.core.EntrezeSymbolCache;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.ObjectPair;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticResult;
import com.omicseq.domain.TxrRef;
import com.omicseq.exception.StatisticException;
import com.omicseq.utils.ConvertUtil;
import com.omicseq.utils.MathUtils;

public class TCGARankStatistic implements IRankStatistic {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static List<ObjectPair<String, GeneCountType>> genePostionPairList = new ArrayList<ObjectPair<String, GeneCountType>>();
	static {
		genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.TES.csv", GeneCountType.tss_tes));
	}
	
	public TCGARankStatistic() {
		super();
	}
	
	public StatisticResult computeRank(List<ObjectPair<Integer, String>> sampleIdPairList) {
		if (CollectionUtils.isEmpty(sampleIdPairList)) {
			StatisticResult statisticResult = new StatisticResult(); 
			statisticResult.setGeneRankList(Collections.EMPTY_LIST);
			return statisticResult;
		}
		
		List<GeneRank> geneRankList = new ArrayList<GeneRank>();
		for (ObjectPair<Integer, String> objectPair : sampleIdPairList) {
			StatisticResult statisticResult = computeRank(objectPair.getObject2(), objectPair.getObject1());
			geneRankList.addAll(statisticResult.getGeneRankList());
		}
		StatisticResult statisticResult = new StatisticResult(); 
		statisticResult.setGeneRankList(geneRankList);
		
		Collections.sort(geneRankList, ComparatorFactory.getGeneIdComparator());
		return statisticResult;
	}
	
	
	@Override
	public StatisticResult computeRank(String samplePath, Integer sampleId) {
		List<GeneRank> geneRankList = computeCount(samplePath, sampleId);
		updateRank(geneRankList);
		List<Integer> geneIdList = GeneCache.getInstance().getGeneIds();
		Map<Integer, GeneRank> geneRankMap = new HashMap<Integer, GeneRank>();
		for (GeneRank geneRank : geneRankList) {
			geneRankMap.put(geneRank.getGeneId(), geneRank);
		}
		//Integer count = 0;
		for (Integer geneId : geneIdList) {
			if (!geneRankMap.containsKey(geneId)) {
				StringBuffer geneBuffer = new StringBuffer();
				 List<Gene> geneList = GeneCache.getInstance().getGeneById(geneId);
				for (Gene gene : geneList) {
					geneBuffer.append(gene.getTxName() + ",");
				}
				///fileWriter.newLine();
				//count = count + geneList.size();
				//fileWriter.write(geneBuffer.toString());
			}
		}
		
		StatisticResult statisticResult = new StatisticResult();
		statisticResult.setGeneRankList(geneRankList);
		Collections.sort(geneRankList, new Comparator<GeneRank>() {
			@Override
			public int compare(GeneRank o1, GeneRank o2) {
				if (null != o1.getGeneId() && null != o2.getGeneId()) {
					return o1.getGeneId().compareTo(o2.getGeneId());
				} else {
					return 0;
				}
			}
		});
		return statisticResult;
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
			geneRank.setTss5kPerc(1.0);
			geneRank.setTotalCount(geneRankList.size());
			geneRank.setMixturePerc(tssTesPerc);
		}
		// update count ==
	}
	
	private List<GeneRank> computeCount(String samplePath, Integer sampleId) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(" print sample path : " + samplePath);
			}
			BufferedReader bufferReader = new BufferedReader(new FileReader(samplePath));
			List<GeneRank> geneRankList = new ArrayList<GeneRank>();
			Map<Integer, GeneRank> geneRankMap = new HashMap<Integer, GeneRank>();
			boolean isV1 = true;
			//Integer totalCount = 0;
			//BufferedWriter  fileWriter = new BufferedWriter(new FileWriter("E://brecord.txt"));
			
			Sample sample = SampleCache.getInstance().get(sampleId);
			if (sample == null) {
				throw new StatisticException(" can't find sample for " + sampleId);
			}
			String line = null;
			Integer count = 0;
			Set<Integer> geneIdSet = new HashSet<Integer>();
			while(StringUtils.isNotBlank(line = bufferReader.readLine())) {
				if (count == 0) {
					if (line.contains("RPKM")) {
						isV1 = true;
					} else {
						isV1 = false;
					}
				} else {
					Double tssTesCount = 0.0;
					String[] values = line.split("\t");
					String geneSymbol = values[0].split("\\|")[0];
					if (isV1) {
						tssTesCount = ConvertUtil.toDouble(values[3], 0.0);
					} else {
						tssTesCount = ConvertUtil.toDouble(values[1], 0.0);
					}
					
					
					List<TxrRef>  txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
					// if txrefList it empty, then retrieve genesymbol by entrezeid
					if (CollectionUtils.isEmpty(txrRefList)) {
						String[] ids = values[0].split("\\|");
						if (ids.length >= 2) {
							String entrezeId = ids[1].replaceAll("_calculated", "");
							geneSymbol = EntrezeSymbolCache.getInstance().getGeneSymbol(entrezeId);
							if (StringUtils.isNoneBlank(geneSymbol)) {
								txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
							}
						}
					}
					Integer refCount = 0;
					if (CollectionUtils.isNotEmpty(txrRefList)) {
						for (TxrRef txrRef : txrRefList) {
							if (txrRef != null) {
								Gene gene = GeneCache.getInstance().getGeneByName(txrRef.getRefseq());
								if (gene != null) {
									if (!geneIdSet.add(gene.getGeneId())) {
										continue;
									}
									refCount = refCount + 1;
									GeneRank geneRank = geneRankMap.get(gene.getGeneId());
									if (geneRank != null) {
										geneRank.setTssTesCount(Math.max(tssTesCount, geneRank.getTssTesCount()));
									} else {
										geneRank = new GeneRank();
										geneRank.setGeneId(gene.getGeneId());
										geneRank.setSampleId(sampleId);
										geneRank.setTss5kCount(0.0);
										geneRank.setTssT5Count(0.0);
										geneRank.setTssTesCount(tssTesCount);
										geneRank.setSource(SourceType.TCGA.value());
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
				count = count + 1;
			}
			//fileWriter.flush();
			//fileWriter.close();
			return geneRankList;
		} catch (FileNotFoundException e) {
			logger.error(" sample " + samplePath + " don't been found ", e);
			return Collections.EMPTY_LIST;
		} catch (IOException e) {
			logger.error(" sample " + samplePath + " don't been found ", e);
			return Collections.EMPTY_LIST;
		}
		
	}
	
}
