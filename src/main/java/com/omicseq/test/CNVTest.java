package com.omicseq.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.omicseq.common.SortType;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CNVTest {

	
	public static void main(String[] args) {
		IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
		for(int i=23410 ;i < 32746; i++){
			SmartDBObject query = new SmartDBObject();
			query.put("geneId", i);
			query.put("etype",11);
			query.addSort("mixturePerc", SortType.ASC);
			List<GeneRank> rankList = geneRankDAO.find(query);
			if(rankList.size() < 20000) continue;
			Map<Integer,GeneRank> map = new HashMap<Integer,GeneRank>();
			
			for(GeneRank rank : rankList){
				if(!map.containsKey(rank.getSampleId())){
					map.put(rank.getSampleId(), rank);
				}
				
			}
			List<GeneRank> geneRanknew = new ArrayList<GeneRank>();
			for(Integer sampleId : map.keySet()){
				geneRanknew.add(map.get(sampleId));
			}
			SmartDBObject qu = new SmartDBObject();
			qu.put("geneId", i);
			qu.put("etype", 11);
			geneRankDAO.delete(qu);
			geneRankDAO.create(geneRanknew);
		}
	}



}
