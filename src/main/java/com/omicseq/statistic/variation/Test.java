package com.omicseq.statistic.variation;

import java.util.List;

import com.omicseq.common.SortType;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.Sample;
import com.omicseq.domain.VariationRank;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IVariationRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class Test {
	private static IVariationRankDAO variationRankDAO = DAOFactory.getDAO(IVariationRankDAO.class);
	protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	
	public static void main(String[] args) {
//		SampleCache.getInstance().doInit();
//		VariationGeneCache.getInstance().doInit();
//		IVariationRankStatistic proc = VariationStatisticFactory.get(SourceType.ENCODE);
////		StatisticResult result = proc.computeRank("/files/download/encode/wgEncodeHaibTfbsH1hescRxlchPcr1xAlnRep1.bam", 100320);
//		StatisticResult result = proc.computeRank("E:\\wgEncodeHaibTfbsA549Creb1sc240Pcr1xEtoh02AlnRep1.bam", 100006);
//		List<VariationRank> rankList = result.getVariationRank();
//		for(VariationRank rank : rankList)
//		{
//			System.out.println(rank.getVariationId() + " " + rank.getSampleId() + " " + rank.getReadCount() + " " + rank.getMixturePerc());
//		}
//		variationRankDAO.create(rankList);
		SampleCache.getInstance().doInit();
		SmartDBObject query = new SmartDBObject();
		query.put("variationId", "rs440446");
		query.addSort("sampleId", SortType.ASC);
		List<VariationRank> rankList = variationRankDAO.find(query);
		String result = "";
		for(VariationRank vr : rankList) {
			Integer sampleId = vr.getSampleId();
			Sample sample = dao.findOne(new SmartDBObject("sampleId", sampleId));
			result += sample.getSampleId() + "	" + sample.getCell() + "	" + sample.getDetail() + "	" + sample.getVariationReadTotal() + "\n";
		}
		System.out.println(result);
	}

}
