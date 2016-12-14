package com.omicseq.robot.process;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class TCGAMetaDataModify {
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	
	public static void main(String[] args) {
		TCGAMetaDataModify t = new TCGAMetaDataModify();
		t.modify();
	}
	
	private void modify() {
		SmartDBObject query = new SmartDBObject();
		query.put("source", 1);
		query.put("etype", 2);
		query.put("sampleCode", new SmartDBObject("$regex", "UNCID_"));
		List<Sample> sampleList = sampleDAO.find(query);
		
		for(Sample sample : sampleList)
		{
			String url = sample.getUrl();
//			String cell = sample.getCell();
			String sampleCode = "";
			String[] arr = StringUtils.split(url, "/");
//			if(arr.length > 8)
//			{
//				sampleCode = "TCGA-" + arr[7];
//			}
			String s = arr[arr.length-1];

			String[] arr2 = s.split("\\.");
			
			sampleCode = arr2[1];

			sample.setSampleCode(sampleCode);
//			sample.setSampleCode(sample.getCell());
			
//			int typeI = Integer.parseInt(arr2[3].substring(0,2));
//			if(typeI < 10)
//			{
//				sample.setCell(cell + "-tumor");
//			}else if (typeI < 20)
//			{
//				sample.setCell(cell + "-normal");
//			} else {
//				sample.setCell(cell + "-control");
//			}
//			sample.setCell(cell + "-tumor");
//			sample.setFactor(null);
			
			sampleDAO.update(sample);
		}
	}
}
