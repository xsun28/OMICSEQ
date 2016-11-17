package com.omicseq.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.omicseq.domain.HashDB;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 *
 */
public class EntrezeSymbolCache {
	
	private static Map<String, String> geneSymbolMap = new HashMap<String, String>();
	private IHashDBDAO geneSymbolDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "genesymbol");

	private static EntrezeSymbolCache entrezeSymbolCache = new EntrezeSymbolCache();

	public static EntrezeSymbolCache getInstance() {
		return entrezeSymbolCache;
	}
	
	public void init() {
		synchronized (EntrezeSymbolCache.class) {
			Integer start = 0;
			Integer limit = 3000;
			List<HashDB> hashDBList = null;
			while (CollectionUtils.isNotEmpty(hashDBList = geneSymbolDAO.loadValue(start, limit))) {
				for (HashDB value : hashDBList) {
					geneSymbolMap.put(value.getKey(), value.getValue());
				}
				start = start + limit;
			}
		}
	}
	
	public String getGeneSymbol(String entrezeId) {
		return geneSymbolMap.get(entrezeId);
	}

	public static void main(String[] args) {
		EntrezeSymbolCache.getInstance().init();
		System.out.println(" symbol size is : " + EntrezeSymbolCache.getInstance().getGeneSymbol("100001584"));
	}

}
