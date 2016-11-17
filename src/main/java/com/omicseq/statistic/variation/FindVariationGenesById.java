package com.omicseq.statistic.variation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.omicseq.domain.VariationGene;
import com.omicseq.store.dao.IVariationGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.ExcelReader;

public class FindVariationGenesById {
	private static IVariationGeneDAO variationGeneDAO = DAOFactory.getDAO(IVariationGeneDAO.class);
	
	public static void main(String[] args) {
		String filePath = "E:\\变异\\100320.xls";
		
		File file = new File(filePath);
		
		ExcelReader reader = new ExcelReader();
		try {
			InputStream is = new FileInputStream(file);
			Map<Integer, String> map = reader.readExcelContent(is);
			Iterator it = map.keySet().iterator();
			int i=0;
			
			File fileResult = new File("E:\\变异\\100320_all.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet st = wb.createSheet("sheet1");
	        XSSFRow header = st.createRow(0);
	        String[] arr = "variationId,start,end".split(",");
	        for (int j = 0; j < arr.length; j++) {
	            CellUtil.createCell(header, j, arr[j]);
	        }
	        
			while(it.hasNext()){
				if(i == 0){
					i++;
					it.next();
					continue;
				}
				String value = map.get(Integer.parseInt(it.next().toString()));
				System.out.println(value);
				String variationId = value.split("@")[0];
				VariationGene variationGene = variationGeneDAO.findOne(new SmartDBObject("variationId", variationId));
		        
		        XSSFRow row = st.createRow(i);
		        XSSFCell cell_0 = row.createCell(0);
		        cell_0.setCellValue(variationGene.getVariationId());
		        XSSFCell cell_1 = row.createCell(1);
		        cell_1.setCellValue(variationGene.getChromStart());
		        XSSFCell cell_2 = row.createCell(2);
		        cell_2.setCellValue(variationGene.getChromEnd());
		        i++;
			}
			wb.write(new FileOutputStream(fileResult));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
