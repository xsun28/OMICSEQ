package com.omicseq.test;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class FindUnProcessdSampleInGeneRank {

	public static void main(String[] args) {
		IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
		ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
		List<Sample> samples = null;
		int start = 0 ;
        while (CollectionUtils.isNotEmpty(samples = sampleDAO.find(new SmartDBObject("deleted",0), start, 3000))) {
			for(Sample sample : samples){
				if(geneRankDAO.findOne(new SmartDBObject("sampleId",sample.getSampleId())) == null){
					sample.setDeleted(10);
					sampleDAO.update(sample);
				}
			}
			start += 3000;
        }
	}

}
