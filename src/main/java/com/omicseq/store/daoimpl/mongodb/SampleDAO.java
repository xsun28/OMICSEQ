package com.omicseq.store.daoimpl.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;

import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.helper.MongodbHelper;

/**
 * 
 * 
 * @author zejun.du
 */
public class SampleDAO extends GenericMongoDBDAO<Sample> implements ISampleDAO {

    @Override
    public List<Sample> loadSampleList(Integer start, Integer limit) {
        SmartDBObject query = new SmartDBObject("deleted",0);
        
//        Integer[] etypes = {1};
//        query.put("etype", new SmartDBObject("$in", etypes));
//        Integer[] sources = {2};
//        query.put("source", new SmartDBObject("$in", sources));
        
        query.addSort("sampleId", SortType.ASC);
        return super.find(query, start, limit);
    }

    @Override
    public Sample getBySampleId(Integer sampleId) {
        return super.findOne(new SmartDBObject("sampleId", sampleId));
    }

    @Override
    public Sample getByUrl(String url) {
        return super.findOne(new SmartDBObject("url", url));
    }

    @Override
    public List<Sample> listBySampleIds(List<Integer> sampleIds) {
        if (CollectionUtils.isEmpty(sampleIds)) {
            return Collections.emptyList();
        }
        SmartDBObject query = MongodbHelper.in("sampleId", sampleIds);
        return super.find(query);
    }

    @Override
    public Integer getSequenceId(SourceType source) {
        return super.sequence(source.name(), source.ordinal() * 100000);
    }

    @Override
    public void removeByUrl(String url) {
        super.delete(new SmartDBObject("url", url));
    }

    @Override
    public void removeBy_id(String _id) {
        super.delete(new SmartDBObject("_id", new ObjectId(_id)));
    }

	@Override
	public Integer count(List<Integer> sampleIds, List<Integer> intSourceList,
			List<Integer> intEtypeList) {
		SmartDBObject query = new SmartDBObject();
		if(sampleIds == null || sampleIds.size() ==0)
		{
			return 0;
		}
		MongodbHelper.putIn(query, "sampleId", sampleIds.toArray());
	    if (CollectionUtils.isNotEmpty(intSourceList)) {
	    	query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
		}
		if (CollectionUtils.isNotEmpty(intEtypeList)) {
		    query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
		}
		query.put("deleted", 0);
		return super.count(query);
	}

	@Override
	public List<Integer> findSampleIdBySourceAndEtype(List<Integer> sampleIds,
			List<Integer> intSourceList, List<Integer> intEtypeList) {
		SmartDBObject query = new SmartDBObject();
		if(sampleIds == null || sampleIds.size() ==0)
		{
			return null;
		}
		MongodbHelper.putIn(query, "sampleId", sampleIds.toArray());
	    if (CollectionUtils.isNotEmpty(intSourceList)) {
	    	query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
		}
		if (CollectionUtils.isNotEmpty(intEtypeList)) {
		    query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
		}
		query.put("deleted", 0);
		List<Sample> ps = super.find(query);
		List<Integer> result = new ArrayList<Integer>();
		for(Sample p : ps)
		{
			result.add(p.getSampleId());
		}
		return result;
	}

	public Integer[] countProcessAndInProcess(String showTab) {
		Integer [] counts = new Integer[4];
		SmartDBObject q1 = new SmartDBObject();
		SmartDBObject q2 = new SmartDBObject();
		SmartDBObject q3 = new SmartDBObject();
		SmartDBObject q4 = new SmartDBObject();
		q1.put("deleted", 0);
		q1.put("fromType", new SmartDBObject("$ne","mouse"));
		
		q2.put("deleted", 10);
		q2.put("fromType", new SmartDBObject("$ne","mouse"));
		q3.put("deleted", 0);
		q3.put("fromType", "mouse");
		
		q4.put("deleted", 10);
		q4.put("fromType", "mouse");
		Integer pro_human = 0;
		Integer inPro_human = 0;
		if(showTab.contains("variation")){
			q1.put("source", SourceType.ENCODE.getValue());
			Integer[] etypes = {1,17,18};
			q1.put("etype", new SmartDBObject("$in",etypes));
			q1.put("variationReadTotal", new SmartDBObject("$ne", null));
			pro_human = super.count(q1);
			q1.put("variationReadTotal",null);
			inPro_human = super.count(q1);
		}else{
			pro_human = super.count(q1);
			inPro_human = super.count(q2);
		}
		Integer pro_mouse = super.count(q3);
		Integer inPro_mouse = super.count(q4);
		counts[0] = pro_human;
		counts[1] = inPro_human;
		counts[2] = pro_mouse;
		counts[3] = inPro_mouse;
		return counts;
	}
}
