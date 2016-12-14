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

import com.omicseq.domain.VariationGene;
import com.omicseq.store.dao.IVariationGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class VariationGeneCache extends AbstractCache<String, VariationGene> implements IInitializeable  {
	
    private IVariationGeneDAO geneDAO = DAOFactory.getDAO(IVariationGeneDAO.class);
    
    private static VariationGeneCache geneCache = new VariationGeneCache(false);
//    private static ConcurrentMap<Integer, List<VariationGene>> startGeneMap = new ConcurrentHashMap<Integer, List<VariationGene>>();
    private static String tssTesTotalKeyTempalte = "gene_s{0}_e{1}_se{2}";
    // key is start,end+seqname
    private static Set<String> tssTesTotalSet = new HashSet<String>();
    private static String tss5kTotalKeyTempalte = "gene_s{0}_se{1}";
    // key is start+seqname
    private static Set<String> tss5kTotalSet = new HashSet<String>();
    private static String tes5kTotalKeyTempalte = "gene_e{0}_se{1}";
    // key is end+seqname
    private static Set<String> tes5kTotalSet = new HashSet<String>();
    private static Map<String, List<VariationGene>> geneMap = new HashMap<String, List<VariationGene>>();
    
    public static Collection<VariationGene> listAll = new ArrayList<VariationGene>();

    private VariationGeneCache(boolean lazy) {
        super(lazy);
    }

    @Override
    public void doInit() {
        synchronized (GeneCache.class) {
            Integer start = 0;
            Integer limit = 6000;
            List<VariationGene> geneList = null;
            while (CollectionUtils.isNotEmpty(geneList = geneDAO.loadGeneList(start, limit))) {
                for (VariationGene gene : geneList) {
                    if (gene.getChrom().contains("_")) {
                        continue;
                    }
                    put(gene.getVariationId().toLowerCase(), gene);
                    List<VariationGene> childGeneList = geneMap.get(gene.getVariationId());
                    if (childGeneList == null) {
                        childGeneList = new ArrayList<VariationGene>();
                        geneMap.put(gene.getVariationId(), childGeneList);
                    }
                    childGeneList.add(gene);

//                    List<VariationGene> startGeneList = startGeneMap.get(gene.getChromStart());
//                    if (startGeneList == null) {
//                        startGeneList = new ArrayList<VariationGene>();
//                        startGeneMap.put(gene.getChromStart(), startGeneList);
//                    }
//                    startGeneList.add(gene);
                    String tssTesTotalKey = MessageFormat.format(tssTesTotalKeyTempalte, gene.getChromStart(),
                            gene.getChromEnd(), gene.getChrom());
                    tssTesTotalSet.add(tssTesTotalKey);

//                    if ("+".equalsIgnoreCase(gene.getStrand())) {
//                        String tss5kTotalKey = MessageFormat.format(tss5kTotalKeyTempalte, gene.getChromStart(),
//                                gene.getChrom());
//                        tss5kTotalSet.add(tss5kTotalKey);
//                    } else {
//                        String tss5kTotalKey = MessageFormat.format(tss5kTotalKeyTempalte, gene.getChromEnd(),
//                                gene.getChrom());
//                        tss5kTotalSet.add(tss5kTotalKey);
//                    }
//
//                    String tes5kTotalKey = MessageFormat
//                            .format(tes5kTotalKeyTempalte, gene.getChromEnd(), gene.getChrom());
//                    tes5kTotalSet.add(tes5kTotalKey);
                }
                start = start + limit;
            }
        }
    }

    public List<VariationGene> getGeneById(String geneId) {
        return geneMap.get(geneId);
    }

    public VariationGene getGeneByName(String txName) {
        if (StringUtils.isBlank(txName)) {
            return null;
        }
        return get(txName.toLowerCase(), txName);
    }

    public VariationGene get(String key, String query) {
        if (lazy && !isContainsKey(key)) {
            String lock = String.format("%s_%s", getClass().getName(), key).intern();
            synchronized (lock) {
                if (logger.isDebugEnabled()) {
                    logger.debug("lazy load data by:" + key);
                }
                VariationGene val = lazyLoad(query);
                put(key, val);
            }
        }
        return get(key);
    }

    public String getGeneIdStartAndEnd(Integer start, Integer end, String reqName, Collection<VariationGene> listAll) {
    	if(CollectionUtils.isEmpty(listAll))
    	{
    		listAll =  VariationGeneCache.getInstance().genes();
    	}
//    	int i=0;
    	for(VariationGene v : listAll)
    	{
//    		i++;
    		if(v.getChrom().equalsIgnoreCase(reqName)){
    			if ((start.compareTo(v.getChromStart()) == 1 && start.compareTo(v.getChromEnd()) == -1) || (end.compareTo(v.getChromStart()) == 1 && end.compareTo(v.getChromEnd()) == -1)) {
//        			System.out.println(v.getVariationId() + "==============  " + i);
                    return v.getVariationId();
                }
    		}
    	}
//        List<VariationGene> startGeneList = startGeneMap.get(start);
//    	SmartDBObject query2 = new SmartDBObject("$gte", start-100);
//    	query2.append("$lte", end+100);
//    	
//    	SmartDBObject query = new SmartDBObject("chromStart", query2);
//    	List<VariationGene> startGeneList = geneDAO.find(query);
//        if(startGeneList == null)
//        {
//        	System.out.println("start:" + start);
//        	return null;
//        }
//        for (VariationGene gene : startGeneList) {
//            if (end.compareTo(gene.getChromEnd()) == -1 && gene.getChrom().equalsIgnoreCase(reqName)) {
//                return gene.getVariationId();
//            }
//        } 
        return null;
    }

    /**
     * 获取所有不重复的gene_id
     * 
     * @return
     */
    public List<String> getGeneIds() {
        Collection<VariationGene> coll = values();
        if (CollectionUtils.isEmpty(coll)) {
            return Collections.emptyList();
        }
        List<String> rs = new ArrayList<String>(5);
        for (VariationGene gene : coll) {
            String key = gene.getVariationId();
            if (!rs.contains(key)) {
                rs.add(key);
            }
        }
        Collections.sort(rs);
        return rs;
    }

    public Collection<VariationGene> genes() {
        return super.values();
    }

    @Override
    VariationGene lazyLoad(String key) {
        return geneDAO.getById(key);
    }

    public static void main(String[] args) {
        VariationGeneCache geneCache = new VariationGeneCache(false);
        geneCache.init();
        System.out.println(geneCache.getTssTesTotal());
    }

    public static VariationGeneCache getInstance() {
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
