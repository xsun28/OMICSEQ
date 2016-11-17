package com.omicseq.common;

/**
 * 
 * 
 * @author zejun.du
 */
public enum FileInfoStatus {
    INIT(0), // 初始状态
    CREATED(1), // 创建文件
    SPLITTED(2), // 完成切分
    DOWNLOADING(3), // 下载中
    FAILED(4), // 下载失败
    DOWNLOADED(5), // 下载成功
    PROCESSING(6), // 处理中
    PROCESS_FAILED(7), // 处理失败
    PROCESSED(99), //
    ;

    private Integer state;

    FileInfoStatus(Integer state) {
        this.state = state;
    }

    public Integer value() {
        return this.state;
    }

    public static boolean isDownloaded(Integer state) {
        return null != state && DOWNLOADED.value() <= state;
    }

    public static boolean isProcessed(Integer state) {
        return PROCESSED.value().equals(state);
    }
}
