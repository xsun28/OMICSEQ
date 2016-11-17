package com.omicseq.robot.ftp;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import com.omicseq.utils.DateTimeUtils;

public class Test {

    // ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/referenceSequences/
    public static void main(String[] args) throws Exception {
        download();

    }

    private static void download() throws IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException {
        DateTime dt = DateTime.now();
        FTPClient client = new FTPClient();
        String surl = "ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/referenceSequences/md5sum.txt";
        URL url = new URL(surl);
        System.out.println(url.toString());
        System.out.println(url.getHost());
        System.out.println(-1==url.getPort() ? url.getDefaultPort() : url.getPort());
        client.connect(url.getHost());
        client.login("anonymous", "anonymous");
        client.setType(FTPClient.TYPE_BINARY);
        System.out.println(url.getPath());
        System.out.println(url.getFile());
        System.out.println(url.getRef());
        System.out.println(client.fileSize(url.getPath()));
        client.changeDirectory("/goldenPath/hg19/encodeDCC/referenceSequences/");
        FTPFile[] list = client.list();
        for (FTPFile ftpFile : list) {
            System.out.println(String.format("name:[%s]\tModifiedDate:[%s]\tsize:[%s]", ftpFile.getName(),
                    ftpFile.getModifiedDate(), ftpFile.getSize()));
        }
        // download ftp file
        // client.download(remoteFileName, localFile);
        client.disconnect(true);
        System.out.println(DateTimeUtils.diff(dt, DateTime.now()));
    }
}
