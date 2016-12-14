package com.omicseq.store.imp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.omicseq.domain.Gene;
import com.omicseq.domain.HashDB;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;

public class NewGeneImp extends BaseImp {
    private static IGeneDAO geneDAO = DAOFactory.getDAOByTableType(IGeneDAO.class, "new");
    private static IHashDBDAO geneIdDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "gene");
    private Map<String, Gene> cache = new LinkedHashMap<String, Gene>(5);
    private Set<String> exits = new HashSet<String>(5);
    private List<Gene> data = new ArrayList<Gene>(5);

    public static void main(String[] args) throws Exception {
        NewGeneImp impl = new NewGeneImp();
        try {
            String file = "./src/test/resources/gene_meta_hg19_new.csv";
            impl.before();
            impl.impl(file);
            impl.after();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private void before() {
        List<Gene> all = geneDAO.find(new SmartDBObject());
        for (Gene gene : all) {
            String key = String.format("%s_%s_%s", gene.getSeqName(), gene.getStart(), gene.getEnd());
            cache.put(key, gene);
        }
        List<HashDB> coll = geneIdDAO.find(new SmartDBObject());
        for (HashDB item : coll) {
            exits.add(item.getKey());
        }
    }

    private void after() {
        DateTime dt = DateTime.now();
        logger.debug("create gene {} recores", data.size());
        geneDAO.create(data);
        logger.debug("created gene used {}", DateTimeUtils.diff(dt, DateTime.now()));
        Set<Entry<String, Gene>> entrySet = cache.entrySet();
        List<HashDB> data = new ArrayList<HashDB>(5);
        for (Entry<String, Gene> entry : entrySet) {
            if (exits.contains(entry.getKey())) {
                continue;
            }
            HashDB obj = new HashDB(entry.getKey(), String.valueOf(entry.getValue().getGeneId()));
            data.add(obj);
        }
        geneIdDAO.create(data);
        logger.debug("created gene idmapping used {}", DateTimeUtils.diff(dt, DateTime.now()));
    }

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
        Gene gene = new Gene();
        gene.setSeqName(seqName);
        gene.setStart(toInteger(lines[1]));
        gene.setEnd(toInteger(lines[2]));
        gene.setWidth(toInteger(lines[3]));
        gene.setStrand(lines[4]);
        gene.setGeneId(toInteger(lines[5]));
        gene.setTxName(lines[6]);
        String key = String.format("%s_%s_%s", gene.getSeqName(), gene.getStart(), gene.getEnd());
        if (!cache.containsKey(key)) {
            int geneId = cache.size() + 1;
            gene.setGeneId(geneId);
            cache.put(key, gene);
        }
        gene.setGeneId(cache.get(key).getGeneId());
        data.add(gene);
    }
}
