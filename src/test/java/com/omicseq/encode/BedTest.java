package com.omicseq.encode;

import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.StatisticResult;
import com.omicseq.statistic.IRankStatistic;
import com.omicseq.statistic.RoadmapRankStatistic;


public class BedTest {

	public static void main(String[] args) {
		try {
			GeneCache.getInstance().init();
			SampleCache.getInstance().init();
			//System.out.println(GeneCache.getInstance().getGeneIdStartAndEnd(190930322, 190967910, "chr3"));
			IRankStatistic rankStatistic = new RoadmapRankStatistic();
			StatisticResult  statisticResult =  rankStatistic.computeRank("E:\\projects\\new_omicseq\\GSM669984_BI.Adipose_Nuclei.Input.95.bed\\GSM669984_BI.Adipose_Nuclei.Input.95.bed", 10006);
			Integer readCount = statisticResult.getReadCount();
			System.out.println(" readcount is " + readCount);
			/*
			BedReadRecordIterator iterator = new BedReadRecordIterator("E:\\机器学习课程\\神经网络\\test.bed");
			Integer size = 0;
			Long time = System.currentTimeMillis();Qc
			while(true) {
	        	ReadRecord samRecord = iterator.next();
	        	if (samRecord == null) {
	        		break;
	        	}
	        	size = size + 1;
	        	if (size % 1000000 == 0) {
	        		System.out.println(" process size : " + size + "; using time : " + (System.currentTimeMillis() - time));
	        	}
	        }
	        System.out.println("size is : " + size);
			// TODO Auto-generated catch block
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
}
