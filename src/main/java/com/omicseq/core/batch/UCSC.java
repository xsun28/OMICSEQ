package com.omicseq.core.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.ThreadTaskPoolsExecutor;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.PropertiesHolder;
import com.omicseq.domain.Gene;
import com.omicseq.domain.HashDB;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.ThreadUtils;

public class UCSC {
    private static Logger logger = LoggerFactory.getLogger(UCSC.class);
    private static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
    private static IHashDBDAO hashDBDAO = DAOFactory.getDAO(IHashDBDAO.class);
    private static String UCSC_BATCH_KEY = "UCSC_Batch_start";
    //
    private static String BaseUrl = "http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=%s:%s-%s";
    // http://genome.ucsc.edu/cgi-bin/hgTracks?hgt.imageV1=1&hgt.trackImgOnly=1&hgsid=369464169_fI7tLV52kAtVd1amGKQASK8wA5iW&_=1396966712751
    private static String PNG_URL = "http://genome.ucsc.edu/cgi-bin/hgTracks?hgt.imageV1=1&hgt.trackImgOnly=1&hgsid=%s&_=%s";
    //
    private static String PNG = "http://genome.ucsc.edu/%s";
    private static int timeout = 60 * 1000;
    private static Set<String> exits = new HashSet<String>(5);
    private static String dir = PropertiesHolder.get(PropertiesHolder.FILES, "ucsc.img");

    public static void main(String[] args) {
        DateTime dt = DateTime.now();
        Collection<File> files = FileUtils.listFiles(new File(dir), new String[] { "png" }, false);
        for (File file : files) {
            exits.add(FilenameUtils.getBaseName(file.getName()));
        }
        HashDB runtime = hashDBDAO.getByKey(UCSC_BATCH_KEY);
        Integer start = null == runtime ? 0 : Integer.valueOf(runtime.getValue());
        Integer limit = 5;
        List<Gene> geneList = null;
        Semaphore semaphore = new Semaphore(limit);
        while (CollectionUtils.isNotEmpty(geneList = geneDAO.loadGeneList(start, limit))) {
            try {
                List<WaitFutureTask<Boolean>> tasks = new ArrayList<WaitFutureTask<Boolean>>(limit);
                for (final Gene gene : geneList) {
                    Callable<Boolean> callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            download(gene);
                            return true;
                        }
                    };
                    tasks.add(new WaitFutureTask<Boolean>(callable, semaphore));
                }
                ThreadTaskPoolsExecutor.getInstance().blockRun(tasks, 10l, TimeUnit.HOURS);
                int val = start + geneList.size();
                if (null == runtime) {
                    runtime = new HashDB(UCSC_BATCH_KEY, String.valueOf(val));
                    hashDBDAO.create(runtime);
                } else {
                    runtime.setValue(String.valueOf(val));
                    hashDBDAO.update(runtime);
                }
            } catch (Exception e) {
                logger.error("thread  error:", e);
            }
            ThreadUtils.sleep(100);
            start = start + limit;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("download ucsc image used {} ", DateTimeUtils.used(dt));
        }
        System.exit(0);
    }

    private static void download(Gene gene) {
        String key = String.format("%s_%s_%s", gene.getSeqName(), gene.getStart(), gene.getEnd());
        if (exits.contains(key)) {
            return;
        }
        File imageFile = new File(dir, key + ".png");
        if (imageFile.exists()) {
            exits.add(key);
            return;
        }
        exits.add(key);
        InputStream input = null;
        OutputStream output = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("download ucsc image {} !", gene);
            }
            String surl = String.format(BaseUrl, gene.getSeqName(), gene.getStart(), gene.getEnd());
            Document doc = Jsoup.parse(new URL(surl), timeout);
            Element el = doc.select("input[name=hgsid]").first();
            String hgsid = el.val();
            String pngUrl = String.format(PNG_URL, hgsid, System.currentTimeMillis());
            doc = Jsoup.parse(new URL(pngUrl), timeout);
            String src = doc.select("IMG").attr("src");
            src = src.substring(2);
            input = new URL(String.format(PNG, src)).openStream();

            FileUtils.forceMkdir(imageFile.getParentFile());
            output = new FileOutputStream(imageFile);
            while (true) {
                byte[] data = new byte[1024];
                int count = input.read(data);
                if (count == -1) {
                    break;
                }
                output.write(data, 0, count);
            }
            output.flush();
        } catch (Exception e) {
            logger.error("");
            logger.error("下载UCSC图片文件出错", e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }
}
