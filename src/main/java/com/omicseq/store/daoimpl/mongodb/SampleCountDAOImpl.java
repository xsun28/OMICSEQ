package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import com.omicseq.domain.SampleCount;
import com.omicseq.store.dao.ISampleCountDAO;

/**
 * @author Min.Wang
 *
 */
public class SampleCountDAOImpl extends GenericMongoDBDAO<SampleCount> implements ISampleCountDAO {

	@Override
	public List<SampleCount> findSampleCountById(Integer sampleId) {
		if (sampleId == null) {
			return Collections.emptyList();
		}
		
		SmartDBObject smartDBObject = new SmartDBObject();
		smartDBObject.put("sampleId", sampleId);
		return super.find(smartDBObject);
	}

    @Override
    public void removeBySampleId(Integer sampleId) {
       super.delete(new SmartDBObject("sampleId", sampleId));
    }

	
}
