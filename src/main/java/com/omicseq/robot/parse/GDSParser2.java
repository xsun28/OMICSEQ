package com.omicseq.robot.parse;
//http://www.ncbi.nlm.nih.gov/gds/?term=human+chip-seq

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




import com.omicseq.common.ExperimentType;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SourceType;
import com.omicseq.domain.CronTask;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.MiscUtils;
import com.omicseq.utils.ThreadUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class GDSParser2 extends BaseParser {
//	boolean stoppoint = false;
	private List<Sample> sampleList = new ArrayList<Sample>();

    private static final String format = "yyyy/MM/dd";
//    private String term = "(((chip-seq) AND \"Homo sapiens\"[orgn:__txid9606])) AND (\"%s\"[Publication Date] : \"3000\"[Publication Date]) AND \"gds_sra\"[filter]";
//    private String term = "(((\"%s\"[PDAT] : \"3000\"[PDAT]) AND \"Homo sapiens\"[Organism]) AND \"strategy chip seq\"[Properties]) AND \"sra gds\"[Filter]&cmd=DetailsSearch";
    private String term = "(\"humans\"[MeSH Terms] OR \"Homo sapiens\"[Organism] OR human[All Fields]) AND chip-seq[All Fields]";
    @Override
    SourceType getSourceType() {
        return SourceType.GEO;
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
//            String sterm = String.format(term, date);
//            url += "/?term=" + URLEncoder.encode(sterm, Charsets.UTF_8.name());
//            url = url + "/?term=%28%28%28%222014/01/01%22%5BPDAT%5D%20%3A%20%223000%22%5BPDAT%5D%29%20AND%20%22Homo%20sapiens%22%5BOrganism%5D%29%20AND%20%22strategy%20chip%20seq%22%5BProperties%5D%29%20AND%20%22sra%20gds%22%5BFilter%5D&cmd=DetailsSearch";
            url = url + "/?term=human+chip-seq";
            
            if (logger.isDebugEnabled()) {
                logger.debug("获取:{}", url);
            }
            Document doc = Jsoup.connect(url).timeout(10000).get();
            Integer total = Integer.valueOf(doc.getElementById("resultcount").val());
            if (0 == total) {
                logger.info("{} not items found", date);
                return;
            }
            Elements els = doc.select("meta[name=ncbi_pagesize]");
            Integer pageSize = Integer.valueOf(null == els || els.isEmpty() ? "20" : els.first().attr("content"));
            Element el = doc.getElementById("pageno");
            Integer pages = Integer.valueOf(null == el ? "1" : doc.getElementById("pageno").attr("last"));
            if (logger.isDebugEnabled()) {
                logger.debug("total {} pagesize {} pages {} ", total, pageSize, pages);
            }
//            process(doc.getElementsByClass("rprt"), 0);
            if(CollectionUtils.isEmpty(sampleList)){
            	SmartDBObject query = new SmartDBObject();
            	query.put("source", SourceType.GEO.getValue());
            	query.put("etype", ExperimentType.CHIP_SEQ_TF.getValue());
            	sampleList = samplePrevDAO.find(query);
            }
            if(total>sampleList.size()){
            	this.p(doc.getElementsByClass("title"));
            }
            if(total == sampleList.size()){
            	logger.debug("All dataset has already been downloaded completed");
            }else{
            	if (pages > 1) {
            		String sessionId = doc.select("meta[name=ncbi_sessionid]").attr("content");
            		parser(getSourceType().url(), date, sessionId, total, pageSize, pages, 2);
            	}
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
    		
    		String key = rslts.get(i).child(0).attr("href").split("=")[1];
    		if(key.contains("GDS")) continue; 
    		boolean isExist = false ;
    		for(Sample s : sampleList){
    			if(s.getUrl().contains(key)){
    				isExist = true;
    				break;
    			}
    		}
    		if(isExist) continue;
    		Sample sample = new Sample();
        	
        	FileInfo info = new FileInfo();
            info.setServerIp(MiscUtils.getServerIP());
            info.setSource(getSourceType().value());
        	sample.setSource(SourceType.GEO.getValue());
        	sample.setEtype(ExperimentType.CHIP_SEQ_TF.getValue());
        	Map<String, String> map = new HashMap<String, String>();
    		String url = "http://www.ncbi.nlm.nih.gov" + rslts.get(i).child(0).attr("href");
    		sample.setUrl(url);
    		Document doc = Jsoup.connect(url).timeout(timeout).get();
    		System.out.println("current Url :"+url);
    		
    		Elements els = doc.getElementsByClass("pubmed_id");
			String href = null;
			if(els != null) {
				for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();)
				{
					Element el = iterator.next();	
					href = el.select("a").first().attr("href");
					break;
				}
				sample.setPubmedUrl(href);
			}
    		
    		
    		Elements eles = doc.getElementById("ViewOptions").nextElementSibling().child(0).child(0)
    				.child(0).children();	
    		Element e0 = eles.get(0);
    		Elements eles0 = e0.getElementsByAttribute("valign");
    		boolean hasCell = true;
    		for (Element e : eles0){
    			if(e.children().size() == 0) {
    				continue;
    			}
//    			System.out.print(e.child(0).text());
    			if(e.childNodeSize() > 2) {
    				//if(e.child(0).text().)
	    			if(!(e.child(0).text().trim()).equalsIgnoreCase("summary") && !(e.child(0).text().trim()).equalsIgnoreCase("Overall design") && StringUtils.isNotBlank(e.child(1).text())
	    					&& !(e.child(0).text().trim()).equalsIgnoreCase("Street address")){
	   					if(e.child(0).text().startsWith("Samples")){
	   						map.put("Samples", e.child(1).text());
	   					}else if(e.child(1).html().toString().contains("<br />") || e.child(1).html().toString().contains("<br>")){
	   						String [] ths = e.child(1).html().toString().replace("<br>","<br />").split("<br />");
    						for(String k : ths){
    							if(hasCell){
    								if( k.contains("cell line:")|| k.contains("cell type:") ){
    									String cell = k.split(":")[1].trim();
    									sample.setCell(cell);
    									hasCell = false;
    								}
    							}
    			    			if(k.contains(":") && k.split(":").length>1){
    			    				map.put(k.split(":")[0].trim(), k.split(":")[1].trim());
    			    			}else{
    			    				map.put(e.child(0).text().trim(), e.child(1).text().trim());
    			    			}
   		    				}
	   					}else{
	   						map.put(e.child(0).text(), e.child(1).text());
	    				}
	    			}
	    			if(e.child(0).text().equalsIgnoreCase("lab")){
	    				sample.setLab(e.child(1).text());
	    				}	    				if(e.child(0).text().equalsIgnoreCase("Last update date")){
    					sample.setTimeStamp(e.child(1).text());
	   				}
    				if(e.child(0).text().equalsIgnoreCase("Source name")){
	    				String cell = e.child(1).text().trim();
	   					sample.setCell(cell);
	    			}
	   				if(sample.getCell() == null && e.child(0).text().startsWith("Samples")){
	   					String cellurl = e.child(1).child(0).child(0).child(0).child(0).child(0).attr("href");
	   					Document doc1 = Jsoup.connect("http://www.ncbi.nlm.nih.gov"+cellurl).timeout(timeout).get();
	    				Elements eles1 = doc1.getElementById("ViewOptions").nextElementSibling().child(0).child(0)
	    	    				.child(0).children();
	    				Element e01 = eles1.get(0);
	    		   		Elements eles01 = e01.getElementsByAttribute("valign");
	    		    	for(Element el : eles01){
	    		    		if(el.children().size() == 0) {
	    		    				continue;
    		    			}
	    		    		if(el.childNodeSize() > 1) {
	    		    			String [] gc = el.child(1).html().toString().replaceAll("<br>","<br />").split("<br />");
	    		    			for(String forc : gc){
		   		    				if(forc.contains("cell line:") || forc.contains("cell type:")){
			   		    				String cell =  forc.split(":")[1].trim();
			   		    				sample.setCell(cell);
			   		    				break;
		    	    				}else if((el.child(0).text().trim()).equalsIgnoreCase("Source name")){
		    	    					String cell = el.child(1).text().replace("cells", "").trim();
		    		   					sample.setCell(cell);
		    		   				}
	    		    			}
	    		   			}
	    		   		}
	    			}
    			} 
    		}
//    		Element e3 = eles.get(3);
 /*   		Element e5;
    		if (url.indexOf("GSE")>0) {
    			e5 = eles.get(5);
    			
    		} else if (url.indexOf("GSM")>0) {
    			e5 = eles.get(2);
    		} else {
    			continue;
    		}
    		Elements e5s = e5.children().get(0).children();
    		*/
    		int sampleId = sampleDAO.getSequenceId(SourceType.GEO);
        	sample.setSampleId(sampleId);
        	info.setSampleId(sampleId);
    		sample.setDeleted(0);
    		sample.descMap(map);
    		sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
    		info.setUrl("");
			info.setState(10);

    		fileInfoDAO.create(info);
        	samplePrevDAO.create(sample);
    	}
    }

    // update task runtime value
    @SuppressWarnings("unused")
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
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        if (isStoped()) {
            return;
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
            Document doc = Jsoup.connect(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0")
                    .cookie("ncbi_sid", sessionId).data(data).post();
//            Elements els = doc.getElementsByClass("rprt");
//            process(els, pageSize * (pageIndex - 1));
            this.p(doc.getElementsByClass("title"));
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

    @SuppressWarnings("unused")
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
            Document doc = Jsoup.connect(url).timeout(10000).get();
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
            Document runDoc = Jsoup.connect(runUrl).timeout(10000).get();
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
        new GDSParser2().start();
    }

}
