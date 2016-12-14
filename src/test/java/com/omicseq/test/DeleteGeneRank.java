package com.omicseq.test;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBCollection;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class DeleteGeneRank {
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	public void del(){
		DBCollection collection = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
		SmartDBObject query = new SmartDBObject();
		query.put("cell", "TCGA-hnsc");
		query.put("etype", 12);
		List<Sample> sList = dao.find(query);
		for(Sample s :sList){
			geneRankDAO.removeBySampleId(s.getSampleId());
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new DeleteGeneRank().del();
		
	}

}
