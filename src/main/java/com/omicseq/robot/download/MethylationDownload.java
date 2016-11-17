package com.omicseq.robot.download;

import java.util.Vector;

import org.rosuda.JRI.Rengine;

@SuppressWarnings("rawtypes")
public class MethylationDownload implements Runnable {

	private Vector queueData = null;  
    private boolean run = true;
    private static Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
    
    public MethylationDownload() {
    	queueData = new Vector();
    }
    
	public static void main(String[] args) {
		
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
        
//        String[] cancerTypes = {"BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "PRAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
        String[] cancerTypes = {"PRAD"};
        MethylationDownload m = new MethylationDownload();
        Thread processThread = new Thread(m);  
        processThread.start();
        
        for(String cancerType : cancerTypes)
        {
        	m.putEvent(cancerType);
        }
        
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void putEvent(String cancerType) {  
        queueData.addElement(cancerType);  
        notify();  
    }
	
	private synchronized String getEvent() {  
        try {  
            return queueData.remove(0).toString();  
        } catch (ArrayIndexOutOfBoundsException aEx) {  
        }  
        try {  
            wait();  
        } catch (InterruptedException e) {  
            if (run) {  
                return null;  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }

	@Override
	public void run() {
		while (run) {
			String cancerType = getEvent();
			if(cancerType == null)
			{
				break;
			}
			String s = "DownloadMethylationData(traverseResultFile = './DirectoryTraverseResult_Jan-30-2014.rda', saveFolderName = './user/methylation450/'," +
	        		  "cancerType = '" + cancerType + "',assayPlatform = 'humanmethylation450')";
			re.eval(s);
			System.out.println(cancerType);
		}
	}
	
	public synchronized void destroy() {  
        run = false;  
        queueData = null;  
        notify();  
    }

}
