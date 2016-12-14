package com.omicseq.core.batch;

import java.util.List;

import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class GeneRankDelete {
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	
	public static void main(String[] args) {
		SmartDBObject query = new SmartDBObject("etype", 0);
//		query.put("sampleId", new SmartDBObject("$gte", 10000001));
//		query.put("cell", new SmartDBObject("$regex", "tumor std dev"));
		List<Sample> sampleList = sampleDAO.find(query);
		for(Sample s : sampleList)
		{
//			geneRankDAO.removeBySampleId(sampleId);
//			String cell = s.getCell();
//			String[] c = cell.split("-");
//			cell = c[0]+ "-"+ c[1] + "-matched tumor/normal diff";
//			s.setCell(cell);
			s.setLab("Serendi");
			sampleDAO.update(s);
		}
	}

}
