package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.omicseq.common.SortType;
import com.omicseq.domain.Sample;
import com.omicseq.robot.process.CNVParser;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class UpdateMethylationURL {
	
	protected static String root = "E:/";
	protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static Logger logger = LoggerFactory.getLogger(CNVParser.class);
	
	public void setURL() throws IOException{
		//获取url文件名
		String [] names = {};
		File file = new File(root+"methylationURL");
		if (file.isDirectory()){
			names = file.list();
		}
		DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage", "samplenew");
		//找到所有methylation的sample
		for(String name : names ){
			if(name.startsWith("OV")){
			List<String> map = new ArrayList< String>();
			//获取文件内容 barcode - url
			BufferedReader reader = new BufferedReader(new FileReader(root+"methylationURL"+File.separator+name));
			String line = "" ;
			while((line = reader.readLine())!=null){
				String [] temps;
				System.out.println(line);
				if(line.startsWith("TCGA")){
					temps = line.split(",");
					 map.add(temps[1].substring(0, temps[1].length()));
				}
			}
			String cancerType = name.split("_")[0];
			SmartDBObject query = new SmartDBObject();
			query.put("cell", new SmartDBObject("$regex", "TCGA-"+cancerType.toLowerCase()));
			query.put("etype", 12);
			query.addSort("sampleId", SortType.ASC);
			List<Sample> samples = dao.find(query);
			if(samples!=null ){
			for(int  i = 0;i<map.size();i++){
				SmartDBObject query1 = new SmartDBObject();
				query1.put("sampleId", samples.get(i).getSampleId());
				query1.put("etype", 12);
				collection.update(query1, new SmartDBObject("$set",new SmartDBObject("url", map.get(i))));
			}
		}
		}
	}}
	public static void main(String[] args) throws IOException {
		//去factor）
		/*DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage", "samplenew");
		SmartDBObject query = new SmartDBObject("deleted",0);
		List<Sample> sams = dao.find(query);
		for(Sample s : sams){
			boolean f = false;
			String des = s.getDescription()==null?"":s.getDescription();
 			String des1 = "";
			String [] item = des.split(";");
			for(String dd : item){
				String [] kv = dd.split("="); 
				if(kv[0].trim().equalsIgnoreCase("antibody")){
					if(!kv[1].contains("(") && kv[1].endsWith(")")){
						dd=dd.substring(0, dd.length()-1);
						f=true;
					}
				}
				des1 += dd+";";
			}
			des1=des1.substring(0, des1.length()-1);
			if(f){
				SmartDBObject query1 = new SmartDBObject("sampleId",s.getSampleId());
				collection.update(query1, new SmartDBObject("$set",new SmartDBObject("description", des1)));
			}
		}*/
		new UpdateMethylationURL().setURL();
	}
}
