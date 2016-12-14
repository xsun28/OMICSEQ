package com.omicseq.statistic;

import java.util.List;

import com.omicseq.common.Constants;
import com.omicseq.common.SortType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class ChipSeq5KCountModify {
	
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);

	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	public static void main(String[] args) {
		SmartDBObject query = new SmartDBObject("source", 2);
		query.put("etype", 1);
		query.put("deleted", 0);
		query.put("readCount", new SmartDBObject("$gt", 0));
		List<Sample> samples = sampleDAO.find(query, 2934, 50);
		ChipSeq5KCountModify modify = new ChipSeq5KCountModify();
		for(Sample sample : samples)
		{
			if(!"input".equalsIgnoreCase(sample.getSettype()))
			{
				modify.modify5KCount(sample);
			}
		}
	}

	private void modify5KCount(Sample sample) {
		SmartDBObject query = new SmartDBObject("sampleId", sample.getSampleId());
		query.addSort("mixturePerc", SortType.ASC);
		
		List<GeneRank> generankList = geneRankDAO.find(query);
		Double readCount = sample.getReadCount().doubleValue()/1000000;
		for(GeneRank geneRank : generankList)
		{
			Double tss5KCount = geneRank.getTss5kCount();
			if(tss5KCount != null)
			{
				tss5KCount = tss5KCount/readCount;
				geneRank.setTss5kCount(tss5KCount);
			}
		}
		geneRankDAO.removeBySampleId(sample.getSampleId());
		geneRankDAO.create(generankList);
	}

}
