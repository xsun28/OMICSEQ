package com.omicseq.robot.parse;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class NCBISRAPubmedUrlAdd extends BaseParser {

	public static void main(String[] args) {
		SmartDBObject query2 = new SmartDBObject();
        query2.put("$gte", 400413);
        query2.append("$lte", 400413);
        
        SmartDBObject query = new SmartDBObject();
        query.put("sampleId", query2);
        query.put("source", 5);
        query.put("pubmedUrl", null);
        query.addSort("sampleId", SortType.DESC);
        List<Sample> sampleList = samplePrevDAO.find(query);
        for(Sample sample :sampleList)
        {
        	try {
				modify(sample);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
        }
	}

	private static void modify(Sample sample) {
		 Map<String, String> map = sample.descMap();
		 String geoSeries = map.get("geo accession");
		 if(geoSeries != null)
		 {
			 String geoSeriesUrl = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc="+geoSeries;
			 try {
				Document doc = Jsoup.connect(geoSeriesUrl).timeout(5 * 60 * 1000).get();
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
					
					samplePrevDAO.update(sample);
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		 }
		 
	}

	@Override
	public void parser(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	SourceType getSourceType() {
		// TODO Auto-generated method stub
		return null;
	}

}
