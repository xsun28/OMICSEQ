package com.omicseq.robot.download.sra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;

public class SRATransFastqToSam implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(SRADownload.class);
    private static IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
    private IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
    private SourceType source = SourceType.SRA;
    private String name = null;
    private FileInfo fileInfo = null;

    public SRATransFastqToSam(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        Sample sample = sampleDAO.getBySampleId(fileInfo.getSampleId());
        if (null != sample) {
            this.name = MapUtils.getString(sample.descMap(), "run");
        }
    }

    public SRATransFastqToSam(String name) {
        this.name = name;
    }

    public void run() {
    	while (run) {  
            FileInfo fileInfo = getEvent();  
            if(fileInfo == null)
            {
            	fileInfo = getEvent();
            }
            this.fileInfo = fileInfo;
            Sample sample = sampleDAO.getBySampleId(fileInfo.getSampleId());
            if (null != sample) {
                this.name = MapUtils.getString(sample.descMap(), "run");
            }
            processEvent(this.fileInfo);  
        } 
    }

    private StringBuffer exec(String root, ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        pb.directory(new File(root));
        Process proc = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        StringBuffer buf = new StringBuffer();
        while ((line = br.readLine()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(line);
            }
            buf.append(line);
            buf.append(IOUtils.LINE_SEPARATOR);
        }
        proc.waitFor();
        return buf;
    }

    private void error(File err, String msg) {
        try {
            FileUtils.writeStringToFile(err, msg, true);
        } catch (Exception e2) {
        }
    }

    private void sucess(String root, File f) throws IOException {
        if (null != fileInfo) {
            fileInfo.setState(FileInfoStatus.DOWNLOADED.value());
            Integer fid = fileInfo.getSampleId();
            fileInfo.setPath(String.format("%s/%s/%s.sam", root, fid, fid).replaceAll("//", "/"));
            fileInfo.setPriority(0);
            File file = new File(fileInfo.getPath());
            fileInfo.setLength(file.length());
            fileInfo.setLastModified(file.lastModified());
            fileInfoDAO.update(fileInfo);
            StatisticInfo exits = statisticInfoDAO.getBySampleId(fid);
            if (null == exits) {
                StatisticInfo obj = new StatisticInfo();
                obj.setSampleId(fid);
                obj.setSource(fileInfo.getSource());
                obj.setServerIp(fileInfo.getServerIp());
                obj.setPath(fileInfo.getPath());
                obj.setPriority(fileInfo.getPriority());
                obj.setState(StatisticInfoStatus.DEFAULT.value());
                statisticInfoDAO.create(obj);
            }
        }
        //
        FileUtils.writeStringToFile(f, "succes");
        //
    }

    private boolean checkDownload(File dir, File err, final String name) {
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fname) {
                return fname.startsWith(name);
            }
        });
        if (err.exists()) {
            FileUtils.deleteQuietly(err);
            for (File file : files) {
                FileUtils.deleteQuietly(file);
            }
            return false;
        } else {
            return null != files && files.length != 0;
        }
    }


    public static void main(String[] args) {
//        System.out.println(info.getPath());
//        new SRATransFastqToSam(info).run();
        
        SRATransFastqToSam fts = new SRATransFastqToSam();
        Thread processThread = new Thread(fts);  
        processThread.start();
        
        
        SmartDBObject query2 = new SmartDBObject();
        query2.put("$gte", 400255);
        query2.append("$lte", 400285);
        
        SmartDBObject query = new SmartDBObject();
        query.put("sampleId", query2);
        List<FileInfo> files = fileInfoDAO.find(query);
        
        for(FileInfo info : files){
        	fts.putEvent(info);
        }
        
//        FileInfo info = fileInfoDAO.getBySampleId(400029);
//        fts.putEvent(info);
    }

    private Vector queueData = null;  
    private boolean run = true;  
  
    public SRATransFastqToSam() {  
        queueData = new Vector();  
    }
    
    @SuppressWarnings("unchecked")
	public synchronized void putEvent(FileInfo info) {  
        queueData.addElement(info);  
        notify();  
    }
    
    private synchronized FileInfo getEvent() {  
        try {  
            return (FileInfo) queueData.remove(0);  
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
    
    private void processEvent(FileInfo info) {  

        String fid = this.fileInfo.getSampleId().toString();
        String[] names = this.name.split(",");
        String _dir = source.path();
        File root = new File(_dir);
        File dir = new File(root, fid);
        File f = new File(dir, fid + ".over");
        File sam = new File(dir, fid + ".sam");
        if (sam.exists() && sam.length() > 10000000) {
            logger.info("{} is downloaded!", name);
            if (null != fileInfo && !FileInfoStatus.isDownloaded(fileInfo.getState())) {
                fileInfo.setState(FileInfoStatus.DOWNLOADED.value());
                fileInfoDAO.update(fileInfo);
            }
            return;
        }
        DateTime start = DateTime.now();
        try {
            List<String> sucessList = new ArrayList<String>();
            for (String fname : names) {
                sucessList.add(String.format("%s/%s/%s", _dir, fid, fname).trim());
            }
            if (sucessList.size() != names.length) {
                throw new OmicSeqException("file download failed " + names);
            }
            System.out.println(sucessList.get(0));
            String shell = "/opt/sra/tosam.sh";
            File sf = new File(shell);
            if (!sf.exists()) {
                logger.warn("Shell {} not found!", shell);
                throw new OmicSeqException("Shell  " + shell + " not found!");
            }
            DateTime dt = DateTime.now();
            String files = StringUtils.join(sucessList.toArray(new String[] {}), ",");
            if (logger.isDebugEnabled()) {
                logger.debug("exec {} {} {} ", shell, files, fid, fid);
            }
            ProcessBuilder pb = new ProcessBuilder(shell, files, fid, fid);
            StringBuffer buf = exec(_dir, pb);
            if (StringUtils.indexOf(buf, "err:") != -1) {
                File err = new File(root, fid + ".error");
                FileUtils.deleteQuietly(err);
                error(err, buf.toString());
                throw new OmicSeqException("fastq to sam file failed !");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("download {} file used {}!", fid, DateTimeUtils.used(dt));
            }
            sucess(_dir, f);
        } catch (Exception e) {
            logger.error("download {} failed!", fid, e);
            fileInfo.setState(FileInfoStatus.FAILED.value());
            fileInfoDAO.update(fileInfo);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("process {} file used {}!", fid, DateTimeUtils.used(start));
            }
        }
    }  
    
    public synchronized void destroy() {  
        run = false;  
        queueData = null;  
        notify();  
    }
}
