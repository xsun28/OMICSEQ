package com.omicseq.robot.parse;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.omicseq.common.Charsets;
import com.omicseq.common.CronTaskName;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SourceType;
import com.omicseq.domain.CronTask;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.MiscUtils;
import com.omicseq.utils.ThreadUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class NCBIStatistic extends BaseParser {
    private static final String format = "yyyy/MM/dd";
//    private String term = "(((chip-seq) AND \"Homo sapiens\"[orgn:__txid9606])) AND (\"%s\"[Publication Date] : \"3000\"[Publication Date]) AND \"gds_sra\"[filter]";
    private String term = "(((\"%s\"[PDAT] : \"3000\"[PDAT]) AND \"Homo sapiens\"[Organism]) AND \"strategy chip seq\"[Properties]) AND \"sra gds\"[Filter]";
    private String term_RNASeq = "(((\"%s\"[PDAT] : \"3000\"[PDAT]) AND \"Homo sapiens\"[Organism]) AND \"strategy rna seq\"[Properties]) AND \"sra gds\"[Filter]";
    private String term_RIPSeq = "(((\"%s\"[PDAT] : \"3000\"[PDAT]) AND \"Homo sapiens\"[Organism]) AND \"strategy rip seq\"[Properties]) AND \"sra gds\"[Filter]";

    @Override
    SourceType getSourceType() {
        return SourceType.SRA;
    }

    @Override
    public void parser(String url) {
        try {
//            CronTask task = cronTaskDAO.get(CronTaskName.ROBOT_SRA_PARSER.task(), MiscUtils.getServerIP());
//            if (null == task) {
//                logger.error("not config task config!");
//                return;
//            }
//            String date = task.getRuntime();
            String date = "2014/01/01";
            String sterm = String.format(term, date);
            url = "http://www.ncbi.nlm.nih.gov/sra/?term=" + URLEncoder.encode(sterm, Charsets.UTF_8.name());
//            http://www.ncbi.nlm.nih.gov/sra/?term=%28%28%28%222014%2F01%2F01%22%5BPDAT%5D+%3A+%223000%22%5BPDAT%5D%29+AND+%22Homo+sapiens%22%5BOrganism%5D%29+AND+%22strategy+chip+seq%22%5BProperties%5D%29+AND+%22sra+gds%22%5BFilter%5D%26cmd%3DDetailsSearch
//            url = "http://www.ncbi.nlm.nih.gov/sra/?term=%28%28%28%222014/01/01%22%5BPDAT%5D%20%3A%20%223000%22%5BPDAT%5D%29%20AND%20%22Homo%20sapiens%22%5BOrganism%5D%29%20AND%20%22strategy%20chip%20seq%22%5BProperties%5D%29%20AND%20%22sra%20gds%22%5BFilter%5D&cmd=DetailsSearch";
            if (logger.isDebugEnabled()) {
                logger.debug("获取:{}", url);
            }
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            Integer total = Integer.valueOf(doc.getElementById("resultcount").val());
            if (0 == total) {
                logger.info("chip seq {} not items found", date);
            }
            System.out.println("SRA chip seq total: " + total);
            
            String sterm_rna = String.format(term_RNASeq, date);
            url = "http://www.ncbi.nlm.nih.gov/sra/?term=" + URLEncoder.encode(sterm_rna, Charsets.UTF_8.name());
            if (logger.isDebugEnabled()) {
                logger.debug("获取:{}", url);
            }
            doc = Jsoup.connect(url).timeout(timeout).get();
            total = Integer.valueOf(doc.getElementById("resultcount").val());
            if (0 == total) {
                logger.info("rna seq {} not items found", date);
            }
            System.out.println("SRA rna seq total: " + total);
            
            
            String sterm_rip = String.format(term_RIPSeq, date);
            url = "http://www.ncbi.nlm.nih.gov/sra/?term=" + URLEncoder.encode(sterm_rip, Charsets.UTF_8.name());
            if (logger.isDebugEnabled()) {
                logger.debug("获取:{}", url);
            }
            doc = Jsoup.connect(url).timeout(timeout).get();
            total = Integer.valueOf(doc.getElementById("resultcount").val());
            if (0 == total) {
                logger.info("rip seq {} not items found", date);
            }
            System.out.println("SRA rip seq total: " + total);
        } catch (Exception e) {
            logger.error("ncbi_sra parser failed!", e);
        } finally {
            stop();
        }
    }

    @Override
    public void process(String url) {
        Integer sampleId = null;
        try {
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            Sample sample = new Sample();
            sample.setSource(getSourceType().value());
            sample.setEtype(ExperimentType.CHIP_SEQ_TF.value());
            Map<String, String> map = new HashMap<String, String>(5);
            Element rv = doc.getElementById("ResultView");
            if (null == rv) {
                logger.warn("{} not found result!", url);
                throw new OmicSeqException("not found result!");
            }
            Elements els = rv.select("table>tbody>tr");
            if (els.isEmpty()) {
                logger.warn("{} not found run file!", url);
                throw new OmicSeqException(" not found run file!");
            }
            Set<String> runs = new HashSet<String>(els.size());
            for (Iterator<Element> it = els.iterator(); it.hasNext();) {
                Element el = it.next().select("a").first();
                runs.add(el.text().trim());
            }
            String run = StringUtils.join(runs, ",");
            map.put("run", run);
            els = doc.select(".sra-full-data");
            for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
                Element el = iterator.next();
                if (!el.hasClass("e-hidden")) {
                    String key = el.childNode(0).toString().trim().replaceAll("\\:", "");
                    String val = el.children().first().text();
                    if (StringUtils.isBlank(val)) {
                        continue;
                    }
                    map.put(key, val);
                    if (StringUtils.equalsIgnoreCase("Accession", key)) {
                        map.put("geoSampleAccession", "ERR" + val.substring(3));
                    }
                } else {
                    Elements _els = el.select(".expand-body>div");
//                    Elements accUrlE = els.select(".tag").select("a");
//                    if(accUrlE != null)
//                    {
//                    	String accUrl = accUrlE.iterator().next().attr("href");
//                    	System.out.println(accUrl);
//                    }
                    
                    for (Iterator<Element> it = _els.iterator(); it.hasNext();) {
                        Element _el = it.next();
                        Iterator<Element> _it = _el.select(".tag").iterator();
                        if (!_it.hasNext()) {
                            String key = _el.childNode(0).toString().trim().replaceAll("\\:", "");
                            String val = _el.children().first().text();
                            if (null == val || val.length() > 120) {
                                continue;
                            }
                            map.put(key, val);
                        } else {
                            for (; _it.hasNext();) {
                                _el = _it.next();
                                String key = _el.text().trim().replaceAll("\\:", "");
                                Node node = _el.nextSibling();
                                String val = null == node ? null : node.toString();
//                                if (null == val || val.length() > 120) {
//                                    continue;
//                                }
                                if("Project Contact".equals(key))
                                {
                                	key = "lab";
                                	String[] values = val.split(";");
                                	for(int i=0; i<values.length;i++)
                                	{
                                		if(values[i].contains("Institute"))
                                		{
                                			val = values[i].split("=")[1].trim();
                                			break;
                                		}
                                	}
                                }
                                if("antibody catalog number".equals(key))
                                {
                                	key = "vendorID";
                                }
                                if("antibody".equals(key))
                                {
                                	if(val != null && !"none (input)".equals(val))
                                	{
                                		val = val.replace("anti-", "");
                                		val = val.replaceAll("\\(.*?\\)", "").trim();
                                		
                                		if(val.indexOf("Millipore") != -1){
                                			//Anti-trimethyl-histoneH3(lys4) Polyclonal Antibody Cat. #07-473 Millipore
                                			String[] antibodyArray = val.split("#");
                                    		if(antibodyArray.length > 1)
                                    		{
                                    			val = "Millipore " + antibodyArray[antibodyArray.length-1].replace(" Millipore", "");
                                    		}
                                    		map.put("vendorID", val);
                                		}
                                		if(val.indexOf("sc-") !=  -1)
                                		{
                                			//rabbit IgG : Santa Cruz Biotechnology, Inc. : sc-2027
                                			val = val.substring(val.indexOf("sc-"));
                                			map.put("vendorID", val);
                                		}
                                	}
                                }
                                if((key.indexOf("chip antibody") != -1 || key.indexOf("chip-antibody")!= -1) && !"chip antibody vendor".equals(key))
                                {
                                	key = "antibody";
                                	val = val.replace("antibody", "");
                                	val = val.replace("anti-", "");
                            		val = val.replaceAll("\\(.*?\\)", "").trim();
                                }
                                map.put(key, val);
                            }
                        }
                    }
                }
            }
            sample.descMap(map);
            String antibody = map.get("antibody");
            if(antibody!= null && !antibody.isEmpty())
            {
            	if(antibody.equals("none (input)"))
            	{
            		sample.setSettype("input");
                	sample.setFactor("Nnoe");
            	} else {
            		sample.setSettype("ChIP");
            		
                	sample.setFactor(antibody);
            	}
            } else {
            	sample.setSettype("input");
            	sample.setFactor("Nnoe");
            }
            if(map.get("vendorID") != null)
        	{
        		if(map.get("vendorID").indexOf("Millipore 07-473") != -1)
        		{
        			sample.setFactor("H3K4me3");
        		}
        	}
            if(map.get("factor") != null)
            {
            	sample.setFactor(map.get("factor"));
            }
            
            sample.setCell(map.get("cell line"));
            if(sample.getCell() == null || "".equals(sample.getCell()))
            {
            	sample.setCell(map.get("source_name"));
            }
            sample.setLab(map.get("lab"));
            
            String runUrl = "http://trace.ncbi.nlm.nih.gov/Traces/sra/?run="+ map.get("run");
            Document runDoc = Jsoup.connect(runUrl).timeout(timeout).get();
            sample = putPropertiesToSample(runDoc, sample);
            
            // 无数据创建数据
            Sample updateSample = samplePrevDAO.getByUrl(url);
            if (null == updateSample) {
                sample.setUrl(url);
                sampleId = getSampleId();
                sample.setSampleId(sampleId);
                samplePrevDAO.create(sample);
                // 创建需要下载的文件数据
                FileInfo info = new FileInfo();
                info.setSampleId(sampleId);
                info.setServerIp(MiscUtils.getServerIP());
                info.setSource(getSourceType().value());
                info.setUrl(url);
                info.setState(FileInfoStatus.CREATED.value());
                fileInfoDAO.create(info);
            } else {
            	updateSample.setLab(sample.getLab());
            	updateSample.setDescription(sample.getDescription());
            	samplePrevDAO.update(updateSample);
            }
//            // 删除有处理失败的数据
//            sampleFailedDAO.removeByUrl(url);
            
        } catch (Exception e) {
            failed(url, sampleId, e);
        }
    }

    private Sample putPropertiesToSample(Document doc, Sample sample) {
        Elements els = doc.select(".run-metatable");
        if(els != null)
        {
        	for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
                Element el = iterator.next();
                Pattern p = Pattern.compile("(\\d{1,4}[-|\\/]\\d{1,2}[-|\\/]\\d{1,2})", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE); 
                Matcher matcher = p.matcher(el.text());
                if(matcher.find() && matcher.groupCount() >= 1)
                {
                	sample.setTimeStamp(matcher.group(0));
                }
            }
        }
		return sample;
	}

	public static void main(String[] args) {
//        CronTask task = cronTaskDAO.get(CronTaskName.ROBOT_SRA_PARSER.task(), MiscUtils.getServerIP());
//        if (null == task) {
//            task = new CronTask();
//            task.setName(CronTaskName.ROBOT_SRA_PARSER.task());
//            task.setLaunchServer(MiscUtils.getServerIP());
//            task.setRuntime(DateUtils.format(new Date(), format));
//            cronTaskDAO.create(task);
//        }
//		NCBIStatistic s = new NCBIStatistic();
//		s.parser("http://www.ncbi.nlm.nih.gov/sra");
		new NCBIStatistic().start();
    }

}
