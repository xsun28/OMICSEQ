package com.omicseq.robot.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.DBCollection;
import com.omicseq.common.SourceType;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.robot.parse.BaseParser;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CheckGDSFile extends BaseParser {
	@Override
	public void parser(String url) {
		SmartDBObject query = new SmartDBObject();
		query.put("source", 7);
		query.put("state", 10);
		List<FileInfo> infoList = fileInfoDAO.find(query);
		File file = new File("D:/GDSurl1.csv");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file,true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);   
			BufferedWriter bw = new BufferedWriter(osw);
		int k=0;
		for(FileInfo fi : infoList){
			Sample s = samplePrevDAO.findOne(new SmartDBObject("sampleId",fi.getSampleId()));
			k++;
			if(k==180)
			{
				System.out.println();
			}
			url = s.getUrl();
			Document doc;
			try {
				doc = Jsoup.connect(url).timeout(timeout).get();
				Elements eles = doc.getElementById("ViewOptions").nextElementSibling().child(0).child(0)
	    				.child(0).children();
				Element e5;
	    		if (url.indexOf("GSE")>0) {
	    			e5 = eles.get(5);
	    		} else if (url.indexOf("GSM")>0) {
	    			e5 = eles.get(2);
	    		} else {
	    			continue;
	    		}
	 
	    		Elements e5s = e5.children().get(0).getElementsByAttribute("valign");
	    		boolean flag = true;
	    		for (int j = 1; j < e5s.size(); j++) {
	        		if (j == 0) continue;
	        		if (e5s.get(j).childNodeSize()< 2) {
	        			break;
	        		}
	        		Element ee = e5s.get(j);
	        		for(int x = 0; x < ee.children().size();x++){
	        			if(x==2){
	        				String hurl ="";
	        				for(int y = 0; y<ee.child(2).children().size();y++){
	        					hurl = ee.child(2).child(y).attr("href");
	        					if(ee.child(2).child(y).text().contains("http")){
	        						break;
	        					}
	        				}
	        				hurl = URLDecoder.decode(hurl, "UTF-8");
	        				if(hurl.startsWith("/")){
	        					hurl = "http://www.ncbi.nlm.nih.gov" + hurl;
	        				}
	        				bw.write(hurl+",");
	        			}else{
	        				bw.write(ee.child(x).text().replace(",", "，")+",");
	        				if(x == 3 && flag){
	        					bw.write(url);
	        					flag = false;
	        				}
	        			}
	        		}
	        		bw.write("\r\n");
	    		}
		    		
				bw.write("\r\n");
				System.out.println(k);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
		}
		bw.flush();   
		bw.close();  
		osw.close();  
		fos.close();
		} catch (Exception e) {
		}
	}

	@Override
	SourceType getSourceType() {
		return SourceType.GEO;
	}

	public static void main(String[] args) {
		new CheckGDSFile().start();
	}

}
