package com.omicseq.store.dao;

import java.util.ArrayList;
import java.util.List;

import com.omicseq.domain.Example;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 *
 */
public class ExampleDAOTest {
	
	static IExampleDAO exampleDAO = DAOFactory.getInstance().getDAO(IExampleDAO.class);
	
	public static void main(String[] args) {
		//createRecords();
		for (Example example: exampleDAO.loadExamples()) {
			System.out.println(example.toString());
		}
	}
	
	private static void createRecords() {
		List<Example> examples = new ArrayList<Example>();
		for (int i = 0; i < 100; i++) {
			Example example = new Example();
			example.setAge(i);
			example.setName("min.wang" + i);
			example.setPassword("min" + i);
			example.setType(i%10);
			examples.add(example);
		}
		
		exampleDAO.create(examples);
	}
 	
}
