package com.omicseq.robot.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.Charsets;
import com.omicseq.common.Constants;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;

public class GEOParser{
    private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    protected final static Logger logger = LoggerFactory.getLogger(GEOParser.class);
    private static String url = "/tmp/geo.csv";
    protected static int timeout = 10 * 60 * 1000;
    
    public static void main(String[] args) {
        parser(url);
    }
    
    public static void parser(String url) {
        try {
            File file = new File(url);
            if (!file.exists()) {
                logger.warn("the file " + url + "is not exists, create a new file");
            }
            InputStream input = new FileInputStream(file);
            List<String> lines = IOUtils.readLines(input, Charsets.UTF_8);
            for (String line : lines) {
                String[] data = line.split(",");
                String factor = data[0];
                String pubmedId = data[1];
                SmartDBObject query = MongodbHelper.and(new SmartDBObject("source", SourceType.TCGA.getValue()), new SmartDBObject("factor", factor));
                List<Sample> sampleList = sampleDAO.find(query);
                for (Sample sample : sampleList) {
                    Map<String, String> descMap = sample.descMap();
                    descMap.put("geosampleaccession", pubmedId);
                    sample.descMap(descMap);
                    sampleDAO.update(sample);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
