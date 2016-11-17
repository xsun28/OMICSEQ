package com.omicseq.statistic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.omicseq.common.SortType;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.pathway.CalculatePathWayGeneRanks;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class GeneRankTop10 {
	private Logger logger = LoggerFactory.getLogger(CalculatePathWayGeneRanks.class);
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	DBCollection generank_coll = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
	DBCollection gene_coll = MongoDBManager.getInstance().getCollection("manage", "manage", "gene");
	DBCollection txrref_coll = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref");
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	
	public List<Integer> sum() {
		List<Integer> geneIdList = new ArrayList<Integer>();
		List<Integer> geneIdResult = new ArrayList<Integer>();
		int i = 32745;
		int j =0;
		while (i >= 1) {
			geneIdList.add(j, i);
			i--;
			j++;
		}
		SmartDBObject querySample = new SmartDBObject();
		querySample.put("source", 1);
		querySample.put("etype", 11);
		querySample.put("deleted", 0);
		querySample.put("cell", new SmartDBObject("$regex", "prad"));
		querySample.put("detail", new SmartDBObject("$regex", "tumor"));
//		querySample.addSort("sampleId", SortType.ASC);
		List<Sample> samples = sampleDAO.find(querySample);
		
		Map<Integer, Double> sumResult = new HashMap<Integer, Double>();
		
//		SmartDBObject query2 = new SmartDBObject("$gte", samples.get(0).getSampleId());
//		query2.append("$lte", samples.get(samples.size()-1).getSampleId());
//		SmartDBObject query = new SmartDBObject();
//		query.put("sampleId", query2);
//		query.put("mixturePerc", new SmartDBObject("$lte", 0.01));
//		query.addSort("mixturePerc", SortType.ASC);
//		List<GeneRank> all = geneRankDAO.find(query);
		List<GeneRank> all = new ArrayList<GeneRank>();
		
		for(Sample sample : samples)
		{
			Integer sampleId = sample.getSampleId();
			SmartDBObject query = new SmartDBObject("sampleId", sampleId);
			query.put("mixturePerc", new SmartDBObject("$lte", 0.01));
			query.addSort("mixturePerc", SortType.ASC);
			List<GeneRank> geneRankList = geneRankDAO.find(query);
			all.addAll(geneRankList);
		}
		
		for(Integer geneId : geneIdList)
		{
//			System.out.println(geneId);
			List<GeneRank> geneRankList1 = new ArrayList<GeneRank>();
			for(GeneRank geneRank : all)
			{
//				System.out.println(geneRank.getGeneId());
//				System.out.println(geneRank.getGeneId().equals(geneId));
				if(geneRank.getGeneId().equals(geneId))
				{
					geneRankList1.add(geneRank);
				}
			}
			
			if(geneRankList1 == null || geneRankList1.size() ==0)
			{
				continue;
			}
//			double per = 0.0;
//			int m =0;
//			for(GeneRank g : geneRankList1)
//			{
//				if(g.getMixturePerc() != null)
//				{
//					per += g.getMixturePerc();
//					m++;
//				}
//			}
//			sumResult.put(geneId, per/m);
			
//			SmartDBObject query3 = new SmartDBObject();
//			query3.put("geneId", geneId);
//			query3.put("mixturePerc", new SmartDBObject("$lte", 0.01));
//			query.addSort("mixturePerc", SortType.ASC);
			GeneRankCriteria criteria = new GeneRankCriteria();
			criteria.setGeneId(geneId);
			criteria.setMixturePerc(0.01);
			Integer size = geneRankDAO.count(criteria);
			
			
			sumResult.put(geneId, Double.valueOf(geneRankList1.size()*1.00000)/size);
		}
		
		
//		Integer minSampleId = samples.get(0).getSampleId();
//		Integer maxSampleId = samples.get(samples.size() -1).getSampleId();
//		SmartDBObject query2 = new SmartDBObject();
//      query2.put("$gte", minSampleId);
//      query2.append("$lte", maxSampleId);
		
//		for(Integer geneId : geneIdList)
//		{
//			SmartDBObject query = new SmartDBObject("geneId", geneId);
//			query.put("sampleId", query2);
//			query.addSort("mixturePerc", SortType.ASC);
//			List<GeneRank> geneRankList = geneRankDAO.find(query);
//			if(geneRankList == null || geneRankList.size() ==0)
//			{
//				continue;
//			}
//			double per = 0.0;
//			for(GeneRank g : geneRankList)
//			{
//				if(g.getMixturePerc() != null)
//				{
//					per += g.getMixturePerc();
//				}
//			}
//			sumResult.put(geneId, per);
//		}

		 Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>(); 
		 List<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(sumResult.entrySet());
		 Collections.sort(entryList, new MapValueComparator());  
		 Iterator<Map.Entry<Integer, Double>> iter = entryList.iterator();  
		 Map.Entry<Integer, Double> tmpEntry = null;  
		 int k =0;
		 while (iter.hasNext() && k <= 50) {  
			 tmpEntry = iter.next();  
			 sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
			 k++;
		 }
		 
		 Iterator<Entry<Integer, Double>> iter1 = sortedMap.entrySet().iterator();
		 while(iter1.hasNext())
		 {
			 Integer geneId = iter1.next().getKey();
			 geneIdResult.add(geneId);
		 }
		 
//		 Map<Integer, Double> perResult = new HashMap<Integer, Double>();
//
//		 Iterator<Entry<Integer, Double>> iter1 = sortedMap.entrySet().iterator();
//		 while(iter1.hasNext())
//		 {
//			 System.out.println(iter1.next().getKey());
//			 Integer geneId = iter1.next().getKey();
//			 
//			 GeneRankCriteria criteria = new GeneRankCriteria();
//			 criteria.setGeneId(geneId);
//			 criteria.setMixturePerc(0.01);
//			 Integer count = geneRankDAO.count(criteria);
//			 
//			 perResult.put(geneId, sortedMap.get(geneId)/count);
//		 }
//		 
//		 entryList = new ArrayList<Map.Entry<Integer, Double>>(perResult.entrySet());
//		 Collections.sort(entryList, new MapValueComparator());
//		 
//		 Iterator<Entry<Integer, Double>> iter2 = entryList.iterator();
//		 while(iter2.hasNext())
//		 {
//			 Integer geneId = iter2.next().getKey();
//			 List<Integer> geneIds = new ArrayList<Integer>();
//			 geneIds.add(geneId);
//			 this.out(geneIds, perResult.get(geneId));
//		 }
		 
		 return geneIdResult;
	}
	
	//比较器类  
	public class MapValueComparator implements Comparator<Map.Entry<Integer, Double>> {  
		public int compare(Entry<Integer, Double> me1, Entry<Integer, Double> me2) {  
			return me1.getValue().compareTo(me2.getValue()) *-1;  
		}  
	}
	
	//比较器类  
		public class MapValueComparator2 implements Comparator<Map.Entry<String, Double>> {  
			public int compare(Entry<String, Double> me1, Entry<String, Double> me2) {  
				return me1.getValue().compareTo(me2.getValue()) * -1;  
			}  
		}
	
	public String out(List<Integer> geneIds)
	{
		String result = "";
		List<String> rs = new ArrayList<String>();
		
		for(Integer geneId : geneIds)
		{
			List<Gene> gene= geneDAO.find(new SmartDBObject("geneId", geneId));

			 for(int i=0; i<gene.size(); i++)
			 {
				 String txName = gene.get(i).getTxName();
				 List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
				 if(refs == null || refs.size() ==0)
				 {
					 result += "geneId:"+ geneId + "\t" + txName + "\n";
				 } else {
					 for(TxrRef r : refs){
						 System.out.println(r.getGeneSymbol());
						 result += "geneId:"+ geneId + "\t" + r.getGeneSymbol() + "\n";
						 break;
					 }
				 }
				 break;
			 }
		}
		
		System.out.println(result);
		
		 return result;
	}


	public static void main(String[] args) {
		GeneRankTop10 gs = new GeneRankTop10();
		List<Integer> geneIdList = gs.sum();
		Integer[] geneIds = new Integer[geneIdList.size()];
		int i =0;
		for(Integer geneId : geneIdList)
		{
			geneIds[i] = geneId;
			i++;
			System.out.print(geneId+",");
		}
		
		gs.sort(geneIds);
		
//		List<Integer> gIds = new ArrayList<Integer>();
//		gIds.add(891);
//		gIds.add(18845);
//		gIds.add(30112);
//		gIds.add(8099);
//		gIds.add(17233);
//		gIds.add(5279);
//		gIds.add(9695);
//		gIds.add(9377);
//		gIds.add(12172);
//		gIds.add(18822);
//		gs.out(gIds);
	
	}


	private void sort(Integer[] geneIds) {
		Map<String, Double> sumResult = new HashMap<String, Double>();
		for(int i =0; i<geneIds.length; i++) {
			Integer geneId = geneIds[i];
			SmartDBObject query = new SmartDBObject("geneId", geneId);
			query.put("mixturePerc", new SmartDBObject("$lte", 0.01));
			Integer size = geneRankDAO.find(query).size();  //top 1%
			
			
//			query.put("source", 1);
//			query.put("etype", 11);
//			query.put("mixturePerc", new SmartDBObject("$lte", 0.01));
//			query.addSort("mixturePerc", SortType.ASC);
//			Integer num = geneRankDAO.find(query).size();  // CNV TOP 1%
			
			SmartDBObject querySample = new SmartDBObject();
			querySample.put("source", 1);
			querySample.put("etype", 11);
			querySample.put("deleted", 0);
			querySample.put("cell", new SmartDBObject("$regex", "brca"));
//			querySample.addSort("sampleId", SortType.ASC);
			List<Sample> samples = sampleDAO.find(querySample);
			
			Integer p = 0; // prad CNV TOP 1%
			
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			for(Sample sample : samples)
			{
				Integer sampleId = sample.getSampleId();
				if(map.get(sampleId) != null)
				{
					continue;
				}
				map.put(sampleId, sampleId);
				
				SmartDBObject query0 = new SmartDBObject("sampleId", sampleId);
				query0.put("geneId", geneId);
				query0.put("mixturePerc", new SmartDBObject("$lte", 0.01));
				query0.addSort("mixturePerc", SortType.ASC);
				List<GeneRank> list = geneRankDAO.find(query0);
				if(list != null && list.size() > 0)
				{
					p ++;
				}
			}
			
			
			String txName = geneDAO.find(new SmartDBObject("geneId", geneId)).get(0).getTxName();
			List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
			if(refs == null || refs.size() ==0)
			{
				sumResult.put(txName + " total_top_1%="+ size + " brca_CNV_top_1% =" + p, p*100.000000/size);
			}else {
				sumResult.put(refs.get(0).getGeneSymbol() + " total_top_1%="+ size + " brca_CNV_top_1% =" + p, p*100.000000/size);
			}
			
		}
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>(); 
		 List<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(sumResult.entrySet());
		 Collections.sort(entryList, new MapValueComparator2());  
		 Iterator<Map.Entry<String, Double>> iter = entryList.iterator();  
		 Map.Entry<String, Double> tmpEntry = null;  
		 while (iter.hasNext()) {  
			 tmpEntry = iter.next();  
			 sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
		 }

		 Iterator<Entry<String, Double>> iter1 = sortedMap.entrySet().iterator();
		 while(iter1.hasNext())
		 {
			 Entry<String, Double> entry = iter1.next();
			 System.out.println(entry.getKey() + "========percent" + entry.getValue() + "%");
		 }
	}

}
