package com.omicseq.store.tools;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.omicseq.common.Constants;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class EncodeTissueAndStatus {

	private static String url = "http://genome.ucsc.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Cell+Line&tier=3&bgcolor=FFFEE8";
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
	
	public static void main(String[] args) {
		EncodeTissueAndStatus encodeTS = new EncodeTissueAndStatus();
		encodeTS.parse(url);
	}

	private void parse(String url) {
		Document doc;
		try {
			doc = Jsoup.connect(url).timeout(300000).get();
			Elements els_tbody = doc.select("tbody");
			Elements els_tr = els_tbody.select("tr");
			for (int i=0;i <els_tr.size(); i++) {
				Elements els_td = els_tr.get(i).select("td");
				String cell = els_td.get(0).text();
				String tissue = els_td.get(4).text();
				String status = els_td.get(5).text();
				modifyDetailOfSample(cell, tissue, status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void modifyDetailOfSample(String cell, String tissue, String status) {
		SmartDBObject query = new SmartDBObject();
		SmartDBObject qu = new SmartDBObject();
    	qu.put("$regex",cell);
    	qu.put("$options", "i");
    	query.put("cell",qu);
		List<Sample> sampleList = sampleDAO.find(query);
		if(cell.equals("A549"))
		{
			tissue = "lung "+ tissue;
		}
		
		if(status != null && !"".equals(status) && status.equals("cancer"))
		{
			status = "tumor";
		} else {
			if(status == null || " ".equals(status))
			{
				status = "unknown";
			}
		}
		for(Sample sample : sampleList)
		{
			String detail = sample.getDetail();
			if(sample.getSource() == SourceType.CCLE.getValue())
			{
				tissue += "(C)";
			}
			if(detail != null)
			{
				String[] s = detail.split(" ");
				detail = tissue + " " + status;
				if(s.length > 2)
				{
					detail += " " + s[s.length-1];
				}
			} else {
				detail = tissue + " " + status;
			}
			
			sample.setDetail(detail);
			sampleDAO.update(sample);
		}
		
	}

}
