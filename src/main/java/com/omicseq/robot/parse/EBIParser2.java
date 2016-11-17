package com.omicseq.robot.parse;
//http://www.ebi.ac.uk/arrayexpress/experiments/search.html?query=human+chip-seq&organism=&array=&exptype[]=&exptype[]=

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
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
import com.omicseq.robot.SampleDetail;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.MiscUtils;
import com.omicseq.utils.ThreadUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class EBIParser2 extends BaseParser {
//	boolean stoppoint = false;
	private List<String> urlList = new ArrayList<String>();
    private static final String format = "yyyy/MM/dd";
    private String term = "(\"humans\"[MeSH Terms] OR \"Homo sapiens\"[Organism] OR human[All Fields]) AND rna-seq[All Fields]";
  
    String fromType = "Homo+sapiens";
//  String fromType = "Mus+musculus" ;
    
    @Override
    SourceType getSourceType() {
        return SourceType.ArrayExpress;
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
            
            url = url + "?query=chip-seq&organism="+fromType+"&array=&exptype[]=&exptype[]=";
            if (logger.isDebugEnabled()) {
                logger.debug("获取:{}", url);
            }
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            Integer total = Integer.valueOf(doc.getElementsByClass("ae-stats").get(0).children().get(1).ownText());
            if (0 == total) {
                logger.info("{} not items found", date);
                return;
            }
//            Elements els = doc.select("meta[name=ncbi_pagesize]");
            Integer pageSize = Integer.valueOf(doc.getElementsByClass("ae-page-size").get(0).getElementsByTag("span").get(0).ownText());
//            Element el = doc.getElementById("pageno");
            Integer pages = (int)Math.ceil((double)total / pageSize);
            if (logger.isDebugEnabled()) {
                logger.debug("total {} pagesize {} pages {} ", total, pageSize, pages);
            }
//            process(doc.getElementsByClass("rprt"), 0);
            if(CollectionUtils.isEmpty(urlList)){
            	SmartDBObject query = new SmartDBObject();
            	query.put("source", SourceType.ArrayExpress.getValue());
            	Integer[] etypes = {1,17,18};
            	query.put("etype",new SmartDBObject("$in", etypes));
            	if(fromType.equals("Mus+musculus")){
            		query.put("fromType", "mouse");
            	}
            	List<Sample> samples = samplePrevDAO.find(query);
            	for(Sample s : samples){
            		urlList.add(s.getUrl());
            	}
            }
            this.p(doc.getElementsByClass("col_accession"));
            
            if (pages > 1) {
                String sessionId = doc.select("meta[name=ncbi_sessionid]").attr("content");
                parser(getSourceType().url(), date, sessionId, total, pageSize, pages, 2);
            }
//            updateTaskRuntimeValue(task);
        } catch (Exception e) {
            logger.error("ncbi_sra parser failed!", e);
        } finally {
            stop();
        }
    }
    
    private void p(Elements rslts) throws Exception {
    	 for (int i = 0; i < rslts.size(); i++) {
         	Element e1 = rslts.get(i);
         	Elements e2 = e1.getElementsByTag("a");
         	if(StringUtils.isEmpty(e2.text())) continue;
         	String url = "http://www.ebi.ac.uk" + e2.attr("href");
//         	String url = "http://www.ebi.ac.uk/arrayexpress/experiments/E-GEOD-26284/?query=human+RNA-seq&organism=&array=&exptype[]=&exptype[]http://www.ebi.ac.uk/arrayexpress/experiments/E-GEOD-26284/?query=human+RNA-seq&organism=&array=&exptype[]=&exptype[]=&page=4&pagesize=500";
     		Document doc = Jsoup.connect(url).timeout(timeout).get();
     		System.out.println("current Url :"+url);
 			Elements eles = doc.getElementById("ae-detail").child(0).child(0).children();
 			String url_sample = null;
     		for (Element e : eles){
     			if(e.children().size() == 0 ) {
     				continue;
     			} 
     			else if(e.child(0).text().startsWith("Samples")){
     				Elements links = e.getElementsByTag("a");
     				String linkHref = links.attr("href");
     				url_sample = "http://www.ebi.ac.uk" + linkHref+"&s_page=1&s_pagesize=500";
     			}
     		}
     		//pubMed Id
 			Elements doc_pubmed = doc.getElementsByTag("a").select(":contains(Europe PMC)");
 			String pubmedUrl = null ;
     		if(doc_pubmed.size() == 1){
     			pubmedUrl = doc_pubmed.first().attr("href");
     		}
     		else if(doc_pubmed.size() > 1){
     			pubmedUrl = doc_pubmed.get(1).attr("href");
     		}
     		Document doc1 = Jsoup.connect(url_sample).timeout(timeout).get();
 			Elements ele_size = doc1.getElementsByClass("ae-stats").select("span");
 			Elements tables = doc1.getElementsByClass("ae_samples_table");
 			for(Element e_table :tables){
 				String count = ele_size.get(tables.indexOf(e_table)).text();
 				Integer sampleCount = Integer.parseInt(count);
 				List<Sample> sampleList = new ArrayList<Sample>(sampleCount);
 				for(int k = 0; k<sampleCount; k++){
 					Sample sample = new Sample();
 					if(StringUtils.isNotEmpty(pubmedUrl)){
 						sample.setPubmedUrl(pubmedUrl);
 					}
 					if(fromType.equals("Mus+musculus")){
						sample.setFromType("mouse");
					}
 					sample.setSourceUrl(url);
 		            sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
 		            sample.setDeleted(0);
 		        	sample.setSource(SourceType.ArrayExpress.getValue());
 		        	sample.setEtype(ExperimentType.CHIP_SEQ_TF.getValue());
 		        	sampleList.add(sample);
 				}
 				Elements ees_srcName = e_table.select(".left_fixed").select("tbody").select("td");
 				for(Element eesChild : ees_srcName){
 					String srcName = eesChild.text();
 					int index = ees_srcName.indexOf(eesChild);
 					sampleList.get(index).setSampleCode(srcName.split(" ")[0]);
 				}
 				List<String > keys = new ArrayList<String>(); 
 				Elements ess_middle_title = e_table.select(".middle_scrollable").select("thead").first().select("[class^=odd sortable]");
 				for(Element eesChild : ess_middle_title){
 					keys.add(eesChild.text());
 				}
 				Elements ees_middle_val = e_table.select(".middle_scrollable").select("tbody").first().select("tr");
 				for(Element ees : ees_middle_val){
 					int index = ees_middle_val.indexOf(ees);
 					Map<String, String> map = new HashMap<String, String>();
 					for(int j=0;j<ees.children().size();j++){
 						String val = ees.child(j).text();
 						if(StringUtils.isNotEmpty(val)){
 							if(keys.get(j).trim().equalsIgnoreCase("cell line")){
 								sampleList.get(index).setCell(val);
 							}
 							else{
 								map.put(keys.get(j), val);
 							}
 						}
 					}
 					if(sampleList.get(index).getSampleCode().startsWith("GSM") || sampleList.get(index).getSampleCode().startsWith("GSE")||sampleList.get(index).getSampleCode().startsWith("GDS"))
 					{
 						map.put("geosampleaccession", sampleList.get(index).getSampleCode());
 					}
 					sampleList.get(index).descMap(map);
 				}

 				Elements ees_right_title = e_table.getElementsByClass("right_fixed").select("thead").first().child(1).select("th");
 				Integer processed_index = null;
 				for(Element ees : ees_right_title){
 					if(ees.text().toLowerCase().contains("process")) processed_index = ees_right_title.indexOf(ees);
 				}
 				
 				
 				Elements ees_right = e_table.getElementsByClass("right_fixed").select("tbody").first().select("tr");
 				for(Element ees : ees_right){
 					if(processed_index == null) continue;
 					String downloadUrl = ees.child(processed_index).select("a").attr("href");
 					if(downloadUrl.startsWith("/")){
 						downloadUrl = "http://www.ebi.ac.uk"+downloadUrl;
 					}
 					int index = ees_right.indexOf(ees);
 					sampleList.get(index).setUrl(downloadUrl);
 				}
 				List<Sample > createSampleList = new ArrayList<Sample>();
 				List<FileInfo > createInfoList = new ArrayList<FileInfo>();
 				for(Sample sample : sampleList){
 					if(StringUtils.isNotEmpty(sample.getUrl()) && !urlList.contains(sample.getUrl())){
 						urlList.add(sample.getUrl());
     					//System.out.println(sample.getSampleCode()+"--"+sample.getCell()+"--"+"--"+sample.getUrl()); //+sample.getDescription()
     					sample = SampleDetail.getDetailBySourceAndCell(sample);
     					Integer sampleId = sampleDAO.getSequenceId(SourceType.ArrayExpress);
//     					int sampleId = 1 ;
     					sample.setSampleId(sampleId);
     					FileInfo info = new FileInfo();
 			            info.setServerIp(MiscUtils.getServerIP());
 			            info.setSource(getSourceType().value());
 			            info.setUrl(sample.getUrl());
 			            info.setState(1);
 			            info.setSampleId(sampleId);
 			            createInfoList.add(info);
 			            createSampleList.add(sample);
 					}
 				}
 				if(CollectionUtils.isNotEmpty(createSampleList)){
 					samplePrevDAO.create(createSampleList);
 					fileInfoDAO.create(createInfoList);
 				}
 			}
     	}
    }

    // update task runtime value
    private void updateTaskRuntimeValue(CronTask task) {
        Date date = DateUtils.parseToDate(task.getRuntime(), format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        task.setRuntime(DateUtils.format(cal.getTime(), format));
        cronTaskDAO.update(task);
    }

    private void parser(String url, String date, String sessionId, Integer total, Integer pageSize, Integer pages,
            int pageIndex) {
        if (isStoped()) {
            return;
        }
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
            if (pageIndex > pages) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{},pageSize{},pageIndex{}", url, pageSize, pageIndex);
            }
            Map<String, String> data = makeData(date, total, pageSize, pages, pageIndex);
            if (logger.isDebugEnabled()) {
                logger.debug("data : {}", data);
            }
//            Document doc = Jsoup.connect(url)
//                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0")
//                    .cookie("ncbi_sid", sessionId).data(data).post();
//            Elements els = doc.getElementsByClass("rprt");
//            process(els, pageSize * (pageIndex - 1));
//http://www.ebi.ac.uk/arrayexpress/experiments/search.html?query=human+RNA-seq&organism=&array=&exptype[]=&exptype[]=&page=2&pagesize=25            
            url = "http://www.ebi.ac.uk/arrayexpress/experiments/search.html?query=chip-seq&organism="+fromType+"&array=&exptype[]=&exptype[]=&page="
            		+ pageIndex + "&pagesize=" + pageSize;
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            this.p(doc.getElementsByClass("col_accession"));
        } catch (Exception e) {
            logger.error("ncbi_sra parser failed!", e);
        } finally {
            ThreadUtils.sleep(2 * 1000);
        }
        parser(url, date, sessionId, total, pageSize, pages, ++pageIndex);
    }

    /**
     * @param pages
     * @param pageSize
     * @param pageIndex
     */
    private Map<String, String> makeData(String date, int total, int pageSize, int pages, int pageIndex) {
        String sterm = String.format(term, date);
        Map<String, String> map = new HashMap<String, String>();
        map.put("EntrezSystem2.PEntrez.DbConnector.Cmd", "PageChanged");
        map.put("EntrezSystem2.PEntrez.DbConnector.Db", "gds");
        map.put("EntrezSystem2.PEntrez.DbConnector.IdsFromResult", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.LastDb", "gds");
        map.put("EntrezSystem2.PEntrez.DbConnector.LastIdsFromResult", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.LastQueryKey", "1");
        map.put("EntrezSystem2.PEntrez.DbConnector.LastTabCmd", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.LinkName", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.LinkReadableName", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.LinkSrcDb", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.QueryKey", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.TabCmd", "");
        map.put("EntrezSystem2.PEntrez.DbConnector.Term", sterm);
        map.put("EntrezSystem2.PEntrez.Gds.Gds_Facets.BMFacets", "");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_Facets.FacetSubmitted", "false");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_Facets.FacetsUrlFrag", "filters=");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_PageController.PreviousPageName", "results");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.HistoryDisplay.Cmd", "PageChanged");

        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.FFormat", "docsumcsv");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.FFormat2", "docsumcsv");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.FileFormat", "docsum");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.Format", "");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.LastFormat", "");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.LastPageSize", String.valueOf(pageSize));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.LastPresentation", "docsum");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.PageSize", String.valueOf(pageSize));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.Presentation", "docsum");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.WebEnv",
                "NCID_1_800652027_130.14.22.76_5555_1397278722_1004750157_0MetA0_S_HStore");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.sPageSize", String.valueOf(pageSize));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.sPageSize2", String.valueOf(pageSize));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.sPresentation", "DocSum");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_DisplayBar.sPresentation2", "DocSum");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Entrez_Pager.CurrPage", String.valueOf(pageIndex));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Entrez_Pager.cPage", String.valueOf(pageIndex - 1));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_MultiItemSupl.RelatedDataLinks.DbName", "gds");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_MultiItemSupl.RelatedDataLinks.rdDatabase", "rddbto");
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_ResultsController.ResultCount", String.valueOf(total));
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Gds_ResultsController.RunLastQuery", "");
        map.put("p$a", "EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Entrez_Pager.Page");
        map.put("p$l", "EntrezSystem2");
        map.put("p$st", "gds");
        map.put("term", sterm);

        // SearchDetailsTerm
        String tpl = "(\"humans\"[MeSH Terms] OR \"Homo sapiens\"[Organism] OR human[All Fields]) AND rna-seq[All Fields]";
        String dsterm = String.format(tpl, date);
        map.put("EntrezSystem2.PEntrez.Gds.Gds_ResultsPanel.Discovery_SearchDetails.SearchDetailsTerm", dsterm);
        /*
         * if (logger.isDebugEnabled()) { StringBuilder sb = new
         * StringBuilder("\n"); List<String> keys = new
         * ArrayList<String>(map.keySet()); Collections.sort(keys); for (String
         * key : keys) {
         * sb.append(key).append("=").append(map.get(key)).append("\r\n"); }
         * logger.debug(sb.toString()); }
         */
        return map;
    }

    private void process(Elements els, int postion) {
        int idx = postion;
        for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();) {
            Element el = iterator.next();
            String href = el.select("a").first().attr("href");
            String url = "http://www.ncbi.nlm.nih.gov" + href;
            if (logger.isDebugEnabled()) {
                logger.debug("process{}:url {}", ++idx, url);
            }
            process(url);
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
        new EBIParser2().start();
    }

}
