package com.omicseq.robot.http;

import com.omicseq.common.SourceType;
import com.omicseq.domain.FileInfo;
import com.omicseq.robot.download.DownloadFactory;
import com.omicseq.robot.download.IDownload;

public class Test {

    static String url = "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/stad/gsc/broad.mit.edu/illuminaga_dnaseq/mutations/broad.mit.edu_STAD.IlluminaGA_DNASeq.Level_2.0.0.0.tar.gz";

    // static String url =
    // "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/stad/gsc/broad.mit.edu/illuminaga_dnaseq/mutations/broad.mit.edu_STAD.IlluminaGA_DNASeq.mage-tab.0.0.0.tar.gz";

    public static void main(String[] args) {
        // 1.获取要下载的文件大小,or
        // 2.对要下载的文件分段处理
        // 3.启用异步多线程调用下载文件

        // downloadUrl(url);

        String surl = url;
        surl = "http://dcc.icgc.org/api/download?fn=/current/OV-US/gene_expression.OV-US.tsv.gz&token=";
        IDownload download = DownloadFactory.get(surl, SourceType.TCGA);
        FileInfo fileinfo = download.getFileInfoFromServer();
        System.out.println(fileinfo);
    }

}
