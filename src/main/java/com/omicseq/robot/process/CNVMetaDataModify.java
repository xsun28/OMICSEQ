package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.StringUtils;

import com.mongodb.DBCollection;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.core.WebResourceInitiate;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
public class CNVMetaDataModify {
	
//	protected static String root = "/home/TCGA-Assembler/user/";
	protected static String root = "F:\\TCGA-assembler\\user\\";
	protected static ISampleDAO sampleNewdao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static Logger logger = LoggerFactory.getLogger(CNVMetaDataModify.class);

	/*
	 *  读取GeneLevel需要解析的txt 文件
	 */
	public List<String> readMethylationFile(){
		List<String> meFileList = new ArrayList<String>();
		File mefile = new File(root+"GeneLevel");
		if(mefile.isDirectory()){
			String [] meFileNames = mefile.list();
			for(String me : meFileNames){
					meFileList.add(me);
			}
			for(int i=0; i<meFileList.size(); i++){
				if(meFileList.get(i).contains(".rda" )){
					meFileList.remove(i);
					i--;
				}
			}
			
		}
		return meFileList;
	}
	
	/*
	 * 解析txt文件
	 */
	public void parser (){
//		DBCollection collection = MongoDBManager.getInstance().getCollection("generank", "generank", "generank");
		//List<String> meFileList = readMethylationFile();
		String [] cancers = {"ACC", "BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP", "LGG", "LIHC", "LAML", "LUAD", "LUSC", "OV", "PAAD", "PRAD",
				 "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
//		String [] cancers = {"STAD"};
		List<Double> column ;
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
		//for(String meFileName : meFileList){
		for(String cacerType :cancers){
			//String cacerType = meFileName.split("_")[0];
			//根据cacerType到sampleNew表找到所有的sampleId
//			SmartDBObject query = new SmartDBObject();
//			query.put("cell", "TCGA-"+cacerType.toLowerCase());
//			query.put("etype", 11);
//	    	query.addSort("sampleId", SortType.ASC);
//	    	List<Sample> sampleList = dao.find(query);
//			//拿到cacerType起始和结束的sampleId
//			int startSampleId = sampleList.get(0).getSampleId();
//			int endSampleId = sampleList.get(sampleList.size()-1).getSampleId();
//			//删除数据			
//			SmartDBObject query1 = new SmartDBObject();
//			query1.put("$gte", startSampleId);
//			query1.append("$lte", endSampleId);
//			SmartDBObject query2 = new SmartDBObject();
//			query2.put("sampleId", query1);
//			query2.put("etype", 11);
//			collection.remove(query2);
			logger.debug("CNV---{} ", cacerType);
			File file = new File(root+"GeneLevel"+File.separator+cacerType.toUpperCase()+"__genome_wide_snp_6__GeneLevelCNA.txt");
			logger.debug("currentFile：{} start--", cacerType.toUpperCase()+"__genome_wide_snp_6__GeneLevelCNA.txt");
			String [] lineStrings;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String readLine;
				List<String> barCodes = new ArrayList<String>();
				boolean flag = true;
				//第一次读取文件拿到所有barcode添加sample，拿到所有symbol
				while((readLine = reader.readLine())!=null){
					lineStrings = readLine.split("	");
					//排除空行n
					if(lineStrings.length>1){
						//读取第一行所有的TCGA-XXX-XX和sample数
						if(flag){
							for(int i=0; i<lineStrings.length; i++){
								if(lineStrings[i].contains("TCGA")){
									barCodes.add(lineStrings[i]);
								}
							}
							flag = false;
							//
							//修改
							modifySample(barCodes, cacerType);
						}
					}
				}
				
				System.out.println("CNV"+cacerType+" over");
				logger.debug("currentFile：{} is finished", cacerType.toUpperCase()+"__genome_wide_snp_6__GeneLevelCNA.txt");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 根据symbol 取所有geneid
	 */
	public Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//根据symbol找对应的refseq
			//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList ==null ||txrRefList.size()== 0){
				//txrref表找不到对应的refseq 记录下来
				geneIds.put(symbol, null);
				record(symbol,"CNV_txfreftable_cantfind");
			}else{
				boolean flag = true; 
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//根据refseq对应gene表txName字段 找geneId
						//Gene gene = geneDAO.getByName(refseq); 
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							geneIds.put(symbol, gene.getGeneId());
							flag = false;
							break;
						}
					}
				}
				if(flag){
					geneIds.put(symbol, null);
					record(symbol, "CNV_genetable_cantfind_or_txfref_nomatch");
				}
			}
		}
		return geneIds;
	}
	
	/*
	 * 记录找不到的refseq的symbol
	 */
	public void record(String symbol,String fileName){
		try {
			File file1 = new File(root+"recoder"); 
			if(!file1.exists() && !file1.isDirectory()){
				file1.mkdir();
			}
			File file = new File(root+"recoder"+File.separator+fileName+".txt");    
			FileOutputStream fos;
			fos = new FileOutputStream(file,true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);   
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(symbol+" ");   
			bw.flush();   
			bw.close();  
			osw.close();  
			fos.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	/*
	 *  数据库创建sample
	 */
	public List<Integer> modifySample(List<String> list,String cacerType){
		List<Integer> samplesIds = new ArrayList<Integer>();
		Map<String,String> urls = getURL(cacerType);
		for(String barCode : list){
			Sample sample = sampleNewdao.getByUrl(urls.get(barCode));
			if(sample == null || sample.getSampleCode().split("-").length > 3)
			{
				continue;
			}
			
			//读取description
			File file = new File(root+"PatientData"+File.separator+"nationwidechildrens.org_clinical_patient_"+cacerType.toLowerCase()+".txt");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String patientLine;
				boolean flag = true;
				String [] keys = null;
				String cell_old = "TCGA-" + cacerType.toLowerCase();
				while((patientLine = reader.readLine())!=null){
					//读取第一行时拿到所有map的key
					if(flag){
						keys = patientLine.split("	");
						flag = false;
					}
					String [] barCodeStrings = barCode.split("-");
//					String code = barCodeStrings[0]+"-"+barCodeStrings[1]+"-"+barCodeStrings[2];
					sample.setSampleCode(barCode);
					String type = barCodeStrings[3];
					int typeI = Integer.parseInt(type.substring(0, 2));
					if(typeI < 10)
					{
						sample.setCell(cell_old + "-tumor");
					}else if (typeI < 20)
					{
						sample.setCell(cell_old + "-normal");
					} else {
						sample.setCell(cell_old + "-control");
					}
//					if(patientLine.startsWith(code)){
//						String [] values = patientLine.split("	");
//						Map<String, String> map = sample.descMap();
//						for(int i=2; i<keys.length; i++){
//							map.put(keys[i], values[i]);
//						}
//						sample.descMap(map);
//					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			sampleNewdao.update(sample);
		}
		
		return samplesIds;
	}
	
	/*
	 * 读取sample Url
	 */
	public Map<String,String> getURL(String cacerType){
		Map<String,String> map = new HashMap<String, String>();
		try {
			File file = new File(root+"DownloadURL"+File.separator+cacerType.toUpperCase()+".csv");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line=br.readLine())!=null){
				String [] temp = line.split(",");
				System.out.println(temp[0]);
				if(temp[0].startsWith("\"TCGA")){
					map.put(temp[0].substring(1, temp[0].length()-1), temp[1].substring(1, temp[1].length()-1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	public static void main(String[] args) {
//		SampleCache.getInstance().init();
		new CNVMetaDataModify().parser();
	}

}
