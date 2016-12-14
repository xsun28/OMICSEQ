package com.omicseq.robot.parse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.ThreadTaskPoolsExecutor;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.utils.ThreadUtils;

public class CNVParser extends AbstractLifeCycle {
	private static Logger logger = LoggerFactory.getLogger(CNVParser.class);
	private static CNVParser single = new CNVParser();
    private int maxThreads = 5;
    
    public static String s ="";
    
//    public static Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
    
    private String cancerType = "";
    
    public static CNVParser getInstance() {
        return single;
    }
    
    CNVParser(){
    	
    }
    
    CNVParser(String cancerType){
    	this.cancerType = cancerType;
    }
	
	@Override
    public void start() {
        if (isStoped()) {
            super.start();
            this.run();
        }
    }

	private void run() {
		int threads = maxThreads;
		
		final List<WaitFutureTask<Object>> taskList1 = new ArrayList<WaitFutureTask<Object>>(threads);
		final List<WaitFutureTask<Object>> taskList2 = new ArrayList<WaitFutureTask<Object>>(threads);
		final List<WaitFutureTask<Object>> taskList3 = new ArrayList<WaitFutureTask<Object>>(threads);
		final List<WaitFutureTask<Object>> taskList4 = new ArrayList<WaitFutureTask<Object>>(threads);
		final List<WaitFutureTask<Object>> taskList5 = new ArrayList<WaitFutureTask<Object>>(threads);
		final List<WaitFutureTask<Object>> taskList6 = new ArrayList<WaitFutureTask<Object>>(threads);
        Semaphore semaphore = new Semaphore(threads);
        
        
        String[] cancerTypes = {"ACC", "BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "PRAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
        int i = 0;
        for(String cancerType : cancerTypes)
        {
        	if(i >= threads && i < threads*2)
        	{
        		WaitFutureTask<Object> task2 = new WaitFutureTask<Object>(new CNVParserCallable(cancerType), semaphore);
                taskList2.add(task2);
        	}
        	else if(i >= threads*2 && i < threads*3)
        	{
        		WaitFutureTask<Object> task3 = new WaitFutureTask<Object>(new CNVParserCallable(cancerType), semaphore);
                taskList3.add(task3);
        	}
        	else if(i >= threads*3 && i < threads*4)
        	{
        		WaitFutureTask<Object> task4 = new WaitFutureTask<Object>(new CNVParserCallable(cancerType), semaphore);
                taskList4.add(task4);
        	}
        	else if(i >= threads*4 && i < threads*5)
        	{
        		WaitFutureTask<Object> task5 = new WaitFutureTask<Object>(new CNVParserCallable(cancerType), semaphore);
                taskList5.add(task5);
        	}
        	else if(i >= threads*5 && i < threads*6)
        	{
        		WaitFutureTask<Object> task6 = new WaitFutureTask<Object>(new CNVParserCallable(cancerType), semaphore);
                taskList6.add(task6);
        	}else{
        		WaitFutureTask<Object> task1 = new WaitFutureTask<Object>(new CNVParserCallable(cancerType), semaphore);
                taskList1.add(task1);
        	}
            i++;
        }
        try {
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList1, 100l, TimeUnit.DAYS);
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList2, 100l, TimeUnit.DAYS);
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList3, 100l, TimeUnit.DAYS);
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList4, 100l, TimeUnit.DAYS);
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList5, 100l, TimeUnit.DAYS);
			ThreadTaskPoolsExecutor.getInstance().blockRun(taskList6, 100l, TimeUnit.DAYS);
		} catch (Exception e) {
            logger.error("parse {} file failed.", cancerType, e);
        }
		ThreadUtils.sleep(60 * 1000);
	}
	
	public static class CNVParserCallable implements Callable<Object> {
        private String cancerType;
//        Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);

        public CNVParserCallable(String cancerType) {
            this.cancerType = cancerType;
        }

        @Override
        public Object call() throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("download file {}", cancerType);
            }
            
            new CNVParser().callRJava(cancerType);
            
            if (logger.isDebugEnabled()) {
                logger.debug("downloaded file {}", cancerType);
            }
            return true;
        }
    }

	public static void main(String[] args) throws IOException {
        
		try {
			getInstance().start();
		} catch (Exception e) {
			logger.error("parse file failed.", e);
		}
		
		File file = new File("/home/tomcat/omicseq/Rprocess.text");
		if(!file.exists())
		{
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file);
		fw.append(s);
		fw.close();
		
		
//        getInstance().stop();
		 
	}
	
	public void callRJava(String cancerType) throws IOException {
//		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		
//		re.eval("setwd('/home/TCGA-Assembler')");
//      System.out.println(re.eval("getwd()").asString());
		//下载CNV数据源
//        re.eval("source('Module_A.r')"); 
        //解析下载文件
//        re.eval("source('Module_B.r')");
        
//        if (!re.waitForR()) {
//            System.out.println("Cannot load R");
//        }
		s += "ProcessCNAData(inputFilePath ='./user/"+cancerType+"__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt', " +
				  "outputFileName = '"+cancerType+"__genome_wide_snp_6__GeneLevelCNA', outputFileFolder ="+
				  "'./user/GeneLevel', refGenomeFile = './SupportingFiles/Hg19GenePosition.txt')" + "\n";
		
//		re.eval(s);
//		re.end();
		
//        re.eval("ProcessCNAData(inputFilePath ='./user/"+cancerType+"__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt', " +
//	  "outputFileName = '"+cancerType+"__genome_wide_snp_6__GeneLevelCNV', outputFileFolder ="+
//	  "'./user/GeneLevel', refGenomeFile = './SupportingFiles/Hg19GenePosition.txt')").asString();
        
	    if (logger.isDebugEnabled()) {
	    	logger.debug("解析{}__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt完成", cancerType);
	    }
	    
    }

}
