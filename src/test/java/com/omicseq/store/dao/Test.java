package com.omicseq.store.dao;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;

import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.MiscUtils;

public class Test extends TestBatch{
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	private static Test single = new  Test();
	private Test() {
    }
	public static Test getInstance() {
	        return single;
	 }
	
	public void createUrl(Integer geneId){
		//TODO
		//服务器ip
		String serverAdd = MiscUtils.getServerIP();
		List<Integer> values = new ArrayList<Integer>();
		values.add(2);
		values.add(3);
		SmartDBObject query = new SmartDBObject("$in",values);
		SmartDBObject query1 = new SmartDBObject();
		query1.put("geneId", geneId);
		query1.put("source", query);
		query1.addSort("mixturePerc", SortType.ASC);
		List<GeneRank> geneRanks = geneRankDAO.find(query1);
		if(geneRanks == null || geneRanks.size()==0){
			return;
		} 
		int size  = geneRanks.size()>200?200: geneRanks.size();
		for(int i = 0 ;i<size; i++){
			GeneRank gr = geneRanks.get(i);
			int sampleId = gr.getSampleId();
			StatisticInfo si= statisticInfoDAO.getBySampleId(sampleId);
			if(si !=null){
				String serverIp = si.getServerIp();
				if(serverIp.equals(serverAdd)){
					int sourceType = gr.getSource();
					List<Gene> gene = GeneCache.getInstance().getGeneById(geneId);
					String chrom = gene.get(0).getSeqName().replace("chr", "");
					int start = gene.get(0).getStart()-5000;
					int end  = gene.get(0).getStart()+5000;
					Sample sample = SampleCache.getInstance().getSampleById(sampleId);
					String [] urlll = sample.getUrl().split("/");
					String dataSource = SourceType.getUiMap().get(sample.getSource()).toString().toLowerCase();
					String url = "/files/download/"+dataSource+ "/"+ urlll[urlll.length-1];
					send(chrom,start,end,url,sampleId,geneId,sourceType);
					//拼接链接
//					String ucscUrl = "http://"+serverIp+":8080/omicseq_ucsc/rest/tracks?chrom="+chrom+"&start="+start
//							+"&end="+end+"&serverIp="+serverIp+"&geneId="+geneId+"&url="+url+"&sampleid="+sampleId
//							+"&sourceType="+sourceType+"&version=19&format=html";
//					System.out.println(ucscUrl);
				}
//		        try {
//		         	Jsoup.connect(ucscUrl).timeout(1000*180).get();
//				} catch (Exception e) {
//				}
			}
		}
	}
	
	private void send(String chr,int startpos,int endpos,String url,int sampleid,int geneId,int sourceType ){
		String version="19";
		String URL;
		String bedGraphFile=sampleid+"_chr"+chr+"_"+geneId; //302334_chr19_30236
		logger.debug("bedGraphFile is "+bedGraphFile);
		String tempdirpath=null;
		File tempDir = createTempDir();
		tempdirpath=tempDir.getAbsolutePath();
		try {
			File tempFile=new File(tempdirpath+"/"+bedGraphFile+".bedgraph");
			logger.debug("filepath: "+tempdirpath+"/"+bedGraphFile);
			if(!tempFile.exists()){
	//			File tempDebFile = new File(tempdirpath + "/"+ bedGraphFile +".bed");
				String tempDebFile = "/home/all_bed/gene_" + geneId + "_50bp_100.bed";
	//			String s = "chr"+chr+"	"+ startpos+"	"+endpos+"	\n";
	//			tempDebFile.createNewFile();
	//			FileOutputStream fo = new FileOutputStream(tempDebFile);
	//			fo.write(s.getBytes());
	//			fo.close();
				String rFile = "";
				if(sourceType == 2)
				{
					rFile = "getUCSCTrackPlot.r ";
				} else if(sourceType == 3)
				{
					rFile = "getUCSCTrackPlot_roadmap.r ";
				} else {
					logger.debug("sourceType Of " + sourceType + " UCSC view have not developed");
				}
				String cmd = " Rscript /home/TCGA-Assembler/" + rFile + tempdirpath+" chr"+chr+" "+startpos+" "+endpos+" "+url+" "+sampleid+" "+version+" " + bedGraphFile + " " + tempDebFile; 
				logger.debug("cmd: "+cmd);
				logger.debug("tempFile not existed");
				Runtime runTime = Runtime.getRuntime();
				Process process = runTime.exec(cmd);
				System.out.println("reach here3 ");
				process.waitFor();
				System.out.println("reach here4");
				process.destroy();
			}
		}catch(Exception e){
			
		}
	}
		private static File createTempDir() {
			File baseDir = new File(System.getProperty("java.io.tmpdir"));
			String baseName = "omicseq-TempStorage" + "-";

			for (int counter = 0; counter < 10000; counter++) {
				File tempDir = new File(baseDir, baseName + counter);
				if (!tempDir.exists()) {
					if (tempDir.mkdir())
					  return tempDir;
				}
				else
					return tempDir;
			}
			return null;
		}
		
	 static class ExportCallable extends BaseCallable<Object, Test> {
	        public ExportCallable(Test ref, List<Integer> geneIds) {
	            super(ref, geneIds);
	        }

	        @Override
	        public Object call() throws Exception {
	            DateTime dt = DateTime.now();
	            try {
	                ref.start();
	                for (final Integer genId : geneIds) {
	                    List<Integer> geneIds = geneCache.getGeneIds();
	                    Collections.sort(geneIds);
	                    int thread = 8;
	                    final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(geneIds);
	                    Semaphore semaphore = new Semaphore(thread);
	                    List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(thread);
	                    Callable<Object> callable = new Callable<Object>() {
	                        @Override
	                        public Object call() throws Exception {
	                            while (true) {
	                                Integer geneId = queue.poll();
	                                DateTime dt = DateTime.now();
	                                ref.createUrl(geneId);
	                                if (single.logger.isDebugEnabled()) {
	                                    single.logger.debug("生成{} Excel文件,用时:{}", geneId, DateTimeUtils.used(dt));
	                                }
	                                if (queue.isEmpty()) {
	                                    break;
	                                }
	                            }
	                            return null;
	                        }
	                    };
	                    for (int i = 0; i < thread; i++) {
	                        tasks.add(new WaitFutureTask<Object>(callable, semaphore));
	                    }
	                    ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(tasks, 10l, TimeUnit.DAYS);
	                }
	            } catch (Exception e) {
	                ref.logger.error("生成Excel文件出错:", e);
	            } finally {
	                ref.stop();
	                single.logger.debug("生成Excel用时:{}", DateTimeUtils.used(dt));
	            }
	            return Boolean.TRUE;
	        }
	    }

	public static void main(String[] args) throws MalformedURLException, IOException {
		GeneCache.getInstance().init();
		SampleCache.getInstance().init();
		Test.getInstance().refresh();;
	}
	
	@Override
	protected Callable<Object> getCallable(List<Integer> geneIds) {
		return new ExportCallable(new Test(), geneIds);
	}

}
