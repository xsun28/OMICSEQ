package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.FileInfo;

public interface IFileInfoDAO extends IGenericDAO<FileInfo> {
    /**
     * @param url
     * @return
     */
    FileInfo get(String url);

    /**
     * @param sampleId
     * @return
     */
    FileInfo getBySampleId(Integer sampleId);

    /**
     * @return
     */
    List<FileInfo> findAll();

    /**
     * 未下载完成的信息
     * 
     * @param limit
     * @return
     */
    List<FileInfo> findUndownload(String server, int limit);

    /**
     * 已完成下载,未处理的文件
     * 
     * @param server
     * @param start
     * @param limit
     * @return
     */
    List<FileInfo> findDownload(String server, int start, int limit);

    void clean();

}
