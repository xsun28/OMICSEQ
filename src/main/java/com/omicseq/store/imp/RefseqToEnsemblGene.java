package com.omicseq.store.imp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.Charsets;
import com.omicseq.core.EnsemblGeneCache;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.HashDB;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.ThreadUtils;

public class RefseqToEnsemblGene {
    private static Logger logger = LoggerFactory.getLogger(RefseqToEnsemblGene.class);
    private static IHashDBDAO hashDBDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "EnsemblGene");
    private static List<String> errors = new ArrayList<String>(12);

    public static void main(String[] args) throws IOException {
        /*
         * HashDB fromNCBI = fromNCBI("NM_032785");
         * System.out.println(fromNCBI);
         */
        DateTime dt = DateTime.now();
        refseqToEnsemblGene();
        // refseqToEnsemblGeneFromNCBI();
        logger.debug("used {}", DateTimeUtils.used(dt));
        // FileUtils.writeLines(new File("EnsemblGeneFromNCBI.err"), errors);
        // errors.clear();

    }

    static void refseqToEnsemblGeneFromNCBI() {
        EnsemblGeneCache.getInstance().init();
        GeneCache.getInstance().init();
        List<Integer> geneIds = GeneCache.getInstance().getGeneIds();
        List<HashDB> data = new ArrayList<HashDB>(12);
        for (Integer id : geneIds) {
            List<Gene> genes = GeneCache.getInstance().getGeneById(id);
            for (Gene gene : genes) {
                String refseq = gene.getTxName();
                List<String> ensembl = EnsemblGeneCache.getInstance().getEnsembl(refseq);
                if (CollectionUtils.isEmpty(ensembl)) {
                    HashDB obj = fromNCBI(refseq);
                    if (null != obj) {
                        data.add(obj);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(data) && data.size() % 3000 == 0) {
                hashDBDAO.create(data);
                data.clear();
            }
        }
        hashDBDAO.create(data);
    }

    private static HashDB fromNCBI(String refseq) {
        HashDB rs = null;
        String url = "http://www.ncbi.nlm.nih.gov/gene/?term=" + refseq;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("from NCBI:{}", url);
            }
            Document doc = Jsoup.connect(url).timeout(5 * 60 * 1000).get();
            Elements els = doc.getElementById("summaryDl").select("dd");
            for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
                Element el = iterator.next();
                Elements links = el.select("a");
                if (!links.isEmpty()) {
                    Element link = links.first();
                    String text = StringUtils.trimToEmpty(link.text());
                    if (text.toLowerCase().startsWith("ensembl")) {
                        String href = link.attr("href");
                        String ensembl = href.substring(href.lastIndexOf("/") + 1);
                        rs = new HashDB(ensembl, refseq);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("{} Exception!", url, e);
        }
        if (null == rs) {
            errors.add(url);
            logger.error("{} parser failed!", url);
        }
        return rs;
    }

    static void refseqToEnsemblGene() {
        Set<String> refseqs = new HashSet<String>(5);
        EnsemblGeneCache.getInstance().init();
        GeneCache.getInstance().init();
        List<Integer> geneIds = GeneCache.getInstance().getGeneIds();
        for (Integer id : geneIds) {
            List<Gene> genes = GeneCache.getInstance().getGeneById(id);
            for (Gene gene : genes) {
                String refseq = gene.getTxName();
                refseqs.add(refseq);
            }
        }
        ITxrRefDAO dao = DAOFactory.getDAO(ITxrRefDAO.class);
        int start = 0;
        int limit = 3000;
        while (true) {
            List<TxrRef> coll = dao.loadTxrRefList(start, limit);
            if (CollectionUtils.isEmpty(coll)) {
                break;
            }
            start += limit;
            for (TxrRef item : coll) {
                if (StringUtils.isBlank(item.getRefseq())) {
                    continue;
                }
                refseqs.add(item.getRefseq());
            }
        }
        List<String> list = new ArrayList<String>(refseqs);
        for (String refseq : list) {
            List<String> ensembl = EnsemblGeneCache.getInstance().getEnsembl(refseq);
            if (CollectionUtils.isNotEmpty(ensembl)) {
                refseqs.remove(refseq);
            }
        }
        list = new ArrayList<String>(refseqs);
        Collections.sort(list);
        limit = 10;
        start = 0;
        List<HashDB> data = new ArrayList<HashDB>(12);
        while (true) {
            int to = start + limit;
            if (to >= list.size()) {
                to = list.size();
            }
            List<String> subList = list.subList(start, to);
            if (CollectionUtils.isEmpty(subList)) {
                break;
            }
            data.addAll(convert(subList));
            if (to >= list.size()) {
                break;
            }
            start = to;
            ThreadUtils.sleep(2 * 1000);
        }
        hashDBDAO.create(data);
    }

    static List<HashDB> convert(List<String> subList) {
        List<HashDB> list = new ArrayList<HashDB>(5);
        try {
            StringBuffer idlist = new StringBuffer();
            for (String string : subList) {
                idlist.append(string).append("\n");
            }
            idlist.deleteCharAt(idlist.length() - 1);
            Document doc = Jsoup.connect("http://idconverter.bioinfo.cnio.es/IDconverter.php")
                    .data("species", "Homo_sapiens").data("existing_id", "RefSeqRNA").data("text", "on")
                    .data("check_unigene", "on").data("check_gene", "on").data("check_geneid", "on")
                    .data("check_ensembl", "on").data("check_refseqRNA", "on").data("check_locationE", "on")
                    .data("check_locationEGP", "on").data("check_clone", "on").data("check_accession", "on")
                    .data("Convert.x", "35").data("Convert.y", "18").data("id_list", idlist.toString()).post();
            Element el = doc.select("h2").select("a").first();
            String url = "http://idconverter.bioinfo.cnio.es/" + el.attr("href");
            InputStream input = new URL(url).openStream();
            List<String> lines = IOUtils.readLines(input, Charsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] arr = line.split("\t");
                String key = arr[1];
                String value = arr[4];
                logger.debug("Ensembl_Gene:" + key + " RefseqRNA:" + value);
                if (StringUtils.isNotBlank(key)) {
                    list.add(new HashDB(key, value));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return list;
    }
}
