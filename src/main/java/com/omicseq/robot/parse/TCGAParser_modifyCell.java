package com.omicseq.robot.parse;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.HashDB;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.ThreadUtils;

/**
 * 
 * 解析TCGA网站的下载链接
 * 
 * @author zejun.du
 */
public class TCGAParser_modifyCell extends BaseParser {

    static String MATEDATA_TB = SourceType.TCGA.name() + "mate";
    protected static IHashDBDAO hashMetaDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, MATEDATA_TB);
    static String FILEURLS_TB = SourceType.TCGA.name() + "files";
    protected static IHashDBDAO hashFilesDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, FILEURLS_TB);
    static String FILEFAIL_TB = SourceType.TCGA.name() + "failed";
    protected static IHashDBDAO hashFailedDAO = DAOFactory.getDAOByTableType(IHashDBDAO.class, FILEFAIL_TB);

    @Override
    SourceType getSourceType() {
        return SourceType.TCGA;
    }

    // "dataType", "28" RNASeqV2
    String getDataType() {
        return "27";// RNASeq
    }

    @Override
    public void parser(String url) {
        int total = 0;
        int pageSize = 15;
        Map<String, String> data = postData(pageSize, getDataType());
        int pageIndex = 1;
        int postion = 1;
        while (true) {
            if (isStoped()) {
                return;
            }
            try {
                data.put("ec_p", String.valueOf(pageIndex));
                Document doc = Jsoup.connect(url).data(data).timeout(timeout).post();
                Element tbEl = doc.getElementById("ec_table").select(".tableBody").first();
                Elements els = tbEl.select("tr");
                for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Start process {}", postion);
                    }
                    Integer sampleId = null;
                    try {
                        Element el = iterator.next();
                        // javascript:tcga.util.requirePolicyAgreement('/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/esca/cgcc/bcgsc.ca/illuminahiseq_rnaseq/rnaseq/bcgsc.ca_ESCA.IlluminaHiSeq_RNASeq.Level_3.1.0.0.tar.gz')
                        String href = el.children().last().select("a").first().attr("href");
                        if (StringUtils.isBlank(href)) {
                            logger.info("Not found download file url by {} pages,{} records!", pageIndex, postion);
                            continue;
                        }
                        String timeStamp = el.child(1).text();
                        String lab = el.child(2).text();
                        String cancerType = el.child(4).text();
                        href = href.substring(href.indexOf("'") + 1, href.lastIndexOf("'"));
                        String surl = "https://tcga-data.nci.nih.gov" + href;
                        HashDB mateData = buildMateData(surl, cancerType.toLowerCase());
                        if (null == mateData) {
                            continue;
                        }
                        HashDB fileUrls = buildFilesUrl(surl, cancerType.toLowerCase());
                        if (null == fileUrls) {
                            continue;
                        }
                        // mate data
                        Map<String, Map<String, String>> mateMap = buildMateMap(mateData.getValue());

                        // flines
                        List<String> fails = new ArrayList<String>(5);
                        List<String> flines = IOUtils.readLines(new StringReader(fileUrls.getValue()));
                        for (String _url : flines) {
                            ++total;
                            Sample exits = samplePrevDAO.getByUrl(_url);
                            String fname = FilenameUtils.getName(_url);
                            String barcode = getBarcode(fname, mateMap.keySet(), exits);
                            Map<String, String> _mateData = null == barcode ? null : mateMap.get(barcode);
                            if (null == barcode || null == _mateData) {
                                String str = String.format("barcode:%s_mate:%s_%s", barcode, null == mateData, _url);
                                fails.add(str);
                                continue;
                            }
                            
                            if(exits == null)
                            {
                            	continue;
                            } else {
                            	exits.setCell(barcode);
                            	samplePrevDAO.update(exits);
                            }
                        }
                    } catch (Exception e) {
                        failed(String.format("%s?pageIndex=%s&postion=%s", url, pageIndex, postion), sampleId, e);
                    } finally {
                        if (logger.isDebugEnabled()) {
                            logger.debug("end process {},process total {}", postion, total);
                        }
                    }
                    ++postion;
                }
                if (els.size() < pageSize) {
                    break;
                }
                ++pageIndex;
                ThreadUtils.sleep(2 * 1000);
            } catch (Exception e) {
                logger.error("parser {} faild with pageIndex:{},postion:{}", getSourceType(), pageIndex, postion, e);
                ThreadUtils.sleep(3 * 1000);
            }
        }
        logger.info("total:{}", total);
        stop();
    }

    private Map<String, Map<String, String>> buildMateMap(String data) throws IOException {
        List<String> lines = IOUtils.readLines(new StringReader(data));
        Map<String, Map<String, String>> mateMap = new HashMap<String, Map<String, String>>(lines.size());
        String[] titles = lines.get(0).split("\t");
        for (int i = 1; i < lines.size(); i++) {
            String sdata = lines.get(i);
            String[] arr = sdata.split("\t");
            String barcode = arr[0];
            Map<String, String> descMap = new HashMap<String, String>(arr.length);
            for (int j = 0; j < titles.length; j++) {
                try {
                    descMap.put(titles[j], arr[j]);
                } catch (Exception e) {
                    logger.error("line:{},col:{},string:{}", i, titles[i], sdata, e);
                }
            }
            mateMap.put(barcode, descMap);
        }
        return mateMap;
    }

    private HashDB buildMateData(String surl, String cancerType) {
        HashDB mateData = hashMetaDAO.getByKey(surl);
        if (null == mateData) {
            String furl = "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/%s/bcr/biotab/clin/nationwidechildrens.org_clinical_patient_%s.txt";
            String _url = String.format(furl, cancerType, cancerType);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("load mate data from {}", _url);
                }
                StringWriter sw = new StringWriter();
                PrintWriter writer = new PrintWriter(sw);
                List<String> lines = IOUtils.readLines(new URL(_url).openStream());
                // line1=title,line2,line3不要.line4开始为数据
                for (int i = 0; i < lines.size(); i++) {
                    if (i == 1 || i == 2) {
                        continue;
                    }
                    String str = lines.get(i);
                    if (i == 0) {
                        str = str.replaceAll(" ", "\t");
                    }
                    writer.println(str);
                }
                writer.flush();
                mateData = new HashDB(surl, sw.toString());
                hashMetaDAO.create(mateData);
            } catch (Exception e) {
                logger.error("load meta file from server failed,{}", _url, e);
                return null;
            }
        }
        writeFile("mate", cancerType, mateData.getValue());
        return mateData;
    }

    private HashDB buildFilesUrl(String surl, String cancerType) {
        HashDB cache = hashFilesDAO.getByKey(surl);
        if (null == cache) {
            String _url = surl.substring(0, surl.length() - 7);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("load file urls from {} ", _url);
                }
                Document _doc = Jsoup.connect(_url).timeout(timeout).get();
                Elements _els = _doc.select("a");
                StringWriter sw = new StringWriter();
                PrintWriter writer = new PrintWriter(sw);
                for (Iterator<Element> it = _els.iterator(); it.hasNext();) {
                    Element _el = it.next();
                    String _href = _el.attr("href");
                    String fname = FilenameUtils.getName(_href);
                    if (validation(fname)) {
                        writer.println(String.format("%s/%s", _url, fname));
                    }
                }
                cache = new HashDB(surl, sw.toString());
                hashFilesDAO.create(cache);
            } catch (Exception e) {
                logger.error("load  file urls from server failed,{}", _url, e);
                return null;
            }
        }
        writeFile("files", cancerType, cache.getValue());
        return cache;
    }

    private File getFile(String suffix, String cancerType) {
        final String fname = String.format("%s_%s", cancerType, suffix);
        File file = new File(String.format("./logs/%s_%s.txt", cancerType, suffix));
        if (file.exists()) {
            int size = file.getParentFile().list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fname);
                }
            }).length;
            file = new File(String.format("./logs/%s_%s_%s.txt", cancerType, suffix, size + 1));
        }
        return file;
    }

    private void writeLines(String suffix, String cancerType, List<String> lines) {
        try {
            File file = getFile(suffix, cancerType);
            FileUtils.writeLines(file, lines, false);
        } catch (Exception e) {
            // do nothing
        }
    }

    private void writeFile(String suffix, String cancerType, String data) {
        try {
            File file = getFile(suffix, cancerType);
            FileUtils.write(file, data);
        } catch (Exception e) {
            // do nothing
        }
    }

    private Map<String, String> postData(int pageSize, String dataType) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Map<String, String> data = new HashMap<String, String>(3);
        data.put("dataType", dataType);
        data.put("archiveType", "3");
        data.put("dateStart", "01/01/2007");
        data.put("dateEnd", DateUtils.format(cal.getTime(), "MM/dd/yyyy"));
        data.put("center", "-1");
        data.put("platform", "-1");
        data.put("project", "-1");
        data.put("ec_crd", String.valueOf(pageSize));
        data.put("ec_i", "ec");
        data.put("ec_rd", String.valueOf(pageSize));
        return data;
    }

    protected boolean validation(String fname) {
        return fname.endsWith("gene.quantification.txt");
    }

    protected String getBarcode(String fname, Set<String> barcodes, Sample sample) {
        if (StringUtils.isBlank(fname)) {
            return null;
        }
        int idx = fname.toLowerCase().indexOf("tcga");
        String name = fname;
        if (idx > 0) {
            name = fname.substring(idx);
        }
        for (String code : barcodes) {
            if (name.startsWith(code)) {
                return code;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new TCGAParser_modifyCell().start();
    }
}
