package com.omicseq.store.daoimpl.mongodb;

import org.bson.types.ObjectId;

import com.omicseq.domain.HistoryResult;
import com.omicseq.store.dao.IHistoryResultDAO;

public class HistoryResultDAO extends GenericMongoDBDAO<HistoryResult> implements IHistoryResultDAO{

    @Override
    public HistoryResult findById(String id) {
        SmartDBObject dbObject = new SmartDBObject("_id", new ObjectId(id));
        return super.findOne(dbObject);
    }


}
