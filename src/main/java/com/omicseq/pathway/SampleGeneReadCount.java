package com.omicseq.pathway;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class SampleGeneReadCount {
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static DBCollection  collection;
	
	public static void main(String[] args) {
		try {
			Mongo mongo = new Mongo("112.25.20.155", 27017);
			DB db = mongo.getDB("manage");
			db.authenticate("root", "seqjava".toCharArray());
			collection = db.getCollection("sampleOfGeneReadCount");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Integer start = 0;
        Integer limit = 3000;
        List<Sample> sampleList = null;
        while (CollectionUtils.isNotEmpty(sampleList = sampleDAO.loadSampleList(start, limit))) {
            for (Sample sample : sampleList) {
            	Integer sampleId = sample.getSampleId();
            	SmartDBObject queryRank = new SmartDBObject("sampleId", sampleId);
				try {
					List<GeneRank> genRankList = geneRankDAO.find(queryRank);
					String geneId_count = "";
					int i=1;
					for(GeneRank gr : genRankList)
					{
						Double tssTesCount = gr.getTssTesCount();
						
						if(tssTesCount != null)
						{
							if(i < genRankList.size())
							{
								geneId_count += gr.getGeneId()+"="+tssTesCount+",";
							} else {
								geneId_count += gr.getGeneId()+"="+tssTesCount;
							}
						}
						
						i++;
					}
					
					DBObject infoData = new BasicDBObject();  
			        infoData.put("sampleId", sampleId);
			        infoData.put("geneId_count", geneId_count);  
			        collection.insert(infoData);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
            }
            start = start + limit;
        }
	}

}
