package com.omicseq.robot.ucsc;

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

import com.omicseq.common.ExperimentType;
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
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.MiscUtils;

public class UcscPlot extends BaseUcscPlot {
	
	private static UcscPlot single = new UcscPlot();
	
	private UcscPlot() {
		
	}
	
	public static UcscPlot getInstance() {
        return single;
    }


	public static class UCSCPlotCallable extends BaseCallable<Object, UcscPlot> {

		public UCSCPlotCallable(UcscPlot ref, List<GeneRankCriteria> criteries){
			super(ref, criteries);
		}

		@Override
		public Object call() {
			DateTime dt = DateTime.now();
			try {
				ref.start();
				for(final GeneRankCriteria criteria : criteries)
				{
					List<Integer> geneIds = geneCache.getGeneIds();
                    Collections.sort(geneIds);
					int thread = 4;
					final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(geneIds);
					Semaphore semaphore = new Semaphore(thread);
					List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(thread);
					Callable<Object> callable = new Callable<Object>() {
					    @Override
					    public Object call() throws Exception {
					        while (true) {
					            Integer geneId = queue.poll();
					            if(geneId < 43)
					            {
					            	continue;
					            }
					            criteria.setGeneId(geneId);
					            ref.createUrl(criteria);
					            DateTime dt = DateTime.now();
					            single.logger.debug("generated{} bedgraph file,used time:{}", geneId, DateTimeUtils.used(dt));
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
				e.printStackTrace();
			}finally {
                ref.stop();
                single.logger.debug("over");
                single.logger.debug("generated bedgraph used time:{}", DateTimeUtils.used(dt));
            }
			return Boolean.TRUE;
		}

	}

	public void createUrl(GeneRankCriteria criteria) {
		String serverAdd = MiscUtils.getServerIP();
		List<Integer> values = new ArrayList<Integer>();
		values.add(2);
		values.add(3);
		SmartDBObject query = new SmartDBObject("$in", values);
		SmartDBObject query1 = new SmartDBObject();
		query1.put("geneId", criteria.getGeneId());
		query1.put("source", query);
		query1.put("etype", ExperimentType.CHIP_SEQ_TF.getValue());
		query1.addSort("mixturePerc", SortType.ASC);
		List<GeneRank> geneRanks = geneRankDAO.find(query1, 0, 200);
		if (geneRanks == null || geneRanks.size() == 0) {
			return;
		}
		
		for (int i = 0; i < geneRanks.size(); i++) {
			GeneRank gr = geneRanks.get(i);
			int sampleId = gr.getSampleId();
			List<Gene> gene = GeneCache.getInstance().getGeneById(criteria.getGeneId());
			if(gene != null && gene.size() > 0)
            {
	            String chrom = gene.get(0).getSeqName()
						.replace("chr", "");
				String fileName =  sampleId +"_chr" +chrom + "_" +  criteria.getGeneId() +".bedgraph";
				File file = new File("/opt/tomcat7/temp/omicseq-TempStorage-0/"+fileName);
				
				StatisticInfo si = statisticInfoDAO.getBySampleId(sampleId);
				if (si != null) {
					String serverIp = si.getServerIp();
					if (serverIp.equals(serverAdd)) {
						int sourceType = gr.getSource();
						String strand = gene.get(0).getStrand();
			            if("+".equals(strand))
			            {
			            	if(file.exists())
							{
								continue;
							}
			            	int start = gene.get(0).getStart() - 5000;
							int end = gene.get(0).getStart() + 5000;
							Sample sample = SampleCache.getInstance()
									.getSampleById(sampleId);
							String[] urlll = sample.getUrl().split("/");
							String dataSource = SourceType.getUiMap()
									.get(sample.getSource()).toString()
									.toLowerCase();
							String url = "/files/download/" + dataSource + "/"
									+ urlll[urlll.length - 1];
							send(chrom, start, end, url, sampleId, criteria.getGeneId(),
									sourceType);
			            } else {
			            	if(file.exists())
							{
								file.delete();
							}
			            	int start = gene.get(0).getEnd() - 5000;
							int end = gene.get(0).getEnd() + 5000;
							Sample sample = SampleCache.getInstance()
									.getSampleById(sampleId);
							String[] urlll = sample.getUrl().split("/");
							String dataSource = SourceType.getUiMap()
									.get(sample.getSource()).toString()
									.toLowerCase();
							String url = "/files/download/" + dataSource + "/"
									+ urlll[urlll.length - 1];
							send(chrom, start, end, url, sampleId, criteria.getGeneId(),
									sourceType);
			            }
					}

				}
            }
		}
	
	}

	private void send(String chr, int startpos, int endpos, String url,
			int sampleid, int geneId, int sourceType) {
		String version = "19";
		String URL;
		String bedGraphFile = sampleid + "_chr" + chr + "_" + geneId; // 302334_chr19_30236
		logger.debug("bedGraphFile is " + bedGraphFile);
		String tempdirpath = null;
		File tempDir = createTempDir();
		tempdirpath = tempDir.getAbsolutePath();
		try {
			File tempFile = new File(tempdirpath + "/" + bedGraphFile
					+ ".bedgraph");
			logger.debug("filepath: " + tempdirpath + "/" + bedGraphFile);
			if (!tempFile.exists()) {
				// File tempDebFile = new File(tempdirpath + "/"+ bedGraphFile
				// +".bed");
				String tempDebFile = "/home/all_bed/gene_" + geneId
						+ "_50bp_100.bed";
				// String s = "chr"+chr+"	"+ startpos+"	"+endpos+"	\n";
				// tempDebFile.createNewFile();
				// FileOutputStream fo = new FileOutputStream(tempDebFile);
				// fo.write(s.getBytes());
				// fo.close();
				String rFile = "";
				if (sourceType == 2) {
					rFile = "getUCSCTrackPlot.r ";
				} else if (sourceType == 3) {
					rFile = "getUCSCTrackPlot_roadmap.r ";
				} else {
					logger.debug("sourceType Of " + sourceType
							+ " UCSC view have not developed");
				}
				String cmd = " Rscript /home/TCGA-Assembler/" + rFile
						+ tempdirpath + " chr" + chr + " " + startpos + " "
						+ endpos + " " + url + " " + sampleid + " " + version
						+ " " + bedGraphFile + " " + tempDebFile;
				logger.debug("cmd: " + cmd);
				Runtime runTime = Runtime.getRuntime();
				Process process = runTime.exec(cmd);
				System.out.println("reach here3 ");
				process.waitFor();
				System.out.println("reach here4");
				process.destroy();
			}
		} catch (Exception e) {

		}
	}

	private static File createTempDir() {
		// File baseDir = new File(System.getProperty("java.io.tmpdir"));
		File baseDir = new File("/opt/tomcat7/temp");
		String baseName = "omicseq-TempStorage" + "-";

		for (int counter = 0; counter < 10000; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (!tempDir.exists()) {
				if (tempDir.mkdir())
					return tempDir;
			} else
				return tempDir;
		}
		return null;
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		GeneCache.getInstance().init();
		SampleCache.getInstance().init();
		single.refresh();
	}


	@Override
	protected Callable<Object> getCallable(List<GeneRankCriteria> criteries) {
		return new UCSCPlotCallable(new UcscPlot(), criteries);
	}
}
