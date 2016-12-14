package com.omicseq.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class tissueSameWithCell {

	public static void main(String[] args) {
		ISampleDAO sampleDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
		int start = 0 ;
		List<Sample> sampleList = null;
		File file  = new File("D:/TissueSameWithCell.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file,true);
		
		OutputStreamWriter osw = new OutputStreamWriter(fos);   
		BufferedWriter bw = new BufferedWriter(osw);
		
		while(CollectionUtils.isNotEmpty(sampleList = sampleDao.find(new SmartDBObject("deleted",0),start,3000))){
			for(Sample sample : sampleList){
				String detail = sample.getDetail();
				if(StringUtils.isEmpty(detail)) continue;
				if(detail.split(" ").length < 3) continue;
				detail = detail.replace("Tumor", "tumor").replace("Normal", "normal");
				String [] tmp = null;
				if(detail.contains("tumor")){
					tmp = detail.split("tumor");
					if(tmp.length > 1 && StringUtils.isNotEmpty(tmp[1])){
						if(tmp[0].trim().equals(tmp[1].trim())){
							bw.write(sample.getSampleId() + " --> " + sample.getCell() + " --> " + sample.getDetail() + "\r\n");
						}
					}
					continue;
				}
				if(detail.contains("normal")){
					tmp = detail.split("normal");
					if(tmp.length > 1 && StringUtils.isNotEmpty(tmp[1])){
						if(tmp[0].trim().equals(tmp[1].trim())){
							bw.write(sample.getSampleId() + " --> " + sample.getCell() + " --> " + sample.getDetail() + "\r\n");
						}
					}
					continue;
				}
				
			}
			
			
			start += 3000;
		}
			bw.flush();
			bw.close();  
			osw.close();  
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}

}
