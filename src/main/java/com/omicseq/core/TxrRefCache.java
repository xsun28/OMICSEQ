package com.omicseq.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 * 
 */
public class TxrRefCache extends AbstractCache<String, List<TxrRef>> implements IInitializeable {
    private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
    private static TxrRefCache txrRefCache = new TxrRefCache();
    // the first
    ConcurrentMap<String, String> symbolMap = new ConcurrentHashMap<String, String>();
    ConcurrentMap<String, List<String>> firstAsymbolListMap = new ConcurrentHashMap<String, List<String>>();
    ConcurrentMap<String, String> refseqMap = new ConcurrentHashMap<String, String>();

    @Override
    public void doInit() {
        synchronized (TxrRefCache.class) {
            Integer start = 0;
            Integer limit = 3000;
            List<TxrRef> txrRefList = null;
            Set<String> sybmolSet = new HashSet<String>();
            Integer symbolCount = 0;
            Integer refSeqCount = 0;
            Integer invertRefseqCount = 0;
            Integer invertSymbolCount = 0;
            Set<String> geneSymolSet = new HashSet<String>();
            Map<String, Set<String>> refseqSetMap = new HashedMap<String, Set<String>>();
            Map<String, List<TxrRef>> refseqLineMap = new HashedMap<String, List<TxrRef>>();
            while (CollectionUtils.isNotEmpty(txrRefList = txrRefDAO.loadTxrRefList(start, limit))) {
                for (TxrRef txrRef : txrRefList) {
                    String geneSymbol = txrRef.getGeneSymbol();
                    if (StringUtils.isNoneBlank(geneSymbol)) {

                        List<TxrRef> symbolTxrRefList = get(geneSymbol.toLowerCase());
                        if (symbolTxrRefList == null) {
                            symbolTxrRefList = new ArrayList<TxrRef>();
                            put(geneSymbol.toLowerCase(), symbolTxrRefList);
                        }
                        symbolTxrRefList.add(txrRef);

                        if (sybmolSet.add(geneSymbol.toLowerCase())) {
                            symbolMap.put(geneSymbol.toLowerCase(), geneSymbol);

                            String firstA = new String(new char[] { geneSymbol.toLowerCase().charAt(0) });
                            List<String> subSymbolList = firstAsymbolListMap.get(firstA);
                            if (subSymbolList == null) {
                                subSymbolList = new ArrayList<String>();
                                firstAsymbolListMap.put(firstA, subSymbolList);
                            }
                            subSymbolList.add(geneSymbol.toLowerCase());
                        }
                        if (geneSymolSet.add(geneSymbol)) {
                            symbolCount = symbolCount + 1;
                            if (StringUtils.isNoneBlank(txrRef.getRefseq())) {
                                refSeqCount = refSeqCount + 1;
                            }
                        }
                    }

                    if (StringUtils.isNoneBlank(txrRef.getRefseq())) {
                        invertRefseqCount = invertRefseqCount + 1;
                        if (StringUtils.isNoneBlank(geneSymbol)) {
                            invertSymbolCount = invertSymbolCount + 1;
                        }
                        Set<String> symbolSet = refseqSetMap.get(txrRef.getRefseq());
                        if (symbolSet == null) {
                            symbolSet = new HashSet<String>();
                            refseqSetMap.put(txrRef.getRefseq(), symbolSet);
                        }
                        if (StringUtils.isNoneBlank(txrRef.getGeneSymbol())) {
                            symbolSet.add(txrRef.getGeneSymbol());
                        }
                        if (!refseqMap.containsKey(txrRef.getRefseq())) {
                            refseqMap.put(txrRef.getRefseq(), txrRef.getGeneSymbol());

                        }

                        List<TxrRef> lineList = refseqLineMap.get(txrRef.getRefseq());
                        if (lineList == null) {
                            lineList = new ArrayList<TxrRef>();
                            refseqLineMap.put(txrRef.getRefseq(), lineList);
                        }
                        lineList.add(txrRef);
                    }
                }

                // sort symbol key
                for (List<String> subSybmolLit : firstAsymbolListMap.values()) {
                    Collections.sort(subSybmolLit);
                }
                start = start + limit;
            }
        }
    }

    public static void main(String[] args) {
        TxrRefCache txrRefCache = new TxrRefCache();
        txrRefCache.doInit();
    }

    public List<TxrRef> getTxrRefBySymbol(String geneSymbol) {
        if (StringUtils.isBlank(geneSymbol)) {
            return Collections.emptyList();
        }
        return super.get(geneSymbol.toLowerCase());
    }

    public String getGeneSymbolByRefSeq(String refseq) {
        if (refseqMap.containsKey(refseq)) {
            return refseqMap.get(refseq);
        }
        return "";
    }

    @Override
    List<TxrRef> lazyLoad(String key) {
        return txrRefDAO.findByGeneSymbol(key.toUpperCase());
    }

    public static TxrRefCache getInstance() {
        return txrRefCache;
    }

    public List<String> likeQuery(String query, int size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        query = query.toLowerCase();
        String firstA = new String(new char[] { query.toLowerCase().charAt(0) });
        // add lazy process
        if (lazy && !firstAsymbolListMap.containsKey(firstA)) {
            List<TxrRef> coll = txrRefDAO.fuzzyQuery("geneSymbol", firstA, Integer.MAX_VALUE);
            coll.addAll(txrRefDAO.fuzzyQuery("geneSymbol", firstA.toUpperCase(), Integer.MAX_VALUE));
            List<String> subSymbolList = new ArrayList<String>();
            for (TxrRef ref : coll) {
                String geneSymbol = ref.getGeneSymbol();
                String key = geneSymbol.toLowerCase();
                symbolMap.put(key, geneSymbol);
                subSymbolList.add(key);
            }
            Collections.sort(subSymbolList);
            firstAsymbolListMap.put(firstA, subSymbolList);
        }
        List<String> keyList = firstAsymbolListMap.get(firstA);
        if (CollectionUtils.isEmpty(keyList)) {
            return Collections.emptyList();
        }
        List<String> resultList = new ArrayList<String>();
        for (String key : keyList) {
            if (key.startsWith(query)) {
                String val = symbolMap.get(key);
                if (!resultList.contains(val)) {
                    resultList.add(val);
                }
            }
            if (resultList.size() >= size) {
                break;
            }
        }
        return resultList;
    }

}
