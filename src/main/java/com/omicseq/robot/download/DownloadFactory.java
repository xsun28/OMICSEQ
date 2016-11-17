package com.omicseq.robot.download;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.SourceType;
import com.omicseq.domain.FileInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.robot.download.ftp.FtpDownload;
import com.omicseq.robot.download.http.HttpDownload;
import com.omicseq.robot.download.sra.SRADownload;

/**
 * 
 * Donload factory
 * 
 * @author zejun.du
 */
public class DownloadFactory {
    private static final String HTTP = "http";
    private static final String FTP = "ftp";

    public static IDownload get(String surl, SourceType source) {
        try {
            if (SourceType.SRA.equals(source)) {
                throw new OmicSeqException("Not Impl!");
            } else {
                URL url = new URL(surl);
                String protocol = StringUtils.trimToEmpty(url.getProtocol()).toLowerCase();
                if (protocol.startsWith(HTTP)) {
                    return new HttpDownload(url, source);
                } else if (protocol.startsWith(FTP)) {
                    return new FtpDownload(url, source);
                } else {
                    throw new OmicSeqException("Not Impl!");
                }
            }
        } catch (MalformedURLException e) {
            throw new OmicSeqException("unknown protocol!", e);
        }
    }

    public static IDownload get(FileInfo fileInfo) {
        try {
            SourceType source = SourceType.parse(fileInfo.getSource());
            if (SourceType.SRA.equals(source)) {
                return new SRADownload(fileInfo);
            } else {
                URL url = new URL(fileInfo.getUrl());
                String protocol = StringUtils.trimToEmpty(url.getProtocol()).toLowerCase();
                if (protocol.startsWith(HTTP)) {
                    return new HttpDownload(url, fileInfo);
                } else if (protocol.startsWith(FTP)) {
                    return new FtpDownload(url, fileInfo);
                } else {
                    throw new OmicSeqException("Not Impl!");
                }
            }
        } catch (MalformedURLException e) {
            throw new OmicSeqException("unknown protocol!", e);
        }
    }

    public static void main(String[] args) {
        String surl = "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/esca/cgcc/bcgsc.ca/illuminahiseq_rnaseq/rnaseq/bcgsc.ca_ESCA.IlluminaHiSeq_RNASeq.Level_3.1.0.0/TCGA-LN-A4MQ-01A-11R-A28J-31.gene.quantification.txt";
        IDownload down = get(surl, SourceType.TCGA);
        FileInfo info = down.getFileInfoFromServer();
        System.out.println(info);
    }
}
