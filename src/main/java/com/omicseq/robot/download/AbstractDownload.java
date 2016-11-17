package com.omicseq.robot.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.domain.Chunk;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.MiscUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public abstract class AbstractDownload extends AbstractLifeCycle implements IDownload {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static long MB = 1024 * 1024;
    protected SourceType source;
    protected URL url;
    protected FileInfo fileInfo = null;
    protected IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
    protected IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);

    public AbstractDownload(URL url, SourceType source) {
        this.url = url;
        this.source = source;
    }

    public AbstractDownload(URL url, FileInfo fileInfo) {
        this(url, SourceType.parse(fileInfo.getSource()));
        if (null != fileInfo.get_id()) {
            this.fileInfo = fileInfo;
        }
    }

    protected FileInfo newFileInfo() {
        FileInfo info = new FileInfo();
        info.setSource(source.value());
        info.setUrl(url.toString());
        return info;
    }

    private void init(FileInfo info) {
        String surl = url.toString();
        this.fileInfo = fileInfoDAO.get(surl);
        if (null == fileInfo) {
            this.fileInfo = newFileInfo();
            if (null != info) {
                this.fileInfo.setLength(info.getLength());
                this.fileInfo.setLastModified(info.getLastModified());
            }
            this.fileInfoDAO.create(fileInfo);
        }
        this.fileInfo.setResume(info.getResume());
    }

    protected void updateFileInfo() {
        fileInfoDAO.update(this.fileInfo);
    }

    @Override
    public FileInfo getFileInfoFromStore() {
        if (null == fileInfo) {
            init(null);
        }
        return fileInfo;
    }

    @Override
    public void start() {
        if (isStoped()) {
            super.start();
            this.run();

        }
    }

    protected void run() {
        try {
            // 1.connection server
            open();
            // 2.get file info from server
            FileInfo info = getFileInfoFromServer();
            // load or create from store
            init(info);
            // 3.比较文件信息,判断是否下载文件
            if (!compare(info) || !FileInfoStatus.isDownloaded(info.getState())) {
                // 3.1 检查创建本地文件
                File file = makeFile();
                // 3.2分块
                chunks();
                try {
                    // 3.3 下载
                    if (!FileInfoStatus.isDownloaded(fileInfo.getState())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Begin download {} from  {}", file, fileInfo.getUrl());
                        }
                        download(file);
                        fileInfo.setState(FileInfoStatus.DOWNLOADED.value());
                    }
                } catch (Exception e) {
                    String msg = "downlod file[%s] faied from [%s]";
                    logger.error(String.format(msg, fileInfo.getPath(), fileInfo.getUrl()), e);
                    fileInfo.setState(FileInfoStatus.FAILED.value());
                }
            }
            // 4.判断文件是否处理完成，如未处理完成，则待处理文件
            if (FileInfoStatus.isDownloaded(fileInfo.getState()) && !FileInfoStatus.isProcessed(fileInfo.getState())) {
                StatisticInfo exits = statisticInfoDAO.getBySampleId(fileInfo.getSampleId());
                if (null == exits) {
                    StatisticInfo obj = new StatisticInfo();
                    obj.setSampleId(fileInfo.getSampleId());
                    obj.setSource(fileInfo.getSource());
                    obj.setServerIp(fileInfo.getServerIp());
                    obj.setPath(fileInfo.getPath());
                    obj.setPriority(fileInfo.getPriority());
                    obj.setState(StatisticInfoStatus.DEFAULT.value());
                    statisticInfoDAO.create(obj);
                }else{
                    //TODO update state 
                }
            }
        } catch (Exception e) {
            logger.error("download failed!", e);
        } finally {
            // 更新信息
            updateFileInfo();
            // 5 disconnect server
            close();
        }
    }

    /**
     * open connect to server
     */
    protected void open() {
    }

    /**
     * get download file info from server
     * 
     * @return
     */
    public abstract FileInfo getFileInfoFromServer();

    /**
     * 比较文件信息,检查是否完成下载,是否更新.
     * 
     * @param info
     * @return
     */
    protected boolean compare(FileInfo info) {
        return fileInfo.comare(info);
    }

    /**
     * 创建文件
     * 
     * @return
     * @throws IOException
     */
    protected File makeFile() throws IOException {
        String oldPath = fileInfo.getPath();
        fileInfo.setPath(source.convertToDisk(fileInfo.getUrl()));
        // 删除原文件
        if (StringUtils.isNotEmpty(oldPath) && !fileInfo.getPath().equals(oldPath)) {
            FileUtils.deleteQuietly(new File(oldPath));
        }
        String path = fileInfo.getPath().split("\\.")[0]+"_"+fileInfo.getSampleId()+"."+fileInfo.getPath().split("\\.")[1];
        File _file = new File(path);
//      File _file = new File(fileInfo.getPath());
        // 如果文件不存在,创建文件。
        if (!_file.exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("create new file: {}", _file);
            }
            FileUtils.forceMkdir(_file.getParentFile());
            _file.createNewFile();
            fileInfo.setChunks(null);
            fileInfo.setServerIp(MiscUtils.getServerIP());
            fileInfo.setState(FileInfoStatus.CREATED.value());
            updateFileInfo();
        }
        return _file;
    }

    /**
     * 对文件分块处理
     */
    protected void chunks() {
        if (CollectionUtils.isEmpty(fileInfo.getChunks())) {
            Chunk single = new Chunk(0l, fileInfo.getLength());
            fileInfo.setChunks(Arrays.asList(single));
            fileInfo.setState(FileInfoStatus.SPLITTED.value());
        }
    }

    /**
     * 分块后,多线程下载文件
     */
    protected abstract void download(File file) throws Exception;

    /**
     * disconnect server
     */
    protected void close() {

    }

}
