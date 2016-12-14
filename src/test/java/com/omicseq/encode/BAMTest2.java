package com.omicseq.encode;

import java.util.ArrayList;
import java.util.List;

import com.omicseq.common.Constants;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SampleCount;
import com.omicseq.domain.StatisticResult;
import com.omicseq.statistic.EncodeRankStatistic;
import com.omicseq.statistic.IRankStatistic;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleCountDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 *
 */
public class BAMTest2 {

	public static void main(String[] args) {
		try {
			IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
			IRankStatistic rankStatistic = new EncodeRankStatistic();
			
			GeneCache.getInstance().init();
			System.out.println(" tss5k size : " + GeneCache.getInstance().getTss5kTotal());
			System.out.println(" tsstes size : " + GeneCache.getInstance().getTssTesTotal());
			SampleCache.getInstance().init();
			//System.out.println(GeneCache.getInstance().getGeneIdStartAndEnd(190930322, 190967910, "chr3"));
			Integer sampleId = 100384;
			StatisticResult  statisticResult  = rankStatistic.computeRank("D:/software/develop/wgEncodeHaibTfbsH1neuronsRxlchV0422111AlnRep2.bam", sampleId);
			List<GeneRank> geneRankList = statisticResult.getGeneRankList();
			//SampleCache.getInstance().init();
			
			//StatisticResult  statisticResult  = new StatisticResult();
			//statisticResult.setReadCount(15223410);
			ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,  Constants.STAT_SUFFIX);
			
			
			/*
			List<GeneRank> geneRankList = new ArrayList<GeneRank>();
			GeneRank geneRank1 = new GeneRank();
			geneRank1.setSampleId(15223410);
			geneRank1.setGeneId(100);
			geneRank1.setTss5kCount(100.11);
			geneRank1.setTssTesCount(105.12);
			geneRank1.setTssT5Count(102.13);
			geneRankList.add(geneRank1);
			*/
			/*
			System.out.println(" readcount is : " + statisticResult.getReadCount());
			System.out.println(" gene rank list size is : " + geneRankList.size());
			*/
			
			Sample sample = SampleCache.getInstance().get(100039);
			sample.setReadCount(statisticResult.getReadCount());
			sampleDAO.update(sample);
			
			ISampleCountDAO sampleCountDAO = DAOFactory.getDAO(ISampleCountDAO.class);
			List<SampleCount> sampleCountList = new ArrayList<SampleCount>();
			for (GeneRank geneRank : geneRankList) {
				SampleCount sampleCount = new SampleCount();
				sampleCount.setSampleId(geneRank.getSampleId());
				sampleCount.setGeneId(geneRank.getGeneId());
				sampleCount.setTssTesCount(geneRank.getTssTesCount());
				sampleCount.setTss5kCount(geneRank.getTss5kCount());
				sampleCount.setTssT5Count(geneRank.getTssT5Count());
				sampleCountList.add(sampleCount);
			}
			geneRankDAO.create(geneRankList);
			sampleCountDAO.create(sampleCountList);
			/*
			CSVWriter csvWriter = new CSVWriter(new FileWriter("E:/projects/omicseq-master/txu/ENCODE/count5.csv"));
	        csvWriter.writeNext(new String[]{"geneId", GeneCountType.tss_tes.name(), GeneCountType.tss_5k.name(), GeneCountType.tes_5k.name()});
	        for (GeneRank geneRank : geneRankList) {
	        	csvWriter.writeNext(new String[]{String.valueOf(geneRank.getGeneId()), String.valueOf(geneRank.getTssTesRank()), String.valueOf(geneRank.getTss5kRank()), String.valueOf(geneRank.getTssT5Rank())});
	        }
	        csvWriter.flush();
	        csvWriter.close();
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}