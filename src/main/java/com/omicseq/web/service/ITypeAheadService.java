package com.omicseq.web.service;

import java.util.List;

/**
 * @author Min.Wang
 *
 */
public interface ITypeAheadService {

	List<String> search(String query, String option);
	List<String> search_Mouse(String query, String option);
}
