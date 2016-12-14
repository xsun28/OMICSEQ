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
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class MouseEncodeCellType {
	protected static ICellDescDao cellTypeDescDAO = DAOFactory.getDAO(ICellDescDao.class);

	public static void main(String[] args) {
		String url = "http://www.genome.ucsc.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Cell+Line&organism=Mouse&bgcolor=FFFEE8";
//		String url = "http://genome-mirror.duhs.duke.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Cell+Line&tier=3&bgcolor=FFFEE8";
		MouseEncodeCellType ef = new MouseEncodeCellType();
		ef.readFactorDescription(url);
		

	}

	public void readFactorDescription(String url) {
		try {
			Document doc = Jsoup.connect(url).timeout(300000).get();
			Elements els_tbody = doc.select(".sortable>tbody");
			Elements els_tr = els_tbody.select("tr");
			List<CellTypeDesc> cellDescList = new ArrayList<CellTypeDesc>();
			
			for (int i=0;i <els_tr.size(); i++) {
				CellTypeDesc fd = new CellTypeDesc();
				
				Elements els_td = els_tr.get(i).select("td");
				String cell = els_td.get(0).text();
				String description = els_td.get(1).text();
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
			
			SmartDBObject query = new SmartDBObject();
			List<CellTypeDesc> list = cellTypeDescDAO.find(query);
			List<String> cells = new ArrayList<String >();
		
			for(CellTypeDesc ctd : list){
				cells.add(ctd.getCell());
			}
			
			for(CellTypeDesc ctd : cellDescList){
				if(cells.contains(ctd.getCell())){
					System.out.println(ctd.getCell());
				}
			}
			cellTypeDescDAO.create(cellDescList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
