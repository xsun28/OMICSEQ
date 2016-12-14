package com.omicseq.core.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;

public class StatisticGeneRankOfTumorStdSummary {
	
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
	protected static GeneCache geneCache = GeneCache.getInstance();
	protected static List<Integer> geneIds;
	protected static DBCollection  collection = MongoDBManager.getInstance().getCollection("generank", "generank", "summaryOfCNVTumor");
	protected static DBCollection  collectionNormal = MongoDBManager.getInstance().getCollection("generank", "generank", "summaryOfCNVNormal");
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static void main(String[] args) {
		geneCache.init();
		geneIds = geneCache.getGeneIds();
		String[] cancerTypes = {"BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
	        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		 
//		String[] cancerTypes = {"PRAD","ACC"};
		StatisticGeneRankOfTumorStdSummary g = new StatisticGeneRankOfTumorStdSummary();
		int etype = ExperimentType.CVN.getValue();
		for(int i=0 ;i < cancerTypes.length; i++)
		{
			String cancerType = cancerTypes[i];
			g.stitisticRank(cancerType.toLowerCase(), etype);
			
//			g.removeSummaryData(cancerType.toLowerCase(), etype);
		
		}
	}
	
	private void removeSummaryData(String cancerType, int etype) {
		SmartDBObject query = new SmartDBObject();
		query.put("cell", cancerType);
//		query.put("etype", etype);
		
		DateTime dt = DateTime.now();
		collection.remove(query);
		
		logger.debug("delete:{}, query:{},used {} ", collection.getName(), query, DateTimeUtils.diff(dt, DateTime.now()));
	
	}

	private void stitisticRank(String cancerType, int etype) { 
		List<SymbolReader> symbolReaderList = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderList2 = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderList3 = new ArrayList<SymbolReader>();
//		List<SymbolReader> symbolReaderList_normal_average = new ArrayList<SymbolReader>();
//		List<SymbolReader> symbolReaderList_normal_std_dev = new ArrayList<SymbolReader>();
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		
		SmartDBObject query0 = new SmartDBObject("source", 1);
		query0.put("etype", 0);
		query0.put("settype", new SmartDBObject("$regex","CNV"));
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-total tumor/normal diff"));
		Integer sampleId = 0;
		List<Sample> sampleList = sampleDAO.find(query0);
		if(sampleList != null && sampleList.size() >0) {
			sampleId = sampleList.get(0).getSampleId();
		}else {
			return;
		}
		
//		query0.put("cell", new SmartDBObject("$regex", cancerType + "-tumor std dev"));
//		Integer sampleId2 = 0;
//		List<Sample> sampleList2 = sampleDAO.find(query0);
//		if(sampleList2 != null && sampleList2.size() >0) {
//			sampleId2 = sampleList2.get(0).getSampleId();
//		}else {
//			return;
//		}
//		
//		
//		query0.put("cell", new SmartDBObject("$regex", cancerType + "-tumor average"));
//		Integer sampleId3 = 0;
//		List<Sample> sampleList3 = sampleDAO.find(query0);
//		if(sampleList3 != null && sampleList3.size() >0) {
//			sampleId3 = sampleList3.get(0).getSampleId();
//		}else {
//			return;
//		}
		
		geneRankDAO.removeBySampleId(sampleId);
//		geneRankDAO.removeBySampleId(sampleId2);
//		geneRankDAO.removeBySampleId(sampleId3);
		
		SmartDBObject q = new SmartDBObject("etype", etype);
		q.put("source", 1);
		q.put("cell", new SmartDBObject("$regex", cancerType+"-normal"));
		List<Sample> list = sampleDAO.find(q);
		if(list == null || list.size() ==0)
		{
			/**
			for(int i=0; i<geneIds.size(); i++){
				Double tumorAvg = 0.0;
				SmartDBObject query = new SmartDBObject("geneId", geneIds.get(i));
				query.put("cell", cancerType);
				DBCursor cursor = collection.find(query);
				List<Double> values = new ArrayList<Double>();
				int n=0;
				while (cursor.hasNext()) {
					 DBObject  dbObject =  cursor.next();
					 Double count = Double.parseDouble(dbObject.get("count").toString());
					 values.add(count);
					 tumorAvg += count;
					 n++;
				 }
				if(n == 0)
				{
					continue;
				}
				 tumorAvg = tumorAvg/n;
				 
				 double tumorStd = 0.0;
				 for(Double d : values)
				 {
					 tumorStd += Math.pow(d-tumorAvg, 2);
				 }
				 
				 tumorStd = Math.sqrt(tumorStd/(n-1));
				 
				 SymbolReader sr2 = new SymbolReader();
				 sr2.setGeneId(geneIds.get(i));
				 sr2.setRead(tumorStd);
				 
				 SymbolReader sr3 = new SymbolReader();
				 sr3.setGeneId(geneIds.get(i));
				 sr3.setRead(Math.abs(tumorAvg));
				 
				 symbolReaderList2.add(sr2); 
				 symbolReaderList3.add(sr3);
			}
			
			//排序 
			Collections.sort(symbolReaderList2, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
			
			//排序 
			Collections.sort(symbolReaderList3, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
				
				
			//数据库添加geneRank
			List<GeneRank> geneRanks2 = new ArrayList<GeneRank>();
			for(SymbolReader sr: symbolReaderList2){
				GeneRank gr = new GeneRank();
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
				gr.setSource(SourceType.TCGA.value());
				gr.setGeneId(sr.getGeneId());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList2.indexOf(sr)+1)/symbolReaderList2.size())));
				//Tsstescount读数
				gr.setTssTesCount(sr.getRead());
				gr.setTotalCount(symbolReaderList2.size());
				gr.setSampleId(sampleId2);
				geneRanks2.add(gr);
			}
			
			//数据库添加geneRank
			List<GeneRank> geneRanks3 = new ArrayList<GeneRank>();
			for(SymbolReader sr: symbolReaderList3){
				GeneRank gr = new GeneRank();
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
				gr.setSource(SourceType.TCGA.value());
				gr.setGeneId(sr.getGeneId());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList3.indexOf(sr)+1)/symbolReaderList3.size())));
				//Tsstescount读数
				gr.setTssTesCount(sr.getRead());
				gr.setTotalCount(symbolReaderList3.size());
				gr.setSampleId(sampleId3);
				geneRanks3.add(gr);
			}
			
			geneRankDAO.create(geneRanks2);
			
			geneRankDAO.create(geneRanks3);
		*/
		} else {
//			String normal_average = "normal average";
//			String normal_std_dev = "normal std dev";
//			Integer sampleId_normal_average;
//			Integer sampleId_normal_std_dev;
//			
//			query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + normal_average));
//			List<Sample> sampleList_normal_average = sampleDAO.find(query0);
//			if(sampleList_normal_average != null && sampleList_normal_average.size() >0)
//			{
//				sampleId_normal_average = sampleList_normal_average.get(0).getSampleId();
//			} else {
//				return;
//			}
//			
//			query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + normal_std_dev));
//			List<Sample> sampleList_normal_std_dev = sampleDAO.find(query0);
//			if(sampleList_normal_std_dev != null && sampleList_normal_std_dev.size() >0)
//			{
//				sampleId_normal_std_dev = sampleList_normal_std_dev.get(0).getSampleId();
//			} else {
//				return;
//			}
//			
//			geneRankDAO.removeBySampleId(sampleId_normal_average);
//			geneRankDAO.removeBySampleId(sampleId_normal_std_dev);
			
			for(int i=0; i<geneIds.size(); i++){
				
				List<Gene> genes = GeneCache.getInstance().getGeneById(geneIds.get(i));
				if(genes != null && genes.size() > 0) {
					if(genes.get(0).getSeqName().toLowerCase().equals("chrx") || genes.get(0).getSeqName().toLowerCase().equals("chry"))  {
						continue;
					}
				}
				
				Double tumorAvg = 0.0;
				SmartDBObject query = new SmartDBObject("geneId", geneIds.get(i));
				query.put("cell", cancerType);
				DBCursor cursor = collection.find(query);
				List<Double> values = new ArrayList<Double>();
				int n=1;
				while (cursor.hasNext()) {
					 DBObject  dbObject =  cursor.next();
					 Double count = Double.parseDouble(dbObject.get("count").toString());
					 values.add(count);
					 tumorAvg += count;
					 n++;
				 }
				if(n == 1)
				{
					continue;
				}
				tumorAvg = tumorAvg/n;
				 double tumorStd = 0.0;
				 for(Double d : values)
				 {
					 tumorStd += Math.pow(d-tumorAvg, 2);
				 }
				 tumorStd = Math.sqrt(tumorStd/(n-1));
				
				Double normalAvg = 0.0;
				cursor = collectionNormal.find(query);
				values = new ArrayList<Double>();
				int m=1;
				while (cursor.hasNext()) {
					 DBObject  dbObject =  cursor.next();
					 Double count = Double.parseDouble(dbObject.get("count").toString());
					 values.add(count);
					 normalAvg += count;
					 m++;
				 }
				if(m == 1)
				{
					continue;
				}
				 normalAvg = normalAvg/m;
				 double normalStd = 0.0;
				 for(Double d : values)
				 {
					 normalStd += Math.pow(d-normalAvg, 2);
				 }
				 
				 normalStd = Math.sqrt(normalStd/(m-1));
				 
				 SymbolReader sr2 = new SymbolReader();
				 sr2.setGeneId(geneIds.get(i));
				 sr2.setRead(tumorStd);
				 
				 SymbolReader sr3 = new SymbolReader();
				 sr3.setGeneId(geneIds.get(i));
				 sr3.setRead(Math.abs(tumorAvg));
//				 
//				 SymbolReader sr4 = new SymbolReader();
//				 sr4.setGeneId(geneIds.get(i));
//				 sr4.setRead(Math.abs(normalAvg));
//				 
//				 SymbolReader sr5 = new SymbolReader();
//				 sr5.setGeneId(geneIds.get(i));
//				 sr5.setRead(normalStd);
				 
				 
				 double t = (tumorAvg - normalAvg) / Math.sqrt(Math.pow(tumorStd, 2)/n + Math.pow(normalStd, 2)/m);
				 
				 if(tumorStd == 0.0 && normalStd == 0.0)
				 {
					 t = 0.0;
				 }
				 
				 SymbolReader sr1 = new SymbolReader();
				 sr1.setGeneId(geneIds.get(i));
				 sr1.setRead(t); //2014-10-29 保留tumor-normal值的正负
				 
				 symbolReaderList.add(sr1); 
//				 symbolReaderList2.add(sr2); 
//				 symbolReaderList3.add(sr3);
//				 symbolReaderList_normal_average.add(sr4);
//				 symbolReaderList_normal_std_dev.add(sr5);
			}
			
			
			//排序 
			Collections.sort(symbolReaderList, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
			
//			Collections.sort(symbolReaderList2, new Comparator<SymbolReader>() {
//				@Override
//				public int compare(SymbolReader o1, SymbolReader o2) {
//					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
//				}
//			});
//			
//			//排序 
//			Collections.sort(symbolReaderList3, new Comparator<SymbolReader>() {
//				@Override
//				public int compare(SymbolReader o1, SymbolReader o2) {
//					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
//				}
//			});
//			
//			//排序 
//			Collections.sort(symbolReaderList_normal_average, new Comparator<SymbolReader>() {
//				@Override
//				public int compare(SymbolReader o1, SymbolReader o2) {
//					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
//				}
//			});
//			
//			//排序 
//			Collections.sort(symbolReaderList_normal_std_dev, new Comparator<SymbolReader>() {
//				@Override
//				public int compare(SymbolReader o1, SymbolReader o2) {
//					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
//				}
//			});
			
			//数据库添加geneRank
			List<GeneRank> geneRanks = new ArrayList<GeneRank>();
			for(SymbolReader sr: symbolReaderList){
				GeneRank gr = new GeneRank();
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
				gr.setSource(SourceType.TCGA.value());
				gr.setGeneId(sr.getGeneId());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList.indexOf(sr)+1)/symbolReaderList.size())));
				//Tsstescount读数
				gr.setTssTesCount(sr.getRead());
				gr.setTotalCount(symbolReaderList.size());
				gr.setSampleId(sampleId);
				geneRanks.add(gr);
			}
			
			//数据库添加geneRank
//			List<GeneRank> geneRanks2 = new ArrayList<GeneRank>();
//			for(SymbolReader sr: symbolReaderList2){
//				GeneRank gr = new GeneRank();
//				gr.setCreatedTimestamp(System.currentTimeMillis());
//				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
//				gr.setSource(SourceType.TCGA.value());
//				gr.setGeneId(sr.getGeneId());
//				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList2.indexOf(sr)+1)/symbolReaderList2.size())));
//				//Tsstescount读数
//				gr.setTssTesCount(sr.getRead());
//				gr.setTotalCount(symbolReaderList2.size());
//				gr.setSampleId(sampleId2);
//				geneRanks2.add(gr);
//			}
//			
//			//数据库添加geneRank
//			List<GeneRank> geneRanks3 = new ArrayList<GeneRank>();
//			for(SymbolReader sr: symbolReaderList3){
//				GeneRank gr = new GeneRank();
//				gr.setCreatedTimestamp(System.currentTimeMillis());
//				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
//				gr.setSource(SourceType.TCGA.value());
//				gr.setGeneId(sr.getGeneId());
//				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList3.indexOf(sr)+1)/symbolReaderList3.size())));
//				//Tsstescount读数
//				gr.setTssTesCount(sr.getRead());
//				gr.setTotalCount(symbolReaderList3.size());
//				gr.setSampleId(sampleId3);
//				geneRanks3.add(gr);
//			}
			
//			//数据库添加geneRank
//			List<GeneRank> geneRanks4 = new ArrayList<GeneRank>();
//			for(SymbolReader sr: symbolReaderList_normal_average){
//				GeneRank gr = new GeneRank();
//				gr.setCreatedTimestamp(System.currentTimeMillis());
//				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
//				gr.setSource(SourceType.TCGA.value());
//				gr.setGeneId(sr.getGeneId());
//				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList_normal_average.indexOf(sr)+1)/symbolReaderList_normal_average.size())));
//				//Tsstescount读数
//				gr.setTssTesCount(sr.getRead());
//				gr.setTotalCount(symbolReaderList_normal_average.size());
//				gr.setSampleId(sampleId_normal_average);
//				geneRanks4.add(gr);
//			}
//			
//			//数据库添加geneRank
//			List<GeneRank> geneRanks5 = new ArrayList<GeneRank>();
//			for(SymbolReader sr: symbolReaderList_normal_std_dev){
//				GeneRank gr = new GeneRank();
//				gr.setCreatedTimestamp(System.currentTimeMillis());
//				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
//				gr.setSource(SourceType.TCGA.value());
//				gr.setGeneId(sr.getGeneId());
//				gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList_normal_std_dev.indexOf(sr)+1)/symbolReaderList_normal_std_dev.size())));
//				//Tsstescount读数
//				gr.setTssTesCount(sr.getRead());
//				gr.setTotalCount(symbolReaderList_normal_std_dev.size());
//				gr.setSampleId(sampleId_normal_std_dev);
//				geneRanks5.add(gr);
//			}
			
			geneRankDAO.create(geneRanks);
//			geneRankDAO.create(geneRanks2);
//			geneRankDAO.create(geneRanks3);
//			geneRankDAO.create(geneRanks4);
//			geneRankDAO.create(geneRanks5);
		}
		
		
	}

}
