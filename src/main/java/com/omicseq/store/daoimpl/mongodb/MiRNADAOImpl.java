package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.SourceType;
import com.omicseq.domain.MiRNA;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.helper.MongodbHelper;

public class MiRNADAOImpl extends GenericMongoDBDAO<MiRNA> implements ImiRNADAO {

	@Override
	public Integer getSquence(SourceType source) {
		return super.sequence(source.name(), 1);
	}

	@Override
	public List<MiRNA> loadMiRNAList(String name, Integer limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MiRNA findByName(String name) {
		// TODO Auto-generated method stub
		return super.findOne(new SmartDBObject("miRNAName",name));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MiRNA> fuzzyQuery(String feild, String query, Integer limit) {
		if (StringUtils.isBlank(feild)) {
            return Collections.EMPTY_LIST;
        }
		return super.find(MongodbHelper.startLike(feild, query), 0, limit);
	}
}
