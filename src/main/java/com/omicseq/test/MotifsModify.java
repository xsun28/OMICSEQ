package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.omicseq.common.Constants;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class MotifsModify {
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);

	public static void main(String[] args) {
		String pfmUrl = "http://jaspar.genereg.net/html/DOWNLOAD/JASPAR_CORE/pfm/nonredundant/pfm_all.txt";
//		File file = new File("E:\\data\\TF.motif.csv");
//		File file = new File("/home/tomcat/TF.motif.csv");
		try {
//			new MotifsModify().parse(file);
			
			new MotifsModify().getPFM(pfmUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getPFM(String pfmUrl) throws Exception {
		Document doc = Jsoup.connect(pfmUrl).timeout(100000).get();
		String text = doc.text();
		String[] all = text.split(">");
		for(int i=1; i<all.length; i++)
		{
			System.out.println(all[i]);
			String[] pfmInfos = all[i].split(" ");
			String sampleCode = pfmInfos[0]+"__"+pfmInfos[1];
			
			SmartDBObject query = new SmartDBObject();
			query.put("sampleCode", sampleCode);
			query.put("source", SourceType.JASPAR.getValue());
			query.put("deleted", 0);
			
			Sample sample = sampleDAO.findOne(query);
			
			if(sample != null) {
				if(sample.getSampleId() == 1400436) {
					continue;
				}
				String description = sample.getDescription() + ";>" + sampleCode+"=<br>";
				int k = (pfmInfos.length -2)/4;
				for(int j=1; j<pfmInfos.length-1; j++)
				{
					description += " " + pfmInfos[j+1];
					if(j%k == 0) {
						description += "<br>";
					}
				}
				
				sample.setDescription(description + ";          ;");
				
				sampleDAO.update(sample);
			}
		}
	}

	private void parse(File file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		line = br.readLine();
		br.close();
		String [] title = line.split(",");
		
		for(int i=1; i<title.length; i++)
		{
			String ID = title[i].split("__")[0];	
			
			String jasparUrl = "http://jaspar.genereg.net/cgi-bin/jaspar_db.pl?ID=" + ID + "&rm=present&collection=CORE";
			
			SmartDBObject query = new SmartDBObject();
			query.put("sampleCode", title[i]);
			query.put("source", SourceType.JASPAR.getValue());
			query.put("description", null);
			query.put("deleted", 0);
			
			Sample sample = sampleDAO.findOne(query);
			
			if(sample != null) {
				sample.setDescription(jasparUrl);
				
				Document doc = Jsoup.connect(jasparUrl).timeout(60000).get();
				Elements elements = doc.getElementsContainingText("Homo sapiens");
				if(!elements.hasText()){
					sample.setDeleted(1);
					System.out.println(ID);
				}else {
					System.out.println(ID + "===========================");
				}
				
				sampleDAO.update(sample);
			}
		}
	}

}
