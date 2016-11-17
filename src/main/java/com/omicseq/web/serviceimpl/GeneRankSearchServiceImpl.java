package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.omicseq.common.SortType;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.web.service.IGeneRankSearchService;

@Service
public class GeneRankSearchServiceImpl implements IGeneRankSearchService {

private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	@Override
	public List<Gene> searchGeneTop10BySampleId(Integer sampleId) {
		SmartDBObject query = new SmartDBObject("sampleId", sampleId);
		query.addSort("mixturePerc", SortType.ASC);
		List<GeneRank> geneRankList = geneRankDAO.find(query, 0, 20);
		List<Gene> geneList = new ArrayList<Gene>();
		HashMap<String, Boolean> hashMap = new HashMap<String, Boolean>();
		for(GeneRank gr : geneRankList)
		{
			Integer geneId = gr.getGeneId();
			Gene gene = new Gene();
			gene.setGeneId(geneId);
			List<Gene> genes= geneDAO.find(new SmartDBObject("geneId", geneId));

			 for(int i=0; i<genes.size(); i++)
			 {
				 String txName = genes.get(i).getTxName();
				 List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
				 gene.setTxName(txName);
				 if(refs != null && refs.size() !=0)
				 {
					 for(TxrRef r : refs){
						 if((hashMap.get(r.getGeneSymbol()) != null && hashMap.get(r.getGeneSymbol())==true) || geneList.size() == 10)
						 {
							 break;
						 }
						 hashMap.put(r.getGeneSymbol(), true);
						 gene.setSeqName(r.getGeneSymbol());
						 gene.setStart(genes.get(i).getStart());
						 gene.setEnd(genes.get(i).getEnd());
						 gene.setWidth(genes.get(i).getWidth());
						 geneList.add(gene);
						 break;
					 }
					 break;
				 }
			 }
		}
		return geneList;
	}

}
