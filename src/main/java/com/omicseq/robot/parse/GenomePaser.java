package com.omicseq.robot.parse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.ThreadUtils;

public class GenomePaser extends BaseParser {
	private List<String> doiList = new ArrayList<String>();

	@Override
	public void parser(String url) {
		try {			SmartDBObject query = new SmartDBObject();
			query.put("source", SourceType.SUPPLEMENTTARY.getValue());
			query.put("etype", ExperimentType.SUPPLEMENTTARY.getValue());
			List<Sample> samples = samplePrevDAO.find(query);
			for(Sample sample : samples){
				doiList.add(sample.getSampleCode());
			}
			//http://genome.cshlp.org/search?submit=yes&y=14&FIRSTINDEX=0&fulltext=mutation&x=8&format=standard&hits=80&sortspec=relevance&submit=Go
			url = "http://genome.cshlp.org/content/by/year";
			
			if (logger.isDebugEnabled()) {
	            logger.debug("获取:{}", url);
	        }
			String year = "2015";
			
			url = url + "/" + year;
			Document doc = null;
			
			try {
				Boolean connected = false;
				System.out.print("connecting " + url);
				while(!connected){
					doc = Jsoup.connect(url).timeout(timeout).get();
					if(doc != null){
						connected = true;
						System.out.print(" Success");
						System.out.println();
					}
				}
			} catch (Exception e) {
			}
			
			Element monthContent = doc.getElementById("proxied-contents");
			
			Elements month_item = monthContent.getElementsByClass("proxy-archive-by-year").first().getElementsByClass("proxy-archive-by-year-month");
			
			this.parse_month(month_item);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void parse_month(Elements els) {
		for(Element month : els){
			try{
				String href = month.getElementsByTag("a").first().attr("href");
				String date = month.getElementsByTag("a").first().text();
				Integer pageIndex = Integer.parseInt(month.text().substring(month.text().indexOf("(") + 1, month.text().indexOf(")")));
				Document doc = null;
				try {
					Boolean flag = false;
					System.out.print("connecting http://genome.cshlp.org"+href);
					while(!flag){
						doc = Jsoup.connect("http://genome.cshlp.org" + href).timeout(timeout).get();
						if(doc!=null){
							flag = true;
							System.out.print("	Success");
							System.out.println();
						}
					}
				} catch (Exception e) {
				}
				String yc = month.text().substring(month.text().indexOf(";") + 1, month.text().indexOf("(")).trim();
				if (logger.isDebugEnabled()) {
	                logger.debug("parse {} ,pagePre {}", date, pageIndex);
	            }
				if(pageIndex != 1){
					try {
						Boolean flag1 = false;
						System.out.print("connecting http://genome.cshlp.org/content/" + yc + "/" + pageIndex + ".toc");
						while(!flag1){
							doc = Jsoup.connect("http://genome.cshlp.org/content/" + yc + "/" + pageIndex + ".toc").timeout(timeout).get();
							if(doc!=null){
								flag1 = true;
								System.out.print("	Success");
								System.out.println();
							}
						}
					} catch (Exception e) {
					}
				}
				pageIndex ++ ;
				Elements list = doc.getElementsByClass("cit-list");
				for(Element ul : list){
					for(Element item : ul.getElementsByClass("cit-extra")){
						Element ulE =ul.getElementsByClass("cit-doi").first();
						if(ulE == null) continue;
						String doi = ulE.text();
						if(doiList.contains(doi)) continue;
						doiList.add(doi);
						Sample sample = new Sample();
						sample.setSampleCode(doi);
						sample.setSource(SourceType.SUPPLEMENTTARY.getValue());
						sample.setEtype(ExperimentType.SUPPLEMENTTARY.getValue());
						sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
						sample.setDeleted(10);
						Element supp = item.getElementsByAttributeValue("rel", "supplemental-data").first();
						if(supp == null) continue;
						try {
							String href_supp = supp.attr("href");
							Boolean f = false;
							Document doc1 = null;
							System.out.print("connecting http://genome.cshlp.org" + href_supp);
							while(!f){
								doc1 = Jsoup.connect("http://genome.cshlp.org" + href_supp).timeout(timeout).get();
								if(doc1!=null){
									f = true;
									System.out.print("	Success");
									System.out.println();
								}
							}
							Elements files = doc1.getElementById("content-block").getElementsByClass("auto-clean").first().getElementsByTag("a");
							sample.setSourceUrl("http://genome.cshlp.org" + href_supp);
							if(files.size() < 1) continue;

							List<FileInfo> infos = new ArrayList<FileInfo>();
							for(Element file : files){
								String down = file.attr("href");
								String name = file.text();
//									System.out.println(name +" : " + down);
								FileInfo info = new FileInfo();
								info.setUrl("http://genome.cshlp.org" + down);
								info.setSource(SourceType.SUPPLEMENTTARY.getValue());
								info.setState(1);
								infos.add(info);
							}
							if(infos.size() > 0){
								Integer sampleId = sampleDAO.getSequenceId(SourceType.SUPPLEMENTTARY);
								sample.setSampleId(sampleId);
								samplePrevDAO.create(sample);
								for(FileInfo info : infos){
									info.setSampleId(sampleId);
								}
								fileInfoDAO.create(infos);
							}
						} catch (Exception e) {
							
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void parser(String url, int pageCount, int pagePre , Map<String,String> data){
	    if (isStoped()) {
            return;
        }
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        
        try {
        	if(pagePre > pageCount) return;
        	if (logger.isDebugEnabled()) {
                logger.debug("{},pageSize{},pageIndex{}", url, 80, pagePre);
            }
        	data.put("FIRSTINDEX", String.valueOf(pagePre * 80));
            logger.debug("data : {}", data);
            Document doc = Jsoup.connect(url).data(data).timeout(timeout).get();
          //  this.pas(doc.getElementsByAttributeValue("rel", "supplemental-data"));
        } catch (Exception e) {
        	logger.error("Journal parser failed!", e);
        } finally {
            ThreadUtils.sleep(2 * 1000);
        }
        
        parser(url, pageCount, ++pagePre, data);
	}

	@Override
	SourceType getSourceType() {
		return null;
	}

	public static void main(String[] args) {
		new GenomePaser().parser(null);
	}

}
