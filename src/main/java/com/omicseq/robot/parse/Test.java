package com.omicseq.robot.parse;

import java.io.UnsupportedEncodingException;

import org.rosuda.JRI.Rengine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test{
	private static Logger logger = LoggerFactory.getLogger(Test.class);
	
	public static String s ="";

	private void run(Rengine re) {
//        String[] cancerTypes = {"BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "PRAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
        String[] cancerTypes = {"BLCA"};
        for(String cancerType : cancerTypes)
        {
        	boolean result = callRJava(cancerType, re);
			if(result)
			{
				continue;
			}
        }
        if (logger.isDebugEnabled()) {
            logger.debug("download methlation over");
        }
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		re.eval("setwd('F://TCGA-assembler')");
		//下载 数据源
        re.eval("source('Module_A.r')");
        
        System.out.println(re.eval("getwd()").asString());
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
        }
        //打印变量
        String version = re.eval("R.version.string").asString();
        System.out.println(version);
		
        try {
			Test test = new Test();
			test.run(re);
		} catch (Exception e) {
			logger.debug("download methlation fail");
			e.printStackTrace();
		}
        re.end(); 
	}
	
	public boolean callRJava(String cancerType, Rengine re) {
		
		if (logger.isDebugEnabled()) {
            logger.debug("download file {}", cancerType);
        }
        s = "DownloadMethylationData(traverseResultFile = './DirectoryTraverseResult_Jan-30-2014.rda', saveFolderName = './user/methylation450/'," +
        		  "cancerType = '" + cancerType + "',assayPlatform = 'humanmethylation450')";
        System.out.println(s);
        re.eval(s);

        //解析下载文件
//        re.eval("source('Module_B.r')");
		
//		String s = "ProcessCNAData(inputFilePath ='./user/"+cancerType+"__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt', " +
//				  "outputFileName = '"+cancerType+"__genome_wide_snp_6__GeneLevelCNV', outputFileFolder ="+
//				  "'./user/GeneLevel', refGenomeFile = './SupportingFiles/Hg19GenePosition.txt')";
//		System.out.println(s);
//		re.eval(s);
		
		
//        re.eval("ProcessCNAData(inputFilePath ='./test/"+cancerType+"__broad.mit.edu__genome_wide_snp_6__hg19__Jan-30-2014.txt', " +
//  "outputFileName = '"+cancerType+"__genome_wide_snp_6__GeneLevelCNV', outputFileFolder ="+
//  "'./test/GeneLevel', refGenomeFile = './SupportingFiles/Hg19GenePosition.txt')");
	    
	    return true;
    }

}
