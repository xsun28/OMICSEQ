package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.domain.SearchHistory;
import com.omicseq.store.dao.ISearchHistoryDAO;

public class SearchHistoryDAO extends GenericMongoDBDAO<SearchHistory> implements ISearchHistoryDAO {

    @Override
    public List<SearchHistory> findAll(Integer userId) {
        SmartDBObject smartDBObject = new SmartDBObject("userId", userId);
        return super.find(smartDBObject);
    }

    @Override
    public SearchHistory findByKeyword(String keyword, Integer userId) {
        SmartDBObject dbObject = new SmartDBObject("keyword", keyword);
        dbObject.put("userId", userId);
        return super.findOne(dbObject);
    }

}
