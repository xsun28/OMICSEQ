package com.omicseq.robot.process;

import org.rosuda.JRI.Rengine;

public class DownLoadURL {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String [] cacerTypes = {/*"ACC","BLCA","BRCA","CESC","COAD","DLBC","ESCA","GBM","HNSC","KICH","KIRC",*/"KIRP","LAML","LGG","LIHC",
				"LUAD","LUSC","OV","PAAD","PRAD","READ","SARC","SKCM","STAD","THCA","UCEC","UCS"};

		//for(int i =1;i<cacerTypes.length+1;i++){
			Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
			re.eval("setwd('F://TCGA-Assembler')");
			re.eval("source('getCNAurl.R');");
			String a = "GetCNAUrl('"+"UCS"+"');";
			re.eval(a);
			System.out.println("UCS");
		//}
	}
}
