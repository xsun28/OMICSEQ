package com.omicseq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.ThreadUtils;

public class SN {
    public static void main(String[] args) throws IOException {
        IStatisticInfoDAO dao = DAOFactory.getDAO(IStatisticInfoDAO.class);
        List<StatisticInfo> find = dao.find(new SmartDBObject("state",4));
        for (StatisticInfo info : find) {
            info.setState(0);
            dao.update(info);
        }
        // List<String> list = buildFileList();
        // makeSamples(list);
    }

    static List<String> buildFileList() throws IOException {
        String[] terms = new String[] { "SRX022569", "SRX022570", "SRX022571", "SRX022572", "SRX022573", "SRX022574" };
        List<String> list = new ArrayList<String>(terms.length);
        for (String term : terms) {
            Set<String> set = new HashSet<String>(4);
            Document doc = Jsoup.connect("http://www.ncbi.nlm.nih.gov/sra/?term=" + term).timeout(10 * 1000).get();
            Element rv = doc.getElementById("ResultView");
            Elements els = rv.select("table>tbody>tr");
            for (Iterator<Element> it = els.iterator(); it.hasNext();) {
                Element el = it.next().select("a").first();
                set.add(el.text().trim());
            }
            list.add(StringUtils.join(set, ","));
            ThreadUtils.sleep(1000);
        }
        return list;
    }

    static void makeSamples(List<String> list) {
        List<Sample> objs = new ArrayList<Sample>(6);
        ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
        Sample sample1 = new Sample();
        sample1.setSampleId(dao.getSequenceId(SourceType.SRA));
        sample1.setSource(SourceType.SRA.value());
        sample1.setEtype(ExperimentType.CHIP_SEQ_TF.value());
        sample1.setUrl("ftp://ftp-trace.ncbi.nlm.nih.gov/sra/sra-instant/reads/ByExp/sra/SRX/SRX022/SRX022569");
        sample1.setLab("Chinnaiyan");
        sample1.setCell("LNCaP");
        sample1.setFactor("None");
        sample1.setTimeStamp("2008-12-22");
        Map<String, String> map1 = sample1.descMap();
        map1.put("lab", "Chinnaiyan");
        map1.put("Submitter", "Jindan Yu");
        map1.put("Email", "jindan-yu@northwestern.edu");
        map1.put("Data type", "ChIP-seq");
        map1.put("Cell", "LNCaP");
        map1.put("Instrument", "Illumina GA");
        map1.put("Antibody", "None");
        map1.put("Control", "NA");
        map1.put("Date submitted", "Dec 22, 2008");
        map1.put("Date public", "May 18, 2010");
        map1.put("GEO sample Accession", "GSM353643");
        map1.put("Treatment", "cells were hormone starved for 48 hours prior to treatment with ethanol control.");
        map1.put("run", list.get(0));
        sample1.descMap(map1);
        objs.add(sample1);

        Sample sample2 = new Sample();
        sample2.setSampleId(dao.getSequenceId(SourceType.SRA));
        sample2.setSource(SourceType.SRA.value());
        sample2.setEtype(ExperimentType.CHIP_SEQ_TF.value());
        sample2.setUrl("ftp://ftp-trace.ncbi.nlm.nih.gov/sra/sra-instant/reads/ByExp/sra/SRX/SRX022/SRX022570");
        sample2.setLab("Chinnaiyan");
        sample2.setCell("LNCaP");
        sample2.setFactor(" Millipore no. 06-680 (against AR)");
        sample2.setTimeStamp("2008-12-22");
        Map<String, String> map2 = sample2.descMap();
        map2.put("lab", "Chinnaiyan");
        map2.put("Submitter", "Jindan Yu");
        map2.put("Email", "jindan-yu@northwestern.edu");
        map2.put("Data type", "ChIP-seq");
        map2.put("Cell", "LNCaP");
        map2.put("Instrument", "Illumina GA");
        map2.put("Antibody", "Millipore no. 06-680 (against AR)");
        map2.put("Control", "LNCaP_ethl_AR_jy9");
        map2.put("GEO accession", "GSM353643");
        map2.put("Date submitted", "Dec 22, 2008");
        map2.put("Date public", "May 18, 2010");
        map2.put("GEO sample Accession", "GSM353644");
        map2.put("Treatment",
                "cells were hormone starved for 48 hours prior to treatment with 10nm synthetic androgen R1881");
        map2.put("run", list.get(1));
        sample2.descMap(map2);
        sample2.setInputSampleIds(sample1.getSampleId().toString());
        objs.add(sample2);

        Sample sample3 = new Sample();
        sample3.setSampleId(dao.getSequenceId(SourceType.SRA));
        sample3.setSource(SourceType.SRA.value());
        sample3.setEtype(ExperimentType.CHIP_SEQ_TF.value());
        sample3.setUrl("ftp://ftp-trace.ncbi.nlm.nih.gov/sra/sra-instant/reads/ByExp/sra/SRX/SRX022/SRX022571");
        sample3.setLab("Chinnaiyan");
        sample3.setCell("VCaP");
        sample3.setFactor("None");
        sample3.setTimeStamp("2008-12-22");
        Map<String, String> map3 = sample3.descMap();
        map3.put("lab", "Chinnaiyan");
        map3.put("Submitter", "Jindan Yu");
        map3.put("Email", "jindan-yu@northwestern.edu");
        map3.put("Data type", "ChIP-seq");
        map3.put("Cell", "VCaP");
        map3.put("Instrument", "Illumina GA");
        map3.put("Antibody", "None");
        map3.put("Control", "NA");
        map3.put("Date submitted", "Dec 22, 2008");
        map3.put("Date public", "May 18, 2010");
        map3.put("GEO sample Accession", "GSM353645");
        map3.put("Treatment", "cells were hormone starved for 48 hours prior to treatment with ethanol control");
        map3.put("run", list.get(2));
        sample3.descMap(map3);
        objs.add(sample3);

        Sample sample4 = new Sample();
        sample4.setSampleId(dao.getSequenceId(SourceType.SRA));
        sample4.setSource(SourceType.SRA.value());
        sample4.setEtype(ExperimentType.CHIP_SEQ_TF.value());
        sample4.setUrl("ftp://ftp-trace.ncbi.nlm.nih.gov/sra/sra-instant/reads/ByExp/sra/SRX/SRX022/SRX022572");
        sample4.setLab("Chinnaiyan");
        sample4.setCell("VCaP");
        sample4.setFactor(" Millipore no. 06-680 (against AR)");
        sample4.setTimeStamp("2008-12-22");
        Map<String, String> map4 = sample4.descMap();
        map4.put("lab", "Chinnaiyan");
        map4.put("Submitter", "Jindan Yu");
        map4.put("Email", "jindan-yu@northwestern.edu");
        map4.put("Data type", "ChIP-seq");
        map4.put("Cell", "VCaP");
        map4.put("Instrument", "Illumina GA");
        map4.put("Antibody", "Millipore no. 06-680 (against AR)");
        map4.put("Control", "VCaP_ethl_AR_jy11");
        map4.put("GEO accession", "GSM353645");
        map4.put("Date submitted", "Dec 22, 2008");
        map4.put("Date public", "May 18, 2010");
        map4.put("GEO sample Accession", "GSM353646");
        map4.put("Treatment",
                "cells were hormone starved for 48 hours prior to treatment with 10nm synthetic androgen R1881");
        map4.put("run", list.get(3));
        sample4.descMap(map4);
        sample4.setInputSampleIds(sample3.getSampleId().toString());
        objs.add(sample4);

        Sample sample5 = new Sample();
        sample5.setSampleId(dao.getSequenceId(SourceType.SRA));
        sample5.setSource(SourceType.SRA.value());
        sample5.setEtype(ExperimentType.CHIP_SEQ_TF.value());
        sample5.setUrl("ftp://ftp-trace.ncbi.nlm.nih.gov/sra/sra-instant/reads/ByExp/sra/SRX/SRX022/SRX022573");
        sample5.setLab("Chinnaiyan");
        sample5.setCell("VCaP");
        sample5.setFactor("Santa Cruz, SC354X (against ERG)");
        sample5.setTimeStamp("2008-12-22");
        Map<String, String> map5 = sample5.descMap();
        map5.put("lab", "Chinnaiyan");
        map5.put("Submitter", "Jindan Yu");
        map5.put("Email", "jindan-yu@northwestern.edu");
        map5.put("Data type", "ChIP-seq");
        map5.put("Cell", "VCaP");
        map5.put("Instrument", "Illumina GA");
        map5.put("Antibody", "Santa Cruz, SC354X (against ERG)");
        map5.put("Control", "None");
        map5.put("Date submitted", "Dec 22, 2008");
        map5.put("Date public", "May 18, 2010");
        map5.put("GEO sample Accession", "GSM353647");
        map5.put("Treatment",
                "cells were hormone starved for 48 hours prior to treatment with 10nm synthetic androgen R1881.");
        map5.put("run", list.get(4));
        sample5.descMap(map5);
        objs.add(sample5);

        Sample sample6 = new Sample();
        sample6.setSampleId(dao.getSequenceId(SourceType.SRA));
        sample6.setSource(SourceType.SRA.value());
        sample6.setEtype(ExperimentType.CHIP_SEQ_TF.value());
        sample6.setUrl("ftp://ftp-trace.ncbi.nlm.nih.gov/sra/sra-instant/reads/ByExp/sra/SRX/SRX022/SRX022574");
        sample6.setLab("Chinnaiyan");
        sample6.setCell("LNCaP");
        sample6.setFactor("Santa Cruz, SC354X (against ERG)");
        sample6.setTimeStamp("2008-12-22");
        Map<String, String> map6 = sample6.descMap();
        map6.put("lab", "Chinnaiyan");
        map6.put("Submitter", "Jindan Yu");
        map6.put("Email", "jindan-yu@northwestern.edu");
        map6.put("Data type", "ChIP-seq");
        map6.put("Cell", "LNCaP");
        map6.put("Instrument", "Illumina GA");
        map6.put("Antibody", "Santa Cruz, SC354X (against ERG)");
        map6.put("Control", "None");
        map6.put("Date submitted", "Dec 22, 2008");
        map6.put("Date public", "May 18, 2010");
        map6.put("GEO sample Accession", "GSM353648");
        map6.put("Treatment",
                "cells were hormone starved for 48 hours prior to treatment with 10nm synthetic androgen R1881.");
        map6.put("run", list.get(5));
        sample6.descMap(map6);
        objs.add(sample6);
        dao.create(objs);
    }
}
