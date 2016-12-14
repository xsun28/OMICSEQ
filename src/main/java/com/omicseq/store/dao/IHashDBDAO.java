package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.HashDB;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public interface IHashDBDAO extends IGenericDAO<HashDB> {

    /**
     * @param key
     * @return
     */
    HashDB getByKey(String key);

    /**
     * @param val
     * @return
     */
    HashDB getByValue(String val);

    /**
     * @param query
     */
    void delete(SmartDBObject query);
    
    public List<HashDB> find(SmartDBObject query, Integer start, Integer limit);
    /**
     * @param query
     * @param start
     * @param limit
     * @param cls
     * @return
     */
    public <V> List<V> find(SmartDBObject query, Integer start, Integer limit, Class<V> cls) ;
    
    List<HashDB> loadValue(Integer start, Integer limit);

}
