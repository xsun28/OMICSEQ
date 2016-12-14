package com.omicseq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.store.imp.BaseImp;

public class Temp {

    private static final String INPUT = "input";
    static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
    static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    static IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);

    public static void main(String[] args) throws Exception {
        // dinstinctFileds();
        // splitTCGA();
        // splitRoadMap();
        // checkRoadMap();
        SourceType[] types = SourceType.values();
        for (SourceType type : types) {
            debugToDev(type, null);
        }
        //debugToDev(SourceType.ICGC, null);
        // checkfiles(SourceType.ENCODE, ExperimentType.DNASE_SEQ);
        /*
         * HashDB obj=new HashDB("112.25.20.155_STAT_SUFFIX","");
         * DAOFactory.getDAO(IHashDBDAO.class).create(obj);
         */
        // cleanInputSampleIds();
        // updateSampleDesc();
        // update2();

        // update1( );
        System.exit(0);
    }

    static void dinstinctFileds() {
        ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
        int start = 0;
        int limit = 5000;
        Set<String> cells = new HashSet<String>(5);
        Set<String> factors = new HashSet<String>(5);
        Set<String> labs = new HashSet<String>(5);
        while (true) {
            List<Sample> coll = dao.find(new SmartDBObject(), start, limit);
            if (CollectionUtils.isEmpty(coll)) {
                break;
            }
            for (Sample sample : coll) {
                cells.add(StringUtils.trimToEmpty(sample.getCell()));
                factors.add(StringUtils.trimToEmpty(sample.getFactor()));
                labs.add(StringUtils.trimToEmpty(sample.getLab()));
            }
            start += limit;
        }
        toFile(cells, "./cell.csv");
        toFile(factors, "./factor.csv");
        toFile(labs, "./lab.csv");
    }

    private static void toFile(Set<String> coll, String fname) {
        try {
            List<String> lines = new ArrayList<String>(coll);
            Collections.sort(lines);
            FileUtils.writeLines(new File(fname), "utf-8", lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void splitTCGA() {
        int ip = 163;
        int start = 0;
        int limit = 1400;
        SmartDBObject query = new SmartDBObject("source", SourceType.TCGA.value());
        while (true) {
            List<Sample> coll = sampleDAO.find(query, start, limit);
            if (CollectionUtils.isEmpty(coll)) {
                break;
            }
            String serverIp = String.format("112.25.20.%s", ip);
            System.out.println(serverIp);
            System.out.println(coll.size());
            List<FileInfo> files = new ArrayList<FileInfo>(coll.size());
            for (Sample item : coll) {
                FileInfo info = new FileInfo();
                info.setSampleId(item.getSampleId());
                info.setServerIp(serverIp);
                info.setUrl(item.getUrl());
                info.setSource(item.getSource());
                info.setState(FileInfoStatus.CREATED.value());
                files.add(info);
            }
            fileInfoDAO.create(files);
            start += limit;
            ip -= 1;
        }

    }

    static void splitRoadMap() throws IOException {
        SmartDBObject query = new SmartDBObject();
        query.put("source", SourceType.Roadmap.value());
        query.addSort("sampleId", SortType.ASC);
        List<StatisticInfo> coll = statisticInfoDAO.find(query);
        Set<Integer> exits = new HashSet<Integer>(5);
        for (StatisticInfo info : coll) {
            exits.add(info.getSampleId());
        }
        List<Sample> find = sampleDAO.find(query);
        List<String> lines = new ArrayList<String>(5);
        for (Sample obj : find) {
            if (exits.contains(obj.getSampleId())) {
                continue;
            }
            lines.add(obj.getUrl());
        }
        System.out.println(lines.size());
        /*
         * int ip = 157; for (int i = 0; i < 7; i++) { int fromIndex = i * 100;
         * int toIndex = fromIndex + 100; if (i == 6) { toIndex = lines.size();
         * } List<String> subList = lines.subList(fromIndex, toIndex);
         * FileUtils.writeLines(new File(String.format("./%s.txt", ip + i)),
         * subList); }
         */
    }

    static void checkRoadMap() throws IOException {
        File f = new File("./src/test/resources/bed.txt");
        List<String> lines = FileUtils.readLines(f);
        Map<String, String> urlMap = new HashMap<String, String>();
        for (String line : lines) {
            String key = line.substring(0, line.lastIndexOf("/") + 1);
            urlMap.put(key, line);
        }
        String file = "./src/test/resources/temp.csv";
        lines = FileUtils.readLines(new File(file));
        int i = 0;
        for (String line : lines) {
            String[] arr = BaseImp.split(line);
            String ftpurl = arr[6];
            String experiment = arr[2];
            if ("ChIP-Seq input".equalsIgnoreCase(experiment) || "DNase hypersensitivity".equalsIgnoreCase(experiment)
                    || "mRNA-Seq".equalsIgnoreCase(experiment) || experiment.startsWith("H")) {
                if (StringUtils.isBlank(ftpurl)) {
                    System.out.println(++i + "\t" + line);
                    continue;
                }
                if (!urlMap.containsKey(ftpurl)) {
                    System.out.println((++i) + "\t" + ftpurl);
                } else {
                    urlMap.remove(ftpurl);
                }
            }
        }
        System.out.println(urlMap.size());
    }

    static void debugToDev(SourceType source, ExperimentType type) {
        if (null == source) {
            throw new OmicSeqException("source is not null");
        }
        SmartDBObject query = new SmartDBObject();
        query.put("source", source.value());
        if (null != type) {
            query.put("etype", type.value());
        }
        query.addSort("sampleId", SortType.ASC);
        List<Sample> coll = sampleDAO.find(query);
        query = new SmartDBObject("source", source.value());
        query.put("state", MongodbHelper.ne(StatisticInfoStatus.PROCESSED.value()));
        List<StatisticInfo> infos = statisticInfoDAO.find(query);
        Set<Integer> exclude = new HashSet<Integer>(infos.size());
        for (StatisticInfo info : infos) {
            exclude.add(info.getSampleId());
        }
        List<Sample> data = new ArrayList<Sample>(12);
        for (Sample obj : coll) {
            if (!exclude.contains(obj.getSampleId())) {
                obj.set_id(null);
                data.add(obj);
            }
        }
        DAOFactory.getDAO(ISampleDAO.class).create(data);
    }

    static void checkfiles(SourceType source, ExperimentType type) {
        SmartDBObject query = new SmartDBObject();
        query.put("source", source.value());
        query.put("etype", type.value());
        query.addSort("sampleId", SortType.ASC);
        List<Sample> coll = sampleDAO.find(query);
        Map<String, Sample> map = new HashMap<String, Sample>(5);
        for (Sample sample : coll) {
            String fname = FilenameUtils.getName(sample.getUrl());
            map.put(fname, sample);
        }
        query = new SmartDBObject();
        query.put("source", SourceType.ENCODE.value());
        query.addSort("sampleId", SortType.ASC);
        List<StatisticInfo> list = statisticInfoDAO.find(query);
        for (StatisticInfo info : list) {
            String fname = FilenameUtils.getName(info.getPath());
            map.remove(fname);
        }
        System.out.println(map.size());
        Collection<Sample> values = map.values();
        for (Sample sample : values) {
            String fname = FilenameUtils.getName(sample.getUrl());
            System.out.println(fname);
        }
        System.out.println(values);
    }

    static void cleanInputSampleIds() {
        Integer[] ids = new Integer[] { 100026, 100027, 100044, 100045 };
        for (Integer id : ids) {
            Sample obj = sampleDAO.getBySampleId(id);
            System.out.println(obj);
            obj.setInputSampleIds(null);
            sampleDAO.update(obj);
        }
    }

    static void updateSampleDesc() throws IOException {
        String file = "./src/test/resources/sample_encode_chip_hg19.csv";
        List<String> lines = FileUtils.readLines(new File(file));
        SmartDBObject query = new SmartDBObject();
        query.put("source", SourceType.ENCODE.value());
        List<Sample> coll = sampleDAO.find(query);
        int cnt = 0;
        for (Sample obj : coll) {
            if (obj.getDescription().startsWith("\"")) {
                Integer sampleId = obj.getSampleId();
                int idx = sampleId - 100000;
                String line = lines.get(idx);
                String[] arr = BaseImp.split(line);
                System.out.println(obj);
                obj.setSegment(arr[1]);
                obj.setDescription(arr[3]);
                Map<String, String> descMap = obj.descMap();
                obj.setCell(descMap.get("cell"));
                obj.setFactor(descMap.get("antibody"));
                obj.setLab(descMap.get("lab"));
                obj.setTimeStamp(descMap.get("datesubmitted"));
                if (StringUtils.isBlank(obj.getSettype())) {
                    obj.setSettype(descMap.get("settype"));
                }
                sampleDAO.update(obj);
                if (StringUtils.equalsIgnoreCase(INPUT, obj.getSettype())) {
                    StatisticInfo info = statisticInfoDAO.getBySampleId(obj.getSampleId());
                    if (!Integer.valueOf(99).equals(info.getPriority())) {
                        System.out.println(info);
                        info.setPriority(99);
                        statisticInfoDAO.update(info);
                    }
                }
                ++cnt;
            }
        }
        System.out.println(cnt);
    }

    static void update2() {
        Integer[] ids = new Integer[] { 100798, 100800 };
        for (Integer id : ids) {
            Sample obj = sampleDAO.getBySampleId(id);
            if (StringUtils.isBlank(obj.getSettype())) {
                obj.setSettype(INPUT);
                System.out.println(obj);
                sampleDAO.update(obj);
            }
            StatisticInfo info = statisticInfoDAO.getBySampleId(obj.getSampleId());
            if (!Integer.valueOf(99).equals(info.getPriority())) {
                System.out.println(info);
                info.setPriority(99);
                statisticInfoDAO.update(info);
            }
        }
    }

    static void update1() {
        SmartDBObject query = new SmartDBObject();
        // query.put("factor", "Input");
        // query.put("settype", "exp");
        query.put("source", SourceType.ENCODE.value());
        List<Sample> coll = sampleDAO.find(query);
        for (Sample obj : coll) {
            if (StringUtils.equalsIgnoreCase(INPUT, obj.getSettype())) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(INPUT, obj.getFactor())) {
                obj.setSettype(INPUT);
                String desc = obj.getDescription().replaceAll("setType=exp", "setType=input");
                obj.setDescription(desc);
            }
            if (StringUtils.equalsIgnoreCase("Control", obj.getFactor())) {
                obj.setSettype(INPUT);
            }
            Map<String, String> map = obj.descMap();
            String settype = map.get("settype");
            if (!StringUtils.equalsIgnoreCase(INPUT, settype)) {
                continue;
            }
            System.out.println(obj);
            sampleDAO.update(obj);
            StatisticInfo info = statisticInfoDAO.getBySampleId(obj.getSampleId());
            if (!Integer.valueOf(99).equals(info.getPriority())) {
                System.out.println(info);
                info.setPriority(99);
                statisticInfoDAO.update(info);
            }
        }
    }
}
