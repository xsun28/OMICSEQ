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
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.CancerType;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.ThreadTaskPoolsExecutor;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
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
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.ThreadUtils;

public class MethylationParser extends AbstractLifeCycle {
	
		protected static String root = "/home/TCGA-Assembler/user/methylation450/";
//		protected static String root = "E:/";
		protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
		//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
		protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
		protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
		protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
		protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
		private static Logger logger = LoggerFactory.getLogger(MethylationParser.class);
		
		private String cancerType = "";
		
		MethylationParser() {
			
		}

		MethylationParser(String cancerType) {
			this.cancerType = cancerType;
		}
		
		private static MethylationParser single = new MethylationParser();
		
		public static MethylationParser getInstance() {
	        return single;
	    }
	/*
	 *  读取methylation需要解析的txt 文件
	 */
	public List<String> readMethylationFile(){
		List<String> meFileList = new ArrayList<String>();
		File mefile = new File(root+"geneLevel2");
		if(mefile.isDirectory()){
			String [] meFileNames = mefile.list();
			for(String me : meFileNames){
				meFileList.add(me);
			}
			for(int i=0; i<meFileList.size(); i++){
				if(meFileList.get(i).contains(".rda")){
					meFileList.remove(i);
					i--;
				}
			}
		}
		return meFileList;
	}
	
//	/*
//	 * 解析txt文件
//	 */
//	public void parser_old (String cancerType){
//		List<String> meFileList = readMethylationFile();
//		List<Double> column ;
//		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");  
//		for(String meFileName : meFileList){
//			String cacerType = meFileName.split("_")[0];
//			List<String> symbolList = new ArrayList<String>();
//			List<Integer> sampleIds = null;
//			File file = new File(root+"geneLevel2"+File.separator+meFileName);
//			logger.debug("当前解析文件：{}", meFileName);
//			String [] lineStrings;
//			try {
//				BufferedReader reader = new BufferedReader(new FileReader(file));
//				String readLine;
//				List<String> barCodes = new ArrayList<String>();
//				boolean flag = true;
//				//第一次读取文件拿到所有barcode添加sample，拿到所有symbol
//				while((readLine = reader.readLine())!=null){
//					lineStrings = readLine.split("	");
//					//排除空行
//					if(lineStrings.length>1){
//						//读取第一行所有的TCGA-XXX-XX和sample 数
//						if(flag){
//							for(int i=0; i<lineStrings.length; i++){
//								if(lineStrings[i].contains("TCGA")){
//									barCodes.add(lineStrings[i]);
//								}
//							}
//							flag = false;
//							//增加sample
//							sampleIds = CreateSample(barCodes, cacerType);
//						}	
//						symbolList.add(lineStrings[0]);
//					}
//				}
//				//去掉第一个"GeneSymbol"
//				symbolList.remove("GeneSymbol");
//				//取所有geneId
//				Map<String,Integer> geneIds = getGeneIde(symbolList);
//				//循环methylation文件取读数
//				for(int i=0; i<barCodes.size(); i++){
//					BufferedReader reader1 = new BufferedReader(new FileReader(file));
//					column = new ArrayList<Double>();
//					while((readLine = reader1.readLine())!=null){
//						lineStrings = readLine.split("	"); 
//						//排除空行
//						if(lineStrings.length>1){
//							if(lineStrings.length>(i+2)){
//								if(!"".equals(lineStrings[i+2]) && !lineStrings[i+2].contains("TCGA") && !lineStrings[i+2].equals("NaN")){
//									column.add(Double.parseDouble(lineStrings[i+2]));
//								}else if("".equals(lineStrings[i+2]) || "NaN".equals(lineStrings[i+2])){
//									column.add(0.0);
//								}
//							}else{
//								column.add(0.0);
//							}
//						}
//					}
//					List<SymbolReader> list = new ArrayList<SymbolReader>();
//					for(int m=0; m<symbolList.size(); m++){
//						SymbolReader sr = new SymbolReader();
//						sr.setRead(column.get(m));
//						sr.setBarCode(barCodes.get(i));
//						sr.setSymbol(symbolList.get(m));
//						list.add(sr);
//					}
//					//排序 
//					Collections.sort(list, new Comparator<SymbolReader>() {
//						@Override
//						public int compare(SymbolReader o1, SymbolReader o2) {
//							return o1.getRead().compareTo(o2.getRead()) *(-1);
//						}
//					});
//					//数据库添加geneRank
//					List<GeneRank> geneRanks = new ArrayList<GeneRank>();
//					for(SymbolReader sr: list){
//						if(geneIds.get(sr.getSymbol())!=null){
//							GeneRank gr = new GeneRank();
//							gr.setCreatedTimestamp(System.currentTimeMillis());
//							gr.setEtype(ExperimentType.METHYLATION.value());
//							gr.setSource(SourceType.TCGA.value());
//							gr.setGeneId(geneIds.get(sr.getSymbol()));
//							gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
//							gr.setTotalCount(list.size());
//							gr.setSampleId(sampleIds.get(i));
//							geneRanks.add(gr);
//						}
//					}
//					geneRankDAO.create(geneRanks);
//					logger.debug("当前文件：{}解析完成", meFileName);
//				}
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
	/*
	 * 根据symbol 取所有geneid
	 */
	public Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//根据symbol找对应的refseq
			//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList==null || txrRefList.size()== 0){
				//txrref表找不到对应的refseq 记录下来
				geneIds.put(symbol, null);
				record(symbol,"methylation_txfreftable_cantfind");
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
					record(symbol, "methylation_genetable_cantfind_or_txfref_nomatch");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	/*
	 *  数据库创建sample
	 */
	public List<Integer> CreateSample(List<String> list,String cacerType){
		List<Integer> samplesIds = new ArrayList<Integer>();
		List<Sample> samples = new ArrayList<Sample>();
		for(String barCode : list){
			int sampleId = sampleDAO.getSequenceId(SourceType.TCGA);
			samplesIds.add(sampleId);
			Sample sample = new Sample();
			sample.setSampleId(sampleId);
			sample.setDeleted(0);
			sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			String [] barCodeStrings = barCode.split("-");
			String code = barCodeStrings[0]+"-"+barCodeStrings[1]+"-"+barCodeStrings[2];
			sample.setSampleCode(barCode);
			String type = barCodeStrings[3];
			int typeI = Integer.parseInt(type.substring(0, 2));
			String tissue = CancerType.valueOf(cacerType).getName();
			if(typeI < 10)
			{
				sample.setCell("TCGA-"+cacerType.toLowerCase() + "-tumor");
				sample.setDetail(tissue + " Tumor");
			}else if (typeI < 20)
			{
				sample.setCell("TCGA-"+cacerType.toLowerCase() + "-normal");
				sample.setDetail(tissue + " Normal");
			} else {
				sample.setCell("TCGA-"+cacerType.toLowerCase() + "-control");
				sample.setDetail(tissue + " Control");
			}
			sample.setSource(SourceType.TCGA.value());
			sample.setEtype(ExperimentType.METHYLATION.value());
			//读取description
			File file = new File(root+File.separator+"PatientData" + File.separator + "nationwidechildrens.org_clinical_patient_"+cacerType.toLowerCase()+".txt");
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			samples.add(sample);
		}
		dao.create(samples);
		return samplesIds;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WebResourceInitiate.getInstance().init();
//		new MethylationParser().parser();
		
		try {
			getInstance().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
    public void start() {
        if (isStoped()) {
            super.start();
            this.run();
        }
    }
	
	private void run(){
		int threads = 8;
		
		final List<WaitFutureTask<Object>> taskList1 = new ArrayList<WaitFutureTask<Object>>(threads);
//		final List<WaitFutureTask<Object>> taskList2 = new ArrayList<WaitFutureTask<Object>>(threads);
//		final List<WaitFutureTask<Object>> taskList3 = new ArrayList<WaitFutureTask<Object>>(threads);
//		final List<WaitFutureTask<Object>> taskList4 = new ArrayList<WaitFutureTask<Object>>(threads);
//		final List<WaitFutureTask<Object>> taskList5 = new ArrayList<WaitFutureTask<Object>>(threads);
//		final List<WaitFutureTask<Object>> taskList6 = new ArrayList<WaitFutureTask<Object>>(threads);
        Semaphore semaphore = new Semaphore(threads);
        
        
//        String[] cancerTypes = {"ACC", "BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD",
//				 "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
        String[] cancerTypes = {"PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
        int i = 0;
        for(String cancerType : cancerTypes) {
//        	if(i >= threads && i < threads*2)
//        	{
//        		WaitFutureTask<Object> task2 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
//                taskList2.add(task2);
//        	}
//        	else if(i >= threads*2 && i < threads*3)
//        	{
//        		WaitFutureTask<Object> task3 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
//                taskList3.add(task3);
//        	}
//        	else if(i >= threads*3 && i < threads*4)
//        	{
//        		WaitFutureTask<Object> task4 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
//                taskList4.add(task4);
//        	}
//        	else if(i >= threads*4 && i < threads*5)
//        	{
//        		WaitFutureTask<Object> task5 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
//                taskList5.add(task5);
//        	}
//        	else if(i >= threads*5 && i < threads*6)
//        	{
//        		WaitFutureTask<Object> task6 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
//                taskList6.add(task6);
//        	}
//        	else{
//        		WaitFutureTask<Object> task1 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
//                taskList1.add(task1);
//        	}
        	WaitFutureTask<Object> task1 = new WaitFutureTask<Object>(new MethylationCallable(cancerType), semaphore);
            taskList1.add(task1);
            i++;
        }
        
        try {
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList1, 100l, TimeUnit.DAYS);
//			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList2, 100l, TimeUnit.DAYS);
//			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList3, 100l, TimeUnit.DAYS);
//			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList4, 100l, TimeUnit.DAYS);
//			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList5, 100l, TimeUnit.DAYS);
//			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList6, 100l, TimeUnit.DAYS);
		} catch (Exception e) {
            logger.error("parse {} file failed.", cancerType, e);
        }
		ThreadUtils.sleep(60 * 1000);
	}

	public static class MethylationCallable implements Callable<Object> {
        private String cancerType;
//        Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);

        public MethylationCallable(String cancerType) {
            this.cancerType = cancerType;
        }

        @Override
        public Object call() throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("download file {}", cancerType);
            }
            
            new MethylationParser().parser(cancerType);
            
            if (logger.isDebugEnabled()) {
                logger.debug("downloaded file {}", cancerType);
            }
            return true;
        }
    }
	
	public void parser(String cacerType) throws Exception{ 
		List<Double> column ;
		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");  

		List<String> symbolList = new ArrayList<String>();
		List<Integer> sampleIds = null;
		String meFileName = cacerType+"__humanmethylation450__SingleValue__TSS1500__Both.txt";
		File file = new File(root+"geneLevel2"+File.separator+meFileName);
		logger.debug("当前解析文件：{}", meFileName);
		String [] lineStrings;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String readLine;
			List<String> barCodes = new ArrayList<String>();
			boolean flag = true;
			//第一次读取文件拿到所有barcode添加sample，拿到所有symbol
			while((readLine = reader.readLine())!=null){
				lineStrings = readLine.split("	");
				//排除空行
				if(lineStrings.length>1){
					//读取第一行所有的TCGA-XXX-XX和sample 数
					if(flag){
						for(int i=0; i<lineStrings.length; i++){
							if(lineStrings[i].contains("TCGA")){
								barCodes.add(lineStrings[i]);
							}
						}
						flag = false;
						//增加sample
//						sampleIds = CreateSample(barCodes, cacerType);
					}	
					symbolList.add(lineStrings[0]);
				}
			}
			//去掉第一个"GeneSymbol"
			symbolList.remove("GeneSymbol");
			//取所有geneId
			Map<String,Integer> geneIds = getGeneIde(symbolList);
			//循环methylation文件取读数
			for(int i=0; i<barCodes.size(); i++){
				String barCode = barCodes.get(i);
				SmartDBObject query = new SmartDBObject();
				query.put("sampleCode", barCode);
				query.put("etype", 12);
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
						if(lineStrings.length>(i+2)){
							if(!"".equals(lineStrings[i+2]) && !lineStrings[i+2].contains("TCGA") && !lineStrings[i+2].equals("NaN")){
								column.add(Double.parseDouble(lineStrings[i+2]));
							}else if("".equals(lineStrings[i+2]) || "NaN".equals(lineStrings[i+2])){
								column.add(0.0);
							}
						}else{
							column.add(0.0);
						}
					}
				}
				List<SymbolReader> list = new ArrayList<SymbolReader>();
				for(int m=0; m<symbolList.size(); m++){
					SymbolReader sr = new SymbolReader();
					sr.setRead(column.get(m));
					sr.setBarCode(barCodes.get(i));
					sr.setSymbol(symbolList.get(m));
					list.add(sr);
				}
				//排序 
				Collections.sort(list, new Comparator<SymbolReader>() {
					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						return o1.getRead().compareTo(o2.getRead()) *(-1);
					}
				});
				//数据库添加geneRank
				List<GeneRank> geneRanks = new ArrayList<GeneRank>();
				for(SymbolReader sr: list){
					if(geneIds.get(sr.getSymbol())!=null){
						GeneRank gr = new GeneRank();
						gr.setCreatedTimestamp(System.currentTimeMillis());
						gr.setEtype(ExperimentType.METHYLATION.value());
						gr.setSource(SourceType.TCGA.value());
						gr.setGeneId(geneIds.get(sr.getSymbol()));
						gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
						gr.setTotalCount(list.size());
						gr.setSampleId(sampleId);
						gr.setTssTesCount(sr.getRead());
						geneRanks.add(gr);
					}
				}
//				geneRankDAO.removeBySampleId(sampleId);
				geneRankDAO.create(geneRanks);
				logger.debug("sampleId:{}", sampleId);
			}
			logger.debug("当前文件：{}解析完成", meFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			logger.debug("cancertype：{} fail", cacerType);
			e.printStackTrace();
			throw e;
		}
	
	}
}
