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

public class NCBISRADescriptionModify extends BaseParser {

	public static void main(String[] args) {
		SmartDBObject query2 = new SmartDBObject();
        query2.put("$gte", 400006);
//        query2.append("$lte", 400413);
        
        SmartDBObject query = new SmartDBObject();
        query.put("sampleId", query2);
        query.put("source", 5);
        query.addSort("sampleId", SortType.ASC);
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
		 String description = sample.getDescription();
		 if(description.contains("Gb  ;"))
		 {
			 description = description.substring(description.indexOf("Gb  ;") + 5);
			 sample.setDescription(description);
			 samplePrevDAO.update(sample);
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
