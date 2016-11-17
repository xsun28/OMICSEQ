package com.omicseq.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





import org.bson.types.ObjectId;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.omicseq.common.SortType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CCLE_Update {
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private IGeneRankDAO rankdao = DAOFactory.getDAO(IGeneRankDAO.class);
	public static void main(String[] args) {
		new CCLE_Update().update();
	}

	private void update() {
		DBCollection collection_rank = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
		SmartDBObject query = new SmartDBObject();
		query.put("source", 6);
		query.put("etype",8);
		query.addSort("sampleId", SortType.ASC);
	
		List<Sample> samples = sampleDAO.find(query);
		/*for(int i =1 ; i<samples.size(); i++){
			int sampleId = samples.get(i).getSampleId();
			System.out.println("Update SampleID : " + sampleId);
			collection_rank.update(new SmartDBObject("sampleId",sampleId), new SmartDBObject("$set", new SmartDBObject("sampleId",(sampleId-1))),false,true);
		}*/
		List<GeneRank> list = rankdao.find(new SmartDBObject("sampleId",503186));
		List<Integer> geneIDs = new ArrayList<Integer>();
		for(GeneRank rank : list){
			geneIDs.add(rank.getGeneId());
		}
		for(Sample sample : samples){
			SmartDBObject qu = new SmartDBObject();
			qu.put("sampleId", sample.getSampleId());
			qu.addSort("geneId", SortType.ASC);
			List<GeneRank> rankList = rankdao.find(qu);
			Map<Integer,GeneRank> map = new HashMap<Integer, GeneRank>();
			for(GeneRank rank :rankList){
				if(!geneIDs.contains(rank.getGeneId())){
					SmartDBObject re = new SmartDBObject();
					re.put("geneId", rank.getGeneId());
					re.put("sampleId", rank.getSampleId());
					collection_rank.remove(re);
				}
				if(!map.containsKey(rank.getGeneId())){
					map.put(rank.getGeneId(), rank);
				}else{
					if(map.get(rank.getGeneId()).getTssTesCount() .compareTo( rank.getTssTesCount()) == 0){
						SmartDBObject re = new SmartDBObject();
						ObjectId id = new ObjectId(rank.get_id());
						re.put("_id", id);
						collection_rank.remove(re);
					}
				}
			}
		}
		
	}

}
