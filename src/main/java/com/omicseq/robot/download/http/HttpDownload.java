package com.omicseq.robot.download.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;

import com.omicseq.common.SourceType;
import com.omicseq.domain.Chunk;
import com.omicseq.domain.FileInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.robot.download.AbstractDownload;
import com.omicseq.utils.DateTimeUtils;

/**
 * 
 * HTTP to download the file
 * 
 * @author zejun.du
 */
public class HttpDownload extends AbstractDownload {
    private FileInfo _fileInfo = null;
    private CloseableHttpClient client = HttpClients.createDefault();
    // 默认每次获取10M的数据
    private int blockSize = 10 * 1024 * 1024;

    public HttpDownload(URL url, SourceType source) {
        super(url, source);
    }

    public HttpDownload(URL url, FileInfo fileInfo) {
        super(url, fileInfo);
    }

    private HttpGet setRange(long begin, long end) {
        HttpGet get = new HttpGet(url.toString());
        setRange(begin, end, get);
        return get;
    }

    private void setRange(long begin, long end, HttpRequestBase req) {
        req.setHeader("Range", String.format("bytes=%s-%s", begin, end));
    }

    @Override
    protected void open() {

    }

    /**
     * @param client
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public FileInfo getFileInfoFromServer() {
        if (null != _fileInfo) {
            return _fileInfo;
        }
        final FileInfo info = newFileInfo();
        try {
            HttpHead head = new HttpHead(url.toString());
            setRange(1, 2, head);
            CloseableHttpResponse res = client.execute(head);
            Header contentLengthHeader = res.getFirstHeader("Content-Length");
            String contentLength = null == contentLengthHeader ? null : contentLengthHeader.getValue();
            Header rangeHeader = res.getFirstHeader("Content-Range");
            if (rangeHeader != null && StringUtils.isNoneBlank(rangeHeader.getValue())) {
                contentLength = StringUtils.split(rangeHeader.getValue(), '/')[1];
            } else {
                info.setResume(Boolean.FALSE);
            }
            info.setLength(StringUtils.isBlank(contentLength) ? Integer.MAX_VALUE : Long.valueOf(contentLength));
            Header h_modified = res.getFirstHeader("Last-Modified");
            if (null != h_modified && StringUtils.isNoneBlank(h_modified.getValue())) {
                Date d = DateUtils.parseDate(h_modified.getValue());
                info.setLastModified(null == d ? System.currentTimeMillis() : d.getTime());
            }
        } catch (Exception e) {
            throw new OmicSeqException(String.format("Get File[%s] info failed", url.toString()), e);
        }
        this._fileInfo = info;
        return info;
    }

    /**
     * 下载文件
     */
    @Override
    protected void download(File file) throws Exception {
        RandomAccessFile rf = null;
        DateTime dt = DateTime.now();
        try {
            // 1.创建或打开本地文件
            rf = new RandomAccessFile(file, "rw");
            // 不支持断点
            if (Boolean.FALSE.equals(fileInfo.getResume())) {
                long length = download(file, rf, 0, fileInfo.getLength());
                fileInfo.setLength(length);
            } else {
                // 2.多线程下载文件
                List<Chunk> chunks = fileInfo.getChunks();
                for (Chunk chunk : chunks) {
                    while (true) {
                        long postion = chunk.getBegin();
                        // 10M 一次10M
                        long end = postion + blockSize;
                        if (end > chunk.getEnd()) {
                            end = chunk.getEnd();
                        }
                        postion = download(file, rf, postion, end);
                        chunk.setPostion(postion);
                        if (end >= chunk.getEnd()) {
                            break;
                        }
                    }
                }
            }
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("Download file {},used {}", file, DateTimeUtils.diff(dt, DateTime.now()));
            }
            IOUtils.closeQuietly(rf);
        }
    }

    private long download(File file, RandomAccessFile rf, long postion, Long end) throws IOException {
        HttpGet get = setRange(postion, end);
        if (logger.isDebugEnabled()) {
            logger.debug("get data {} - {}", postion, end);
        }
        CloseableHttpResponse res = client.execute(get);
        InputStream is = res.getEntity().getContent();
        rf.seek(postion);
        byte[] b = new byte[1024];
        int l = 0;
        while ((l = is.read(b)) != -1) {
            rf.write(b, 0, l);
            postion += l;
        }
        return postion;
    }

    @Override
    protected void close() {
        HttpClientUtils.closeQuietly(client);
    }

}
