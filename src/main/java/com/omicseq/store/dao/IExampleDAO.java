package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.Example;

/**
 * @author Min.Wang
 *
 */
public interface IExampleDAO {

	void create(Example example);
	
	void create(List<Example> examples);
	
	List<Example> complexFind1();
	
	List<Example> complexFind2();
	
	List<Example> inFind();
	
	List<Example> loadExamples();
}
