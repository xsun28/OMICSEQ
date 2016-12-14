package com.omicseq.store.tools;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.omicseq.domain.CellTypeDesc;
import com.omicseq.domain.FactorDes;
import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class EncodeCellType {
	protected static ICellDescDao cellTypeDescDAO = DAOFactory.getDAO(ICellDescDao.class);

	public static void main(String[] args) {
//		String url = "http://genome-mirror.duhs.duke.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Cell+Line&tier=3&bgcolor=FFFEE8";
//		EncodeCellType ef = new EncodeCellType();
//		ef.readFactorDescription(url);
		List<CellTypeDesc> cellDescList = new ArrayList<CellTypeDesc>();
		CellTypeDesc fd1 = new CellTypeDesc();
		fd1.setCell("GM12878");
		fd1.setCell_desc("lymphoblastoid, International HapMap Project - CEPH/Utah - European Caucasion, Epstein-Barr Virus");
		
		CellTypeDesc fd2 = new CellTypeDesc();
		fd2.setCell("H1-hESC");
		fd2.setCell_desc("embryonic stem cells");
		
		CellTypeDesc fd3 = new CellTypeDesc();
		fd3.setCell("K562");
		fd3.setCell_desc("leukemia, 'The continuous cell line K-562 was established by Lozzio and Lozzio from the pleural effusion of a 53-year-old female with chronic myelogenous leukemia in terminal blast crises.' - ATCC");
		
		CellTypeDesc fd4 = new CellTypeDesc();
		fd4.setCell("HeLa-S3");
		fd4.setCell_desc("cervical carcinoma");
		
		CellTypeDesc fd5 = new CellTypeDesc();
		fd5.setCell("HepG2");
		fd5.setCell_desc("liver carcinoma");
		
		CellTypeDesc fd6 = new CellTypeDesc();
		fd6.setCell("HUVEC");
		fd6.setCell_desc("umbilical vein endothelial cells");
		
		cellDescList.add(fd1);
		cellDescList.add(fd2);
		cellDescList.add(fd3);
		cellDescList.add(fd4);
		cellDescList.add(fd5);
//		cellDescList.add(fd6);
		cellTypeDescDAO.create(cellDescList);
	}

	public void readFactorDescription(String url) {
		try {
			Document doc = Jsoup.connect(url).timeout(300000).get();
			Elements els_tbody = doc.select("tbody");
			Elements els_tr = els_tbody.select("tr");
			List<CellTypeDesc> cellDescList = new ArrayList<CellTypeDesc>();
			
			for (int i=0;i <els_tr.size(); i++) {
				CellTypeDesc fd = new CellTypeDesc();
				
				Elements els_td = els_tr.get(i).select("td");
				String cell = els_td.get(0).text();
				String description = els_td.get(2).text();
				System.out.println("cell: "+ cell + " desc: "+description);
				fd.setCell(cell);
				fd.setCell_desc(description);
				cellDescList.add(fd);
			}
			
			List<String> factors = new ArrayList<String>();
			for(int i=0; i<cellDescList.size(); i++)
			{
				if(factors.contains(cellDescList.get(i).getCell())) {
					cellDescList.remove(i);
					continue;
				}
				factors.add(cellDescList.get(i).getCell());
			}
			
			cellTypeDescDAO.create(cellDescList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
