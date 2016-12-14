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
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.ThreadUtils;

public class MethylationMetaDataModify extends AbstractLifeCycle {
	
//		protected static String root = "/home/TCGA-Assembler/user/methylation450/";
	protected static String root = "F:\\TCGA-assembler\\user\\methylation450\\";
		protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
		//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
		protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
		protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
		protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
		protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
		private static Logger logger = LoggerFactory.getLogger(MethylationMetaDataModify.class);
		
		private String cancerType = "";
		
		MethylationMetaDataModify() {
			
		}

		MethylationMetaDataModify(String cancerType) {
			this.cancerType = cancerType;
		}
		
		private static MethylationMetaDataModify single = new MethylationMetaDataModify();
		
		public static MethylationMetaDataModify getInstance() {
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
	 * 读取sample Url
	 */
	public Map<String,String> getURL(String cacerType){
		Map<String,String> map = new HashMap<String, String>();
		try {
			File file = new File(root+"methylationURL"+File.separator+cacerType.toUpperCase()+"_URL_methylation.csv");
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
	
	/*
	 *  数据库创建sample
	 */
	public List<Integer> modifySample(List<String> list,String cacerType){
		List<Integer> samplesIds = new ArrayList<Integer>();
		Map<String,String> urls = getURL(cacerType);
		for(String barCode : list){
			Sample sample = dao.getByUrl(urls.get(barCode));
			if(sample == null || sample.getSampleCode() != null)
			{
				continue;
			}	
			//读取description
			File file = new File(root+File.separator+"PatientData" + File.separator + "nationwidechildrens.org_clinical_patient_"+cacerType.toLowerCase()+".txt");
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
					String code = barCodeStrings[0]+"-"+barCodeStrings[1]+"-"+barCodeStrings[2];
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
					if(patientLine.startsWith(code) && sample.getDescription() == null){
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
			dao.update(sample);
		}
		return samplesIds;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		WebResourceInitiate.getInstance().init();
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
        
//        String[] cancerTypes = {"ACC", "BLCA", "BRCA", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP", "LGG", "LIHC", "LAML", "LUAD", "LUSC", "OV", "PAAD", "PRAD",
//				 "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		String[] cancerTypes = {"PRAD"};
        for(String cacerType :cancerTypes){
        	try {
				this.parser(cacerType);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        
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
            
            new MethylationMetaDataModify().parser(cancerType);
            
            if (logger.isDebugEnabled()) {
                logger.debug("downloaded file {}", cancerType);
            }
            return true;
        }
    }
	
	public void parser(String cacerType) throws Exception{ 
		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");  


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
						modifySample(barCodes, cacerType);
					}
				}
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
