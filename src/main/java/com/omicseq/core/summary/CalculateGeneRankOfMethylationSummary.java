package com.omicseq.core.summary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SummaryTrackData;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ISummaryTrackDataDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CalculateGeneRankOfMethylationSummary {
	
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static ISummaryTrackDataDao summaryTrackDao = DAOFactory.getDAO(ISummaryTrackDataDao.class);
	protected static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
	protected static GeneCache geneCache = GeneCache.getInstance();
	protected static List<Integer> geneIds;
	protected static DBCollection  collection = MongoDBManager.getInstance().getCollection("generank", "generank", "summaryOfMethyTumor");
	protected static DBCollection  collectionNormal = MongoDBManager.getInstance().getCollection("generank", "generank", "summaryOfMethyNormal");
	
	public static void main(String[] args) {
		geneCache.init();
		geneIds = geneCache.getGeneIds();
//		String[] cancerTypes = {"ACC", "BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//	        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		
		String[] cancerTypes = {"OV"};
		CalculateGeneRankOfMethylationSummary g = new CalculateGeneRankOfMethylationSummary();
		for(int i=0 ;i < cancerTypes.length; i++)
		{
			String cancerType = cancerTypes[i];
			SmartDBObject query = new SmartDBObject("cell", new SmartDBObject("$regex", "TCGA-"+cancerType.toLowerCase()+"-normal"));
	    	query.put("source", 1);
	    	query.put("etype", ExperimentType.METHYLATION.getValue());
	    	query.put("deleted", 0);
	    	List<Sample> sampleList = sampleDAO.find(query);
	    	
	    	if(sampleList == null || sampleList.size() ==0)
	    	{
	    		continue;
	    	}
			g.createGeneRank(cancerType.toLowerCase());

			//create tumor std dev AND total tumor/normal diff
//			g.createSummaryOfMethyTumor(cancerType.toLowerCase()); 
//			g.createSummaryOfMethyNormal(cancerType.toLowerCase());
			
		}
		
	}
	
	private void createSummaryOfMethyNormal(String cancerType) {
		SmartDBObject query = new SmartDBObject("source", 1);
		query.put("etype", 12);
		query.put("cell", "TCGA-"+ cancerType +"-normal");
		query.put("deleted", 0);
		List<Sample> tumorSampleList = sampleDAO.find(query);
		BasicDBList values = new BasicDBList();
		for(int i=0; i<tumorSampleList.size(); i++)
		{
			values.add(tumorSampleList.get(i).getSampleId());
		}
		
		Integer start = 0;
        Integer limit = 1000000;
		List<GeneRank> tumorGeneRankList = null;
		while(CollectionUtils.isNotEmpty(tumorGeneRankList = geneRankDAO.find(new SmartDBObject("sampleId", new SmartDBObject("$in", values)), start, limit))) {
			List<DBObject> infoDataList = new ArrayList<DBObject>();
			for(GeneRank g : tumorGeneRankList)
			{
				Integer geneId = g.getGeneId();
				Double  count = g.getTssTesCount();
				Integer sampleId = g.getSampleId();
				
				DBObject infoData = new BasicDBObject();  
		        infoData.put("geneId", geneId);  
		        infoData.put("sampleId", sampleId);
		        infoData.put("count", count);
		        infoData.put("cell", cancerType);
		        infoDataList.add(infoData);
			}
			
			collectionNormal.insert(infoDataList);
			
			start = start + limit;
		}
		
	}

	private void createSummaryOfMethyTumor(String cancerType)
	{
		SmartDBObject query = new SmartDBObject("source", 1);
		query.put("etype", 12);
		query.put("cell", "TCGA-"+ cancerType +"-tumor");
		query.put("deleted", 0);
		List<Sample> tumorSampleList = sampleDAO.find(query);
		BasicDBList values = new BasicDBList();
		for(int i=0; i<tumorSampleList.size(); i++)
		{
			values.add(tumorSampleList.get(i).getSampleId());
		}
		
		Integer start = 0;
        Integer limit = 1000000;
		List<GeneRank> tumorGeneRankList = null;
		while(CollectionUtils.isNotEmpty(tumorGeneRankList = geneRankDAO.find(new SmartDBObject("sampleId", new SmartDBObject("$in", values)), start, limit))) {
			List<DBObject> infoDataList = new ArrayList<DBObject>();
			for(GeneRank g : tumorGeneRankList)
			{
				Integer geneId = g.getGeneId();
				Double  count = g.getTssTesCount();
				Integer sampleId = g.getSampleId();
				
				DBObject infoData = new BasicDBObject();  
		        infoData.put("geneId", geneId);  
		        infoData.put("sampleId", sampleId);
		        infoData.put("count", count);
		        infoData.put("cell", cancerType);
		        infoData.put("etype", 12);
		        infoDataList.add(infoData);
			}
			
			collection.insert(infoDataList);
			
			start = start + limit;
		}
		
	}
	
	public void createGeneRank(String cancerType)
	{
		String matched_tumor_normal_diff = "matched tumor/normal diff";
		Integer sampleId_matched_tumor_normal_diff;
		List<SymbolReader> symbolReaderList_matched_tumor_normal_diff = new ArrayList<SymbolReader>();
		
		String normal_average = "normal average";
		String normal_std_dev = "normal std dev";
		Integer sampleId_normal_average;
		Integer sampleId_normal_std_dev;
		List<SymbolReader> symbolReaderList_normal_average = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderLis_normal_std_dev = new ArrayList<SymbolReader>();
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		SmartDBObject query0 = new SmartDBObject("source", 1);
		query0.put("etype", 0);
		query0.put("settype", "Methylation");
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + matched_tumor_normal_diff));
		List<Sample> sampleList_matched_tumor_normal_diff = sampleDAO.find(query0);
		if(sampleList_matched_tumor_normal_diff != null && sampleList_matched_tumor_normal_diff.size() >0)
		{
			sampleId_matched_tumor_normal_diff = sampleList_matched_tumor_normal_diff.get(0).getSampleId();
		} else {
			return;
		}
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + normal_average));
		List<Sample> sampleList_normal_average = sampleDAO.find(query0);
		if(sampleList_normal_average != null && sampleList_normal_average.size() >0)
		{
			if("".equals(sampleList_normal_average.get(0).getInputSampleIds()))
			{
				return;
			}
			
			sampleId_normal_average = sampleList_normal_average.get(0).getSampleId();
		} else {
			return;
		}
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + normal_std_dev));
		List<Sample> sampleList_normal_std_dev = sampleDAO.find(query0);
		if(sampleList_normal_std_dev != null && sampleList_normal_std_dev.size() >0)
		{
			if("".equals(sampleList_normal_std_dev.get(0).getInputSampleIds()))
			{
				return;
			}
			
			sampleId_normal_std_dev = sampleList_normal_std_dev.get(0).getSampleId();
		} else {
			return;
		}
		
		geneRankDAO.removeBySampleId(sampleId_matched_tumor_normal_diff);  //删除原来计算的值
		geneRankDAO.removeBySampleId(sampleId_normal_average);  //删除原来计算的值
		geneRankDAO.removeBySampleId(sampleId_normal_std_dev);  //删除原来计算的值

		for(int i=0; i<geneIds.size(); i++)
		{
			SmartDBObject query = new SmartDBObject("geneId", geneIds.get(i));
			query.put("cellType", "TCGA-"+cancerType);
			query.put("etype", 12);
			List<SummaryTrackData> summaryList = summaryTrackDao.find(query);
			if(summaryList == null || summaryList.size() ==0)
			{
				continue;
			}
			
			Double read1 = 0.0;
			Double read2 = 0.0;
			Double read3 = 0.0;
			for(SummaryTrackData s : summaryList)
			{
				read1 += s.getNormalCount();
				read2 += s.getTumorDiffNormalCount();
				read3 += s.getNormalCount();
			}
			
			SymbolReader sr1 = new SymbolReader();
			SymbolReader sr3 = new SymbolReader();
			
			SymbolReader sr2 = new SymbolReader();
			double sd = 0.0;
			double dav 	= read2/summaryList.size();
			double normalAvg  = read3/summaryList.size();
			double normalStd = 0.0;
			
			for(SummaryTrackData s : summaryList) {
				sd += Math.pow(s.getTumorDiffNormalCount()-dav, 2);
				normalStd += Math.pow(s.getNormalCount()-normalAvg, 2);
			}
			
			double sed = Math.sqrt(sd/(summaryList.size()-1))/Math.sqrt(summaryList.size());

			if(sed <= 0)
			{
				continue;
			}
			
			double t = dav/sed;
		
			double normalDev = Math.sqrt(normalStd/(summaryList.size()-1));
			
			sr2.setRead(t);
			sr2.setGeneId(geneIds.get(i));
			symbolReaderList_matched_tumor_normal_diff.add(sr2);
			
			sr1.setRead(Math.abs(normalAvg));
			sr3.setRead(normalDev);
			sr1.setGeneId(geneIds.get(i));
			sr3.setGeneId(geneIds.get(i));
			symbolReaderList_normal_average.add(sr1);
			symbolReaderLis_normal_std_dev.add(sr3);
			
		}
		
		//排序 
		Collections.sort(symbolReaderList_matched_tumor_normal_diff, new Comparator<SymbolReader>() {
			@Override
			public int compare(SymbolReader o1, SymbolReader o2) {
				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
			}
		});
				
		Collections.sort(symbolReaderList_normal_average, new Comparator<SymbolReader>() {
			@Override
			public int compare(SymbolReader o1, SymbolReader o2) {
				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
			}
		});
		
		Collections.sort(symbolReaderLis_normal_std_dev, new Comparator<SymbolReader>() {
			@Override
			public int compare(SymbolReader o1, SymbolReader o2) {
				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
			}
		});
		
		
		//数据库添加geneRank
		List<GeneRank> geneRanks2 = new ArrayList<GeneRank>();
		for(SymbolReader sr: symbolReaderList_matched_tumor_normal_diff){
			GeneRank gr = new GeneRank();
			gr.setCreatedTimestamp(System.currentTimeMillis());
			gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
			gr.setSource(SourceType.TCGA.value());
			gr.setGeneId(sr.getGeneId());
			gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList_matched_tumor_normal_diff.indexOf(sr)+1)/symbolReaderList_matched_tumor_normal_diff.size())));
			//Tsstescount读数
			gr.setTssTesCount(sr.getRead());
			gr.setTotalCount(symbolReaderList_matched_tumor_normal_diff.size());
			gr.setSampleId(sampleId_matched_tumor_normal_diff);
			geneRanks2.add(gr);
		}
		
		List<GeneRank> geneRanks1 = new ArrayList<GeneRank>();
		for(SymbolReader sr: symbolReaderList_normal_average){
			GeneRank gr = new GeneRank();
			gr.setCreatedTimestamp(System.currentTimeMillis());
			gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
			gr.setSource(SourceType.TCGA.value());
			gr.setGeneId(sr.getGeneId());
			gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList_normal_average.indexOf(sr)+1)/symbolReaderList_normal_average.size())));
			gr.setTssTesCount(sr.getRead());
			gr.setTotalCount(symbolReaderList_normal_average.size());
			gr.setSampleId(sampleId_normal_average);
			geneRanks1.add(gr);
		}
		
		List<GeneRank> geneRanks3 = new ArrayList<GeneRank>();
		for(SymbolReader sr: symbolReaderLis_normal_std_dev){
			GeneRank gr = new GeneRank();
			gr.setCreatedTimestamp(System.currentTimeMillis());
			gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
			gr.setSource(SourceType.TCGA.value());
			gr.setGeneId(sr.getGeneId());
			gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderLis_normal_std_dev.indexOf(sr)+1)/symbolReaderLis_normal_std_dev.size())));
			gr.setTssTesCount(sr.getRead());
			gr.setTotalCount(symbolReaderLis_normal_std_dev.size());
			gr.setSampleId(sampleId_normal_std_dev);
			geneRanks3.add(gr);
		}
		
		geneRankDAO.create(geneRanks2);
		geneRankDAO.create(geneRanks1);
		geneRankDAO.create(geneRanks3);
	}

}
