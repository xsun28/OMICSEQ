package com.omicseq.core.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

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

public class CalculateGeneRankOfRNASummary {
	
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static ISummaryTrackDataDao summaryDao = DAOFactory.getDAO(ISummaryTrackDataDao.class);
	protected static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
	protected static GeneCache geneCache = GeneCache.getInstance();
	protected static List<Integer> geneIds;
	protected static DBCollection  collection = MongoDBManager.getInstance().getCollection("generank", "generank", "summaryOfRNAseqTumor");
	
	public static void main(String[] args) {
		geneCache.init();
		geneIds = geneCache.getGeneIds();
//		String[] cancerTypes = {"ACC","BRCA","BLCA","CESC","COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//	        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		
		String[] cancerTypes = {"SKCM"};
		CalculateGeneRankOfRNASummary g = new CalculateGeneRankOfRNASummary();
		for(int i=0 ;i < cancerTypes.length; i++)
		{
			String cancerType = cancerTypes[i];
			g.createGeneRank(cancerType.toLowerCase());
//			g.createGeneRankOfTumorStd(cancerType.toLowerCase());  //tumor std dev AND total tumor/normal diff
		}
		
//		Double[] d = {3550.1630999999998,5411.7092999999995,3108.7871999999998,2577.4591,648.9714999999997,1046.4905000000003,3105.1542999999997,2962.7379,4527.6738000000005,3500.4804999999997,1868.5141000000003,1541.346,1554.6158,3379.5818,1037.9665,2284.5324,6209.278,4818.7577,6424.0139,3314.6153000000004,2335.0732,3672.6099999999997,4006.7786,3279.3744,-154.01310000000012,4011.5232,2505.9281,3423.2693,3598.3414000000002,2121.0775,3045.0408,1862.7947,3007.632,2792.564,705.9464999999998,4238.1727,2285.8368,1778.8397,4822.3162,5411.6434,4330.0705,2024.6197,2678.9943000000003,-412.73030000000017,2085.7876,5485.7042,2387.1769999999997,2653.449,2384.8554000000004,2946.095,4418.0997};
//		Double read = 0.0;
//		double sd = 0.0;
//		for(int i=0; i<d.length; i++)
//		{
//			read += d[i];
//		}
//		
//		double dav 	= read/d.length;
//		
//		for(int i=0; i<d.length; i++){
//			sd += Math.pow(d[i]-dav, 2);
//		}
//		
//		double sed = Math.sqrt(sd/(d.length -1))/Math.sqrt(d.length);
//		
//		double t = Math.abs(dav/sed);
//		
//		System.out.println("dav:" + dav + "\t sed:" + sed + "\t T:" + t + "\n");
	}
	
	public void createGeneRankOfTumorStd(String cancerType)
	{
		SmartDBObject query = new SmartDBObject("source", 1);
		query.put("etype", 2);
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
		        infoData.put("etype", 2);
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
		
//		String normal_average = "normal average";
		String normal_std_dev = "normal std dev";
//		Integer sampleId_normal_average;
		Integer sampleId_normal_std_dev;
		List<SymbolReader> symbolReaderList_normal_average = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderLis_normal_std_dev = new ArrayList<SymbolReader>();
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		SmartDBObject query0 = new SmartDBObject("source", 1);
		query0.put("etype", 0);
		query0.put("settype", "RNA-seq");
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + matched_tumor_normal_diff));
		List<Sample> sampleList_matched_tumor_normal_diff = sampleDAO.find(query0);
		if(sampleList_matched_tumor_normal_diff != null && sampleList_matched_tumor_normal_diff.size() >0)
		{
			sampleId_matched_tumor_normal_diff = sampleList_matched_tumor_normal_diff.get(0).getSampleId();
		} else {
			return;
		}
		
//		query0.put("cell", new SmartDBObject("$regex", cancerType + "-" + normal_average));
//		List<Sample> sampleList_normal_average = sampleDAO.find(query0);
//		if(sampleList_normal_average != null && sampleList_normal_average.size() >0)
//		{
//			if("".equals(sampleList_normal_average.get(0).getInputSampleIds()))
//			{
//				return;
//			}
//			
//			sampleId_normal_average = sampleList_normal_average.get(0).getSampleId();
//		} else {
//			return;
//		}
		
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
//		geneRankDAO.removeBySampleId(sampleId_normal_average);  //删除原来计算的值
		geneRankDAO.removeBySampleId(sampleId_normal_std_dev);  //删除原来计算的值

		for(int i=0; i<geneIds.size(); i++)
		{
			SmartDBObject query = new SmartDBObject("geneId", geneIds.get(i));
			query.put("cellType", "TCGA-"+cancerType);
			query.put("etype", 2);
			List<SummaryTrackData> summaryList = summaryDao.find(query);
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
				
//		Collections.sort(symbolReaderList_normal_average, new Comparator<SymbolReader>() {
//			@Override
//			public int compare(SymbolReader o1, SymbolReader o2) {
//				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
//			}
//		});
		
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
		
//		List<GeneRank> geneRanks1 = new ArrayList<GeneRank>();
//		for(SymbolReader sr: symbolReaderList_normal_average){
//			GeneRank gr = new GeneRank();
//			gr.setCreatedTimestamp(System.currentTimeMillis());
//			gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
//			gr.setSource(SourceType.TCGA.value());
//			gr.setGeneId(sr.getGeneId());
//			gr.setMixturePerc(Double.parseDouble(df.format((double)(symbolReaderList_normal_average.indexOf(sr)+1)/symbolReaderList_normal_average.size())));
//			gr.setTssTesCount(sr.getRead());
//			gr.setTotalCount(symbolReaderList_normal_average.size());
//			gr.setSampleId(sampleId_normal_average);
//			geneRanks1.add(gr);
//		}
		
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
//		geneRankDAO.create(geneRanks1);
		geneRankDAO.create(geneRanks3);
	}

}
