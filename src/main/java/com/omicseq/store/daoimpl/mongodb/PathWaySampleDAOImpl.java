package com.omicseq.store.daoimpl.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.omicseq.pathway.PathWaySample;
import com.omicseq.store.criteria.PathWayCriteria;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.helper.MongodbHelper;

public class PathWaySampleDAOImpl extends GenericMongoDBDAO<PathWaySample> implements IPathWaySampleDAO {

	@Override
	public Integer countTotal(PathWayCriteria criteria) throws Exception {
		SmartDBObject query = buildQueryByCriteria(criteria);
		
		return super.count(query);
	}

	private SmartDBObject buildQueryByCriteria(PathWayCriteria criteria) {
		Integer pathId = criteria.getPathId();
        SmartDBObject query = new SmartDBObject();
        if (null != criteria.getSampleId()) {
            query.put("sampleId", criteria.getSampleId());
        }
        if (null != pathId) {
            query.put("pathId", pathId);
        }
        if (CollectionUtils.isNotEmpty(criteria.getSourceList())) {
            query = MongodbHelper.and(query, MongodbHelper.in("source", criteria.getSourceList().toArray()));
        }
        if (CollectionUtils.isNotEmpty(criteria.getEtypeList())) {
            query = MongodbHelper.and(query, MongodbHelper.in("etype", criteria.getEtypeList().toArray()));
        }
        return query;
	}

	@Override
	public List<Integer> findSampleIdByPathId(PathWayCriteria criteria)
			throws Exception {
		SmartDBObject query = buildQueryByCriteria(criteria);
		
		List<PathWaySample> ps = super.find(query);
		List<Integer> sampleIds = new ArrayList<Integer>();
		for(PathWaySample p : ps)
		{
			sampleIds.add(p.getSampleId());
		}
		return sampleIds;
	}

}
