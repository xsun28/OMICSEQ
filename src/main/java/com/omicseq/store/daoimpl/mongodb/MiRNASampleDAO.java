package com.omicseq.store.daoimpl.mongodb;


import java.util.List;

import com.omicseq.common.SourceType;
import com.omicseq.domain.MiRNASample;
import com.omicseq.store.dao.ImiRNASampleDAO;

/*
 *  author zhengyu zhang
 *  
 */
public class MiRNASampleDAO extends GenericMongoDBDAO<MiRNASample> implements
		ImiRNASampleDAO {
	@Override
	public Integer getSequenceId(SourceType source) {
		return super.sequence(source.name(),1);
	}

	@Override
    public List<MiRNASample> loadMiRNASampleList(Integer start, Integer limit) {
        SmartDBObject query = new SmartDBObject();
        return super.find(query, start, limit);
    }
	
	@Override
    public MiRNASample getByMiRNASampleId(Integer miRNAampleId) {
        return super.findOne(new SmartDBObject("miRNASampleId", miRNAampleId));
    }
}
