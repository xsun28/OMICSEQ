package com.omicseq.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.omicseq.common.Charsets;
import com.omicseq.common.FileInfoStatus;
import com.omicseq.domain.FileInfo;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;
import com.omicseq.utils.ThreadUtils;

public class Wget {

    public static void main(String[] args) {
        // 1生成10台机器所下载的文件列表
        // makeDownloadFiles();

        // 2生成下载完成后的文件 信息
        // makeDownloadedInfo();

        // 3生成bed所需下载的文件列表
        // makeBedDownloadFiles();
        makeBedDownloadFiles2();
        System.exit(0);
    }

    protected static void makeBedDownloadFiles2() {
        try {
            List<String> urls = new ArrayList<String>(5);
            List<String> errors = new ArrayList<String>(5);
            for (int i = 1; i <= 10; i++) {
                File _file = new File(String.format("./wget/bed/%s.txt", i));
                urls.addAll(FileUtils.readLines(_file));
            }
            File all = new File(String.format("./wget/bed/all.txt"));
            Writer w = new PrintWriter(all);
            IOUtils.writeLines(urls, null, w);
            File err = new File(String.format("./wget/bed/err.txt"));
            List<String> coll = FileUtils.readLines(err);
            int pos = urls.size();
            for (String surl : coll) {
                System.out.println("process " + (++pos) + " " + surl);
                try {
                    InputStream ins = new URL(surl).openStream();
                    byte[] b = new byte[102400];
                    int length = ins.read(b);
                    String html = new String(b, 0, length);
                    String[] lines = html.split("\n");
                    for (String str : lines) {
                        str = StringUtils.trim(str);
                        String name = str.substring(str.lastIndexOf(" "));
                        if (name.toLowerCase().endsWith("bed.gz")) {
                            String url = String.format("%s%s", surl, StringUtils.trim(name));
                            w.write(url);
                            w.write(IOUtils.LINE_SEPARATOR);
                            urls.add(url);
                            break;
                        }
                    }
                } catch (Exception e) {
                    errors.add(surl);
                    e.printStackTrace();
                }
                ThreadUtils.sleep(3 * 1000);
            }
            int start = 0;
            int limit = urls.size() % 10 == 0 ? urls.size() / 10 : urls.size() / 10 + 1;
            for (int i = 1; i <= 10; i++) {
                File _file = new File(String.format("./wget/bed/%s.txt", i));
                FileOutputStream out = getFileOutputStream(_file);
                try {
                    int end = start + limit;
                    List<String> list = urls.subList(start, end);
                    IOUtils.writeLines(list, IOUtils.LINE_SEPARATOR_UNIX, out);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
            System.out.println("sucess size " + urls.size());
            System.out.println("error size " + errors.size());
            FileUtils.writeLines(err, errors);
            IOUtils.closeQuietly(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void makeBedDownloadFiles() {
        String file = "./src/test/resources/sample.bed.meta.csv";
        DateTime dt = DateTime.now();
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            String line = null;
            List<String> urls = new ArrayList<String>(5);
            List<String> errors = new ArrayList<String>(5);
            int pos = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println("line no: " + (++pos));
                if (line.indexOf("ftp://") == -1) {
                    continue;
                }
                String[] arr = line.split("\t");
                if ("MeDIP-Seq".equalsIgnoreCase(arr[2])) {
                    continue;
                }
                String surl = arr[5];
                try {
                    InputStream ins = new URL(surl).openStream();
                    byte[] b = new byte[102400];
                    int length = ins.read(b);
                    String html = new String(b, 0, length);
                    String[] lines = html.split("\n");
                    for (String str : lines) {
                        str = StringUtils.trim(str);
                        String name = str.substring(str.lastIndexOf(" "));
                        if (name.toLowerCase().endsWith("bed.gz")) {
                            urls.add(String.format("%s%s", surl, StringUtils.trim(name)));
                            break;
                        }
                    }
                } catch (Exception e) {
                    errors.add(surl);
                }
                ThreadUtils.sleep(1 * 1000);
            }
            System.out.println(urls.size());
            int start = 0;
            int limit = urls.size() % 10 == 0 ? urls.size() / 10 : urls.size() / 10 + 1;
            for (int i = 1; i <= 10; i++) {
                File _file = new File(String.format("./wget/bed/%s.txt", i));
                FileOutputStream out = getFileOutputStream(_file);
                try {
                    int end = start + limit;
                    List<String> list = urls.subList(start, end);
                    IOUtils.writeLines(list, IOUtils.LINE_SEPARATOR_UNIX, out);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
            FileOutputStream out = getFileOutputStream(new File(String.format("./wget/bed/err.txt")));
            IOUtils.writeLines(errors, IOUtils.LINE_SEPARATOR_UNIX, out);
            IOUtils.closeQuietly(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
            System.out.println(DateTimeUtils.diff(dt, DateTime.now()));
        }

    }

    /**
     * 生成下载完成后的文件 信息
     */
    protected static void makeDownloadedInfo() {
        ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
        IFileInfoDAO fileInfoDAO = DAOFactory.getDAO(IFileInfoDAO.class);
        Integer start = 0;
        Integer limit = 300;
        int ip = 154;
        for (int i = 1; i <= 10; i++) {
            String serverIp = String.format("112.25.20.%s", ip + i);
            try {
                List<Sample> coll = sampleDAO.loadSampleList(start, limit);
                List<FileInfo> infos = new ArrayList<FileInfo>(coll.size());
                for (Sample sample : coll) {
                    FileInfo info = new FileInfo();
                    info.setServerIp(serverIp);
                    info.setUrl(sample.getUrl());
                    info.setSource(sample.getSource());
                    String fname = FilenameUtils.getName(info.getUrl());
                    info.setPath(String.format("/files/download/%s", fname));
                    info.setState(FileInfoStatus.DOWNLOADED.value());
                    infos.add(info);
                }
                fileInfoDAO.create(infos);
            } catch (Exception e) {
                e.printStackTrace();
            }
            start += limit;
        }
    }

    /**
     * 生成需要下载的文件列表
     */
    protected static void makeDownloadFiles() {
        ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
        Integer start = 0;
        Integer limit = 300;
        Charset cs = Charsets.UTF_8;
        for (int i = 1; i <= 10; i++) {
            FileOutputStream out = null;
            try {
                List<Sample> coll = sampleDAO.loadSampleList(start, limit);
                File file = new File(String.format("./wget/%s.txt", i));
                out = getFileOutputStream(file);
                for (Sample item : coll) {
                    out.write(item.getUrl().getBytes(cs));
                    out.write(IOUtils.LINE_SEPARATOR_UNIX.getBytes(cs));
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(out);
            }
            start += limit;
        }
    }

    private static FileOutputStream getFileOutputStream(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return new FileOutputStream(file, false);
    }
}
