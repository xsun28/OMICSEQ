package com.omicseq.relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.omicseq.common.SortType;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class GeneRankSearch {
//	private Logger logger = LoggerFactory.getLogger(CalculatePathWayGeneRanks.class);
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
//	DBCollection generank_coll = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
//	DBCollection gene_coll = MongoDBManager.getInstance().getCollection("manage", "manage", "gene");
//	DBCollection txrref_coll = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref");
	
	public void sum() {
		List<Integer> geneIdList = new ArrayList<Integer>();
		int i = 0;
		while (i <= 32745) {
			geneIdList.add(i, i);
			i++;
		}
		Map<Integer, Double> sumResult = new HashMap<Integer, Double>();
		for(Integer geneId : geneIdList)
		{
			SmartDBObject query = new SmartDBObject("geneId", geneId);
			query.addSort("mixturePerc", SortType.ASC);
			List<GeneRank> geneRankList = geneRankDAO.find(query, 0, 200);
			double per = 0.0;
			for(GeneRank g : geneRankList)
			{
			if(g.getMixturePerc() != null)
			{
			per += g.getMixturePerc();
			}
			}
			sumResult.put(geneId, per);
		}
		
		 Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>(); 
		 List<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(sumResult.entrySet());
		 Collections.sort(entryList, new MapValueComparator());  
		 Iterator<Map.Entry<Integer, Double>> iter = entryList.iterator();  
		 Map.Entry<Integer, Double> tmpEntry = null;  
		 int k =0;
		 while (iter.hasNext() && k <= 100) {  
			 tmpEntry = iter.next();  
			 sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
			 k++;
		 }

		 Iterator<Entry<Integer, Double>> iter1 = sortedMap.entrySet().iterator();
		 while(iter1.hasNext())
		 {
			 System.out.println(iter1.next().getKey());
		 }
	}
	
	//比较器类  
	public class MapValueComparator implements Comparator<Map.Entry<Integer, Double>> {  
		public int compare(Entry<Integer, Double> me1, Entry<Integer, Double> me2) {  
			return me1.getValue().compareTo(me2.getValue());  
		}  
	}
	
	//比较器类  
		public class MapValueComparator2 implements Comparator<Map.Entry<String, Double>> {  
			public int compare(Entry<String, Double> me1, Entry<String, Double> me2) {  
				return me1.getValue().compareTo(me2.getValue());  
			}  
		}
	
	public void out(Integer[] geneIds)
	{
		List<String> rs = new ArrayList<String>();
		 List<Gene> gene= geneDAO.find(new SmartDBObject("geneId", new SmartDBObject("$in", geneIds)));
		 for(int i=0; i<gene.size(); i++)
		 {
			 String txName = gene.get(i).getTxName();
			 List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
			 for(TxrRef r : refs){
				 if(!rs.contains(r.getGeneSymbol())){
					 rs.add(r.getGeneSymbol());
				 }
			 }
		 }
		 
		 for(String s : rs)
		 {
			 System.out.println(s);
		 }
	}


	public static void main(String[] args) {
		GeneRankSearch gs = new GeneRankSearch();
//		gs.sum();
		Integer[] geneIds = {891,28284,31675,9677,12172,1408,30112,21014,21016,19706,4505,31676,24444,11201,27607,24606,27330,21406,3958,
				5142,8009,508,2303,9200,10929,10883,12995,12636,13331,13521,15271,22498,21183,23815,23457,26287,26286,30677,30236,9463,
				31314
				,21687
				,19112
				,19857
				,28285
				,10768
				,26919
				,19743
				,2344
				,9695
				,12368
				,28458
				,27894
				,18845
				,25841
				,13173
				,31110
				,2334
				,2178
				,4654
				,28004
				,9816
				,19744
				,2338
				,14964
				,11842
				,27333
				,13770
				,32331
				,21407
				,22271
				,26962
				,25538
				,8099
				,2809
				,13659
				,10702
				,27321
				,9449
				,10887
				,20244
				,31970
				,5147
				,18822
				,27389
				,32197
				,32333
				,5007
				,14323
				,14325
				,14644
				,4754
				,21565
				,14773
				,32640
				,1941
				,23052
				,3737
				,13648
				,2304};
//		gs.out(geneIds);
		
		gs.sort(geneIds);
	
	}


	private void sort(Integer[] geneIds) {
		Map<String, Double> sumResult = new HashMap<String, Double>();
		for(int i =0; i<geneIds.length; i++) {
			Integer geneId = geneIds[i];
			SmartDBObject query = new SmartDBObject("geneId", geneId);
			query.addSort("mixturePerc", SortType.ASC);
			List<GeneRank> geneRankList = geneRankDAO.find(query, 0, 200);
			
			double per = 0.0;
			
			for(GeneRank g : geneRankList)
			{
				if(g.getMixturePerc() != null)
				{
//					Integer count = g.getTotalCount();
//					if(count == null)
//					{
//						GeneRankCriteria geneRankCriteria = new GeneRankCriteria();
//				        geneRankCriteria.setGeneId(geneId);
//
//						count = geneRankDAO.count(geneRankCriteria);
//					}
					
					
					per += g.getMixturePerc();
				}
			}
			String txName = geneDAO.find(new SmartDBObject("geneId", geneId)).get(0).getTxName();
			List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
			if(refs == null || refs.size() ==0)
			{
				sumResult.put(txName, per/200);
			}else {
				sumResult.put(refs.get(0).getGeneSymbol(), per/200);
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
			 System.out.println(entry.getKey() + "========" + entry.getValue());
		 }
	}

}
