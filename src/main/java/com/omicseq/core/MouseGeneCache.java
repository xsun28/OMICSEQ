package com.omicseq.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.Gene;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 * 
 */
public class MouseGeneCache extends AbstractCache<String, Gene> implements IInitializeable {
	
    private IGeneDAO geneDAO = DAOFactory.getDAOByTableType(IGeneDAO.class, "mouse");
    
    private static MouseGeneCache geneCache = new MouseGeneCache(false);
    private static ConcurrentMap<Integer, List<Gene>> startGeneMap = new ConcurrentHashMap<Integer, List<Gene>>();
    private static String tssTesTotalKeyTempalte = "gene_s{0}_e{1}_se{2}";
    // key is start,end+seqname
    private static Set<String> tssTesTotalSet = new HashSet<String>();
    private static String tss5kTotalKeyTempalte = "gene_s{0}_se{1}";
    // key is start+seqname
    private static Set<String> tss5kTotalSet = new HashSet<String>();
    private static String tes5kTotalKeyTempalte = "gene_e{0}_se{1}";
    // key is end+seqname
    private static Set<String> tes5kTotalSet = new HashSet<String>();
    private static Map<Integer, List<Gene>> geneMap = new HashMap<Integer, List<Gene>>();

    private MouseGeneCache(boolean lazy) {
        super(lazy);
    }

    @Override
    public void doInit() {
        synchronized (MouseGeneCache.class) {
            Integer start = 0;
            Integer limit = 3000;
            List<Gene> geneList = null;
            while (CollectionUtils.isNotEmpty(geneList = geneDAO.loadGeneList(start, limit))) {
                for (Gene gene : geneList) {
                    if (gene.getSeqName().contains("_")) {
                        continue;
                    }
                    put(gene.getTxName().toLowerCase(), gene);
                    List<Gene> childGeneList = geneMap.get(gene.getGeneId());
                    if (childGeneList == null) {
                        childGeneList = new ArrayList<Gene>();
                        geneMap.put(gene.getGeneId(), childGeneList);
                    }
                    childGeneList.add(gene);

                    List<Gene> startGeneList = startGeneMap.get(gene.getStart());
                    if (startGeneList == null) {
                        startGeneList = new ArrayList<Gene>();
                        startGeneMap.put(gene.getStart(), startGeneList);
                    }
                    startGeneList.add(gene);
                    String tssTesTotalKey = MessageFormat.format(tssTesTotalKeyTempalte, gene.getStart(),
                            gene.getEnd(), gene.getSeqName());
                    tssTesTotalSet.add(tssTesTotalKey);

                    if ("+".equalsIgnoreCase(gene.getStrand())) {
                        String tss5kTotalKey = MessageFormat.format(tss5kTotalKeyTempalte, gene.getStart(),
                                gene.getSeqName());
                        tss5kTotalSet.add(tss5kTotalKey);
                    } else {
                        String tss5kTotalKey = MessageFormat.format(tss5kTotalKeyTempalte, gene.getEnd(),
                                gene.getSeqName());
                        tss5kTotalSet.add(tss5kTotalKey);
                    }

                    String tes5kTotalKey = MessageFormat
                            .format(tes5kTotalKeyTempalte, gene.getEnd(), gene.getSeqName());
                    tes5kTotalSet.add(tes5kTotalKey);
                }
                start = start + limit;
            }
        }
    }

    public List<Gene> getGeneById(Integer geneId) {
        return geneMap.get(geneId);
    }

    public Gene getGeneByName(String txName) {
        if (StringUtils.isBlank(txName)) {
            return null;
        }
        return get(txName.toLowerCase(), txName);
    }

    public Gene get(String key, String query) {
        if (lazy && !isContainsKey(key)) {
            String lock = String.format("%s_%s", getClass().getName(), key).intern();
            synchronized (lock) {
                if (logger.isDebugEnabled()) {
                    logger.debug("lazy load data by:" + key);
                }
                Gene val = lazyLoad(query);
                put(key, val);
            }
        }
        return get(key);
    }

    public Integer getGeneIdStartAndEnd(Integer start, Integer end, String reqName) {
        List<Gene> startGeneList = startGeneMap.get(start);
        for (Gene gene : startGeneList) {
            if (end.compareTo(gene.getEnd()) == 0 && gene.getSeqName().equalsIgnoreCase(reqName)) {
                return gene.getGeneId();
            }
        } 
        return null;
    }

    /**
     * 获取所有不重复的gene_id
     * 
     * @return
     */
    public List<Integer> getGeneIds() {
        Collection<Gene> coll = values();
        if (CollectionUtils.isEmpty(coll)) {
            return Collections.emptyList();
        }
        List<Integer> rs = new ArrayList<Integer>(5);
        for (Gene gene : coll) {
            Integer key = gene.getGeneId();
            if (!rs.contains(key)) {
                rs.add(key);
            }
        }
        Collections.sort(rs);
        return rs;
    }

    public Collection<Gene> genes() {
        return super.values();
    }

    @Override
    Gene lazyLoad(String key) {
        return geneDAO.getByName(key);
    }

    public static void main(String[] args) {
        MouseGeneCache geneCache = new MouseGeneCache(false);
        geneCache.init();
        System.out.println(geneCache.getTssTesTotal());
    }

    public static MouseGeneCache getInstance() {
        return geneCache;
    }

    public Integer getTssTesTotal() {
        return tssTesTotalSet.size();
    }

    public Integer getTss5kTotal() {
        return tss5kTotalSet.size();
    }

    public Integer getTes5kTotal() {
        return tes5kTotalSet.size();
    }
}
