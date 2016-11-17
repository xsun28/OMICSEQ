package com.omicseq.robot.download.sra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import com.omicseq.robot.download.IDownload;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;

public class SRADownload extends AbstractLifeCycle implements IDownload {
    private static Logger logger = LoggerFactory.getLogger(SRADownload.class);
    private static IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
    private IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
    private SourceType source = SourceType.SRA;
    private String name = null;
    private FileInfo fileInfo = null;

    public SRADownload(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        Sample sample = sampleDAO.getBySampleId(fileInfo.getSampleId());
        if (null != sample) {
            this.name = MapUtils.getString(sample.descMap(), "run");
        }
    }

    public SRADownload(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        if (isStoped()) {
            super.start();
            this.run();
        }
    }

    private void run() {
        String fid = this.fileInfo.getSampleId().toString();
        String[] names = this.name.split(",");
        String _dir = source.path();
        File root = new File(_dir);
        File dir = new File(root, fid);
        logger.debug("root: {} dir:{}\n names:{} ",root,dir,names);
        File f = new File(dir, fid + ".over");
        if (f.exists()) {
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
                File err = new File(dir, fname + ".error");
                if (StringUtils.isBlank(fname)) {
                    logger.warn("please check name {} ", fileInfo);
                    error(err, "not found name !");
                    continue;
                }
                if (checkDownload(dir, err, fname)) {
                    sucessList.add(fname);
                    continue;
                }
                String shell = "/opt/sra/tofq.sh";
                File sf = new File(shell);
                if (!sf.exists()) {
                    logger.warn("Shell {} not found!", shell);
                    throw new OmicSeqException("Shell  " + shell + " not found!");
                }
                DateTime dt = DateTime.now();
                if (logger.isDebugEnabled()) {
                    logger.debug("exec {} {} {} ", shell, fid, fname);
                }
                ProcessBuilder pb = new ProcessBuilder(shell, fid, fname);
                StringBuffer buf = exec(_dir, pb);
                if (StringUtils.indexOf(buf, "err:") != -1) {
                    error(err, buf.toString());
                    continue;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("download {} file used {}!", fname, DateTimeUtils.used(dt));
                }
                sucessList.add(String.format("%s/%s/%s", _dir, fid, fname).trim());
            }
            if (sucessList.size() != names.length) {
                throw new OmicSeqException("file download failed " + names);
            }
            //TODO 拆分任务到其他机器  SRATransFastqToSam.java（112.25.20.160）
//            String shell = "/opt/sra/tosam.sh";
//            File sf = new File(shell);
//            if (!sf.exists()) {
//                logger.warn("Shell {} not found!", shell);
//                throw new OmicSeqException("Shell  " + shell + " not found!");
//            }
//            DateTime dt = DateTime.now();
//            String files = StringUtils.join(sucessList.toArray(new String[] {}), ",");
//            if (logger.isDebugEnabled()) {
//                logger.debug("exec {} {} {} ", shell, files, fid, fid);
//            }
//            ProcessBuilder pb = new ProcessBuilder(shell, files, fid, fid);
//            StringBuffer buf = exec(_dir, pb);
//            if (StringUtils.indexOf(buf, "err:") != -1) {
//                File err = new File(root, fid + ".error");
//                FileUtils.deleteQuietly(err);
//                error(err, buf.toString());
//                throw new OmicSeqException("fastq to sam file failed !");
//            }
//            if (logger.isDebugEnabled()) {
//                logger.debug("download {} file used {}!", fid, DateTimeUtils.used(dt));
//            }
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
//            StatisticInfo exits = statisticInfoDAO.getBySampleId(fid);
//            if (null == exits) {
//                StatisticInfo obj = new StatisticInfo();
//                obj.setSampleId(fid);
//                obj.setSource(fileInfo.getSource());
//                obj.setServerIp(fileInfo.getServerIp());
//                obj.setPath(fileInfo.getPath());
//                obj.setPriority(fileInfo.getPriority());
//                obj.setState(StatisticInfoStatus.DEFAULT.value());
//                statisticInfoDAO.create(obj);
//            }
        }
        //
//        FileUtils.writeStringToFile(f, "succes");
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

    @Override
    public FileInfo getFileInfoFromStore() {
        throw new OmicSeqException("Not Impl!");
    }

    @Override
    public FileInfo getFileInfoFromServer() {
        throw new OmicSeqException("Not Impl!");
    }

    public static void main(String[] args) {
        FileInfo info = fileInfoDAO.getBySampleId(400006);
        System.out.println(info.getPath());
        new SRADownload(info).run();
    }

}
