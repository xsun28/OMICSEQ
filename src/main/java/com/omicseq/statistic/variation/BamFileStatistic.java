package com.omicseq.statistic.variation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.Sample;
import com.omicseq.domain.VariationRank;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IVariationRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class BamFileStatistic {
	protected static ISampleDAO sampleDao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IVariationRankDAO variationRankDAO = DAOFactory.getDAO(IVariationRankDAO.class);
	
//	static String rootPath = "E:\\变异\\";
	static String rootPath = "/home/tomcat/";
	static String snpFilePath = rootPath + "all_variations.csv";
	static String bamFilePath = rootPath + "bamfiles.txt";
	static String countFilePath = rootPath + "counts" + File.separator;
	
	public static void main(String[] args) {
		File bamFile = new File(bamFilePath);
		try {
			FileReader bamFR = new FileReader(bamFile);
			BufferedReader bufferReader = new BufferedReader(bamFR);
			Map<String, Integer> fileNameMap = new HashMap<String, Integer>();
			String line = "";
			Integer i=0;
			while(StringUtils.isNoneBlank(line = bufferReader.readLine())) {
				i++;
				String fileName = line.replace("/files/download/encode/", "");
				fileNameMap.put(fileName, i);
			}
			bufferReader.close();
			
			Set<Map.Entry<String, Integer>> set = fileNameMap.entrySet();
	        for (Iterator<Map.Entry<String, Integer>> it = set.iterator(); it.hasNext();) {
	        	Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
	        	
	        	SmartDBObject query = new SmartDBObject();
				query.put("url", new SmartDBObject("$regex", entry.getKey()));
				query.put("settype", "exp");
				Sample sample = sampleDao.findOne(query);
				if(sample != null) {
					variationRankDAO.removeBySampleId(sample.getSampleId());
					Integer countFileNum = entry.getValue();
					String inputSampleIds = sample.getInputSampleIds();
					List<Integer> inputCountFiles = new ArrayList<Integer>();
					if(StringUtils.isNotBlank(inputSampleIds)) {
						String[] sampleIds = inputSampleIds.split(",");
						for(int j=0; j<sampleIds.length; j++) {
							if(StringUtils.isBlank(sampleIds[j])) {
								continue;
							}
							variationRankDAO.removeBySampleId(Integer.parseInt(sampleIds[j]));
							Sample inputSample = sampleDao.findOne(new SmartDBObject("sampleId", Integer.parseInt(sampleIds[j])));
							
							String inputFileName = inputSample.getUrl().split("/")[inputSample.getUrl().split("/").length - 1];
							Integer inputCountFileNum =  fileNameMap.get(inputFileName);
							
							inputCountFiles.add(inputCountFileNum);
						}
					}
					
					createVariationRanks(sample, countFileNum, inputCountFiles);
				}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createVariationRanks(Sample sample, Integer countFileNum, List<Integer> inputCountFiles) {
		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");
		File countFile = new File(countFilePath + countFileNum + ".txt");
		if(!countFile.exists()) {
			return;
		}
		try {
			FileReader fr = new FileReader(countFile);
			BufferedReader bufferReader = new BufferedReader(fr);
			String line = "";
			Integer i=0;
			Integer[] counts = new Integer[310456];
			while(StringUtils.isNoneBlank(line = bufferReader.readLine())) {
				counts[i] = Integer.parseInt(line);
				i++;
			}
			bufferReader.close();
			
			for(int m=0; m<inputCountFiles.size(); m++) {
				if(inputCountFiles.get(m) == null) {
					continue;
				}
				File inputFile = new File(countFilePath + inputCountFiles.get(m) + ".txt");
				FileReader fr_input = new FileReader(inputFile);
				BufferedReader bufferReader_input = new BufferedReader(fr_input);
				String line_input = "";
				int l = 0;
				while(StringUtils.isNoneBlank(line_input = bufferReader_input.readLine())) {
					counts[l] = Math.abs(counts[l] - Integer.parseInt(line_input));
					l++;
				}
				bufferReader_input.close();
			}
			
			FileReader fr_snp = new FileReader(snpFilePath);
			BufferedReader bufferReader_snp = new BufferedReader(fr_snp);
			String line_snp = "";
			Integer i_snp=0;
			List<String> symbolList = new ArrayList<String>();
			while(StringUtils.isNoneBlank(line_snp = bufferReader_snp.readLine())) {
				if(i_snp == 0) {
					i_snp++;
					continue;
				}
				symbolList.add(line_snp.split(",")[0]);
				i_snp++;
			}
			bufferReader_snp.close();
			
			List<SymbolReader> list = new ArrayList<SymbolReader>();
			for(int m=0; m<symbolList.size()-1; m++){
				if(counts[m] < 10) {
					continue;
				}
				SymbolReader sr = new SymbolReader();
				sr.setRead(counts[m]*1.00);
				sr.setSymbol(symbolList.get(m));
				list.add(sr);
			}
			//排序 
			Collections.sort(list, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return o1.getRead().compareTo(o2.getRead()) *(-1);
				}
			});
			
			List<VariationRank> geneRankList = new ArrayList<VariationRank>();
			for(SymbolReader sr: list) {
				VariationRank geneRank = new VariationRank();
				geneRank.setVariationId(sr.getSymbol());
				geneRank.setReadCount(sr.getRead());
				geneRank.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
				geneRank.setOrderNo(list.indexOf(sr)+1);
				geneRank.setTotalCount(list.size());
				geneRank.setSampleId(sample.getSampleId());
				geneRank.setCreatedTimestamp(System.currentTimeMillis());
				geneRank.setEtype(sample.getEtype());
				geneRank.setSource(sample.getSource());
				geneRankList.add(geneRank);
			}
			
			variationRankDAO.create(geneRankList);
			System.out.println(sample.getSampleId() + ":" + sample.getUrl()); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
