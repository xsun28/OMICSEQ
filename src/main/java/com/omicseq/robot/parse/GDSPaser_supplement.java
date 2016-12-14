package com.omicseq.robot.parse;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class GDSPaser_supplement extends BaseParser {
	private static ISampleDAO sampleDao = DAOFactory.getDAO(ISampleDAO.class);
	private static ISampleDAO sampleNewDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GDSPaser_supplement().start();
	}

	@Override
	public void parser(String url) {
		SmartDBObject query = new SmartDBObject();
		query.put("cell", null);
		List<Sample> sampleList = sampleNewDao.find(query);
		for(Sample sample : sampleList){
			String url1 = sample.getUrl();
			System.out.println(url1);
			try {
				Document doc = Jsoup.connect(url1).timeout(timeout).get();
				Elements eles = doc.getElementById("ViewOptions").nextElementSibling().child(0).child(0)
	    				.child(0).children();
				Element e0 = eles.get(0);
	    		Elements eles0 = e0.getElementsByAttribute("valign");
	    		for(Element el : eles0){
	    			if(el.children().size()==0) continue;
	    			if(el.children().size()>1){
	    				if(el.child(0).text().trim().equalsIgnoreCase("Characteristics")){
	    					String [] keys = el.child(1).html().toString().split("<br />");
	    					for(String k : keys){
 	    						if(k.contains("cell line")|| k.contains("cell type")){
	    							String cell = k.split(":")[1];
	    							System.out.println(cell.trim());
	    							sample.setCell(cell);
	    							sampleNewDao.update(sample);
	    						}
	    					}
	    				}
	    			}
	    			
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	SourceType getSourceType() {
		return SourceType.GEO;
	}

}
