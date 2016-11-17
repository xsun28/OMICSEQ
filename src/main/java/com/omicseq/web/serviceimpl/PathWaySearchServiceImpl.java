package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.omicseq.core.PathWayCache;
import com.omicseq.pathway.PathWay;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.web.service.IPathWaySearchService;

@Service
public class PathWaySearchServiceImpl implements IPathWaySearchService {

	private IPathWayDAO pathWayDAO = DAOFactory.getDAO(IPathWayDAO.class);
	
	@Override
	public List<PathWay> search(String query) {
		int size = 10;
		List<PathWay> pathWayList = PathWayCache.getInstance().likeQuery(query, size);
		List<PathWay> finalList = new ArrayList<PathWay>();
		if(CollectionUtils.isNotEmpty(pathWayList))
		{
			finalList.addAll(pathWayList);
		}
		query = query.toUpperCase();
		
		finalList.addAll(pathWayDAO.fuzzyQuery("pathwayName", query, 10));
		return finalList;
	}

	@Override
	public List<PathWay> searchByFirstCharactor(String key) {
		SmartDBObject query = new SmartDBObject("status", 1);
		query.put("pathwayName", new SmartDBObject("$regex", "^"+key));
		List<PathWay> finalList = pathWayDAO.find(query);
		return finalList;
	}

}
