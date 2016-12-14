package com.omicseq.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.pathway.PathWay;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class PathWayCache extends AbstractCache<String, List<PathWay>> implements
		IInitializeable {
	private IPathWayDAO pathWayDAO = DAOFactory.getDAO(IPathWayDAO.class);
	private static PathWayCache pathWayCache = new PathWayCache();
	
	// the first
    ConcurrentMap<String, PathWay> pathWayMap = new ConcurrentHashMap<String, PathWay>();
    ConcurrentMap<String, List<PathWay>> firstAPathWayListMap = new ConcurrentHashMap<String, List<PathWay>>();

	@Override
	void doInit() {
		synchronized(PathWayCache.class){
            List<PathWay> pathWayList =  pathWayDAO.find(new SmartDBObject());
            for(PathWay pw : pathWayList) {
            	String pwName = pw.getPathwayName();
            	pathWayMap.put(pwName, pw);
            }
		}
	}

	@Override
	List<PathWay> lazyLoad(String key) {
		return pathWayDAO.find(new SmartDBObject("pathwayName",key));
	}
	
	public static PathWayCache getInstance() {
        return pathWayCache;
    }
	
	 public List<PathWay> likeQuery(String query, int size) {
	        if (StringUtils.isBlank(query)) {
	            return Collections.emptyList();
	        }
	        query = query.toLowerCase();
	        String firstA = new String(new char[] { query.toLowerCase().charAt(0) });
	        // add lazy process
	        if (lazy && !firstAPathWayListMap.containsKey(firstA)) {
	            List<PathWay> coll = pathWayDAO.fuzzyQuery("pathwayName", firstA, Integer.MAX_VALUE);
	            coll.addAll(pathWayDAO.fuzzyQuery("pathwayName", firstA.toUpperCase(), Integer.MAX_VALUE));
	            List<PathWay> subSymbolList = new ArrayList<PathWay>();
	            for (PathWay pw : coll) {
	                String key = pw.getPathwayName().toLowerCase();
	                pathWayMap.put(key, pw);
	                subSymbolList.add(pw);
	            }
	            firstAPathWayListMap.put(firstA, subSymbolList);
	        }
	        List<PathWay> keyList = firstAPathWayListMap.get(firstA);
	        if (CollectionUtils.isEmpty(keyList)) {
	            return Collections.emptyList();
	        }
	        List<PathWay> resultList = new ArrayList<PathWay>();
	        for (PathWay pw : keyList) {
	            if (pw.getPathwayName().startsWith(query)) {
	            	PathWay val = pathWayMap.get(pw.getPathwayName());
	                if (!resultList.contains(val)) {
	                    resultList.add(val);
	                }
	            }
	            if (resultList.size() >= size) {
	                break;
	            }
	        }
	        return resultList;
	    }

}
