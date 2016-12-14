package com.omicseq.robot.exec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.ThreadTaskPoolsExecutor;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.HashDB;
import com.omicseq.robot.download.DownloadFactory;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.MiscUtils;
import com.omicseq.utils.ThreadUtils;

public class DownLoad extends AbstractLifeCycle {
    private static Logger logger = LoggerFactory.getLogger(DownLoad.class);
    private IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
    private IHashDBDAO hashDBDAO = DAOFactory.getDAO(IHashDBDAO.class);
    private static DownLoad single = new DownLoad();
    private static int limit = 100;
    private boolean stoped = true;
    private int maxThreads = 2;

    private DownLoad() {
    }

    @Override
    public void start() {
        if (stoped && isStoped()) {
            super.start();
            this.run();
        }
    }

    private void run() {
        Set<Integer> cache = new HashSet<Integer>(5);
        while (true) {
            if (isStoped()) {
                stoped = true;
                return;
            }
            try {
                List<FileInfo> coll = fileInfoDAO.findUndownload(MiscUtils.getServerIP(), limit);
                HashDB db = hashDBDAO.getByKey("batch_download_threads");
                int threads = maxThreads;
                if (null != db) {
                    threads = Integer.valueOf(db.getValue());
                }
                while (CollectionUtils.isNotEmpty(coll)) {
                    
                    final List<WaitFutureTask<Object>> taskList = new ArrayList<WaitFutureTask<Object>>(threads);
                    Semaphore semaphore = new Semaphore(threads);
                    while (true) {
                        if (CollectionUtils.isEmpty(coll)) {
                            break;
                        }
                        FileInfo info = coll.remove(0);
//                        注释后下载未成功可继续下载
                        if (cache.contains(info.getSampleId())) {
                            continue;
                        }
//                        cache.add(info.getSampleId());
                        WaitFutureTask<Object> task = new WaitFutureTask<Object>(new DownLoadCallable(info), semaphore);
                        taskList.add(task);
                        if (taskList.size() >= threads) {
                            break;
                        }
                    }
                    if (CollectionUtils.isEmpty(taskList)) {
                        break;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("downloading files {} ", taskList);
                    }
                    ThreadTaskPoolsExecutor.getInstance().blockRun(taskList, 100l, TimeUnit.DAYS);
                }
                ThreadUtils.sleep(60 * 1000);
            } catch (Exception e) {
                logger.error("download {} file failed.", MiscUtils.getServerIP(), e);
            }
        }
    }

    public static void main(String[] args) {
        getInstance().start();
    }

    public static DownLoad getInstance() {
        return single;
    }

    public static class DownLoadCallable implements Callable<Object> {
        private FileInfo info;

        public DownLoadCallable(FileInfo info) {
            this.info = info;
        }

        @Override
        public Object call() throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("download file {}", info);
            }
            DownloadFactory.get(info).start();
            if (logger.isDebugEnabled()) {
                logger.debug("downloaded file {}", info);
            }
            return true;
        }
    }
}
