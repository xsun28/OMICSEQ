
package com.omicseq.robot.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.SourceType;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.robot.SampleDetail;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class Test5 extends BaseParser {

	@Override
	public void parser(String url) {
		/*SmartDBObject query = new SmartDBObject();
		query.put("source", 7);
		query.put("state", 1);
		query.put("deleted", 0);
		List<FileInfo> infos = fileInfoDAO.find(query);
		for(FileInfo s : infos) {
			url = s.getUrl();
			if(url.startsWith("http")) continue;
			String [] url_arr = url.split("/");
			String G_start = url_arr[6];
			String url_prev = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc="+G_start;
			try {
				Document doc = Jsoup.connect(url_prev).timeout(timeout).get();
				Elements eles = doc.getElementById("ViewOptions").nextElementSibling().child(0).child(0)
	    				.child(0).children();
				Element e5;
	    		if (url_prev.indexOf("GSE")>0) {
	    			e5 = eles.get(5);
	    			
	    		} else if (url_prev.indexOf("GSM")>0) {
	    			e5 = eles.get(2);
	    		} else {
	    			continue;
	    		}
	    		
	    		Elements e5s = e5.children().get(0).children();
	    		for (int j = 0; j < e5s.size(); j++) {
	        		if (j == 0) continue;
	        		if (e5s.get(j).childNodeSize()< 2) {
	        			break;
	        		}
	        		for(int m = 0 ; m<e5s.get(j).child(2).children().size();m++){
	        			if(e5s.get(j).child(0).text().contains("diff") && e5s.get(j).child(2).child(0).attr("href").equals(url)){
	        				Element e_url = e5s.get(j).child(2);
	        				String url_final = "";
	        				for(int n=0; n<e_url.children().size();n++){
	        					url_final = e_url.child(n).attr("href");
	        					if(e_url.child(n).text().equals("(http)")){
	        						break;
	        					}
	        				}
	        				url_final = URLDecoder.decode(url_final, "UTF-8");
	        				s.setUrl("http://www.ncbi.nlm.nih.gov"+url_final);
	        				fileInfoDAO.update(s);
	        			}
	        		}
	    		}
	    		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
		SmartDBObject query = new SmartDBObject();
		//{"source":7,"etype":1 ,"cell":null}
		query.put("etype", 1);
		query.put("source",7);
		query.put("cell", null);
		query.put("fromType","mouse");
		query.put("createTiemStamp","2015-01-28");
		List<Sample> samples = samplePrevDAO.find(query);
		int i = 0;
		for(Sample sample : samples){
			i++;
			String sampleCode = sample.getSampleCode();
			if(sampleCode == null ){
				String url1 = sample.getUrl();
				if(StringUtils.isEmpty(url1)) continue;
				String [] t = url1.split("acc=");
				if(t.length<2) continue;
				sampleCode = t[1].trim();
				if(StringUtils.isEmpty(sampleCode)) continue;
				sample.setSampleCode(sampleCode);
			}
			url = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=" + sampleCode;
			System.out.println("current Url(" + i +"):"+url);
			Map <String,String> map = sample.descMap();
			Document doc = null;
			try {
				doc = Jsoup.connect(url).timeout(timeout).get();
				Elements els = doc.getElementsByClass("pubmed_id");
				String href = null;
				if(els != null) {
					for (Iterator<Element> iterator = els.iterator(); iterator.hasNext();){
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
	//			System.out.print(e.child(0).text());
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
										System.out.println(k);
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
	   					Document doc1 = null;
						doc1 = Jsoup.connect("http://www.ncbi.nlm.nih.gov"+cellurl).timeout(timeout).get();
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
			sample = SampleDetail.getDetailBySourceAndCell(sample);
			samplePrevDAO.update(sample);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	SourceType getSourceType() {
		// TODO Auto-generated method stub
		return SourceType.CCLE;
	}
	
	public static void main(String[] args) {
		new Test5().start();
		/*SmartDBObject query = new SmartDBObject("deleted", 2);
		query.put("source", 7);
		query.put("etype", 1);
		query.put("deleted", 0);
		List<Sample> samples = samplePrevDAO.find(query);
		for(Sample s :samples){
			int sampleId =s.getSampleId();
			List<FileInfo> infos = fileInfoDAO.find(new SmartDBObject("sampleId", sampleId));
			FileInfo info = infos.get(0);
			info.setUrl(s.getUrl());
			fileInfoDAO.update(info);
		}*/
	}
}
