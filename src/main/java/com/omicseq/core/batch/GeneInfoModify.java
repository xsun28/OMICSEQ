package com.omicseq.core.batch;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class GeneInfoModify {

	protected static GeneCache geneCache = GeneCache.getInstance();
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	public static void main(String[] args) throws Exception {
		geneCache.init();
		
		GeneInfoModify g = new GeneInfoModify();
//		File file = new File("E:\\refgene.csv");
		g.addExonInfos();
	}

	private void addExonInfos() throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook("E:\\refgene.xlsx");
		XSSFSheet sheet  = wb.getSheetAt(0);
		
		int rowNum = sheet.getLastRowNum();      
		XSSFRow row = sheet.getRow(0);      
//        int colNum = row.getPhysicalNumberOfCells(); 
        int colNum = 10;
        String str = "";  
        Map<Integer,String> map = new HashMap<Integer,String>(); 
        //正文内容应该从第二行开始,第一行为表头的标题      
        for (int i = 1; i <= rowNum; i++) {      
            row = sheet.getRow(i);
            if(row == null)
            {
            	continue;
            }
            int j = 0;      
            while (j<colNum) {      
        //每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据      
        //也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
            	if(row.getCell((short) j) != null) {
            		str +=  getStringCellValue(row.getCell((short) j)) + "@";
            	} else {
            		str += "null@";
            	}
                      
                j ++;      
            }      
            map.put(i, str);      
            str = "";      
        }
		
        for (int i=1; i<=map.size(); i++) {
        	String rowContent= map.get(i);
        	
        	String[] values = rowContent.split("@");
        	String txName = values[1];
        	
        	Integer start = Integer.parseInt(values[3].replace(".0", ""));
        	Integer end = Integer.parseInt(values[4].replace(".0", ""));
        	
        	Integer exon_num = Integer.parseInt(values[8].replace(".0", ""));
        	Integer exon_length = Integer.parseInt(values[9].replace(".0", ""));
        	Integer geneLength = end - start;
        	System.out.println(txName + "====" + geneLength + "==" + exon_num + "==" + exon_length);
        	
        	Gene gene = geneCache.getGeneByName(txName);
        	
        	if(gene != null)
        	{
        		gene.setGeneLength(geneLength);
        		gene.setExonNum(exon_num);
        		gene.setExonLength(exon_length);
        		
        		geneDAO.update(gene);
        	}
        }
	}
	
	 /**    
     * 获取单元格数据内容为字符串类型的数据    
     * @param cell Excel单元格    
     * @return String 单元格数据内容    
     */     
    private String getStringCellValue(XSSFCell cell) {      
        String strCell = "";      
        switch (cell.getCellType()) {      
        case HSSFCell.CELL_TYPE_STRING:      
            strCell = cell.getStringCellValue();      
            break;      
        case HSSFCell.CELL_TYPE_NUMERIC:      
        	Double double1 = cell.getNumericCellValue();
        	DecimalFormat decimalFormat = new DecimalFormat("##0.0");//格式化设置 
            strCell = String.valueOf(decimalFormat.format(double1));      
            break;      
        case HSSFCell.CELL_TYPE_BOOLEAN:      
            strCell = String.valueOf(cell.getBooleanCellValue());      
            break;      
        case HSSFCell.CELL_TYPE_BLANK:      
            strCell = "";      
            break;      
        default:      
            strCell = "";      
            break;      
        }      
        if (strCell.equals("") || strCell == null) {      
            return "";      
        }      
        if (cell == null) {      
            return "";      
        }      
        return strCell;      
    }      

}
