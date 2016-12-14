package com.omicseq.robot.process;

import java.util.ArrayList;
import java.util.List;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class RemoveSampleAndGeneRanks {

	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	
	public static void main(String[] args) {
		RemoveSampleAndGeneRanks rsg = new RemoveSampleAndGeneRanks();
		
		SmartDBObject query = new SmartDBObject();
//		query.put("source", SourceType.TCGAFirebrowse.getValue());
		query.put("etype", ExperimentType.MUTATION.getValue());
//		query.put("cell", new SmartDBObject("$regex", "ov"));
//		query.put("sampleId", new SmartDBObject("$gte", 10000937));
		
		rsg.remove(query);
	}

	private void remove(SmartDBObject query) {
		List<Sample> sampleList = sampleDAO.find(query);
		for(Sample s : sampleList)
		{
			System.out.println(s.getSampleId());
			if(!s.getCell().contains("normalized"))
			{
				geneRankDAO.removeBySampleId(s.getSampleId());
				sampleDAO.removeBy_id(s.get_id());
			}
		}
		
//		sampleDAO.delete(query);
	}

}
