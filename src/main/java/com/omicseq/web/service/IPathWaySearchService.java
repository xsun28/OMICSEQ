package com.omicseq.web.service;

import java.util.List;

import com.omicseq.pathway.PathWay;

public interface IPathWaySearchService {
	
	List<PathWay> search(String query); 

	List<PathWay> searchByFirstCharactor(String key); 
}
