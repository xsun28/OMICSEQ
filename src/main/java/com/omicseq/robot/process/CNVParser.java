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

import com.mongodb.DBCollection;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
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
public class CNVParser {
	
	protected static String root = "/home/TCGA-Assembler/user/";
//	protected static String root = "E:/";
	protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static Logger logger = LoggerFactory.getLogger(CNVParser.class);

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
		String [] cancers = {"BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "PRAD",
				 "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
//		String [] cancers = {"ACC"};
		List<Double> column ;
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
		//for(String meFileName : meFileList){
		for(String cacerType :cancers){
			//String cacerType = meFileName.split("_")[0];
			//根据cacerType到sampleNew表找到所有的sampleId
//			SmartDBObject query = new SmartDBObject();
//			query.put("cell", new SmartDBObject("$regex", "TCGA-"+cacerType.toLowerCase()));
//			query.put("etype", 11);
//	    	query.addSort("sampleId", SortType.ASC);
//	    	List<Sample> sampleList = dao.find(query);
			//拿到cacerType起始和结束的sampleId
//			int startSampleId = sampleList.get(0).getSampleId();
//			int endSampleId = sampleList.get(sampleList.size()-1).getSampleId();
			//删除数据			
//			SmartDBObject query1 = new SmartDBObject();
//			query1.put("$gte", startSampleId);
//			query1.append("$lte", endSampleId);
//			SmartDBObject query2 = new SmartDBObject();
//			query2.put("sampleId", query1);
//			query2.put("etype", 11);
//			collection.remove(query2);
//			logger.debug("CNV---{} removed form geneRank", cacerType);
			List<String> symbolList = new ArrayList<String>();
//			List<Integer> sampleIds = null;
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
//							增加sample
//							sampleIds = CreateSample(barCodes, cacerType);
						}	
						symbolList.add(lineStrings[0]);
					}
				}
				//去掉第一个"GeneSymbol"
				symbolList.remove("GeneSymbol");
				//取所有geneId
				Map<String,Integer> geneIds = getGeneIde(symbolList);
				//循环GeneLevel文件取读数
				for(int i=0; i<barCodes.size(); i++){
					String barCode = barCodes.get(i);
					SmartDBObject query = new SmartDBObject();
					query.put("sampleCode", barCode);
					query.put("etype", 11);
					Sample sample = dao.findOne(query);
					if(sample == null)
					{
						continue;
					}
					Integer sampleId = sample.getSampleId();
					
					BufferedReader reader1 = new BufferedReader(new FileReader(file));
					column = new ArrayList<Double>();
					while((readLine = reader1.readLine())!=null){
						lineStrings = readLine.split("	"); 
						//排除空行
						if(lineStrings.length>1){
							if(lineStrings.length>(i+3)){
								if(!"".equals(lineStrings[i+3]) && !lineStrings[i+3].contains("TCGA") && !lineStrings[i+3].equals("NaN")){
									column.add(Double.parseDouble(lineStrings[i+3]));
								}else if("".equals(lineStrings[i+3]) || "NaN".equals(lineStrings[i+3])){
									column.add(0.0);
								}
							}else{
								column.add(0.0);
							}
						}
					}
					List<SymbolReader> list = new ArrayList<SymbolReader>();
					//
					for(int m=0; m<symbolList.size(); m++){
						SymbolReader sr = new SymbolReader();
						sr.setRead(column.get(m));
						sr.setBarCode(barCodes.get(i));
						sr.setSymbol(symbolList.get(m));
						list.add(sr);
					}
					//去掉无效数据（symbol对应不到gene的数据）
					for(int k=0; k<list.size(); k++){
						if(geneIds.get(list.get(k).getSymbol())==null){
							list.remove(k);
							k--;
						}
					}
					//排序 
					Collections.sort(list, new Comparator<SymbolReader>() {
						@Override
						public int compare(SymbolReader o1, SymbolReader o2) {
							//return o1.getRead().compareTo(o2.getRead()) *(-1);
							return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
						}
					});
					//数据库添加geneRank
					List<GeneRank> geneRanks = new ArrayList<GeneRank>();
					for(SymbolReader sr: list){
						if(geneIds.get(sr.getSymbol())!=null){
							GeneRank gr = new GeneRank();
							gr.setCreatedTimestamp(System.currentTimeMillis());
							gr.setEtype(ExperimentType.CVN.value());
							gr.setSource(SourceType.TCGA.value());
							gr.setGeneId(geneIds.get(sr.getSymbol()));
							gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
							//Tsstescount读数
							gr.setTssTesCount(sr.getRead());
							gr.setTotalCount(list.size());
							//gr.setSampleId(sampleIds.get(i));
							gr.setSampleId(sampleId);
							geneRanks.add(gr);
						}
					}
					geneRankDAO.removeBySampleId(sampleId);
					geneRankDAO.create(geneRanks);
					logger.debug("sampleId:{}", sampleId);
//					System.out.println("currentSampleId="+startSampleId+"--barcode="+barCodes.get(i)+"--is finished--");
//					startSampleId++;
				}
//				System.out.println("CNV"+cacerType+"last sampleId is "+(startSampleId-1) );
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
//			File file1 = new File("F:/"); 
			if(!file1.exists() && !file1.isDirectory()){
				file1.mkdir();
			}
			File file = new File(root+"recoder"+File.separator+fileName+".txt");    
			FileOutputStream fos;
			fos = new FileOutputStream(file,true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);   
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(symbol);   
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
	public List<Integer> CreateSample(List<String> list,String cacerType){
		List<Integer> samplesIds = new ArrayList<Integer>();
		List<Sample> samples = new ArrayList<Sample>();
		Map<String,String> urls = getURL(cacerType);
		for(String barCode : list){
			int sampleId = sampleDAO.getSequenceId(SourceType.TCGA);
			samplesIds.add(sampleId);
			Sample sample = new Sample();
			sample.setSampleId(sampleId);
			sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			sample.setCell("TCGA-"+cacerType.toLowerCase());
			sample.setSource(SourceType.TCGA.value());
			sample.setEtype(ExperimentType.CVN.value());
			sample.setUrl(urls.get(barCode));
			sample.setDeleted(0);
			//读取description
			File file = new File(root+"PatientData"+File.separator+"nationwidechildrens.org_clinical_patient_"+cacerType.toLowerCase()+".txt");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String patientLine;
				boolean flag = true;
				String [] keys = null;
				while((patientLine = reader.readLine())!=null){
					//读取第一行时拿到所有map的key
					if(flag){
						keys = patientLine.split("	");
						flag = false;
					}
					String [] barCodeStrings = barCode.split("-");
					String code = barCodeStrings[0]+"-"+barCodeStrings[1]+"-"+barCodeStrings[2];
					if(patientLine.startsWith(code)){
						String [] values = patientLine.split("	");
						Map<String, String> map = sample.descMap();
						for(int i=2; i<keys.length; i++){
							map.put(keys[i], values[i]);
						}
						sample.descMap(map);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			samples.add(sample);
		}
		dao.create(samples);
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
		WebResourceInitiate.getInstance().init();
		new CNVParser().parser();
//		TxrRefCache.getInstance().doInit();
//		GeneCache.getInstance().doInit();
//		List<String> symbolList = new ArrayList<String>();
//		symbolList.add("ERG");
//		CNVParser c = new CNVParser();
//		Map<String, Integer> map = c.getGeneIde(symbolList);
//		System.out.println(map.size() + " " + map.get("ERG"));
	}

}
