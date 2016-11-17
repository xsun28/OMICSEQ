package com.omicseq.store.daoimpl.mongodb;

import com.omicseq.domain.SummaryTrackData;
import com.omicseq.store.dao.ISummaryTrackDataDao;

public class SummaryTrackDataDaoImpl extends GenericMongoDBDAO<SummaryTrackData> implements
		ISummaryTrackDataDao {

	@Override
	public void removeByCellAndEtype(String cancerType, int etype) {
		SmartDBObject query = new SmartDBObject("cellType", "TCGA-" + cancerType);
//		query.put("etype", etype);
		super.delete(query);
	}

}
