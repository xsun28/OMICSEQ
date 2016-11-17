package com.omicseq.pathway;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;

public class ModifyPathWaySample {
	private Logger logger = LoggerFactory.getLogger(CalculatePathWayGeneRanks.class);
//	private IPathWaySampleDAO pathWaySampleDao = DAOFactory.getDAO(IPathWaySampleDAO.class);
	private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");

	public static void main(String[] args) {
		ModifyPathWaySample mps = new ModifyPathWaySample();
		mps.modifyPS();
	}

	private void modifyPS() {
		synchronized (ModifyPathWaySample.class) {
			Integer start = 3000;
            Integer limit = 3000;
            List<Sample> sampleList = null;
            DBCollection collection = MongoDBManager.getInstance().getCollection("generank", "generank", "pathwaysample");
            SmartDBObject query = new SmartDBObject();
            List<Integer> intSourceList = new ArrayList<Integer>();
            intSourceList.add(5);
            List<Integer> intEtypeList = new ArrayList<Integer>();
            intEtypeList.add(1);
            query =MongodbHelper.in("source", intSourceList.toArray());
            query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
    		
    		List<Sample> ps = sampleDAO.find(query);
    		List<Integer> result = new ArrayList<Integer>();
    		for(Sample p : ps)
    		{
    			result.add(p.getSampleId());
    		}
    		if(result == null || result.size() == 0)
    		{
    			return;
    		}
    		BasicDBObject b = new BasicDBObject("source",5);
    		b.put("etype",1);
    		int n = collection.update(MongodbHelper.in("sampleId", result.toArray()), new BasicDBObject("$set", b), false, true).getN();
    		
    		logger.debug("modified sourceType: {} etype: {} total: {}", 5, 1, n);
    		
//            while(CollectionUtils.isNotEmpty(sampleList = sampleDAO.loadSampleList(start, limit))) {
//            	for (Sample sample : sampleList) {
//            		Integer sampleId = sample.getSampleId();
//            		Integer source = sample.getSource();
//            		Integer etype = sample.getEtype();
//            		
//            		
////            		pathWaySampleDao.updatePathWaySample(sample);
//            		BasicDBObject b = new BasicDBObject("source", source);
//            		b.put("etype", etype);
//            		collection.update(new BasicDBObject("sampleId", sampleId), new BasicDBObject("$set", b), false, true);
//            		logger.debug("modified sampleId: {}", sampleId);
//            	}
//            	
//            	logger.debug("modified : {}", start);
//            	start = start + limit;
//            }
		}
	}

}
