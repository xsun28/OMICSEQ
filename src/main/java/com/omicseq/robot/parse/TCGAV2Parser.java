package com.omicseq.robot.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.JSONUtils;
import com.omicseq.utils.MiscUtils;

public class TCGAV2Parser extends TCGAParser {
    private HashMap<String, String> uuidMap = new HashMap<String, String>(5);

    @Override
    String getDataType() {
        return "38";// RNASeqV2
    }

    @Override
    protected boolean validation(String fname) {
        return fname.endsWith(".rsem.genes.normalized_results");
    }

    @Override
    protected String getBarcode(String fname, Set<String> barcodes, Sample sample) {
        String lab = sample.getLab();
        String uuid = fname.substring(lab.length() + 1);
        uuid = uuid.substring(0, uuid.indexOf("."));
        String barcode = uuidMap.get(uuid);
//        String code = super.getBarcode(barcode, barcodes, sample);
//        return null == code ? barcode : code;
        return barcode;
    }

    private BaseParser before() {
        File jsonFile = new File("./uuidata.json");
        try {
            TypeReference<HashMap<String, String>> type = new TypeReference<HashMap<String, String>>() {
            };
            String json = FileUtils.readFileToString(jsonFile);
            if (StringUtils.isNoneBlank(json)) {
                this.uuidMap = JSONUtils.from(json, type);
                return this;
            }
        } catch (Exception e) {
            logger.error("file to json faile", e);
        }

        String url = "https://tcga-data.nci.nih.gov/uuid/uuidBrowser.json?_dc=" + System.currentTimeMillis();
        int start = 0;
        int limit = 1000;
        long total = Long.MAX_VALUE;
        while (true) {
            try {
                List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
                parameters.add(new BasicNameValuePair("start", String.valueOf(start)));
                parameters.add(new BasicNameValuePair("searchParams",
                        "{\"uuidSearchRadio\":\"true\",\"uuidField\":\"\",\"barcodeField\":\"\"}"));
                TypeReference<UUIDData> type = new TypeReference<TCGAV2Parser.UUIDData>() {
                };
                UUIDData data = JSONUtils.from(url, parameters, type);
                for (UUIDBrowserData item : data.uuidBrowserData) {
                    uuidMap.put(item.uuid, item.barcode);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("start{},size:{},toaal:{}", start, uuidMap.size(), data.totalCount);
                }
                if (uuidMap.size() >= data.totalCount || start + limit >= data.totalCount) {
                    break;
                }
                start += limit;
                total = data.totalCount;
            } catch (Exception e) {
                logger.error("start{}", start, e);
                if (start >= total) {
                    break;
                }
            }
        }
        try {
            FileUtils.write(jsonFile, JSONUtils.to(uuidMap));
        } catch (Exception e) {
            logger.error("write json failed ", e);
        }
        return this;
    }

    public static class UUIDData {
        public Integer totalCount;
        public List<UUIDBrowserData> uuidBrowserData;
    }

    public static class UUIDBrowserData {
        public String barcode;
        public String uuid;
    }

    public static void main(String[] args) {
        new TCGAV2Parser().before().start();
//    	SmartDBObject query = new SmartDBObject("createTiemStamp", new SmartDBObject("$regex", "2014-9-10"));
//    	query.put("source", 1);
//    	query.put("etype", 2);
//    	List<Sample> sampleList = samplePrevDAO.find(query);
//    	
//    	for(Sample sample : sampleList)
//    	{
//    		FileInfo info = new FileInfo();
//            info.setSampleId(sample.getSampleId());
//            info.setServerIp("112.25.20.160");
//            info.setSource(sample.getSource());
//            info.setUrl(sample.getUrl());
//            info.setState(FileInfoStatus.CREATED.value());
//            fileInfoDAO.create(info);
//    	}
    	
    }

}
