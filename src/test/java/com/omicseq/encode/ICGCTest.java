package com.omicseq.encode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;

public class ICGCTest {

	
	public static void main(String[] args) {
		try {
			String fileName = "E:\\projects\\new_omicseq\\gene_expression.BRCA-US.tsv.gz";
			BufferedReader bufferedReader;
			if (fileName.endsWith(".gz")) {
				bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
			} else {
				bufferedReader = new BufferedReader(new FileReader(fileName));
			}
			String line = "";
			Integer count = 0;
			Long start = System.currentTimeMillis();
			Set<String> idSet = new HashSet<String>();
			while (StringUtils.isNotBlank(line = bufferedReader.readLine())) {
				String[] lineArray = line.split("\t");
				idSet.add(lineArray[3]);
				count = count + 1;
				System.out.println(" line is : " + line);
				if (count > 10) {
					break;
				}
				if (count % 100000 == 0) {
					System.out.println(" using time : " + String.valueOf(System.currentTimeMillis() - start) + " for count : " + count);
				}
			}
			System.out.println(" count is : " + count + "; sample size : " + idSet.size());
			
			/*
			String line = "";
			Integer count = 0;
			//FileWriter fileWriter = new FileWriter(new File("E:\\ICGCTop10000.txt"));
			Set<String> codeSet = new HashSet<String>();
			Map<Integer, String> headerMap = new HashMap<Integer, String>();
			List<String> sampleHeaderList = new ArrayList<String>();
			Map<String,List<String>> valueListMap = new HashMap<String, List<String>>();
			while(StringUtils.isNoneBlank(line = bufferedReader.readLine())) {
				count = count + 1;
				if (count > 1) {
					System.out.println("line is " + line);
					String[] lineArray = line.split("\t");
					Integer valueCount = 0;
					for (String element : lineArray) {
						valueCount = valueCount + 1;
						String header = headerMap.get(valueCount);
						List<String> valueList = valueListMap.get(header);
						if (valueList == null) {
							valueList = new ArrayList<String>();
							valueListMap.put(header, valueList);
						}
						valueList.add(element);
					}

					System.out.println(" lineArray size : " + lineArray.length);
					String donorId = lineArray[0];
					String projectCode = lineArray[1];
					String geneInfo = lineArray[6];
					String normalizedCount = lineArray[13];
					//System.out.println(" geneInfo : " + geneInfo + "; normalizedCount : " + normalizedCount);
					//System.out.println("  donorId,projectcode is : " + donorId+"_"+projectCode);
					codeSet.add(donorId+"_"+projectCode);
				} else {
					System.out.println("line is " + line);
					String[] lineArray = line.split("\t");
					Integer headerCount = 0;
					Integer sampleCount = -1;
					for (String element : lineArray) {
						headerCount = headerCount + 1;
						headerMap.put(headerCount, element);
						if ("gene_build_version".equalsIgnoreCase(element)) {
							sampleCount = headerCount;
						}
						if (sampleCount != -1) {
							sampleHeaderList.add(element);
						}
					}
				}
				if (count > 10) {
					break;
				}
			}
			
			for (String key : valueListMap.keySet()) {
				System.out.println("header:" + key + "; value:" + valueListMap.get(key).toString());
			}
			
			//fileWriter.close();
			System.out.println(" count is : " + count);
			System.out.println(" codeSet size is : " + codeSet.size());
			System.out.println(" sampleHeaderList: " + sampleHeaderList.toString());
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
