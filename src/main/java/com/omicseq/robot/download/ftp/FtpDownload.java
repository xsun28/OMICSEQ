package com.omicseq.robot.download.ftp;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.omicseq.common.SourceType;
import com.omicseq.common.SourceType.Account;
import com.omicseq.domain.Chunk;
import com.omicseq.domain.FileInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.robot.download.AbstractDownload;

/**
 * 
 * FTP to download the file
 * 
 * @author zejun.du
 */
public class FtpDownload extends AbstractDownload {
    private FileInfo _fileInfo = null;
    private FTPClient client = new FTPClient();

    public FtpDownload(URL url, SourceType source) {
        super(url, source);
    }

    public FtpDownload(URL url, FileInfo fileInfo) {
        super(url, fileInfo);
    }

    @Override
    protected void open() {
        if (logger.isDebugEnabled()) {
            logger.debug("Connect to server {} ", url.getHost());
        }
        if (client.isConnected()) {
            return;
        }
        Account acc = source.account();
        try {
            int port = -1 == url.getPort() ? url.getDefaultPort() : url.getPort();
            client.connect(url.getHost(), port);
            client.login(acc.username, acc.password);
            client.setType(FTPClient.TYPE_BINARY);
            if (logger.isDebugEnabled()) {
                logger.debug("Connected to server {} ", url.getHost());
            }
        } catch (Exception e) {
            String msg = "Connect to host:[%s] user:[%s] password:[%s] failed!";
            throw new OmicSeqException(String.format(msg, url.getHost(), acc.username, acc.password), e);
        }

    }

    @Override
    public FileInfo getFileInfoFromServer() {
        if (null != _fileInfo) {
            return _fileInfo;
        }
        FileInfo info = newFileInfo();
        boolean connected = client.isConnected();
        try {
            if (!connected) {
                open();
            }
            info.setLength(client.fileSize(url.getPath()));
            Date modifiedDate = client.modifiedDate(url.getPath());
            info.setLastModified(null != modifiedDate ? modifiedDate.getTime() : System.currentTimeMillis());
        } catch (Exception e) {
            throw new OmicSeqException(String.format("Get File[%s] info failed!", url.toString()), e);
        } finally {
            if (!connected) {
                close();
            }
        }
        this._fileInfo = info;
        return info;
    }

    @Override
    protected void download(File file) throws Exception {
        client.changeDirectory("/");
        String dir = url.getPath().substring(0, url.getPath().lastIndexOf("/"));
        client.changeDirectory(dir);
        client.setPassive(true);
        Chunk chunk = fileInfo.getChunks().get(0);
        String fname = FilenameUtils.getName(url.getPath());
        while (true) {
            try {
                client.download(fname, file, chunk.getBegin(), new DataTransferListener(chunk));
                return;
            } catch (FTPException e) {
                if (e.getCode() == 451) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("FTPException:", e);
                    }
                    continue;
                } else {
                    throw e;
                }
            }catch (SocketTimeoutException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("SocketTimeoutException:", e);
                }
                continue;
            }
        }
    }

    @Override
    protected void close() {
        try {
            client.disconnect(true);
        } catch (Exception e) {
            logger.error("Close ftp connect failed!", e);
        }
    }

    static class DataTransferListener implements FTPDataTransferListener {
        private Chunk chunk;

        DataTransferListener(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void started() {

        }

        @Override
        public void transferred(int length) {
            chunk.setPostion(chunk.getPostion() + length);
        }

        @Override
        public void completed() {

        }

        @Override
        public void aborted() {

        }

        @Override
        public void failed() {

        }
    }

    public static void main(String[] args) throws Exception {
        String url = "ftp://ftp.ncbi.nlm.nih.gov/geo/series/GSE60nnn/GSE60171/suppl/GSE60171_cuffdiff.FGF-5days.vs.iBET-5days.txt.gz";
        FtpDownload dw = new FtpDownload(new URL(url), SourceType.Roadmap);
        dw.open();
        dw.fileInfo =new FileInfo();
        List<Chunk> chunks=new ArrayList<Chunk>(1);
        Chunk chunk=new Chunk(0l, dw.client.fileSize(dw.url.getPath()));
        chunks.add(chunk);
        dw.fileInfo.setChunks(chunks);
        File file = new File("e:/temp.bed.gz");
        dw.download(file);
        dw.close();
    }
}
