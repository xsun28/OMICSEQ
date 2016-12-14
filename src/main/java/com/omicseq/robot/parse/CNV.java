package com.omicseq.robot.parse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.rosuda.JRI.Rengine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CNV {
	private static Logger logger = LoggerFactory.getLogger(CNV.class);
    
    public static String s ="";
    
	private synchronized void run(Rengine re) {
		
//        String[] cancerTypes = {"BLCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "PRAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC"};
        
        String[] cancerTypes = {"GBM"};
        for(String cancerType : cancerTypes)
        {
        	try {
				boolean result = callRJava(cancerType, re);
				if(result)
				{
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	
	public static void main(String[] args) throws IOException {
		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		
		re.eval("setwd('F://TCGA-Assembler')");
		System.out.println(re.eval("getwd()").asString());
		//下载CNV数据源
//        re.eval("source('Module_A.r')"); 
        //解析下载文件
        re.eval("source('Module_B.r')");
        
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
        }
		try {
			CNV cnv = new CNV();
			cnv.run(re);
		} catch (Exception e) {
			logger.error("parse file failed.", e);
		}
		re.end();
		
//		File file = new File("/home/tomcat/omicseq/Rprocess.text");
//		if(!file.exists())
//		{
//			file.createNewFile();
//		}
//		FileWriter fw = new FileWriter(file);
//		fw.append(s);
//		fw.close();
		
		
//        getInstance().stop();
		 
	}
	
	public boolean callRJava(String cancerType, Rengine re) throws IOException {
		
		s = "ProcessCNAData(inputFilePath ='./user/"+cancerType+"__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt', " +
				  "outputFileName = '"+cancerType+"__genome_wide_snp_6__GeneLevelCNA', outputFileFolder ="+
				  "'./test/GeneLevel', refGenomeFile = './SupportingFiles/Hg19GenePosition.txt')";
		
		re.eval(s);
		
		
//        re.eval("ProcessCNAData(inputFilePath ='./user/"+cancerType+"__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt', " +
//	  "outputFileName = '"+cancerType+"__genome_wide_snp_6__GeneLevelCNV', outputFileFolder ="+
//	  "'./user/GeneLevel', refGenomeFile = './SupportingFiles/Hg19GenePosition.txt')").asString();
        
	    if (logger.isDebugEnabled()) {
	    	logger.debug("解析{}__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt完成", cancerType);
	    }
	    
	    return true;
	    
    }

}
