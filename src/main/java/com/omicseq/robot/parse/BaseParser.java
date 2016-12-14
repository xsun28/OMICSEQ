package com.omicseq.robot.parse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SourceType;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.core.ILifeCycle;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.robot.download.DownloadFactory;
import com.omicseq.robot.download.IDownload;
import com.omicseq.store.dao.ICronTaskDAO;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public abstract class BaseParser extends AbstractLifeCycle implements IParser, ILifeCycle {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static IHashDBDAO hashDBDAO = DAOFactory.getDAO(IHashDBDAO.class);
    protected static IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
    protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
    protected static ISampleDAO samplePrevDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
    protected static ISampleDAO sampleFailedDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "failed");
    protected static ICronTaskDAO cronTaskDAO = DAOFactory.getDAO(ICronTaskDAO.class);
//    protected int timeout = 10 * 60 * 1000;// 默认超时时间10分钟
    protected int timeout = 10000;
    /**
     * @return
     */
    abstract SourceType getSourceType();

    @Override
    public void start() {
        if (isStoped()) {
            super.start();
            parser(getSourceType().url());
        }
    }

    /**
     * @return 唯一的sampleId
     */
    protected Integer getSampleId() {
        return sampleDAO.getSequenceId(getSourceType());
    }

    /**
     * 
     * @param url
     */
    public void process(String url) {
        process(url, null);
    }

    /**
     * 
     * @param url
     */
    public void process(String url, Sample sample) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        Integer priority = null == sample || !StringUtils.equalsIgnoreCase("input", sample.getSettype()) ? 0 : 99;
        if (logger.isDebugEnabled()) {
            logger.debug("create fileinfo {}", url);
        }
        // exits
        FileInfo fileInfo = fileInfoDAO.get(url);
        if (null == fileInfo) {
            IDownload down = DownloadFactory.get(url, getSourceType());
            fileInfo = down.getFileInfoFromServer();
            fileInfo.setState(FileInfoStatus.INIT.value());
            fileInfo.setPriority(priority);
            fileInfoDAO.create(fileInfo);
        } else if (null != fileInfo && null != priority && !priority.equals(fileInfo.getPriority())) {
            fileInfo.setPriority(priority);
            fileInfoDAO.update(fileInfo);
        }
    }

    public void process(FileInfo file) {
        if (null == file || StringUtils.isBlank(file.getUrl())) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("create fileinfo {}", file);
        }
        // exits
        FileInfo exits = fileInfoDAO.get(file.getUrl());
        if (null != exits) {
            if (logger.isDebugEnabled()) {
                logger.debug("found data {}", exits);
            }
        } else {
            file.setState(FileInfoStatus.INIT.value());
            file.setSource(getSourceType().value());
            fileInfoDAO.create(file);
        }
    }

    protected void failed(String url, Integer sampleId, Exception e) {
        try {
            Sample sample = new Sample();
            sample.setSource(getSourceType().value());
            sample.setUrl(url);
            sample.setEtype(ExperimentType.CHIP_SEQ_TF.value());
            Map<String, String> map = new HashMap<String, String>(2);
            map.put("Ex", e.getMessage());
            map.put("date", DateUtils.formatSDate(new Date()));
            sample.descMap(map);
            sample.setSampleId(sampleId);
            sampleFailedDAO.create(sample);
        } catch (Exception ex) {
            logger.debug("记录处理错误信息出错：{}", e);
        }
        logger.error("处理{}数据出错{}", url, e);
        logger.error(e.getMessage(), e);
    }

}
