package com.omicseq.store.daoimpl.mongodb;


import com.omicseq.domain.MiRNARank;
import com.omicseq.store.dao.ImiRNARankDAO;

public class MiRankDAO extends GenericMongoDBDAO<MiRNARank> implements ImiRNARankDAO {

	@Override
	public Integer count(Integer miRNAId) {
		SmartDBObject query  = new SmartDBObject("miRNAId", miRNAId);
		SmartDBObject limit = new SmartDBObject("$lte",0.01);
		query.put("mixtureperc", limit);
		return super.count(query);
	}
	
	@Override
	public Integer count_all(Integer miRNAId) {
		SmartDBObject query  = new SmartDBObject("miRNAId", miRNAId);
		return super.count(query);
	}
}
