package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.domain.HashDB;
import com.omicseq.store.dao.IHashDBDAO;

public class HashDBDAO extends GenericMongoDBDAO<HashDB> implements IHashDBDAO {

    @Override
    public HashDB getByKey(String key) {
        return findOne(new SmartDBObject("key", key));
    }

    @Override
    public HashDB getByValue(String val) {
        return findOne(new SmartDBObject("value", val));
    }

    @Override
    public void delete(SmartDBObject query) {
        super.delete(query);
    }

	@Override
	public List<HashDB> loadValue(Integer start, Integer limit) {
		 SmartDBObject query = new SmartDBObject();
		 return super.find(query, start, limit);
	}

}
