package com.omicseq.statistic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.omicseq.domain.Gene;
import com.omicseq.domain.TxrRef;
import com.omicseq.pathway.CalculatePathWayGeneRanks;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CNVGeneRankTop100 {
	private Logger logger = LoggerFactory.getLogger(CalculatePathWayGeneRanks.class);
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	DBCollection generank_coll = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
	DBCollection gene_coll = MongoDBManager.getInstance().getCollection("manage", "manage", "gene");
	DBCollection txrref_coll = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref");
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	
	private static Map<Integer, Double> sumResult_cnv = new HashMap<Integer, Double>();
	
	public Map<String, List<Integer>> sum(String cancerType) {
		List<Integer> geneIdList = new ArrayList<Integer>();
		Map<String, List<Integer>> geneIdResult = new HashMap<String, List<Integer>>();
		Integer i = 1;
		while (i <= 32745) {
			geneIdList.add(i);
			i++;
		}
//		geneIdList.add(4);
		for(Integer geneId : geneIdList)
		{
			double cnvNum = 0.0;
			int cn=0;
			File file = new File("/files/nginx/export/so_e/" + geneId + ".xlsx");
//			File file = new File("E:\\" + geneId + ".xlsx");
			try {
				if(file.exists() == false) 
				{
					continue;
				}
				InputStream is = new FileInputStream(file);
				XSSFWorkbook workBook = new XSSFWorkbook(is);
				XSSFSheet sheet = workBook.getSheet("sheet1");
				int total = sheet.getLastRowNum();
				if(total < 500)
				{
					continue;
				}
				
				for(int n=1; n<sheet.getLastRowNum(); n++)
				{
					XSSFRow xRow = sheet.getRow(n);
					XSSFCell xCell3 = xRow.getCell(2);
					if(xCell3 != null && xCell3.toString().contains("CNV"))
					{
						
						XSSFCell xCell4 = xRow.getCell(3);
						if(xCell4 != null && xCell4.toString().contains(cancerType + "-normal"))
						{
							XSSFCell xCell = xRow.getCell(7);
							if (xCell == null) {
								continue;
							}
							if(Double.parseDouble(xCell.toString()) > 0)
							{
								cnvNum += Double.parseDouble(xCell.toString());
								cn++;
								if(cn == 100)
								{
									break;
								}
							}
						}
					}
				}
				
				if(cnvNum > 0)
				{
					System.out.println(geneId + " : " + cnvNum);
					sumResult_cnv.put(geneId, cnvNum);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	
		//allType
		 Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>(); 
		 Map.Entry<Integer, Double> tmpEntry = null;  
		 
		 //cnv
		 sortedMap = new LinkedHashMap<Integer, Double>(); 
		 List<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(sumResult_cnv.entrySet());
		 Collections.sort(entryList, new MapValueComparator());  
		 Iterator<Map.Entry<Integer, Double>> iter = entryList.iterator();
		 iter = entryList.iterator();  
		 tmpEntry = null;  
		 int k =0;
		 while (iter.hasNext() && k <= 100) {  
			 tmpEntry = iter.next();  
			 sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
			 k++;
		 }
		 
		 Iterator<Map.Entry<Integer, Double>> iter1 = sortedMap.entrySet().iterator();
		 List<Integer> cnvList = new ArrayList<Integer>();
		 while(iter1.hasNext())
		 {
			 Integer geneId = iter1.next().getKey();
			 cnvList.add(geneId);
		 }
		 
		 geneIdResult.put("CNV", cnvList);
		 return geneIdResult;
	}
	
	//比较器类  
	public class MapValueComparator implements Comparator<Map.Entry<Integer, Double>> {  
		public int compare(Entry<Integer, Double> me1, Entry<Integer, Double> me2) {  
			return me1.getValue().compareTo(me2.getValue());  
		}  
	}
	
	public String out(List<Integer> geneIds)
	{
		String result = "";
		
		for(Integer geneId : geneIds)
		{
			List<Gene> geneList= geneDAO.find(new SmartDBObject("geneId", geneId));

			if(geneList != null && geneList.size()>0)
			{
				String txName = geneList.get(0).getTxName();
				 List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
				 if(refs == null || refs.size() ==0)
				 {
					 result += geneId + "," + txName + "\n";
				 } else {
					 for(TxrRef r : refs){
						 if(r.getAlias() == null)
						 {
							 result += geneId + "," + r.getGeneSymbol() + "\n";
							 break;
						 } else {
							 result += geneId + "," + r.getAlias() + "\n";
							 break;
						 }
					 }
				 }
			}
		}
		return result;
	}


	public static void main(String[] args) {
		CNVGeneRankTop100 gs = new CNVGeneRankTop100();
		
		try {
			FileWriter file = new FileWriter("/home/tomcat/cnv_normal_Top100.txt", true);
			String[] cancerType = {"gbm", "lusc","stad", "read"};
			for(int i=0; i<cancerType.length; i++)
			{
				Map<String, List<Integer>> results = gs.sum(cancerType[i]);
				
				List<Integer> geneIdListCNV = results.get("CNV");
				String resultCNV = gs.out(geneIdListCNV);
				
				System.out.println("top100 genes of CNV_"+ cancerType[i].toUpperCase()+": ");
				System.out.println(resultCNV);
				System.out.println("======================================================");
				
				file.append("top100 genes of CNV_"+ cancerType[i].toUpperCase()+": \n");
				file.append(resultCNV + "\n");
				file.append("======================================================\n");
			}
			file.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
//		Integer[] geneIds = new Integer[geneIdList.size()];
//		int i =0;
//		for(Integer geneId : geneIdList)
//		{
//			geneIds[i] = geneId;
//			i++;
//			System.out.print(geneId+",");
//		}
//		
//		gs.sort(geneIds);
		
//		Integer geneId = 30236;
//		double num = 0.0;
//		File file = new File("E:\\" + geneId + ".xlsx");
//		try {
//			
//			InputStream is = new FileInputStream(file);
//			XSSFWorkbook workBook = new XSSFWorkbook(is);
//			XSSFSheet sheet = workBook.getSheet("sheet1");
//			for(int m=1;m<sheet.getLastRowNum();m++){
//				XSSFRow xRow = sheet.getRow(m);
//				XSSFCell xCell = xRow.getCell(8);
//				if (xCell == null) {
//					continue;
//				}
//				if(NumberUtils.isNumber(xCell.toString()))
//				{
//					num += Double.parseDouble(xCell.toString());
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println(geneId + ":" + num);
		
//		sumResult.put(geneId, num);
		
//		List<Integer> gIds = new ArrayList<Integer>();
//		gIds.add(891);
//		gIds.add(18845);
//		gIds.add(30112);
//		gIds.add(8099);
//		gIds.add(17233);
//		gIds.add(5279);
//		gIds.add(9695);
//		gIds.add(9377);
//		gIds.add(12172);
//		gIds.add(18822);
//		gs.out(gIds);
	
	}
}
