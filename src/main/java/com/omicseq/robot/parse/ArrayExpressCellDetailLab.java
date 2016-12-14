package com.omicseq.robot.parse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class ArrayExpressCellDetailLab extends BaseParser{

	@Override
	public void parser(String url) {
	}
	
	public Sample parser(String url, Sample sample ){
		try {
			Document doc = Jsoup.connect(url).timeout(timeout).get();
			Elements eles = doc.getElementById("ViewOptions").nextElementSibling().child(0).child(0).child(0).children();
			Elements eles0 = eles.select("tr[valign=top]");
			Map<String, String> map = new HashMap<String, String>();
			boolean hasCell = true;
			for(Element e : eles0){
				if(e.children().size() == 0) {
    				continue;
    			}
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
	    				}	    				
	    			if(e.child(0).text().equalsIgnoreCase("Last update date")){
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
		    	    				}
		   		    				else if(forc.contains("tissue")){
		   		    					String tissue =  forc.split(":")[1].trim();
			   		    				sample.setDetail(tissue);
		   		    				}
		   		    				else if((el.child(0).text().trim()).equalsIgnoreCase("Source name")){
		    	    					String cell = el.child(1).text().replace("cells", "").trim();
		    		   					sample.setCell(cell);
		    		   				}
		   		    	
	    		    			}
	    		   			}
	    		   		}
	    			}
    			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sample;
	}
	
	
	@Override
	SourceType getSourceType() {
		return SourceType.ArrayExpress;
	}

	public void findSample(){
		SmartDBObject query = new SmartDBObject();
		query.put("source", SourceType.ArrayExpress.getValue());
		query.put("cell", null);
		List<Sample> sampleList = null;
		int start = 0;
		int size = 3000;
		while(CollectionUtils.isNotEmpty(sampleList = samplePrevDAO.find(query, start, size))){
			for(Sample sample : sampleList){
				String gsm = sample.getSampleCode();
				if(!gsm.startsWith("GSM")) continue;
				String url = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc="+gsm;
				sample = parser(url,sample);
				samplePrevDAO.update(sample);
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		new ArrayExpressCellDetailLab().findSample();
	}
}
