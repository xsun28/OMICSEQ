package com.omicseq.core.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.omicseq.domain.CellTypeDesc;
import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.ExcelReader;

public class ImportDictionary {
	protected static ICellDescDao cellTypeDescDAO = DAOFactory.getDAO(ICellDescDao.class);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String filePath = "E:\\cell_factor字典未收录_9.6修改-by Qin Sun Sep 7 2014.xls";
		
		try {
			ExcelReader excelReader = new ExcelReader();  
//			InputStream is = new FileInputStream(filePath);
//            String[] title = excelReader.readExcelTitle(is);
//            
//            System.out.println("获得Excel表格的标题:");      
//            for (String s : title) {      
//                System.out.println(s);      
//            }      
            
            InputStream is2 = new FileInputStream(filePath);
            Map<Integer,String> map = excelReader.readExcelContent(is2);      
            System.out.println("获得Excel表格的内容:");      
            for (int i=4; i<=map.size(); i++) {      
                String row = map.get(i);
                if(row != null && !"@".equals(row) && row.indexOf("null") == -1 && row.split("@").length > 1)
                {
                	String[] values = row.split("@");
                	String cell = values[0];
                	String cell_desc = values[1];
                	
                	if(cell_desc == null || "".equals(cell_desc))
                	{
                		continue;
                	} else {
                		cell_desc.replace("\n", "");
                	}
                	
                	System.out.println(cell + "======" + cell_desc);
                	List<CellTypeDesc> fds = cellTypeDescDAO.find(new SmartDBObject("cell", new SmartDBObject("$regex", cell)));
                			
                	if(fds != null && fds.size() > 0)
                	{
                		System.out.println("already existing");
                	} else {
                		CellTypeDesc cd = new CellTypeDesc();
                		cd.setCell(cell);
                		cd.setCell_desc(cell_desc);
                		
                		cellTypeDescDAO.create(cd);
                	}
                }
            } 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}

}
