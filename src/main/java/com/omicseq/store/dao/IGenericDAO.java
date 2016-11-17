package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.BaseDomain;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

/**
 * 
 * 
 * @author zejun.du
 */
public interface IGenericDAO<T extends BaseDomain> {

    /**
     * @param obj
     */
    public void create(T obj);

    /**
     * @param objs
     */
    public void create(List<T> objs);

    /**
     * @param obj
     */
    public void update(T obj);
    
    /**
     * @param query
     * @return
     */
    public List<T> find(SmartDBObject query);
    
    /**
     * @param query
     * @return
     */
    public List<T> find(SmartDBObject query, Integer start, Integer size);

	public T findOne(SmartDBObject query);
	
	public void delete(SmartDBObject query);

}
