package com.omicseq.store.imp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.domain.HashDB;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.imp.Eutils.BaseConverter;
import com.omicseq.store.imp.Eutils.ESearchCallable;
import com.omicseq.store.imp.Eutils.ESummaryResult;
import com.omicseq.utils.DateTimeUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class GeneSymbolHashDB {
    private static Logger logger = LoggerFactory.getLogger(GeneSymbolHashDB.class);
    private static ITxrRefDAO dao = DAOFactory.getDAO(ITxrRefDAO.class);
    private static IHashDBDAO geneSymbolDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "genesymbol");

    public static void main(String[] args) {
        DateTime dt = DateTime.now();
        IHashDBDAO _geneSymbolDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "genesymbol_20140509");
        Set<String> keys = new HashSet<String>(5);
        int start = 0;
        int limit = 5000;
        while (true) {
            SmartDBObject query = new SmartDBObject();
            query.addReturnFields("key");
            logger.debug("load data  start {} ,limit {} ",start,limit);
            List<String> coll = _geneSymbolDAO.find(query, start, limit, String.class);
            if (CollectionUtils.isEmpty(coll)) {
                break;
            }
            keys.addAll(coll);
            start += limit;
        }
        start = 0;
        while (true) {
            logger.debug("load data  start {} ,limit {} ",start,limit);
            List<HashDB> coll = geneSymbolDAO.find(new SmartDBObject(),start,limit);
            if (CollectionUtils.isEmpty(coll)) {
                break;
            }
            for (HashDB obj : coll) {
                keys.remove(obj.getKey());
            }
            start += limit;
        }
        logger.info("keys size {} ",keys.size());
        if (logger.isDebugEnabled()) {
            logger.debug("checked used {}", DateTimeUtils.used(dt));
        }
        DateTime _dt = DateTime.now();
        idToName(keys);
        if (logger.isDebugEnabled()) {
            logger.debug("idToName used {}", DateTimeUtils.used(_dt));
        }
        
    }

    public static void main2(String[] args) {
        DateTime dt = DateTime.now();
        List<HashDB> coll = geneSymbolDAO.find(new SmartDBObject());
        for (HashDB obj : coll) {
            obj.setValue(obj.getValue().toUpperCase());
            geneSymbolDAO.update(obj);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("toUpperCase used {}", DateTimeUtils.used(dt));
        }
    }

    public static void main1(String[] args) {
        List<TxrRef> coll = dao.find(new SmartDBObject());
        Set<String> geneSymbols = new HashSet<String>(5);
        for (TxrRef txrRef : coll) {
            if (StringUtils.isNotBlank(txrRef.getGeneSymbol())) {
                geneSymbols.add(txrRef.getGeneSymbol());
            }
        }
        DateTime dt = DateTime.now();
        Set<String> ids = nameToId(geneSymbols);
        if (logger.isDebugEnabled()) {
            logger.debug("nameToId used {}", DateTimeUtils.used(dt));
        }
        DateTime _dt = DateTime.now();
        idToName(ids);
        if (logger.isDebugEnabled()) {
            logger.debug("idToName used {}", DateTimeUtils.used(_dt));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("all used {}", DateTimeUtils.used(dt));
        }
    }

    private static Set<String> nameToId(Set<String> geneSymbols) {
        final Set<String> ids = new HashSet<String>();
        List<String> list = new ArrayList<String>(geneSymbols);
        Collections.sort(list);
        if (logger.isDebugEnabled()) {
            logger.debug("name list size is {} ", list.size());
        }
        while (true) {
            DateTime dt = DateTime.now();
            List<String> names = new ArrayList<String>(5);
            for (int i = 0; i < 10; i++) {
                if (list.isEmpty()) {
                    break;
                }
                names.add(list.remove(0));
            }
            List<WaitFutureTask<Object>> taskList = Eutils.buildTasks(names, new ESearchCallable() {
                @Override
                public Object unmarshal(HierarchicalStreamReader reader, String param) {
                    int size = 0;
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        if (StringUtils.equalsIgnoreCase("IdList", reader.getNodeName())) {
                            while (reader.hasMoreChildren()) {
                                reader.moveDown();
                                String key = reader.getValue();
                                ids.add(key);
                                size += 1;
                                reader.moveUp();
                            }
                        } else if (StringUtils.isBlank(reader.getNodeName())) {
                            break;
                        }
                        reader.moveUp();
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("geneSymbol {} data size {} ", param, size);
                    }
                    return null;
                }
            });
            try {
                if (taskList.isEmpty()) {
                    break;
                }
                ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(taskList, 100l, TimeUnit.HOURS);
                if (logger.isDebugEnabled()) {
                    logger.debug("process the batch used {}", DateTimeUtils.used(dt));
                }
            } catch (Exception e) {
            }
        }
        return ids;
    }

    private static void idToName(Set<String> ids) {
        if (logger.isDebugEnabled()) {
            logger.debug("process idList size is {} ", ids.size());
        }
        final List<HashDB> data = new ArrayList<HashDB>();
        List<String> idList = new ArrayList<String>(ids);
        Collections.sort(idList);
        String baseUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&version=2.0&id=";
        int fromIndex = 0;
        while (true) {
            if (CollectionUtils.isEmpty(idList)) {
                break;
            }
            DateTime dt = DateTime.now();
            try {
                int toIndex = fromIndex + 10;
                if (toIndex >= idList.size()) {
                    toIndex = idList.size();
                }
                List<String> sublist = idList.subList(fromIndex, toIndex);
                String id = StringUtils.join(sublist, ",");
                String xml = Eutils.getXml(baseUrl + id, 100);
                if (StringUtils.isBlank(xml)) {
                    continue;
                }
                XStream xs = new XStream();
                xs.registerConverter(new BaseConverter<ESummaryResult>() {
                    @Override
                    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                        ESummaryResult rs = new ESummaryResult();
                        reader.moveDown();
                        reader.moveDown();
                        reader.moveUp();
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            String key = reader.getAttribute("uid");
                            reader.moveDown();
                            String value = reader.getValue();
                            data.add(new HashDB(key, value.toUpperCase()));
                            rs.summaries.put(key, value);
                            reader.moveUp();
                            reader.moveUp();
                        }
                        return rs;
                    }
                });
                xs.alias("eSummaryResult", ESummaryResult.class);
                xs.fromXML(xml);
                if (logger.isDebugEnabled()) {
                    logger.debug("convert {} used {}", id, DateTimeUtils.used(dt));
                }
                if (toIndex >= idList.size()) {
                    break;
                }
                fromIndex = toIndex;
            } catch (Exception e) {
                logger.error("error {}", fromIndex);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("create data size {}", data.size());
        }
        geneSymbolDAO.create(data);
    }
}
