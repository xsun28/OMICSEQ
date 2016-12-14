package com.omicseq.service;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.omicseq.core.WebResourceInitiate;
import com.omicseq.web.service.ITypeAheadService;
import com.omicseq.web.serviceimpl.TypeAheadServiceImpl;

public class TypeAheadServiceTest {

	private static ITypeAheadService service;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WebResourceInitiate.getInstance().init();
		service = new TypeAheadServiceImpl();
	}

	@Test
	public void test() {
		List<String> resultLit = service.search("KLK", "");
		System.out.println(resultLit.toString());
	}

}
