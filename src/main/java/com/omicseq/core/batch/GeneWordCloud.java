package com.omicseq.core.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.PropertiesHolder;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.utils.DateTimeUtils;

public class GeneWordCloud extends BaseGeneRankBatch {
    private static GeneWordCloud single = new GeneWordCloud();
    
    private GeneWordCloud() {
    	
    }

    static class WordCloudCallable extends BaseCallable<Object, GeneWordCloud> {
        public WordCloudCallable(GeneWordCloud ref, List<GeneRankCriteria> criteries) {
            super(ref, criteries);
        }

        @Override
        public Object call() throws Exception {
            DateTime dt = DateTime.now();
            try {
                ref.start();
                for (final GeneRankCriteria criteria : criteries) {
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
                                criteria.setGeneId(geneId);
                                DateTime dt = DateTime.now();
                                criteria.setMixturePerc(Double.valueOf(0.01));
                                ref.export(criteria);
//                                ref.exportCSV_cell(criteria);
//                                ref.exportCSV_factor(criteria);
                                if (single.logger.isDebugEnabled()) {
                                    single.logger.debug("generate {} csv,used time:{}", geneId, DateTimeUtils.used(dt));
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
                ref.logger.error("生成图片文件出错:", e);
            } finally {
                ref.stop();
                single.logger.debug("generate Excel used time:{}", DateTimeUtils.used(dt));
            }
            return Boolean.TRUE;
        }
    }
    
    protected void exportCSV_factor(GeneRankCriteria criteria) {
        String fname = criteria.generateKey(GeneRankCriteria.CSVFileTempalte);
        try {
            File root = new File(PropertiesHolder.get(PropertiesHolder.FILES, "csv.gene", "/data/tmp/files/omicseq/"));
            File file = new File(root, "factor_"+ fname);
            if (file.exists()) {
//                file.delete();
            	return;
            } else {
            	file = new File(root, "new_factor_"+ fname);
            }
            FileUtils.forceMkdir(root);
            if (logger.isDebugEnabled()) {
                logger.debug("生成CSV文件 :{}", file.getAbsolutePath());
            }
            DateTime dt = DateTime.now();
            Collection<Object> lines = new ArrayList<Object>();
            lines.add("words,freq");// titles
            List<GeneRank> ranks = geneRankDAO.findByCriteria(criteria);
            HashMap<String, Integer> word_freq = new HashMap<String ,Integer>();
            for (int i = 0; i < ranks.size(); i++) {
                GeneRank geneRank = ranks.get(i);
                Sample sample = SampleCache.getInstance().getSampleById(geneRank.getSampleId());
                if(sample.getFactor() != null && !"".equals(sample.getFactor()))
                {
                	if(word_freq.get(sample.getFactor()) != null)
                    {
                    	word_freq.put(sample.getFactor(), word_freq.get(sample.getFactor()) + 1);
                    }else {
                    	word_freq.put(sample.getFactor(), 1);
                    }
                }
            }
            
            int i = 1;
           
            if(word_freq.keySet().isEmpty())
            {
            	lines.add(toCsv(i, "none", 1));
            }else {
            	Iterator it = word_freq.keySet().iterator();
            	 while(it.hasNext())
                 {
                 	Object word_obj = it.next();
                 	if(word_obj == null)
                 	{
                 		continue;
                 	}else {
                 		lines.add(toCsv(i, word_obj.toString(), word_freq.get(word_obj.toString())));
                 		i++;
                 	}
                 }
            }
            FileUtils.writeLines(file, "utf-8", lines, IOUtils.LINE_SEPARATOR_UNIX);
            if (logger.isDebugEnabled()) {
                logger.debug("生成{}文件,用时:{}", fname, DateTimeUtils.used(dt));
            }
        } catch (Exception e) {
            logger.error("生成[" + fname + "]文件出错!", e);
        }
    }

    protected void export(GeneRankCriteria criteria) {
		exportCSV_cell(criteria);
		exportCSV_factor(criteria);
	}

	protected void exportCSV_cell(GeneRankCriteria criteria) {
        String fname = criteria.generateKey(GeneRankCriteria.CSVFileTempalte);
        try {
            File root = new File(PropertiesHolder.get(PropertiesHolder.FILES, "csv.gene", "/data/tmp/files/omicseq/"));
            File file = new File(root, "cell_"+ fname);
            if (file.exists()) {
//                file.delete();
            	return;
            }else {
            	file = new File(root, "new_cell_"+ fname);
            }
            FileUtils.forceMkdir(root);
            if (logger.isDebugEnabled()) {
                logger.debug("生成CSV文件 :{}", file.getAbsolutePath());
            }
            DateTime dt = DateTime.now();
            Collection<Object> lines = new ArrayList<Object>();
            lines.add("words,freq");// titles
            List<GeneRank> ranks = geneRankDAO.findByCriteria(criteria);
            HashMap<String, Integer> word_freq = new HashMap<String ,Integer>();
            for (int i = 0; i < ranks.size(); i++) {
                GeneRank geneRank = ranks.get(i);
                Sample sample = SampleCache.getInstance().getSampleById(geneRank.getSampleId());
                ExperimentType etype = ExperimentType.parse(sample.getEtype());
                
                if(ExperimentType.RNA_SEQ.equals(etype))
                {
                	SourceType source = SourceType.parse(sample.getSource());
                    if (SourceType.TCGA.equals(source) && StringUtils.isBlank(sample.getFactor()) && StringUtils.isNotBlank(sample.getUrl())) {
                        String[] arr = StringUtils.split(sample.getUrl(), "/");
                        sample.setFactor(arr.length > 8 ? arr[7] : null);
                    }
                    // TCGA
                    if (SourceType.TCGA.equals(source)) {
                        sample.setCell("TCGA-" + sample.getFactor());
                    }
                    // ICGC
                    if (SourceType.ICGC.equals(source) && StringUtils.isNotBlank(sample.getCell())) {
                    	sample.setCell("ICGC-" + sample.getCell());
                    }
                }
                
                if(word_freq.get(sample.getCell()) != null)
                {
                	word_freq.put(sample.getCell(), word_freq.get(sample.getCell()) + 1);
                }else {
                	word_freq.put(sample.getCell(), 1);
                }
            }
            Iterator it = word_freq.keySet().iterator();
            int i = 1;
            while(it.hasNext())
            {
            	Object word_obj = it.next();
            	if(word_obj == null)
            	{
            		continue;
            	}else {
            		lines.add(toCsv(i, word_obj.toString(), word_freq.get(word_obj.toString())));
            		i++;
            	}
            }
            if(word_freq.keySet().isEmpty())
            {
            	lines.add(toCsv(i, "none", 1));
            }
            FileUtils.writeLines(file, "utf-8", lines, IOUtils.LINE_SEPARATOR_UNIX);
            if (logger.isDebugEnabled()) {
                logger.debug("生成{}文件,用时:{}", fname, DateTimeUtils.used(dt));
            }
        } catch (Exception e) {
            logger.error("生成[" + fname + "]文件出错!", e);
        }
    }
    
    private Object toCsv(int idx, String word, Integer freq) {
        Object[] arr = toArray(idx, word.replace(",", " "), freq);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < arr.length; i++) {
//            sb.append(column(arr[i])).append(i != arr.length - 1 ? "," : "");
//        }
        return column(arr[0].toString())+","+arr[1];
    }

    private Object[] toArray(int idx, String word, Integer freq) {
        Object[] arr = new String[2];
        // word
        arr[0] = String.valueOf(word);
        // freq
        arr[1] = String.valueOf(freq);
        return arr;
    }

    private String column(String str) {
        String val = StringUtils.trimToEmpty(str);
        return String.format("\"%s\"", val.replaceAll("\"", "'"));
    }

    protected Callable<Object> getCallable(List<GeneRankCriteria> criteries) {
        return new WordCloudCallable(new GeneWordCloud(), criteries);
    }

    public static void main(String[] args) {
    	SampleCache.getInstance().init();
        geneCache.init();
        single.refresh();
        
//        GeneRankCriteria criteria = new GeneRankCriteria();
//        criteria.setGeneId(30236);
//        GeneWordCloud wd = new GeneWordCloud();
//        wd.exportCSV_cell(criteria);
//        wd.exportCSV_factor(criteria);
    }

    public static GeneWordCloud getInstance() {
        return single;
    }

}
