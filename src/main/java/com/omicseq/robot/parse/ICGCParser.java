package com.omicseq.robot.parse;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.omicseq.bean.Entry;
import com.omicseq.common.SourceType;
import com.omicseq.common.SourceType.Account;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.JSONUtils;
import com.omicseq.utils.ThreadUtils;

/**
 * 
 * 
 * @author zejun.du
 */
public class ICGCParser extends BaseParser {
    private FTPClient client = new FTPClient();
    private List<String> error = new ArrayList<String>(5);

    @Override
    SourceType getSourceType() {
        return SourceType.ICGC;
    }

    protected void open(URL url) {
        if (logger.isDebugEnabled()) {
            logger.debug("Connect to server {} ", url.getHost());
        }
        if (client.isConnected()) {
            return;
        }
        Account acc = getSourceType().account();
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

    protected void close() {
        try {
            client.disconnect(true);
        } catch (Exception e) {
            logger.error("Close ftp connect failed!", e);
        }
    }

    @Override
    public void parser(String url) {
        DateTime start = DateTime.now();
        try {
            File root = new File("/files/download/icgc_ftp/");
            FileUtils.forceMkdir(root);
            String _url = "ftp://data.dcc.icgc.org";
            open(new URL(_url));
            client.setPassive(true);
            client.changeDirectory("current");
            FTPFile[] files = client.list();
            for (FTPFile file : files) {
                if (FTPFile.TYPE_DIRECTORY == file.getType()) {
                    DateTime dt = DateTime.now();
                    String dir = String.format("%s/%s", "current", file.getName());
                    try {
                        client.changeDirectory(file.getName());
                        String[] names = client.listNames();
                        String[] fnames = new String[3];
                        for (String fname : names) {
                            if (StringUtils.isBlank(fname)) {
                                continue;
                            }
                            if (null == fnames[0] && fname.toLowerCase().startsWith("clinicalsample")) {
                                fnames[0] = fname;
                                continue;
                            }
                            if (null == fnames[1] && fname.toLowerCase().startsWith("clinical")) {
                                fnames[1] = fname;
                                continue;
                            }
                            if (null == fnames[2] && fname.toLowerCase().startsWith("gene_expression")) {
                                fnames[2] = fname;
                                continue;
                            }
                        }
                        if (null == fnames[0] || null == fnames[1] || null == fnames[2]) {
                            logger.warn("{} 未找到所需的文件!", dir);
                            continue;
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug(dir + "\t" + StringUtils.join(fnames));
                        }
                        File parent = new File(root, file.getName());
                        FileUtils.forceMkdir(parent);
                        for (String name : fnames) {
                            download(_url, dir, name, parent);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("download {} file used {}.", dir, DateTimeUtils.used(dt));
                        }
                    } catch (Exception e) {
                        logger.error("download failed!", e);
                    } finally {
                        client.changeDirectoryUp();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ICGC parse failed!{}", e);
        } finally {
            close();
            stop();
            logger.info("download icgc files used {}.", DateTimeUtils.used(start));
        }
    }

    private void download(String _url, String dir, String fname, File parent) {
        try {
            DateTime dt = DateTime.now();
            File localFile = new File(parent, fname);
            long length = client.fileSize(fname);
            if (localFile.exists() && localFile.length() != length) {
                localFile.delete();
            }
            if (!localFile.exists()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("download... {}/{}/{}", _url, dir, fname);
                }
                client.download(fname, localFile);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("download {} used {}.", fname, DateTimeUtils.used(dt));
            }
        } catch (Exception e) {
            this.error.add(String.format("%s/%s/%s", _url, dir, fname));
            logger.error("download file failed!", e);
        }
    }

    void fromAPI() throws JsonParseException, JsonMappingException, IOException {
        String api = "https://dcc.icgc.org/api/v1/download/info%s";
        String downTpl = "https://dcc.icgc.org/api/v1/download?fn=%s&token=%s";
        TypeReference<List<Entity>> type = new TypeReference<List<Entity>>() {
        };
        List<Entity> list = JSONUtils.from(String.format(api, "/current/Projects"), null, type);
        for (int i = 0; i < list.size(); i++) {
            Integer sampleId = null;
            try {
                Entity entity = list.get(i);
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing {} Folder is {} ", i + 1, entity.name);
                }
                if (!StringUtils.equalsIgnoreCase("d", entity.type)) {
                    continue;
                }
                String folderUrl = String.format(api, entity.name);
                List<Entity> files = JSONUtils.from(folderUrl, null, type);
                Entry<Entity, Entity> result = new Entry<Entity, Entity>();
                for (Entity file : files) {
                    if (!StringUtils.endsWithIgnoreCase("f", file.type)) {
                        continue;
                    }
                    String fname = FilenameUtils.getName(file.name);
                    if (StringUtils.isBlank(fname)) {
                        continue;
                    }
                    if (null == result.getKey() && fname.toLowerCase().startsWith("clinicalsample")) {
                        result.setKey(file);
                        continue;
                    }
                    if (null == result.getValue() && fname.toLowerCase().startsWith("gene_expression")) {
                        result.setValue(file);
                        continue;
                    }
                }
                if (null == result.getKey() || null == result.getValue()) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("{} 未找所对应的文件!", folderUrl);
                    }
                    continue;
                }
                String mateFile = String.format(downTpl, result.getKey().name, "");
                System.out.println(mateFile);
            } catch (Exception e) {
                failed(String.format("%s_$s", i + 1), sampleId, e);
            } finally {
                ThreadUtils.sleep(2 * 1000);
            }
        }
    }

    public static class Entity {
        public String name;
        public String type;
        public long size;
        public long date;
    }

    public static void main(String[] args) {
        new ICGCParser().start();
        System.exit(0);
    }
}
