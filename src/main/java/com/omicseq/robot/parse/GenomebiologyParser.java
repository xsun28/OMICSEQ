package com.omicseq.robot.parse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class GenomebiologyParser extends BaseParser {

	@Override
	public void parser(String url) {
		Set<String> codes = new HashSet<String>();
		SmartDBObject query = new SmartDBObject();
		query.put("source", 14);
		List<Sample> samples = samplePrevDAO.find(query);
		for(Sample s : samples){
			codes.add(s.getSampleCode());
		}
		//15即2014年
		url = "http://genomebiology.com/content/16/";
		String [] months = {"January","February"};
		//for(int month=12;month>=1;month--){
		for(String month : months){
			System.out.print("connecting "+url+month+"/2015");
			Boolean connected = false;
			Document doc = null;
			try {
				while(!connected){
					try {
						doc = Jsoup.connect(url+month+"/2015").timeout(timeout).get();
						if(doc!=null) {
							connected = true;
							System.out.print("	Success");
							System.out.println();
						}
					} catch (Exception e) {
					}
				}
				Elements eles = doc.getElementsByClass("article-entry");
				for(Element ele : eles){
					Element e = ele.getElementsMatchingOwnText("Full text").first();
					if(e == null) continue;
					String href = e.attr("href");
					System.out.print("conn "+href);
					try {
						Document doc1 = null;
						Boolean flag = false;
						while(!flag){
							try {
								doc1 = Jsoup.connect(href).timeout(timeout).get();
								if(doc1!=null) {
									flag = true;
									System.err.print(" --> Success");
									System.out.println();
								}
							} catch (Exception e2) {
							}
						}
						
						Element ul = doc1.getElementById("box-outline");
						if(ul.getElementsByAttributeValue("title", "Additional files").first()==null) continue;
						Elements sections = doc1.getElementsByTag("section");
						//标识 判断是否爬过
						Element el = ele.getElementsByClass("citation").first().getElementsByClass("article-citation").first();
						String doi = el.text().substring(el.text().indexOf(",")+1, el.text().indexOf("("));
						if(!codes.add(doi)) continue;
						Sample sample = new Sample();
						sample.setSampleCode(doi);

						sample.setSourceUrl(href);
						sample.setSource(SourceType.SUPPLEMENTTARY.getValue());
						sample.setEtype(ExperimentType.SUPPLEMENTTARY.getValue());
						sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
						sample.setDeleted(10);
						
						for(Element section : sections){
							if(section.select("h3").size()==0) continue;
							if(section.select("h3").first().text().equalsIgnoreCase("Additional files")){
								Elements files = section.select(".addfile");
								if(files.size()==0) continue;
//								Integer sampleId = sampleDAO.getSequenceId(SourceType.SUPPLEMENTTARY);
//								int sampleId = 000;
//								sample.setSampleId(sampleId);
//								samplePrevDAO.create(sample);
								List<FileInfo> infos = new ArrayList<FileInfo>();
								for(Element fileElement : files){
									String filePath = fileElement.getElementsMatchingOwnText("Download file").attr("href");
									if(filePath.endsWith(".xlsx") || filePath.endsWith(".xls") || filePath.endsWith(".txt")){
										FileInfo info = new FileInfo();
										info.setUrl("http://genomebiology.com" + filePath);
										info.setSource(SourceType.SUPPLEMENTTARY.getValue());
										info.setState(1);
										infos.add(info);
									}
								}
								if(infos.size()>0){
									Integer sampleId = sampleDAO.getSequenceId(SourceType.SUPPLEMENTTARY);
//									int sampleId = 000;
									sample.setSampleId(sampleId);
									samplePrevDAO.create(sample);
									for(FileInfo info : infos){
										info.setSampleId(sampleId);
									}
									fileInfoDAO.create(infos);
								}
							}
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				System.out.println();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	SourceType getSourceType() {
		return null;
	}

	public static void main(String[] args) {
		new GenomebiologyParser().parser(null);
	}

}
