package com.omicseq.store.tools;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.omicseq.domain.FactorDes;
import com.omicseq.store.dao.IFactorDescDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class EncodeFactor {
	protected static IFactorDescDao factorDescDAO = DAOFactory.getDAO(IFactorDescDao.class);

	public static void main(String[] args) {
		String url = "https://genome.ucsc.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Antibody&bgcolor=FFFEE8";
		EncodeFactor ef = new EncodeFactor();
		ef.readFactorDescription(url);
	}

	public void readFactorDescription(String url) {
		try {
			Document doc = Jsoup.connect(url).timeout(300000).get();
			Elements els_tbody = doc.select("tbody");
			Elements els_tr = els_tbody.select("tr");
			List<FactorDes> factorDeses = new ArrayList<FactorDes>();
			
			for (int i=0;i <els_tr.size(); i++) {
				FactorDes fd = new FactorDes();
				
				Elements els_td = els_tr.get(i).select("td");
				String factor = els_td.get(2).text();
				String description = els_td.get(3).text();
				System.out.println("factor: "+ factor + " desc: "+description);
				fd.setFactor(factor);
				fd.setFactorDesc(description);
				factorDeses.add(fd);
			}
			
			List<String> factors = new ArrayList<String>();
			for(int i=0; i<factorDeses.size(); i++)
			{
				if(factors.contains(factorDeses.get(i).getFactor())) {
					factorDeses.remove(i);
					continue;
				}
				factors.add(factorDeses.get(i).getFactor());
			}
			
			factorDescDAO.create(factorDeses);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
