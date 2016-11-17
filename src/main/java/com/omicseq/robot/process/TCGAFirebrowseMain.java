package com.omicseq.robot.process;

import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;

public class TCGAFirebrowseMain implements Runnable{

	int threadNum;
	TCGAFirebrowseProcess ae;
	
	public TCGAFirebrowseMain(int threadNum , TCGAFirebrowseProcess aep){
		this.threadNum = threadNum;
		this.ae = aep;
	}
	
	@Override
	public void run() {
		while(ae.flag){
			ae.parser();
		}
	}

	public static void main(String[] args) {
		GeneCache.getInstance().doInit();
		TxrRefCache.getInstance().doInit();
		SampleCache.getInstance().doInit();
//		String root ="D:/ArrayExpress/firebrowse-data/";
		String root = "/files/download/tcgafirebrowse/";
		TCGAFirebrowseProcess aep = new TCGAFirebrowseProcess(root);
		for(int i=1;i<5;i++){
			new Thread(new TCGAFirebrowseMain(i, aep)).start();
		}
	}
}
