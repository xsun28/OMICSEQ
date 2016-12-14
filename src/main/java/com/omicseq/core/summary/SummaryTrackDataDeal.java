package com.omicseq.core.summary;

import java.util.List;

import com.omicseq.common.ExperimentType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.store.dao.ISummaryTrackDataDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class SummaryTrackDataDeal {
	protected static ISummaryTrackDataDao summaryDao = DAOFactory.getDAO(ISummaryTrackDataDao.class);
	protected static GeneCache geneCache = GeneCache.getInstance();
	
	public static void main(String[] args) {
		geneCache.doInit();
		
		SummaryTrackDataDeal sd = new SummaryTrackDataDeal();
		sd.deleteByEtype(ExperimentType.CVN.getValue());
	}

	private void deleteByEtype(Integer etype) {
		List<Integer> geneIds = geneCache.getGeneIds();
		
		for(Integer geneId : geneIds)
		{
			List<Gene> genes = GeneCache.getInstance().getGeneById(geneId);
			if(genes != null && genes.size() > 0) {
				if(genes.get(0).getSeqName().toLowerCase().equals("chrx") || genes.get(0).getSeqName().toLowerCase().equals("chry"))  {
					SmartDBObject query = new SmartDBObject("geneId", geneId);
					query.put("etype", etype);
					
					summaryDao.delete(query);
					
					System.out.println("geneId: " + geneId);
				}
			}
		}
	}

}
