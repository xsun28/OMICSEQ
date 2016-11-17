package com.omicseq.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.Hash;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class Check {
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	public void find(){
		//拿到所有geneSymbol
		DBCollection col = MongoDBManager.getInstance().getCollection("manage", "manage","txrref_temp");
		DBCursor cursor = col.find();
		List<String> symbolList = new ArrayList<String>();
		while(cursor.hasNext()){
			DBObject obj = cursor.next();
			String geneSymbol = (String) obj.get("geneSymbol");
			symbolList.add(geneSymbol);
		}
//		symbolList.add("ANKRD30BL");
		//拿到每个geneSymbol对应的geneId
		for(String geneSymbol : symbolList){
//			geneSymbol = "ANKRD30BL";
			if("ANKRD30BL".equals(geneSymbol)) continue;
			Set<Gene> geneSet = new HashSet<Gene>();
			
			List<TxrRef>  txrrefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
			if(txrrefList == null)
			{
				continue;
			}
			for(TxrRef txrref : txrrefList){
				Gene gene = GeneCache.getInstance().getGeneByName(txrref.getRefseq());
				if(gene == null) continue;
				geneSet.add(gene);
			}
			if(geneSet.size()<2) continue;
			
			Integer length = 0;
			for(Gene gene : geneSet){
				Integer width = gene.getWidth();
				if(width > length){
					length = width;
				}
			}
			Gene gene_longest = new Gene();
			for(Gene gene : geneSet){
				if(gene.getWidth() == length){
					gene_longest = gene;
				}
			}
			
			SmartDBObject qu = new SmartDBObject();
			qu.put("geneId", gene_longest.getGeneId());
			Integer [] etypes = {1,4};
			qu.put("etype",new SmartDBObject("$nin",etypes));
			List<GeneRank> rankList_long = geneRankDAO.find(qu);
			Set<Integer> sampleIdSet = new HashSet<Integer>();
			for(GeneRank grank : rankList_long){
				sampleIdSet.add(grank.getSampleId());
			}
			
			List<GeneRank> rankList = new ArrayList<GeneRank>();
			Set<String> set = new HashSet<String>();
			set.add(gene_longest.getTxName());
			for(Gene gene : geneSet){
				if(!set.add(gene.getTxName())) continue;
				SmartDBObject query = new SmartDBObject();
				query.put("geneId", gene.getGeneId());
				query.put("etype",new SmartDBObject("$nin",etypes));
				List<GeneRank> grs = geneRankDAO.find(query);
				List<GeneRank> grs_new = new ArrayList<GeneRank>();
				for(GeneRank gr : grs){
					if(sampleIdSet.contains(gr.getSampleId())) continue;
					GeneRank rank = new GeneRank();
					rank.setGeneId(gene_longest.getGeneId());
					rank.setSampleId(gr.getSampleId());
					rank.setSource(gr.getSource());
					rank.setEtype(gr.getEtype());
					rank.setCreatedTimestamp(gr.getCreatedTimestamp());
					rank.setEnd(gr.getEnd());
					rank.setStart(gr.getStart());
					rank.setSeqName(gr.getSeqName());
					rank.setTotalCount(gr.getTotalCount());
					rank.setMixturePerc(gr.getMixturePerc());
					rank.setTssTesCount(gr.getTssTesCount());
					rank.setTssTesPerc(gr.getTss5kPerc());
					rank.setTssTesRank(gr.getTssTesRank());
					rank.setTss5kCount(gr.getTss5kCount());
					rank.setTss5kPerc(gr.getTss5kPerc());
					rank.setTss5kRank(gr.getTss5kRank());
					rank.setTssT5Count(gr.getTssT5Count());
					rank.setTssT5Rank(gr.getTssT5Rank());
					
					grs_new.add(rank);
				}
				rankList.addAll(grs_new);
			}
//			geneRankDAO.create(rankList);
			
		}
	}
	
	public static void main(String[] args) {
//		GeneCache.getInstance().doInit();
//		TxrRefCache.getInstance().doInit();
//		new Check().find();
			IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
//			SmartDBObject qu = new SmartDBObject();
			for(int i = 1; i <= 32745; i++){
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(i);
				int count = geneRankDAO.count(criteria);
				if(count > 50000) System.out.println("geneId : " + i);
			}
	}

	
	
}
