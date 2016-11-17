package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class Distinguish_ChipSeq_etype {
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	public void update(List<String > list){
		for(String factor : list){
			SmartDBObject query = new SmartDBObject();
			query.put("etype",ExperimentType.CHIP_SEQ_HISTONE.getValue());
			query.put("factor", factor.trim().toString());
			query.put("fromType", new SmartDBObject("$ne","mouse"));
			List<Sample> sampleList = sampleDAO.find(query);
			DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage", "samplenew");
			collection.update(query, new SmartDBObject("$set", new SmartDBObject("etype",ExperimentType.CHIP_SEQ_CHR.getValue())),false,true);
			for(Sample sample : sampleList){
				if(sample.getEtype() == ExperimentType.CHIP_SEQ_HISTONE.getValue()) continue;
				System.out.println("更新factor为" + factor +",Id为" + sample.getSampleId());
				DBCollection collection_rank = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
				collection_rank.update(new SmartDBObject("sampleId", sample.getSampleId()), new SmartDBObject("$set",new SmartDBObject("etype", ExperimentType.CHIP_SEQ_CHR.getValue())),false,true);
			}
			System.out.println("factor为" + factor +"更新完毕");
		}
	}

	public static void main(String[] args) {
		List list = new ArrayList<String>();
		//TODO 传入需要改的factor类型list
		new Distinguish_ChipSeq_etype().getFile();
	}

	public void getFile(){
		List<String > list = new ArrayList<String>();
		File file = new File("C:/Users/Administrator/Desktop/factor.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while((line=br.readLine())!=null){
				String [] tmp = line.split("\t");
				if(tmp[1].equalsIgnoreCase("chromatin modifier") && tmp[0].contains("*")){
					list.add(tmp[0]);
				}
			}
			update(list);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
