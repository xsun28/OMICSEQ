package com.omicseq.store.imp;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.core.GeneIdMappingCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneIdMapping;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneIdMappingDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class GeneImp extends BaseImp {
    private static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
    private static IGeneIdMappingDAO geneIdMappingDAO = DAOFactory.getDAO(IGeneIdMappingDAO.class);
    private static GeneIdMappingCache cacheMapping = GeneIdMappingCache.getInstance();
    private static Map<String, Integer> cache = new LinkedHashMap<String, Integer>(5);
    private int cnt = 0;

    public static void main(String[] args) throws Exception {
        try {
            geneIdMappingDAO.clean();
            geneDAO.clean();
            String file = "./src/test/resources/gene_meta_hg19.csv";
            new GeneImp().impl(file);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /*
     * static Set<String> exits=new HashSet<String>(5);
     * 
     * @Override void doProcess(String[] lines) { if
     * ("seqname".equalsIgnoreCase(lines[0])) { return; } String SeqName =
     * StringUtils.trimToEmpty(lines[0]); exits.add(SeqName); }
     */

    @Override
    void doProcess(String[] lines) {
        // seqname,start,end,width,strand,tx_id,tx_name
        String seqName = lines[0];
        if ("seqname".equalsIgnoreCase(seqName) || StringUtils.isBlank(seqName)) {
            return;
        }
        if (seqName.indexOf("_") != -1) {
            return;
        }
        // start and end not
        if (null == lines[2] || null == lines[3]) {
            throw new OmicSeqException("start and end not empty!");
        }
        Gene gen = new Gene();
        gen.setSeqName(seqName);

        gen.setStart(toInteger(lines[1]));
        gen.setEnd(toInteger(lines[2]));
        gen.setWidth(toInteger(lines[3]));
        gen.setStrand(lines[4]);
        gen.setGeneId(toInteger(lines[5]));
        gen.setTxName(lines[6]);
        String key = String.format("%s_%s_%s",gen.getSeqName(), gen.getStart(), gen.getEnd());
        if (!cache.containsKey(key)) {
            cache.put(key, ++cnt);
        }
        mapping(key, gen.getGeneId());
        Integer geneId = cache.get(key);
       gen.setGeneId(geneId);
        geneDAO.create(gen);
        logger.debug("Created:" + gen);
    }

    private void mapping(String key, Integer oldId) {
        GeneIdMapping idMap = new GeneIdMapping();
        idMap.setOldId(oldId);
        idMap.setNewId(cache.get(key));
        idMap.setRange(key);
        geneIdMappingDAO.create(idMap);
        cacheMapping.put(oldId, idMap);
    }
}
