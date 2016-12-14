package com.omicseq.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.Charsets;
import com.omicseq.common.Constants;
import com.omicseq.common.SourceType;
import com.omicseq.domain.HashDB;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;


public class AntibodyCache implements IInitializeable{
    protected final static Logger logger = LoggerFactory.getLogger(AntibodyCache.class);
    private static String url = "http://www.epigenomebrowser.org/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Antibody&bgcolor=FFFEE8";
    private IHashDBDAO antibodyDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "antibody");
    private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    protected static int timeout = 10 * 60 * 1000;
    private static AntibodyCache antibodyCache = new AntibodyCache();
    private Map<String, String> antibodyFactorMap = new HashMap<String, String>();
    private Map<String, String> antibodyVendorIdMap = new HashMap<String, String>();
    
    public static AntibodyCache getInstance() {
        return antibodyCache;
    }
    
    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("Init All antibody data cache ");
        }
        synchronized (AntibodyCache.class) {
            List<HashDB> hashDBList = antibodyDAO.find(new SmartDBObject());
            for (HashDB hashDB : hashDBList) {
                String value = hashDB.getValue();
                String[] values = value.split(",");
                antibodyFactorMap.put(hashDB.getKey(), values[0]);
                if (values.length > 1) {
                    antibodyVendorIdMap.put(hashDB.getKey(), values[1]);
                }
            }
        }
    }
    
    public String getTargetByAntibody(String antibody) {
        if (antibodyFactorMap.containsKey(antibody)) {
            return antibodyFactorMap.get(antibody);
        }else{
            return "";
        }
    }
    
    public String getVendorIdByAntibody(String antibody) {
        if (antibodyVendorIdMap.containsKey(antibody)) {
            return antibodyVendorIdMap.get(antibody);
        }else{
            return "";
        }
    }
    //解析antibody-target文件导入hashdbantibody表
    private static void parse(String url) {
        IHashDBDAO hashDBDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "antibody");
        List<HashDB> data = new ArrayList<HashDB>(12);
        try {
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            Element tbEl = doc.select("tbody").first();
            Elements els = tbEl.select("tr");
            for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
                Element el = iterator.next();
                String antibody = el.select("td").first().html().replaceAll("\\(", "").replaceAll("\\)", "");
                String target = el.select("td").get(2).html();
                String vendorId = el.select("td").get(4).select("a").html().replace("\n", " ");
                String value = target + "," + vendorId;
                HashDB obj = new HashDB(antibody, value);
                data.add(obj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        hashDBDAO.create(data);
    }
    
    public static void parser2(String url) {
        IHashDBDAO hashDBDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, "antibody");
        List<HashDB> list = new ArrayList<HashDB>(12);
        try {
            File file = new File(url);
            if (!file.exists()) {
                logger.warn("the file " + url + "is not exists");
            }
            InputStream input = new FileInputStream(file);
            List<String> lines = IOUtils.readLines(input, Charsets.UTF_8);
            for (String line : lines) {
                String[] data = line.split(",");
                if (data.length > 1) {
                    String factor = data[0].replaceAll("\\(", "").replaceAll("\\)", "");
                    String target = data[1];
                    if (StringUtils.isNotBlank(target)) {
                        System.out.println("factor:" + factor + ", target:" + target);
                        HashDB obj = new HashDB(factor, target);
                        list.add(obj);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("find [" + list.size() + "] records need to save to database.");
        if (logger.isDebugEnabled()) {
            logger.debug("find [" + list.size() + "] records need to save to database.");
        }
        hashDBDAO.create(list);
    }
    
  //将匹配不上的factor导入csv
    private static void generateCSV() {
        Map<String, String> antibodyMap = new HashMap<String, String>();
        try {
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            Element tbEl = doc.select("tbody").first();
            Elements els = tbEl.select("tr");
            for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
                Element el = iterator.next();
                String antibody = el.select("td").first().html().replaceAll("\\(", "").replaceAll("\\)", "");
                String target = el.select("td").get(2).html();
                antibodyMap.put(antibody, target);
            }
            System.out.println("data's size is:" + antibodyMap.size());
            List<String> factors = new ArrayList<String>();
            Integer start = 0;
            Integer limit = 3000;
            List<Sample> sampleList = null;
            while (CollectionUtils.isNotEmpty(sampleList = sampleDAO.loadSampleList(start, limit))) {
                for (Sample sample : sampleList) {
                    if (StringUtils.isNotBlank(sample.getFactor())) {
                        String fac = sample.getFactor().replaceAll("\\(", "").replaceAll("\\)", "");
                        String target = antibodyMap.get(fac);
                        if (StringUtils.isBlank(target) && !factors.contains(sample.getFactor())) {
                            factors.add(sample.getFactor());
                        }
                    }
                }
                start = start + limit;
            }
            System.out.println("factor's size is:" + factors.size());
            File file = new File("/tmp/factorMapping.csv");
            OutputStream out;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                out = new FileOutputStream(file);
                IOUtils.writeLines(factors, IOUtils.LINE_SEPARATOR_UNIX, out, Charsets.UTF_8);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        //parse(url);
        //generateCSV();
        parser2("E:/log/bak/factorMapping.csv");
    }

}
