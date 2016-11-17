package com.omicseq.robot.download;

import com.omicseq.domain.FileInfo;

/**
 * 
 * 
 * @author zejun.du
 */
public interface IDownload  {
    
    /**
     * 
     */
    void start();

    /**
     * 
     * @return
     */
    FileInfo getFileInfoFromStore();

    /**
     * @return
     */
    FileInfo getFileInfoFromServer();

}
