package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.web.service.ITypeAheadService;

/**
 * @author Min.Wang
 *
 */
@Service
public class TypeAheadServiceImpl implements ITypeAheadService {

	ITxrRefDAO refDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	IGeneDAO geneMouseDAO = DAOFactory.getDAOByTableType(IGeneDAO.class, "Mouse");
	
	public List<String> search(String query, String option) {
		int size = 10;
		List<String> finalList = new ArrayList<String>();
		List<String> texrRefList = TxrRefCache.getInstance().likeQuery(query, size);
		if (CollectionUtils.isNotEmpty(texrRefList)) {
			finalList.addAll(texrRefList);
		}
		
		query = query.toUpperCase();
		if (query.startsWith("NM_") || query.startsWith("NR_"))
		{
			List<Gene> seqRefList = geneDAO.fuzzyQuery("txName", query, 10);
			List<String> matchRefseqList = new ArrayList<String>();
			for (Gene ref : seqRefList) {
				matchRefseqList.add(ref.getTxName());
			}
			finalList.addAll(matchRefseqList);
			return finalList;
		}
		return finalList;
	}
	
	public List<String> search_Mouse(String query, String option) {
		int size = 10;
		List<String> finalList = new ArrayList<String>();
		List<String> texrRefList = MouseTxrRefCache.getInstance().likeQuery(query, size);
		if (CollectionUtils.isNotEmpty(texrRefList)) {
			finalList.addAll(texrRefList);
		}
		
		query = query.toUpperCase();
		if (query.startsWith("NM_") || query.startsWith("NR_"))
		{
			List<Gene> seqRefList = geneMouseDAO.fuzzyQuery("txName", query, 10);
			List<String> matchRefseqList = new ArrayList<String>();
			for (Gene ref : seqRefList) {
				matchRefseqList.add(ref.getTxName());
			}
			finalList.addAll(matchRefseqList);
			return finalList;
		}
		return finalList;
	}
}
