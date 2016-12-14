package com.omicseq.store.imp;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.utils.ThreadUtils;

public class SampleRoadmapImp extends BaseImp {
    private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
    private static ISampleDAO samplePrevDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
    private static Map<String, String> urlMap = new HashMap<String, String>();
    private static File urlFile = new File("./src/test/resources/bed.txt");

    public static void main(String[] args) throws Exception {
        try {
            File f = urlFile.exists() ? urlFile : new File("./wget/bed/all.txt");
            List<String> lines = FileUtils.readLines(f);
            for (String line : lines) {
                String key = line.substring(0, line.lastIndexOf("/") + 1);
                urlMap.put(key, line);
            }
            if (null != args && args.length != 0) {
                for (String file : args) {
                    new SampleRoadmapImp().impl(file);
                }
            } else {
                String file = "./src/test/resources/sample_roadmap_meta.csv";
                SampleRoadmapImp impl = new SampleRoadmapImp();
                impl.before();
                impl.impl(file);
                impl.after();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            List<String> lines =new ArrayList<String>( urlMap.values());
            Collections.sort(lines);
            FileUtils.writeLines(urlFile, lines);
            System.exit(0);
        }
    }

    private Map<String, Sample> map = new HashMap<String, Sample>(5);
    private List<String> erros = new ArrayList<String>(5);

    private void before() {
        SmartDBObject query = new SmartDBObject();
        query.put("source", SourceType.Roadmap.value());
        query.addSort("sampleId", SortType.ASC);
        List<Sample> coll = samplePrevDao.find(query);
        for (Sample obj : coll) {
            map.put(obj.getUrl(), obj);
        }
    }

    int postion = 0;
    int timeout = 5 * 60 * 1000;

    @Override
    void doProcess(String[] lines) {
        if (logger.isDebugEnabled()) {
            logger.debug("line {}", ++postion);
        }
        // # GEO Accession,Sample Name,Experiment,NA Accession,Center,
        // SRA FTP,GEO FTP,Embargo end date
        if ("# GEO Accession".equals(lines[0])) {
            return;
        }
        String experiment = lines[2];
        /**
         * <pre>
         * H 开头的
         * ChIP-Seq input 
         * DNase hypersensitivity 
         * mRNA-Seq
         * </pre>
         */
        if ("ChIP-Seq input".equalsIgnoreCase(experiment) || "DNase hypersensitivity".equalsIgnoreCase(experiment)
                || "mRNA-Seq".equalsIgnoreCase(experiment) || experiment.startsWith("H")) {
            Map<String, String> desc = new HashMap<String, String>();
            Sample obj = new Sample();
            String cell = lines[1];
            if (cell.indexOf("cell") != -1) {
                cell = StringUtils.trim(cell.substring(0, cell.indexOf("cell")));
            }
            obj.setCell(cell);
            obj.setLab(lines[4]);
            String ftpUrl = lines[6];
            if (urlMap.containsKey(ftpUrl)) {
                obj.setUrl(urlMap.get(ftpUrl));
            } else if (StringUtils.isNotBlank(ftpUrl)) {
                try {
                    URLConnection urlConn = new URL(ftpUrl).openConnection();
                    urlConn.setConnectTimeout(timeout);
                    InputStream ins = urlConn.getInputStream();
                    byte[] b = new byte[102400];
                    int length = ins.read(b);
                    String html = new String(b, 0, length);
                    String[] _lines = html.split("\n");
                    for (String str : _lines) {
                        str = StringUtils.trim(str);
                        String name = str.substring(str.lastIndexOf(" "));
                        if (name.toLowerCase().endsWith("bed.gz")) {
                            obj.setUrl(String.format("%s%s", ftpUrl, StringUtils.trim(name)));
                            break;
                        }
                    }
                    urlMap.put(ftpUrl, obj.getUrl());
                } catch (Exception e) {
                    erros.add(ftpUrl);
                    logger.info("获取文件信息出错,{},{}", ftpUrl, e);
                } finally {
                    ThreadUtils.sleep(3 * 1000);
                }
            }
            if (StringUtils.isBlank(obj.getUrl())) {
                logger.error("parser failed lineNO:{},{}", postion, StringUtils.join(lines, ","));
                if(StringUtils.isNotBlank(ftpUrl)){
                    logger.error(ftpUrl);
                }
                return;
            }
            Sample exits = map.get(obj.getUrl());
            if (null != exits) {
                logger.debug("sample exits {}", exits);
                return;
            }
            obj.setTimeStamp(lines[7]);
            obj.setFactor(experiment);
            String acc = StringUtils.trim(lines[0]);
            desc.put("geoSampleAccession", acc);
            desc.put("cell", obj.getCell());
            desc.put("anitoby", obj.getFactor());
            desc.put("lab", obj.getLab());
            desc.put("project", SourceType.Roadmap.desc());
            try {
                String accUrl = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=" + acc;

                Document doc = Jsoup.connect(accUrl).timeout(timeout).get();
                Element el = doc.getElementById("ViewOptions").nextElementSibling();
                Element tb = el.select("table>tbody").first();
                Elements trs = tb.select("tr");
                for (Iterator<Element> iterator = trs.iterator(); iterator.hasNext();) {
                    Element tr = iterator.next();
                    Elements tds = tr.children();
                    if (tds.size() != 2) {
                        continue;
                    }
                    String key = StringUtils.trimToEmpty(tds.get(0).text());
                    if ("Characteristics".equals(key)) {
                        // Characteristics
                        String val = StringUtils.trimToEmpty(tds.get(1).text());
                        val.replaceAll("<br>", ";");
                        desc.put(key, val);
                    } else if ("Library source".equals(key) || "Library selection".equals(key)
                            || "Sample type".equals(key)) {
                        // Library source
                        // Library selection
                        // Sample type SRA
                        desc.put(key, StringUtils.trimToEmpty(tds.get(1).text()));
                    }
                }
            } catch (Exception e) {
                logger.info("获取信息出错:http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc={}", acc);
            }
            obj.descMap(desc);
            // mRNA-Seq=RNA_SEQ,DNase hypersensitivity=DNASE_SEQ,(H 开头的,ChIP-Seq
            // input)=CHIP_SEQ
            if ("mRNA-Seq".equalsIgnoreCase(experiment)) {
                obj.setEtype(ExperimentType.RNA_SEQ.value());
            } else if ("DNase hypersensitivity".equalsIgnoreCase(experiment)) {
                obj.setEtype(ExperimentType.DNASE_SEQ.value());
            } else if ("ChIP-Seq input".equalsIgnoreCase(experiment) || experiment.startsWith("H")) {
                obj.setEtype(ExperimentType.CHIP_SEQ_TF.value());
            }
            obj.setSource(SourceType.Roadmap.value());
            Integer sampleId = sampleDAO.getSequenceId(SourceType.Roadmap);
            obj.setSampleId(sampleId);
            samplePrevDao.create(obj);
            logger.debug("Created:" + obj);
        }
    }

    private void after() {
        SmartDBObject query = new SmartDBObject();
        // {source:3,"cell":"adipose nuclei",factor:"ChIP-Seq input"}
        query.put("source", SourceType.Roadmap.value());
        query.put("cell", "adipose nuclei");
        query.put("factor", "ChIP-Seq input");
        List<Sample> list = samplePrevDao.find(query);
        for (Sample sample : list) {
            Integer inputSampleId = sample.getSampleId();
            String url = sample.getUrl();
            // ftp://ftp.ncbi.nlm.nih.gov/geo/samples/GSM621nnn/GSM621401/suppl/GSM621401_BI.Adipose_Nuclei.Input.7.bed.gz
            String fname = FilenameUtils.getName(url);
            String suffix = fname.substring(fname.length() - 7);
            fname = fname.substring(0, fname.length() - 7);
            String like = String.format("\\%s%s", fname.substring(fname.lastIndexOf(".")), suffix);
            SmartDBObject subQuery = MongodbHelper.endLike("url", like);
            subQuery.put("source", SourceType.Roadmap.value());
            List<Sample> subList = samplePrevDao.find(subQuery);
            for (Sample item : subList) {
                item.setInputSampleIds(String.valueOf(inputSampleId));
                item.setSettype("exp");
                samplePrevDao.update(item);
            }
            sample.setInputSampleIds(String.valueOf(inputSampleId));
            sample.setSettype("input");
            samplePrevDao.update(sample);
        }

        if (CollectionUtils.isNotEmpty(erros)) {
            try {
                FileUtils.writeLines(new File("./logs/roadmap.err"), erros);
            } catch (Exception e) {
            }
        }
    }

}
