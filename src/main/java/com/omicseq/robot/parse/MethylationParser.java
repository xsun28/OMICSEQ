package com.omicseq.robot.parse;

import java.io.File;
import java.io.IOException;

import org.rosuda.JRI.Rengine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethylationParser extends Thread{
	private static Logger logger = LoggerFactory.getLogger(MethylationParser.class);
    
    public static String s ="";
    
    public static String stepTow = "";
    
    public static Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
    
	public void run() {
		
//        String[] cancerTypes = {"ACC","BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "PAAD", "PRAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
        
        String[] cancerTypes = {"PRAD"};
        for(String cancerType : cancerTypes)
        {
        	try {
				boolean result = callRJava(cancerType, re);
				System.out.println(result);
				boolean flag = true;
				int i = 1;
				while(flag)
				{
					File file = new File("/home/TCGA-Assembler/user/methylation450/geneLevel2/" + cancerType + "__humanmethylation450__SingleValue__TSS1500__Both.txt");
					if(!file.exists()) {
						sleep(1000*60*60*24);
						logger.debug("cancerType {} thread sleep 60 minits, time{}", cancerType, i);
					} else {
						flag = false;
					}
					i++;
				}
				if(!result)
				{
					logger.debug("{} fail", cancerType);
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	
	public static void main(String[] args) throws IOException {
		re.eval("setwd('/home/TCGA-Assembler')");
		System.out.println(re.eval("getwd()").asString());
        //解析下载文件
        re.eval("source('Module_B.r')");
        re.eval("source('my_B.r')");
        re.eval("source('CalculateMethlationDataResult.R')");
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
        }
		try {
			MethylationParser cnv = new MethylationParser();
			cnv.start();
		} catch (Exception e) {
			logger.error("parse file failed.", e);
		}
//		re.end();
		
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
	
	public boolean callRJava(String cancerType, Rengine re) throws Exception {
		
//		s = "Methylation450Data=ProcessMethylation450Data(inputFilePath ='./user/methylation450/OV__jhu-usc.edu__humanmethylation450__Jan-30-2014.txt', " +
//				  "outputFileName = 'OV__humanmethylation450', outputFileFolder ="+
//				  "'./user/methylation450/processedMethy450/')";
//		
//		re.eval(s);
//		
//		Thread.sleep(1000*60*60*10);
		
//		stepTow = "CalculateSingleValueMethylationData.tianlei(input=Methylation450Data, regionOption = 'TSS1500'," + 
//	"DHSOption = 'Both',outputFileName ='OV__humanmethylation450__SingleValue'," + 
//	"outputFileFolder ='./user/methylation450/geneLevel2', chipAnnotationFile ='./SupportingFiles/MethylationChipAnnotation.rda')";
		
		re.eval("CalculateMethlationDataResult('"+ cancerType +"')");
		
	    if (logger.isDebugEnabled()) {
	    	logger.debug("process cancerType {}", cancerType);
	    }
	    
	    if(!re.waitForR())
	    {
	    	return false;
	    }
	    
	    return true;
	    
    }

}
