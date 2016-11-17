package com.omicseq.statistic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class GeneRankOnePercentailNums {

	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);

	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);

	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);

	protected static GeneCache geneCache = GeneCache.getInstance();

	public static void main(String[] args) {
		geneCache.init();
		List<Integer> geneIds = geneCache.getGeneIds();
		Collections.sort(geneIds);
//		List<Integer> geneIds = new ArrayList<Integer>();
//		geneIds.add(1);
//		geneIds.add(30236);
		GeneRankOnePercentailNums go = new GeneRankOnePercentailNums();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (Integer geneId : geneIds) {
			Integer num = go.countNum(geneId);
			map.put(geneId, num);
		}

		go.writeDataToExcel(map);
	}

	private void writeDataToExcel(HashMap<Integer, Integer> map) {
		try {
//			File file = new File("E:\\numOfGenes.xlsx");
			File file = new File("/home/tomcat/numOfGenes.xlsx");
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}

			XSSFWorkbook wb = new XSSFWorkbook();

			XSSFSheet st = wb.createSheet("sheet1");
			XSSFRow header = st.createRow(0);
	        String[] arr = "GeneId,GeneSymbol,Number".split(",");
	        for (int i = 0; i < arr.length; i++) {
	            CellUtil.createCell(header, i, arr[i]);
	        }
	        
	        Iterator<Entry<Integer, Integer>> genes = map.entrySet().iterator();
	        int i =1;
	        while(genes.hasNext())
	        {
	        	Entry<Integer, Integer> entry = genes.next();
	        	Integer geneId = entry.getKey();
	        	Integer num = entry.getValue();
	        	String geneSymbol = "";
	        	
	        	List<Gene> geneList= geneDAO.find(new SmartDBObject("geneId", geneId));

	        	
				if(geneList != null && geneList.size()>0)
				{
					String txName = geneList.get(0).getTxName();
					 List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
					 if(refs == null || refs.size() ==0)
					 {
						 geneSymbol =  txName;
					 } else {
						 for(TxrRef r : refs){
							 if(r.getAlias() == null)
							 {
								 geneSymbol = r.getGeneSymbol();
								 break;
							 } else {
								 geneSymbol = r.getAlias();
								 break;
							 }
						 }
					 }
				}
		        XSSFRow row = st.createRow(i);
		        XSSFCell cell = row.createCell(0);
		        cell.setCellValue(geneId);
		        
		        XSSFCell cell1 = row.createCell(1);
		        cell1.setCellValue(geneSymbol);
		        
		        XSSFCell cell2 = row.createCell(2);
		        cell2.setCellValue(num);
		        
		        
				i++;
	        }
	        
	        OutputStream out = new FileOutputStream(file);
            wb.write(out);
            IOUtils.closeQuietly(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Integer countNum(Integer geneId) {
		GeneRankCriteria criteria = new GeneRankCriteria();
		criteria.setGeneId(geneId);
		criteria.setMixturePerc(0.01);
		return geneRankDAO.count(criteria);
	}
}
