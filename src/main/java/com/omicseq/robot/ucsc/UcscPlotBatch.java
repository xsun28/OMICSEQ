package com.omicseq.robot.ucsc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.IThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.MiscUtils;

public class UcscPlotBatch extends AbstractLifeCycle {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static SampleCache sampleCache = SampleCache.getInstance();
	protected static GeneCache geneCache = GeneCache.getInstance();
	private static UcscPlotBatch single = new UcscPlotBatch();
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	protected static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    protected int threads = 8;

	public UcscPlotBatch() {
		
	}
    
	public static UcscPlotBatch getInstance() {
        return single;
    }
	
	public void refresh() {
        final IThreadTaskPoolsExecutor executor = ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor();
        FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    start();
                    List<WaitFutureTask<Object>> tasks = buildTasks();
                    executor.blockRun(tasks, 100l, TimeUnit.DAYS);
                } catch (Exception e) {
                    logger.error("文件生成出错", e);
                } finally {
                    stop();
                }
                return Boolean.TRUE;
            }
        });
        executor.run(task);
    }
	
	protected List<WaitFutureTask<Object>> buildTasks() {
        List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(1);
        Semaphore semaphore = new Semaphore(3);
        int size = 1;
        int batch = size <= threads ? 1 : size % threads == 0 ? size / threads : size / threads + 1;
        for (int i = 0; i < threads; i++) {
            int fromIndex = i * batch;
            int toIndex = fromIndex + batch;
            toIndex = size < toIndex ? size : toIndex;
            WaitFutureTask<Object> task = new WaitFutureTask<Object>(getCallable(), semaphore);
            tasks.add(task);
            if (toIndex >= size) {
                break;
            }
        }
        return tasks;
    }
    
    protected Callable<Object> getCallable() {
        return new UcscPlotBatchCallable(new UcscPlotBatch());
    }
	
	static abstract class BaseCallable<T, R> implements Callable<T> {
        protected R ref;

        public BaseCallable(R ref) {
            this.ref = ref;
        }
    }

	static class UcscPlotBatchCallable extends BaseCallable<Object, UcscPlotBatch> {
        public UcscPlotBatchCallable(UcscPlotBatch ref) {
        	super(ref);
        }

        @Override
        public Object call() throws Exception {
            DateTime dt = DateTime.now();
            try {
                ref.start();
                
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
                            if(geneId < 660)
                            {
                            	continue;
                            }
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
            } catch (Exception e) {
                ref.logger.error("生成Excel文件出错:", e);
            } finally {
                ref.stop();
                single.logger.debug("生成Excel用时:{}", DateTimeUtils.used(dt));
            }
            return Boolean.TRUE;
        }
    }


	public void createUrl(Integer geneId) {
		String serverAdd = MiscUtils.getServerIP();
		List<Integer> values = new ArrayList<Integer>();
		values.add(2);
		values.add(3);
		SmartDBObject query = new SmartDBObject("$in", values);
		SmartDBObject query1 = new SmartDBObject();
		query1.put("geneId", geneId);
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
			List<Gene> gene = GeneCache.getInstance().getGeneById(geneId);
			if(gene != null && gene.size() > 0)
            {
	            String chrom = gene.get(0).getSeqName()
						.replace("chr", "");
				String fileName =  sampleId +"_chr" +chrom + "_" +  geneId +".bedgraph";
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
							send(chrom, start, end, url, sampleId, geneId,
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
							send(chrom, start, end, url, sampleId, geneId,
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
}
