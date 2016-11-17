package com.omicseq.statistic.variation;

import org.rosuda.JRI.Rengine;

public class RProgramExcute {

	public static void main(String[] args) {
//		System.out.println(System.getProperty("java.library.path"));
		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		
		re.eval("setwd('/home/TCGA-Assembler')");
		re.eval("library(Rsamtools)");
		re.eval("source('code.R')");
		String[] bamfiles = re.eval("as.matrix(read.table('/home/tomcat/bamfiles.txt'))").asStringArray();
		double[][] mat = re.eval("read.table('/home/tomcat/all_variations.csv',sep=',',header=T)").asDoubleMatrix();
		String mat_gr=re.eval("GRanges(seqnames=Rle(rep('chr19',nrow("+ mat +"))),ranges = IRanges(start="+mat+"[,2],end="+mat+"[,3]))").asString();
		System.out.println(mat_gr);
		System.out.println("reached here 0");
		for(int i=0; i<bamfiles.length; i++) {
			String bam_gr = re.eval("read.BAM("+bamfiles[i]+")").asString();
			int[] count = re.eval("countOverlaps("+mat_gr+","+bam_gr+")").asIntArray();
			String fileName = "/home/tomcat/counts/" + i+1 + ".txt";
			re.eval("write.table("+count+",file="+fileName+",sep='\t',quote=F,row.names=F,col.names=F)");
		}
//		String s ="for(i in 1:length(bamfiles)){" +
//					"	bam.gr=read.BAM(bamfiles[i])" +
//					"	count=countOverlaps(mat.gr,bam.gr)" +
//					"	fileName=paste('/home/tomcat/counts/',i,'.txt',sep='')" +
//					"	write.table(count,file=fileName,sep='\t',quote=F,row.names=F,col.names=F)" +
//					"}";
//		re.eval(s);
		
		System.out.println("reached here 1");
	}

}
