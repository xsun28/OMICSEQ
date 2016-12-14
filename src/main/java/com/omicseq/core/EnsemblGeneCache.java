package com.omicseq.core;

import java.util.ArrayList;
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
public class EnsemblGeneCache {
    private static Map<String, List<String>> ensemblGeneCacheMap = new HashMap<String, List<String>>();
    private static Map<String, List<String>> refseqCache = new HashMap<String, List<String>>();
    private IHashDBDAO ensemblGeneDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "ensemblgene");

    private static EnsemblGeneCache ensemblGeneCache = new EnsemblGeneCache();

    public static EnsemblGeneCache getInstance() {
        return ensemblGeneCache;
    }

    public void init() {
        synchronized (EnsemblGeneCache.class) {
            Integer start = 0;
            Integer limit = 3000;
            List<HashDB> hashDBList = null;
            while (CollectionUtils.isNotEmpty(hashDBList = ensemblGeneDAO.loadValue(start, limit))) {
                for (HashDB value : hashDBList) {
                    String ensembl = value.getKey();
                    String refseq = value.getValue();
                    List<String> valueList = ensemblGeneCacheMap.get(ensembl);
                    if (valueList == null) {
                        valueList = new ArrayList<String>();
                        ensemblGeneCacheMap.put(ensembl, valueList);
                    }
                    valueList.add(refseq);
                    List<String> list = refseqCache.get(refseq);
                    if (null == list) {
                        list = new ArrayList<String>(5);
                        refseqCache.put(refseq, list);
                    }
                    list.add(ensembl);
                }
                start = start + limit;
            }
        }
    }

    public List<String> getRefseq(String ensemblGene) {
        return ensemblGeneCacheMap.get(ensemblGene);
    }

    public List<String> getEnsembl(String refseq) {
        return refseqCache.get(refseq);
    }
}
