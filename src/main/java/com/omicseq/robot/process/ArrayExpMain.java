package com.omicseq.robot.process;

import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;

public class ArrayExpMain implements Runnable{
	int threadNum;
	ArrayExpressP ae;
	
	public ArrayExpMain(int threadNum , ArrayExpressP aep){
		this.threadNum = threadNum;
		this.ae = aep;
	}
	
	@Override
	public void run() {
		while(ae.fileList.size()>0){
			ae.parser(this);
		}
	}

	public static void main(String[] args) {
		GeneCache.getInstance().doInit();
		TxrRefCache.getInstance().doInit();
		SampleCache.getInstance().doInit();
		ArrayExpressP aep = new ArrayExpressP();
		for(int i=1;i<5;i++){
			new Thread(new ArrayExpMain(i, aep)).start();
		}
	}
}
