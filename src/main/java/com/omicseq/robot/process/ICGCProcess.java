package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;

public class ICGCProcess {
    private static final Logger logger = LoggerFactory.getLogger(ICGCProcess.class);

    public static void main(String[] args) {
        DateTime dt = DateTime.now();
        if (logger.isDebugEnabled()) {
            logger.debug("icgc start {} ", dt);
        }
        new ICGCProcess().meta();
        if (logger.isDebugEnabled()) {
            logger.debug("icgc   used {} ", DateTimeUtils.used(dt));
        }
        System.exit(0);
    }

    private void meta() {
        ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
        List<Sample> coll = dao.find(new SmartDBObject("source", SourceType.ICGC.value()));
        Map<String, Sample> cache = new HashMap<String, Sample>(coll.size());
        for (Sample sample : coll) {
            Map<String, String> map = sample.descMap();
            String key = map.get("icgc_specimen_id");
            if (StringUtils.isNotBlank(key)) {
                cache.put(key, sample);
            }
        }
        File dir = new File("/files/download/icgc_ftp");
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        for (File file : files) {
            File[] list = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().startsWith("clinical");
                }
            });
            File f = list != null && list.length > 0 ? list[0] : null;
            if (null != f) {
                updateMeta(f, cache, dao);
            }
            Collection<Sample> collection = cache.values();
            for (Sample obj : collection) {
                Map<String, String> map = obj.descMap();
                String ep = map.get("experimental_protocol");
                if (StringUtils.isNotBlank(ep) && ep.indexOf("https://tcga") != -1) {
                    ep = StringUtils.trimToEmpty(ep.substring(0, ep.indexOf("https://tcga")));
                    map.put("experimental_protocol", ep);
                    obj.descMap(map);
                    dao.update(obj);
                }
            }
        }
    }

    private void updateMeta(File file, Map<String, Sample> cache, ISampleDAO dao) {
        BufferedReader br = null;
        try {
            if (file.getName().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            } else {
                br = new BufferedReader(new FileReader(file));
            }
            String header = br.readLine();
            String[] headers = header.split("\t");
            int pkIdx = 0;
            for (int i = 0; i < headers.length; i++) {
                if (StringUtils.equalsIgnoreCase("icgc_specimen_id", headers[i])) {
                    pkIdx = i;
                }
            }
            String line = "";
            while (StringUtils.isNoneBlank(line = br.readLine())) {
                String[] arr = line.split("\t");
                String key = arr[pkIdx];
                Sample obj = cache.get(key);
                if (null == obj) {
                    continue;
                }
                Map<String, String> map = obj.descMap();
                for (int i = 2; i < arr.length; i++) {
                    String _key = headers[i];
                    if (!map.containsKey(_key)) {
                        map.put(_key, StringUtils.trimToEmpty(arr[i]));
                    }
                }
                String ep = map.get("experimental_protocol");
                if (StringUtils.isNotBlank(ep) && ep.indexOf("https://tcga") != -1) {
                    ep = StringUtils.trimToEmpty(ep.substring(0, ep.indexOf("https://tcga")));
                    map.put("experimental_protocol", ep);
                }
                obj.descMap(map);
                dao.update(obj);
                cache.remove(key);
            }
        } catch (Exception e) {
            logger.error("update mate falied!", e);
        } finally {
            IOUtils.closeQuietly(br);
        }

    }

    void move() {
        String root = SourceType.ICGC.path();
        File dir = new File(root);
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        try {
            File dest = new File("/files/download/icgc_ftp");
            for (File file : files) {
                FileUtils.moveDirectoryToDirectory(file, dest, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);

    private Integer getSampleId() {
        return sampleDAO.getSequenceId(SourceType.ICGC);
    }

    private Map<File, String> urlMap = new HashMap<File, String>(5);

    void split() {
        String root = SourceType.ICGC.path();
        File dir = new File(root);
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        Map<String, String> codeMap = new HashMap<String, String>(12);
        List<File> mateFiles = new ArrayList<File>(5);
        for (File file : files) {
            File[] list = file.listFiles();
            File parsed = null, mate = null, splited = null, split = null;
            for (File f : list) {
                if (f.getName().toLowerCase().startsWith("gene_expression")) {
                    split = f;
                } else if (f.getName().toLowerCase().endsWith(".splied")) {
                    splited = f;
                } else if (f.getName().toLowerCase().startsWith("clinicalsample")) {
                    mate = f;
                } else if (f.getName().toLowerCase().endsWith(".parsed")) {
                    parsed = f;
                }
            }
            if (null != split && null == splited) {
                logger.debug("split file {} ", split);
                codeMap.putAll(split(split, dir));
            }
            if (null != mate && null == parsed) {
                urlMap.put(mate, "ftp://data.dcc.icgc.org/current/" + file.getName() + "/" + split.getName());
                mateFiles.add(mate);
            }
        }
        // create sample data
        toSample(mateFiles, codeMap);
    }

    private void toSample(List<File> mateFiles, Map<String, String> codeMap) {
        List<Sample> data = new ArrayList<Sample>(12);
        List<StatisticInfo> infos = new ArrayList<StatisticInfo>(12);
        for (File file : mateFiles) {
            logger.debug("parsed file {} ", file);
            toSample(file, codeMap, data, infos);
        }
        DAOFactory.getDAOByTableType(ISampleDAO.class, "new").create(data);
        DAOFactory.getDAO(IStatisticInfoDAO.class).create(infos);
    }

    private void toSample(File file, Map<String, String> codeMap, List<Sample> data, List<StatisticInfo> infos) {
        BufferedReader br = null;
        try {
            if (file.getName().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            } else {
                br = new BufferedReader(new FileReader(file));
            }
            String header = br.readLine();
            String[] headers = header.split("\t");
            String line = "";
            while (StringUtils.isNoneBlank(line = br.readLine())) {
                String[] arr = line.split("\t");
                String icgc_sample_id = arr[0];
                String code = icgc_sample_id.toUpperCase();
                if (!codeMap.containsKey(code)) {
                    continue;
                }
                String project_code = arr[1];
                Sample obj = new Sample();
                Integer sampleId = getSampleId();
                obj.setSampleId(sampleId);
                obj.setSource(SourceType.ICGC.value());
                obj.setEtype(ExperimentType.RNA_SEQ.value());
                obj.setCell(project_code);
                obj.setSampleCode(icgc_sample_id);
                obj.setUrl(urlMap.get(file));
                Map<String, String> map = obj.descMap();
                for (int i = 2; i < arr.length; i++) {
                    map.put(headers[i], StringUtils.trimToEmpty(arr[i]));
                }
                obj.descMap(map);
                data.add(obj);
                StatisticInfo info = new StatisticInfo();
                info.setPath(codeMap.get(code));
                info.setPriority(0);
                info.setSampleId(sampleId);
                info.setState(StatisticInfoStatus.DEFAULT.value());
                info.setSource(SourceType.ICGC.value());
                info.setServerIp("112.25.20.157");
                infos.add(info);
            }
            FileUtils.write(new File(file.getParent(), "success.parsed"), "success");
        } catch (Exception e) {
            logger.error("parsed falied!", e);
        } finally {
            IOUtils.closeQuietly(br);
        }
    }

    private Map<String, String> split(File file, File dir) {
    	//将超大文件按sample拆分为多个文件   读取/files/download/icgc/gene_expression.OV-US.tsv.gz 找到“icgc_sample_id”与当前samplecode相等的 ——>> /files/download/icgc/samplecode.tsv  2014-0814
        Map<String, String> codeMap = new HashMap<String, String>(12);
        try {
            BufferedReader bufferedReader;
            if (file.getName().endsWith(".gz")) {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(new FileInputStream(file))));
            } else {
                bufferedReader = new BufferedReader(new FileReader(file));
            }
            String line = "";
            int count = 0;
            String currentSampleCode = "";
            DateTime dt = DateTime.now();
            String header = bufferedReader.readLine();
            List<String> lines = new ArrayList<String>(5);
            while (StringUtils.isNoneBlank(line = bufferedReader.readLine())) {
                count = count + 1;
                if (count % 100000 == 0) {
                    System.out.println(" using time : " + DateTimeUtils.used(dt) + "; for count : " + count);
                }
                String[] lineArray = line.split("\t");
                String sampleId = lineArray[3];
                String sampleCode = sampleId;
                if (!sampleCode.equalsIgnoreCase(currentSampleCode)) {
                    if (StringUtils.isNotBlank(currentSampleCode)) {
                        File toFile = writeToFile(dir, currentSampleCode, lines);
                        codeMap.put(currentSampleCode.toUpperCase(), toFile.getPath());
                    }
                    currentSampleCode = sampleCode;
                    lines.clear();
                    lines.add(header);
                }
                lines.add(line);
            }
            File toFile = writeToFile(dir, currentSampleCode, lines);
            codeMap.put(currentSampleCode.toUpperCase(), toFile.getPath());
            FileUtils.write(new File(file.getParent(), "success.splied"), "success");
        } catch (Exception e) {
            logger.error("split falied!", e);
        }
        return codeMap;
    }

    private File writeToFile(File parent, String code, List<String> lines) throws IOException {
        File file = new File(parent, code + ".tsv");
        FileUtils.writeLines(file, lines);
        return file;
    }

}
