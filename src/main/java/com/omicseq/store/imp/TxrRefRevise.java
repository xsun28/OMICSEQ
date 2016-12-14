package com.omicseq.store.imp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.domain.HashDB;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.imp.Eutils.ESearchResult;
import com.omicseq.store.imp.Eutils.ESummaryResult;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.JSONUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TxrRefRevise  {
    private static Logger logger = LoggerFactory.getLogger(TxrRefRevise.class);
    private static final String TXRREF_ID_GENE_SYMBOL_MAP = "txrref_idGeneSymbolMap";
    private static final String TXRREF_ID_REFSEQ_MAP = "txrref_idRefseqMap";
    private static ITxrRefDAO dao = DAOFactory.getDAO(ITxrRefDAO.class);
    private static IHashDBDAO hashDBDAO = DAOFactory.getDAO(IHashDBDAO.class);
 

    public static void main(String[] args) {
        DateTime dt = DateTime.now();
        // List<TxrRef> coll = dao.loadTxrRefList(0, 10);
        List<TxrRef> coll = dao.find(new SmartDBObject());
        Set<TxrRef> removes = new HashSet<TxrRef>(5);
        Map<String, List<TxrRef>> refseqMap = new HashMap<String, List<TxrRef>>(5);
        Map<String, List<TxrRef>> geneMap = new HashMap<String, List<TxrRef>>(5);
        int count = 0;
        for (TxrRef txrRef : coll) {
            if (StringUtils.isBlank(txrRef.getRefseq())) {
                /*
                 * if (logger.isDebugEnabled()) {
                 * logger.debug("empty refseq geneSymbole is{}",
                 * txrRef.getGeneSymbol()); }
                 */
                ++count;
                continue;
            }
            String refseq = txrRef.getRefseq().toUpperCase();
            List<TxrRef> subList = refseqMap.get(refseq);
            if (null == subList) {
                subList = new ArrayList<TxrRef>(2);
                refseqMap.put(refseq, subList);
            }
            subList.add(txrRef);
            String geneSymbol = txrRef.getGeneSymbol().toUpperCase();
            List<TxrRef> list = geneMap.get(geneSymbol);
            if (null == list) {
                list = new ArrayList<TxrRef>(2);
                geneMap.put(geneSymbol, list);
            }
            list.add(txrRef);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("empty refseq size {}", count);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("deleted data   size {}", removes.size());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("refseq data   size {}", refseqMap.keySet().size());
        }
        final Set<String> ids = new HashSet<String>(5);
        // refseq to id
        final Map<String, String> idRefseqMap = buildIdRefSeqMap(refseqMap.keySet(), ids);
        if (logger.isDebugEnabled()) {
            logger.debug("idRefseqMap size is {} ", idRefseqMap.size());
            logger.debug("ids size is {} ", ids.size());
        }
        // id to geneSymbol
        final Map<String, String> idGeneSymbolMap = buildGeneSymbolMap(ids);
        if (logger.isDebugEnabled()) {
            logger.debug("GeneSymbolMap size is {} ", idGeneSymbolMap.size());
        }
        for (String refseq : idRefseqMap.keySet()) {
            String id = idRefseqMap.get(refseq);
            String geneSymbol = idGeneSymbolMap.get(id);
            if (StringUtils.isBlank(geneSymbol)) {
                logger.info("{} - {} not found geneSymbol", refseq, id);
                continue;
            }
            List<TxrRef> list = geneMap.get(geneSymbol);
            List<TxrRef> subList = refseqMap.get(refseq);
            if (CollectionUtils.isNotEmpty(list)) {
                subList.removeAll(list);
            }
            if (CollectionUtils.isNotEmpty(subList)) {
                removes.addAll(subList);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("data size is all:{};undeleted:{};deleted:{};", coll.size(), geneMap.size(), removes.size());
        }
        DateTime _dt = DateTime.now();
        dao.delete(new ArrayList<TxrRef>(removes));
        if (logger.isDebugEnabled()) {
            logger.debug("remove used {} ", DateTimeUtils.used(_dt));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("used {} ", DateTimeUtils.used(dt));
        }
        System.exit(0);
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Map<? extends String, ? extends String> toMap(HashDB db) {
        try {
            TypeReference<Map> type = new TypeReference<Map>() {
            };
            Map m = JSONUtils.from(db.getValue(), type);
            return m;
        } catch (Exception e) {
            //
        }
        return new HashMap<String, String>(0);
    }

    /**
     * @param refseqs
     * @param ids
     * @return key refseq value id
     */
    private static Map<String, String> buildIdRefSeqMap(Set<String> refseqs, final Set<String> ids) {
        DateTime dt = DateTime.now();
        final Map<String, String> map = new HashMap<String, String>(5);
        HashDB db = hashDBDAO.getByKey(TXRREF_ID_REFSEQ_MAP);
        if (null != db) {
            map.putAll(toMap(db));
            ids.addAll(map.values());
        }
        List<String> list = new ArrayList<String>(refseqs);
        for (String refseq : map.keySet()) {
            list.remove(refseq);
        }
        Collections.sort(list);
        while (true) {
            List<WaitFutureTask<Object>> taskList = new ArrayList<WaitFutureTask<Object>>(5);
            Semaphore semaphore = new Semaphore(5);
            for (int i = 0; i < 5; i++) {
                if (list.isEmpty()) {
                    break;
                }
                final String refseq = list.remove(0);
                Callable<Object> callable = new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=" + refseq;
                        String xml =Eutils.getXml(url, 20);
                        if (StringUtils.isBlank(xml)) {
                            logger.info("not data return {}", refseq);
                            return null;
                        }
                        try {
                            XStream xs = new XStream();
                            xs.registerConverter(new Converter() {

                                @SuppressWarnings("rawtypes")
                                @Override
                                public boolean canConvert(Class type) {
                                    return type.equals(ESearchResult.class);
                                }

                                @Override
                                public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                                    if (StringUtils.equalsIgnoreCase("eSearchResult", reader.getNodeName())) {
                                        while (true) {
                                            reader.moveDown();
                                            if (StringUtils.equalsIgnoreCase("IdList", reader.getNodeName())) {
                                                if (!reader.hasMoreChildren()) {
                                                    logger.info("not foud id by  {}", refseq);
                                                    break;
                                                }
                                                reader.moveDown();
                                                String key = reader.getValue();
                                                reader.moveUp();
                                                map.put(refseq, key);
                                                ids.add(key);
                                                break;
                                            } else if (StringUtils.isBlank(reader.getNodeName())) {
                                                break;
                                            }
                                            reader.moveUp();
                                        }
                                    }
                                    return null;
                                }

                                @Override
                                public void marshal(Object source, HierarchicalStreamWriter writer,
                                        MarshallingContext context) {
                                }
                            });
                            xs.alias("eSearchResult", ESearchResult.class);
                            xs.fromXML(xml);
                        } catch (Exception e) {
                            logger.error("error {} ", refseq);
                        }
                        return null;
                    }
                };
                WaitFutureTask<Object> e = new WaitFutureTask<Object>(callable, semaphore);
                taskList.add(e);
            }
            try {
                if (taskList.isEmpty()) {
                    break;
                }
                ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(taskList, 100l, TimeUnit.HOURS);
            } catch (Exception e) {
            }
        }
        updateHashDB(db, TXRREF_ID_REFSEQ_MAP, map);
        if (logger.isDebugEnabled()) {
            logger.debug("refseq map used {} ", DateTimeUtils.used(dt));
        }
        return map;
    }

    @SuppressWarnings({ "rawtypes" })
    private static Map<String, String> buildGeneSymbolMap(Set<String> ids) {
        DateTime dt = DateTime.now();
        final Map<String, String> map = new HashMap<String, String>(5);
        HashDB db = hashDBDAO.getByKey(TXRREF_ID_GENE_SYMBOL_MAP);
        if (null != db) {
            map.putAll(toMap(db));
        }
        List<String> idList = new ArrayList<String>(ids);
        // List<String> idList = Arrays.asList("100132062",
        // "100287102","79501");
        Collections.sort(idList);
        for (String id : map.keySet()) {
            idList.remove(id);
        }
        int fromIndex = 0;
        while (true) {
            if (CollectionUtils.isEmpty(idList)) {
                break;
            }
            try {
                int toIndex = fromIndex + 10;
                if (toIndex >= idList.size()) {
                    toIndex = idList.size();
                }
                List<String> sublist = idList.subList(fromIndex, toIndex);
                String id = StringUtils.join(sublist, ",");
                String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&version=2.0&id=";
                String xml = Eutils.getXml(url + id, 100);
                if (StringUtils.isBlank(xml)) {
                    continue;
                }
                XStream xs = new XStream();
                xs.registerConverter(new Converter() {
                    @Override
                    public boolean canConvert(Class type) {
                        return ESummaryResult.class.equals(type);
                    }

                    @Override
                    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                        if (StringUtils.equalsIgnoreCase("eSummaryResult", reader.getNodeName())) {
                            ESummaryResult rs = new ESummaryResult();
                            reader.moveDown();
                            reader.moveDown();
                            reader.moveUp();
                            while (reader.hasMoreChildren()) {
                                reader.moveDown();
                                String key = reader.getAttribute("uid");
                                reader.moveDown();
                                String value = reader.getValue();
                                rs.summaries.put(key, value);
                                reader.moveUp();
                                reader.moveUp();
                                map.put(key, value);
                            }
                            return rs;
                        }
                        return null;
                    }

                    @Override
                    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

                    }
                });
                xs.alias("eSummaryResult", ESummaryResult.class);
                ESummaryResult rs = (ESummaryResult) xs.fromXML(xml);
                if (logger.isDebugEnabled()) {
                    logger.debug("result is {} ", rs);
                }
                if (toIndex >= idList.size()) {
                    break;
                }
                fromIndex = toIndex;
            } catch (Exception e) {
                logger.error("error {}", fromIndex);
            }
        }
        updateHashDB(db, TXRREF_ID_GENE_SYMBOL_MAP, map);
        if (logger.isDebugEnabled()) {
            logger.debug("gene symbol map used {} ", DateTimeUtils.used(dt));
        }
        return map;
    }

   
    private static void updateHashDB(HashDB db, String key, Object value) {
        try {
            if (null == db) {
                hashDBDAO.create(new HashDB(key, JSONUtils.to(value)));
            } else {
                db.setValue(JSONUtils.to(value));
                hashDBDAO.update(db);
            }
        } catch (Exception e) {
        }
    }

  
}
