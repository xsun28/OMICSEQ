package com.omicseq.core.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class StatisticUnMatchedTumorNormalRNAseqSummary {
	
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
	protected static GeneCache geneCache = GeneCache.getInstance();
	protected static List<Integer> geneIds;
	protected static DBCollection  collection = MongoDBManager.getInstance().getCollection("generank", "generank", "summaryOfRNAseqTumor");
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static void main(String[] args) {
		geneCache.init();
		geneIds = geneCache.getGeneIds();
//		String[] cancerTypes = {"ACC", "BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//	        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		 
		String[] cancerTypes = {"SKCM"};
		StatisticUnMatchedTumorNormalRNAseqSummary g = new StatisticUnMatchedTumorNormalRNAseqSummary();
		int etype = ExperimentType.RNA_SEQ.getValue();
		for(int i=0 ;i < cancerTypes.length; i++)
		{
			String cancerType = cancerTypes[i];
			g.stitisticRank(cancerType.toLowerCase(), etype);
		}
	}

	private void stitisticRank(String cancerType, int etype) { 
		List<SymbolReader> symbolReaderList = new ArrayList<SymbolReader>();
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		
		SmartDBObject query0 = new SmartDBObject("source", 1);
		query0.put("etype", 0);
		query0.put("settype", new SmartDBObject("$regex","RNA-seq"));
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-total tumor/normal diff"));
		Integer sampleId = 0;
		List<Sample> sampleList = sampleDAO.find(query0);
		if(sampleList != null && sampleList.size() >0) {
			sampleId = sampleList.get(0).getSampleId();
		}else {
			return;
		}
		
		geneRankDAO.removeBySampleId(sampleId);
		
		SmartDBObject q = new SmartDBObject("etype", etype);
		q.put("source", 1);
		q.put("cell", new SmartDBObject("$regex", cancerType+"-normal"));
		List<Sample> list = sampleDAO.find(q);
		if(list == null || list.size() ==0)
		{
			return;
		} else {
			query0.put("cell", new SmartDBObject("$regex", cancerType + "-normal std dev"));
			List<Sample> sampleListNormalStd = sampleDAO.find(query0);
			
			if(sampleListNormalStd == null || sampleListNormalStd.size() == 0)
			{
				return;
			}
			
			Integer normalStdSampleId = sampleListNormalStd.get(0).getSampleId();
			List<GeneRank> normalStdGenRankList = geneRankDAO.find(new SmartDBObject("sampleId", normalStdSampleId));
			query0.put("cell", new SmartDBObject("$regex", cancerType + "-normal average"));
			List<Sample> sampleListNormalAVG = sampleDAO.find(query0);
			Integer normalAVGSampleId = sampleListNormalAVG.get(0).getSampleId();
			List<GeneRank> normalAvgGenRankList = geneRankDAO.find(new SmartDBObject("sampleId", normalAVGSampleId));

			Map<Integer, Double> normalAvgMap = new HashMap<Integer, Double>();
			Map<Integer, Double> normalStdMap = new HashMap<Integer, Double>();
			
			for(GeneRank g : normalAvgGenRankList)
			{
				normalAvgMap.put(g.getGeneId(), g.getTssTesCount());
			}
			
			for(GeneRank g : normalStdGenRankList)
			{
				normalStdMap.put(g.getGeneId(), g.getTssTesCount());
			}
			
			for(int i=0; i<geneIds.size(); i++){
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
				
				 Double normAvg = normalAvgMap.get(geneIds.get(i));
				 if(normAvg == null || normAvg == 0.0) {
					 continue;
				 }
				 Double normalStd = normalStdMap.get(geneIds.get(i));
				 if(normalStd == null || normalStd == 0.0) {
					 continue;
				 }
				 
				 double t = (tumorAvg - normAvg) / Math.sqrt(Math.pow(tumorStd, 2)/n + Math.pow(normalStd, 2)/list.size());
				 
				 if(tumorStd == 0.0 && normalStd == 0.0)
				 {
					 t = 0.0;
				 }
				 
				 SymbolReader sr1 = new SymbolReader();
				 sr1.setGeneId(geneIds.get(i));
				 sr1.setRead(t); //2014-10-29 保留tumor-normal值的正负
				 
				 symbolReaderList.add(sr1); 
			}
			
			
			//排序 
			Collections.sort(symbolReaderList, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
			
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
			
			geneRankDAO.create(geneRanks);
		}
		
		
	}

}
