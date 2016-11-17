package com.omicseq.store.imp;

import java.io.FileReader;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.utils.ResourceLoadUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Min.Wang
 *
 */
public class RoadMapTest {

	public static void main(String[] args) {
		try {
			CSVReader reader = new CSVReader(new FileReader("E:\\projects\\omicseq-master(2)\\omicseq-master\\txu\\Roadmap\\samples(4).csv"), ',');
			Integer count = 0; 
			for (String[] line : reader.readAll()) {
				  String exp = line[2];
				  if ("ChIP-Seq input".equalsIgnoreCase(exp) || "DNase hypersensitivity".equalsIgnoreCase(exp)
			                || "mRNA-Seq".equalsIgnoreCase(exp) || exp.startsWith("H")) {
					  String url = line[6];
					  if (StringUtils.isNoneBlank(url)) {
						  count = count + 1;
					  }
				  }
			  }
			System.out.println(" count is : " + count);
		} catch (Exception e) {
			e.printStackTrace();
		}
		  
	}
	
}
