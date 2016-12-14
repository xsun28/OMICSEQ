package com.omicseq.robot.parse;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.SourceType;

public class biomedcentralParser extends BaseParser{

	public static void main(String[] args) {
		new biomedcentralParser().parser(null);
	}

	@Override
	public void parser(String url) {
		url = "http://www.biomedcentral.com/bmcgenomics/content?page=1&itemsPerPage=100&citation=true&summary=false";
		try {
			if (logger.isDebugEnabled()) {
                logger.debug("获取:{}", url);
            }
			Document doc = Jsoup.connect(url).timeout(timeout).get();
			Elements pageEle = doc.getElementsByClass("pager");
			String pageTotal = pageEle.first().child(0).text().split("of")[1].trim();
			Integer total = Integer.parseInt(pageTotal);
			String date = "2015/1/22";
			if (total == 0) {
                logger.info("{} not items found", date);
                return;
            }
			if (logger.isDebugEnabled()) {
                logger.debug("total {} pagesize {} ", total, 100);
            }
			Element list = doc.getElementById("articles-list").select("tbody").first();
			Elements items = list.select(".checkbox-toggle");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	public void parse(){
		
	}
	@Override
	SourceType getSourceType() {
		return null;
	}

}
